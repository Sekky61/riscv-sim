import { StreamLanguage } from '@codemirror/language';
import { c } from '@codemirror/legacy-modes/mode/clike';
import { EditorView } from '@codemirror/view';
import { useCodeMirror } from '@uiw/react-codemirror';
import React, { useEffect, useRef } from 'react';

import {
  change_dirty_effect,
  change_highlight_effect,
  lineDecor,
} from '@/lib/editor/lineDecorExtension';
import {
  cFieldTyping,
  selectCCodeMappings,
  selectDirty,
  selectEditorMode,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import EditorBar from '@/components/codeEditor/EditorBar';

const baseTheme = EditorView.baseTheme({
  '.cm-activeLine': {
    backgroundColor: 'rgba(var(--line-highlight-color), 0.35)',
    backdropFilter: 'contrast(1.2)',
  },
  '.cm-content': {
    borderTopWidth: '1px',
  },
  '&.cm-focused': {
    outlineStyle: 'none',
  },
  '.cm-gutters': {
    width: '2.2rem',
  },
  '.cm-lineNumbers': {
    flexGrow: 1,
  },
});

// cm-line must be in the code for the css minimizer not to remove it
const _ = 'cm-line cm-focused';

export type CodeInputProps = React.HTMLAttributes<HTMLDivElement>;

export default function CCodeInput() {
  const dispatch = useAppDispatch();
  const mode = useAppSelector(selectEditorMode);
  const dirty = useAppSelector(selectDirty);
  const code = useAppSelector((state) => state.compiler.c_code);
  const mappedCLines = useAppSelector(selectCCodeMappings);

  const isEnabled = mode == 'c';

  const editor = useRef<HTMLDivElement>(null);
  const { setContainer, view } = useCodeMirror({
    value: code,
    height: '100%',
    width: '100%',
    readOnly: !isEnabled,
    extensions: [StreamLanguage.define(c), lineDecor()],
    theme: baseTheme,
    onChange: (value, _viewUpdate) => {
      // Keep state in sync
      dispatch(cFieldTyping(value));
    },
  });

  // Update editor state when compiler data changes
  useEffect(() => {
    // Set the c_line_ar state
    if (!view) {
      return;
    }
    view.dispatch({
      effects: change_highlight_effect.of(mappedCLines),
    });
  }, [view, mappedCLines]);

  useEffect(() => {
    if (!view) {
      return;
    }
    view.dispatch({
      effects: change_dirty_effect.of(dirty),
    });
  }, [view, dirty]);

  // Set the container for the editor
  useEffect(() => {
    if (editor.current) {
      setContainer(editor.current);
    }
  }, [setContainer]);

  // The ref is on an inner div so that the gray background is always after the editor
  return (
    <div className='flex-grow overflow-hidden rounded border'>
      <EditorBar mode='c' />
      <div className='h-full w-full relative'>
        <div className='h-full w-full relative' ref={editor} />
        {!isEnabled && (
          <div className='pointer-events-none absolute inset-0 bg-gray-100/40' />
        )}
      </div>
    </div>
  );
}
