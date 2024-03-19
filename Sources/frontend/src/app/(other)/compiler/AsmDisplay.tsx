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

'use client';

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
  selectEntryPoint,
  setEntryPoint,
} from '@/lib/redux/compilerSlice';
import {
  loadFunctionModels,
  reloadSimulation,
  selectCpu,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { setDiagnostics } from '@codemirror/lint';
import { EditorView } from '@codemirror/view';
import { useCodeMirror } from '@uiw/react-codemirror';
import React, {
  FocusEventHandler,
  MouseEventHandler,
  useEffect,
  useRef,
} from 'react';

import EditorBar from '@/app/(other)/compiler/EditorBar';
import { useLineHighlight } from '@/app/(other)/compiler/LineHighlightContext';
import { StatusIcon } from '@/app/(other)/compiler/StatusIcon';
import { Button } from '@/components/base/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/base/ui/dropdown-menu';
import { selectAllInstructionFunctionModels } from '@/lib/redux/cpustateSlice';
import clsx from 'clsx';
import { Play } from 'lucide-react';
import { useTheme } from 'next-themes';

/**
 * The base theme for the editor.
 * Uses --line-highlight-color which is used for color mapping C lines to assembly lines.
 */
const baseTheme = EditorView.baseTheme({
  '.cm-content': {
    borderTopWidth: '1px',
  },
  '&.cm-focused': {
    outlineStyle: 'none',
  },
  '.cm-lineNumbers': {
    flexGrow: 1,
  },
});

/**
 * Component to display the assembly code editor.
 * Reads and updates compilerSlice state as the code is typed.
 *
 * TODO: debounce typing
 */
export function AsmDisplay() {
  const dispatch = useAppDispatch();
  const asm = useAppSelector(selectAsmCode);
  const cLineMap = useAppSelector(selectAsmMappings);
  const dirty = useAppSelector(selectDirty);
  const asmErrors = useAppSelector(selectAsmCodeMirrorErrors);
  const functionModels = useAppSelector(selectAllInstructionFunctionModels);
  const { resolvedTheme } = useTheme();

  if (resolvedTheme !== 'light' && resolvedTheme !== 'dark') {
    throw new Error('Theme not supported');
  }
  const { setHoveredCLine } = useLineHighlight();

  const editor = useRef<HTMLDivElement>(null);
  // todo: function models are loaded only from the sim page. Visiting the compiler page directly will result in an empty list.
  const { setContainer, view, state } = useCodeMirror({
    value: asm,
    height: '100%',
    width: '100%',
    extensions: [lineDecor(), wordHoverFactory(functionModels)],
    theme: resolvedTheme,
    onChange: (value, _viewUpdate) => {
      // Keep state in sync
      dispatch(asmFieldTyping(value));
    },
  });

  // Load function models once
  useEffect(() => {
    dispatch(loadFunctionModels());
  }, [dispatch]);

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
      <EditorBar
        mode='asm'
        checkSlot={null}
        entryPointSlot={
          <div className='flex items-center gap-2'>
            <AsmCheckButton />
            <EntryPointSelector />
          </div>
        }
      />
      <div className='flex-grow relative'>
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
 * Button to check the assembly code with the backend.
 * Green if no errors, red if errors, gray if not checked yet (dirty).
 */
const AsmCheckButton = () => {
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

  const labels: string[] = [];
  for (const line of asmCode.split('\n')) {
    if (line.includes(':')) {
      const label = line.split(':')[0];
      if (label) {
        labels.push(label);
      }
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
  const handleEntryPointChange = (s: string) => {
    // try parsing a number
    const num = parseInt(s);
    if (!Number.isNaN(num)) {
      dispatch(setEntryPoint(num));
      return;
    }
    dispatch(setEntryPoint(s));
  };

  return (
    <div className='flex items-center'>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button className='flex items-center gap-2 px-2 rounded py-0.5 my-0.5 h-6 text-black bg-gray-200 hover:bg-gray-300'>
            <Play size={16} strokeWidth={1.75} color='black' />
            Entry Point:{' '}
            <span className='font-semibold text-black'>{entryPoint}</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className='w-56'>
          <DropdownMenuLabel>Select Entry Point</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuRadioGroup
            value={entryPoint.toString()}
            onValueChange={handleEntryPointChange}
          >
            <DropdownMenuRadioItem value='0'>Address 0</DropdownMenuRadioItem>
            {labels.map((label) => {
              return (
                <DropdownMenuRadioItem key={label} value={label}>
                  {label}
                </DropdownMenuRadioItem>
              );
            })}
          </DropdownMenuRadioGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
function setHoveredCLine(arg0: number) {
  throw new Error('Function not implemented.');
}
