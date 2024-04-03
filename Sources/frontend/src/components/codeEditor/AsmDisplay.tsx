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

import { setDiagnostics } from '@codemirror/lint';
import { EditorView } from '@codemirror/view';
import { useCodeMirror } from '@uiw/react-codemirror';
import React, { useEffect, useRef } from 'react';

import {
  changeDirtyEffect,
  changeHighlightEffect,
  lineDecor,
} from '@/lib/editor/lineDecorExtension';
import { wordHoverFactory } from '@/lib/editor/wordHover';
import {
  asmFieldTyping,
  callParseAsm,
  selectAsmCode,
  selectAsmCodeMirrorErrors,
  selectAsmDirty,
  selectAsmErrors,
  selectAsmMappings,
  selectDirty,
  selectEditorMode,
  selectEntryPoint,
  setEntryPoint,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import { Button } from '@/components/base/ui/button';
import EditorBar from '@/components/codeEditor/EditorBar';
import { StatusIcon } from '@/components/codeEditor/StatusIcon';
import clsx from 'clsx';
import { selectAllInstructionFunctionModels } from '@/lib/redux/cpustateSlice';

/**
 * The base theme for the editor.
 * Uses --line-highlight-color which is used for color mapping C lines to assembly lines.
 */
const baseTheme = EditorView.baseTheme({
  '.cm-activeLine': {
    backgroundColor: 'rgba(var(--line-highlight-color), 0.25)',
    backdropFilter: 'contrast(1.1)',
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

/**
 * Component to display the assembly code editor.
 * Reads and updates compilerSlice state as the code is typed.
 *
 * TODO: debounce typing
 */
export default function AsmDisplay() {
  const dispatch = useAppDispatch();
  const asm = useAppSelector(selectAsmCode);
  const cLineMap = useAppSelector(selectAsmMappings);
  const dirty = useAppSelector(selectDirty);
  const mode = useAppSelector(selectEditorMode);
  const asmErrors = useAppSelector(selectAsmCodeMirrorErrors);
  const functionModels = useAppSelector(selectAllInstructionFunctionModels);

  const isEnabled = mode === 'asm';

  const editor = useRef<HTMLDivElement>(null);
  // todo: function models are loaded only from the sim page. Visiting the compiler page directly will result in an empty list.
  const { setContainer, view, state } = useCodeMirror({
    value: asm,
    height: '100%',
    width: '100%',
    readOnly: !isEnabled,
    extensions: [lineDecor(), wordHoverFactory(functionModels)],
    theme: baseTheme,
    onChange: (value, _viewUpdate) => {
      // Keep state in sync
      dispatch(asmFieldTyping(value));
    },
  });

  // Update errors when cErrors change
  useEffect(() => {
    if (!state || !view) {
      return;
    }
    const tr = setDiagnostics(state, asmErrors);
    view.dispatch(tr);
  }, [view, state, asmErrors]);

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
    <div className='flex flex-col flex-grow overflow-y-scroll rounded border relative'>
      <EditorBar
        mode='asm'
        checkSlot={<AsmErrorsDisplay />}
        entryPointSlot={<EntryPointSelector />}
      />
      <div className='flex-grow relative'>
        <div className='h-full w-full relative' ref={editor} />
        {!isEnabled && (
          <div className='pointer-events-none absolute inset-0 bg-gray-100/40' />
        )}
      </div>
    </div>
  );
}

/**
 * Button to check the assembly code with the backend.
 * Green if no errors, red if errors, gray if not checked yet (dirty).
 */
const AsmErrorsDisplay = () => {
  const dispatch = useAppDispatch();
  const errors = useAppSelector(selectAsmErrors);
  const dirty = useAppSelector(selectAsmDirty);
  const hasErrors = errors.length > 0;

  const checkAsm = () => {
    dispatch(callParseAsm());
  };

  const boxStyle = clsx(
    'flex items-center px-2 rounded py-0.5 my-0.5 h-6 text-black bg-gray-200 hover:bg-gray-300',
    dirty && 'button-interactions',
    hasErrors &&
      !dirty &&
      'bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40',
    !hasErrors &&
      !dirty &&
      'bg-green-500/20 hover:bg-green-500/30 active:bg-green-500/40',
  );

  let iconType: 'circle' | 'tick' | 'x';
  if (dirty) {
    iconType = 'circle';
  } else if (hasErrors) {
    iconType = 'x';
  } else {
    iconType = 'tick';
  }

  return (
    <Button className={boxStyle} onClick={checkAsm}>
      <div className='mr-2'>
        <StatusIcon type={iconType} />
      </div>
      Check
    </Button>
  );
};

/**
 * The entry point selector. Either a label defined in assembly code or a number.
 */
function EntryPointSelector() {
  const dispatch = useAppDispatch();
  const entryPoint = useAppSelector(selectEntryPoint);
  const asmCode = useAppSelector(selectAsmCode);

  const labels = [];
  for (const line of asmCode.split('\n')) {
    if (line.includes(':')) {
      labels.push(line.split(':')[0]);
    }
  }

  const selectOptions = [
    <option key={0} value={0}>
      Address 0
    </option>,
  ];
  for (const label of labels) {
    selectOptions.push(
      <option key={label} value={label}>
        {label}
      </option>,
    );
  }

  // TODO: solve problem with reseting the sim (tick)
  const handleEntryPointChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    // try parsing a number
    const num = parseInt(e.target.value);
    if (!Number.isNaN(num)) {
      dispatch(setEntryPoint(num));
      return;
    }
    dispatch(setEntryPoint(e.target.value));
  };

  return (
    <div className='flex items-center'>
      <div className='mr-2'>Entry Point:</div>
      <select
        className='rounded'
        value={entryPoint}
        onChange={handleEntryPointChange}
      >
        {selectOptions}
      </select>
    </div>
  );
}
