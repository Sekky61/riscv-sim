/**
 * @file    AsmDisplay.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The assembly code editor
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

import { EditorView } from '@codemirror/view';
import { useCodeMirror } from '@uiw/react-codemirror';
import React, { useEffect, useRef } from 'react';

import {
  changeDirtyEffect,
  changeHighlightEffect,
  lineDecor,
} from '@/lib/editor/lineDecorExtension';
import { wordHover } from '@/lib/editor/wordHover';
import {
  asmFieldTyping,
  selectAsmCode,
  selectAsmMappings,
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

export type AsmDisplayProps = React.HTMLAttributes<HTMLDivElement>;

export default function AsmDisplay() {
  const dispatch = useAppDispatch();
  const asm = useAppSelector(selectAsmCode);
  const cLineMap = useAppSelector(selectAsmMappings);
  const dirty = useAppSelector((state) => state.compiler.dirty);
  const mode = useAppSelector(selectEditorMode);

  const isEnabled = mode == 'asm';

  const editor = useRef<HTMLDivElement>(null);
  const { setContainer, view } = useCodeMirror({
    value: asm,
    height: '100%',
    width: '100%',
    readOnly: !isEnabled,
    extensions: [lineDecor(), wordHover],
    theme: baseTheme,
    onChange: (value, _viewUpdate) => {
      // Keep state in sync
      dispatch(asmFieldTyping(value));
    },
  });

  // Update editor state when compiler data changes
  useEffect(() => {
    // Set the c_line_ar state
    view?.dispatch({
      effects: changeHighlightEffect.of(cLineMap),
    });
  }, [view, cLineMap]);

  useEffect(() => {
    view?.dispatch({
      effects: changeDirtyEffect.of(dirty),
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
      <EditorBar mode='asm' />
      <div className='h-full w-full relative'>
        <div className='h-full w-full relative' ref={editor} />
        {!isEnabled && (
          <div className='pointer-events-none absolute inset-0 bg-gray-100/40' />
        )}
      </div>
    </div>
  );
}
