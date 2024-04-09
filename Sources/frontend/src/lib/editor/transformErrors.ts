/**
 * @file    transformErrors.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Codemirror extension for line coloring based on mapping
 *
 * @date    30 September 2023, 12:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Michal Majer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import type { Action, Diagnostic } from '@codemirror/lint';

import type { ComplexErrorItem } from '@/lib/types/simulatorApi';

/**
 * Code action to navigate to memory tab
 */
const jumpToMemoryAction: Action = {
  name: 'Go to memory tab',
  apply(view, from, to) {
    window.location.href = '/memory';
  },
};

/**
 * Transforms errors from compiler API to codemirror diagnostics.
 * Most notably, it converts line and column to 1D character index.
 *
 * @param errors  Array of errors from compiler API
 * @param code   Code to which the errors belong
 * @returns Array of codemirror diagnostics
 */
export function transformErrors(
  errors: Array<ComplexErrorItem>,
  code: string,
): Array<Diagnostic> {
  // Add one to each line length to account for the fact that caret can be at the end of the line
  const lineLengths = code.split('\n').map((line) => line.length + 1);
  const lineLengthsPrefixSum = [0];
  for (const lineLength of lineLengths) {
    // todo test this
    const prev = lineLengthsPrefixSum.at(-1);
    if (prev === undefined) {
      throw new Error('Invalid line lengths');
    }
    lineLengthsPrefixSum.push(prev + lineLength);
  }

  return errors.map((error: ComplexErrorItem): Diagnostic => {
    // We have a line and column, but code mirror expects a 1D character index
    // We need to convert the line and column to a character index
    const span = error.locations[0];
    if (!span) {
      console.error('Invalid error span from server', error);
      return {
        from: 0,
        to: 0,
        message: error.message,
        severity: error.kind,
      };
    }
    // 1-based line and column
    const line = span.caret.line;
    const index = line <= 0 ? 0 : line - 1;
    const lineStart = lineLengthsPrefixSum[index];
    if (lineStart === undefined) {
      console.warn('Invalid line start', line, lineLengthsPrefixSum);
      return {
        from: 0,
        to: 0,
        message: error.message,
        severity: error.kind,
      };
    }
    const charIndex = lineStart + span.caret['display-column'] - 1;

    let charIndexEnd: number;
    if (span.finish) {
      charIndexEnd =
        charIndex +
        1 +
        (span.finish['display-column'] - span.caret['display-column']);
    } else {
      charIndexEnd = charIndex;
    }

    const undefSymbolMatch = error.message.match(/Symbol .* is not defined/);
    const isUndefinedSymbol = undefSymbolMatch !== null;

    return {
      from: charIndex,
      to: charIndexEnd,
      message: error.message,
      severity: error.kind,
      actions: isUndefinedSymbol ? [jumpToMemoryAction] : undefined,
    };
  });
}
