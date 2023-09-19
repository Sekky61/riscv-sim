import {
  Extension,
  RangeSetBuilder,
  StateEffect,
  StateField,
} from '@codemirror/state';
import {
  Decoration,
  DecorationSet,
  EditorView,
  ViewPlugin,
  ViewUpdate,
} from '@codemirror/view';

import { c_line_to_color } from './lineColoring';

export function c_line_to_line_decorator(c_line: number) {
  const color = c_line_to_color(c_line);
  return Decoration.line({
    attributes: { class: color },
  });
}

// Define effect. Used to identify the effect in update methods
export const change_highlight_effect = StateEffect.define<number[]>();
export const change_dirty_effect = StateEffect.define<boolean>();

function decorateLines(view: EditorView) {
  const builder = new RangeSetBuilder<Decoration>();
  const line_arr = view.state.field(c_line_ar);
  for (const { from, to } of view.visibleRanges) {
    for (let pos = from; pos <= to; ) {
      const line = view.state.doc.lineAt(pos);
      // decorate only if the line number is > 0
      const shouldColor = (line_arr[line.number] ?? 0) > 0;
      if (shouldColor) {
        const decor = c_line_to_line_decorator(line_arr[line.number]);
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
            (eff) =>
              eff.is(change_highlight_effect) || eff.is(change_dirty_effect),
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
const c_line_ar = StateField.define<number[]>({
  create() {
    return [];
  },
  update(value, tr) {
    for (const effect of tr.effects) {
      if (effect.is(change_highlight_effect)) {
        value = effect.value;
      }
    }
    return value;
  },
});

const dirtyState = StateField.define({
  create() {
    return false;
  },
  update(value, tr) {
    for (const effect of tr.effects) {
      if (effect.is(change_dirty_effect)) {
        value = effect.value;
      }
    }
    return value;
  },
});

export function lineDecor(): Extension {
  return [dirtyState, c_line_ar, showLineMapping];
}
