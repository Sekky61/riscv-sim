/**
 * @file    CCodeInput.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The C code editor
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

'use client';

import { StreamLanguage } from '@codemirror/language';
import { c } from '@codemirror/legacy-modes/mode/clike';
import { setDiagnostics } from '@codemirror/lint';
import { EditorView } from '@codemirror/view';
import { useCodeMirror } from '@uiw/react-codemirror';
import React, {
  type FocusEventHandler,
  type MouseEventHandler,
  useEffect,
  useRef,
} from 'react';

import {
  changeDirtyEffect,
  changeHighlightEffect,
  lineDecor,
} from '@/lib/editor/lineDecorExtension';
import {
  cFieldTyping,
  selectCCodeMappings,
  selectCCodeMirrorErrors,
  selectCDirty,
  selectCErrors,
  selectDirty,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import EditorBar from '@/app/(other)/compiler/EditorBar';
import { useLineHighlight } from '@/app/(other)/compiler/LineHighlightContext';
import { StatusIcon } from '@/app/(other)/compiler/StatusIcon';
import { useTheme } from 'next-themes';
import Page from './page';

/**
 * The base theme for the editor.
 */
const baseTheme = EditorView.baseTheme({
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

// cm-line must be mentioned in the code for the css minimizer not to remove it
const _ = 'cm-line cm-focused';

/**
 * The C code editor.
 * Communicates with the compilerSlice.
 */
export function CCodeInput() {
  const dispatch = useAppDispatch();
  const dirty = useAppSelector(selectDirty);
  const code = useAppSelector((state) => state.compiler.cCode);
  const mappedCLines = useAppSelector(selectCCodeMappings); // todo optimize
  const cErrors = useAppSelector(selectCCodeMirrorErrors);
  const { setHoveredCLine } = useLineHighlight();
  const { resolvedTheme } = useTheme();

  if (resolvedTheme !== 'light' && resolvedTheme !== 'dark') {
    throw new Error('Theme not supported');
  }

  const editor = useRef<HTMLDivElement>(null);
  const { setContainer, view, state } = useCodeMirror({
    value: code,
    height: '100%',
    width: '100%',
    extensions: [StreamLanguage.define(c), lineDecor()],
    theme: resolvedTheme,
    onChange: (value, _viewUpdate) => {
      // Keep state in sync
      dispatch(cFieldTyping(value));
    },
  });

  // Update errors when cErrors change
  useEffect(() => {
    if (!state || !view) {
      return;
    }
    const tr = setDiagnostics(state, cErrors);
    view.dispatch(tr);
  }, [view, state, cErrors]);

  // Update editor state when compiler data changes
  useEffect(() => {
    // Set the c_line_ar state
    if (!view) {
      return;
    }
    view.dispatch({
      effects: changeHighlightEffect.of(mappedCLines),
    });
  }, [view, mappedCLines]);

  useEffect(() => {
    if (!view) {
      return;
    }
    view.dispatch({
      effects: changeDirtyEffect.of(dirty),
    });
  }, [view, dirty]);

  // Set the container for the editor
  useEffect(() => {
    if (editor.current) {
      setContainer(editor.current);
    }
  }, [setContainer]);

  const enterLine: MouseEventHandler<HTMLDivElement> &
    FocusEventHandler<HTMLDivElement> = (e) => {
    if (e.target instanceof HTMLElement) {
      const targetIsLine = e.target.classList.contains('cm-line');
      if (targetIsLine) {
        setHoveredCLine(Number(e.target.getAttribute('data-c-line')));
      }
    }
  };

  // The ref is on an inner div so that the gray background is always after the editor
  return (
    <div className='flex flex-col flex-grow overflow-y-scroll rounded border relative'>
      <EditorBar mode='c' checkSlot={<CErrorsDisplay />} />
      <div className='relative flex-grow'>
        <div
          className='h-full w-full relative'
          ref={editor}
          onMouseOver={enterLine}
          onFocus={enterLine}
          onMouseLeave={() => setHoveredCLine(null)}
        />
      </div>
    </div>
  );
}

/**
 * A checkmark/cross icon that indicates if the code has errors (or is dirty).
 * A fixed width element
 */
const CErrorsDisplay = () => {
  const errors = useAppSelector(selectCErrors);
  const dirty = useAppSelector(selectCDirty);
  const hasErrors = errors.length > 0;

  let iconType: 'circle' | 'tick' | 'x';
  if (dirty) {
    iconType = 'circle';
  } else if (hasErrors) {
    iconType = 'x';
  } else {
    iconType = 'tick';
  }

  return (
    <div className='mr-2'>
      <StatusIcon type={iconType} />
    </div>
  );
};
