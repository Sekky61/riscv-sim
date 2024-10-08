/**
 * @file    lineDecorExtension.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Codemirror extension for line coloring based on mapping
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

import {
  type Extension,
  RangeSetBuilder,
  StateEffect,
  StateField,
} from '@codemirror/state';
import {
  Decoration,
  type DecorationSet,
  type EditorView,
  ViewPlugin,
  type ViewUpdate,
} from '@codemirror/view';

import { cLineToColor } from './lineColoring';

export function cLineToLineDecorator(cLine: number): Decoration {
  const color = cLineToColor(cLine);
  if (color === undefined) {
    throw new Error(`Invalid cLine: ${cLine}`);
  }
  // Add a c line data attribute and asm line data attribute
  return Decoration.line({
    attributes: {
      class: color,
      'data-c-line': cLine.toString(),
    },
  });
}

// Define effect. Used to identify the effect in update methods
export const changeHighlightEffect = StateEffect.define<number[]>();
export const changeDirtyEffect = StateEffect.define<boolean>();

function decorateLines(view: EditorView) {
  const builder = new RangeSetBuilder<Decoration>();
  const lineArr = view.state.field(cLineArr);
  for (const { from, to } of view.visibleRanges) {
    for (let pos = from; pos <= to; ) {
      const line = view.state.doc.lineAt(pos);
      // decorate only if the line number is > 0
      const num = lineArr[line.number] ?? 0;
      const shouldColor = num > 0;
      if (shouldColor) {
        const decor = cLineToLineDecorator(num);
        builder.add(line.from, line.from, decor);
      }

      pos = line.to + 1;
    }
  }
  return builder.finish();
}

const showLineMapping = ViewPlugin.fromClass(
  class {
    decorations: DecorationSet;

    constructor(view: EditorView) {
      this.decorations = decorateLines(view);
    }

    update(update: ViewUpdate) {
      if (update.state.field(dirtyState)) {
        // Reset decorations
        this.decorations = Decoration.none;
        return;
      }

      if (
        update.docChanged ||
        update.viewportChanged ||
        update.transactions.some((tr) =>
          tr.effects.some(
            (eff) => eff.is(changeHighlightEffect) || eff.is(changeDirtyEffect),
          ),
        )
      ) {
        this.decorations = decorateLines(update.view);
      }
    }
  },
  {
    decorations: (v) => v.decorations,
  },
);

// The arrays length is the number of lines in the document
const cLineArr = StateField.define<number[]>({
  create() {
    return [];
  },
  update(value, tr) {
    let newValue = value;
    for (const effect of tr.effects) {
      if (effect.is(changeHighlightEffect)) {
        newValue = effect.value;
      }
    }
    return newValue;
  },
});

const dirtyState = StateField.define({
  create() {
    return false;
  },
  update(value, tr) {
    let newValue = value;
    for (const effect of tr.effects) {
      if (effect.is(changeDirtyEffect)) {
        newValue = effect.value;
      }
    }
    return newValue;
  },
});

export function lineDecor(): Extension {
  return [dirtyState, cLineArr, showLineMapping];
}
