/**
 * @file    wordHover.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Word hover for code editor - display instruction info
 *
 * @date    19 September 2023, 22:00 (created)
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

// Taken and adapted from docs: https://codemirror.net/examples/tooltip/

import { hoverTooltip } from '@codemirror/view';

import type { InstructionDescription } from '../types/instructionsDatabase';

/**
 * Create a tooltip (HTML element) for the given instruction
 * @param instruction The instruction to display
 */
function instructionTooltip(instruction: InstructionDescription) {
  // TODO: instructionDescription
  const dom = document.createElement('div');
  dom.className = 'instruction-tooltip';

  const instructionName = document.createElement('div');
  instructionName.textContent = instruction.name;
  instructionName.className = 'tooltip-name';

  const argumentList = document.createElement('ul');
  for (const arg of instruction.arguments) {
    const argElement = document.createElement('li');
    const typ = arg.register ? 'register' : 'immediate';
    if (arg.writeBack) {
      argElement.classList.add('font-bold');
    }
    argElement.textContent = `${arg.name} (${typ})`;
    argumentList.appendChild(argElement);
  }

  // TODO: add syntax example to InstructionFunctionModel

  const instructionInterpretable = document.createElement('div');
  const interpretableAs = document.createElement('div');
  interpretableAs.textContent = 'Interpretable as';
  interpretableAs.className = 'font-bold';
  instructionInterpretable.appendChild(interpretableAs);
  const interp = document.createElement('div');
  interp.textContent = instruction.interpretableAs;
  instructionInterpretable.appendChild(interp);

  dom.appendChild(instructionName);
  dom.appendChild(argumentList);
  dom.appendChild(instructionInterpretable);

  return dom;
}

/**
 * It is ok if the supportedInstructions is empty, the hover will just not work.
 * Factory pattern is used to hand over the instruction list from redux state.
 *
 * @param supportedInstructions All supported instructions and their descriptions
 * @returns Word hover xtension for the ASM code editor
 */
export const wordHoverFactory = (
  supportedInstructions: Record<string, InstructionDescription>,
) => {
  return hoverTooltip((view, pos, side) => {
    // Extract hovered word
    const { from, to, text } = view.state.doc.lineAt(pos);
    let start = pos;
    let end = pos;

    // A word is alphanumeric characters and dots
    while (start > from) {
      const l = text[start - from - 1];
      if (!l || !/[\w\.]/.test(l)) {
        break;
      }
      start--;
    }

    while (end < to) {
      const l = text[end - from];
      if (!l || !/[\w\.]/.test(l)) {
        break;
      }
      end++;
    }

    if ((start === pos && side < 0) || (end === pos && side > 0)) {
      return null;
    }

    // Check if the word is an instruction
    const word = text.slice(start - from, end - from);

    // Get info and create tooltip
    const instructionInfo = supportedInstructions[word];

    if (!instructionInfo) {
      return null;
    }

    return {
      pos: start,
      end,
      above: true,
      create(_view) {
        // Gets wrapped in a .cm-tooltip
        const dom = instructionTooltip(instructionInfo);
        return { dom };
      },
    };
  });
};
