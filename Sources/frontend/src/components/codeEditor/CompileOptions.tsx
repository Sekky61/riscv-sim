/**
 * @file    CompileOptions.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Compile options component - compile button, examples
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

import { useHotkeys } from 'react-hotkeys-hook';
import _examples from 'src/constant/codeExamples.json';

import {
  callCompiler,
  openExampleAndCompile,
  selectAsmManuallyEdited,
  toggleOptimizeFlag,
} from '@/lib/redux/compilerSlice';
import {
  enterEditorMode,
  selectEditorMode,
  selectOptimize,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import Tooltip from '@/components/Tooltip';
import { Button } from '@/components/base/ui/button';
import { Checkbox } from '@/components/base/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/base/ui/dropdown-menu';

import { CodeExample } from '@/constant/codeExamples';

import { RadioInput } from '../form/RadioInput';

const examples = _examples as unknown as Array<CodeExample>;

export default function CompileOptions() {
  const dispatch = useAppDispatch();
  const optimize = useAppSelector(selectOptimize);
  const mode = useAppSelector(selectEditorMode);
  const asmEdited = useAppSelector(selectAsmManuallyEdited);

  function handleCompile() {
    // Show warning if the user is about to lose data
    if (asmEdited) {
      const res = confirm(
        'You have manually edited the assembly code. Are you sure you want to compile?',
      );
      if (!res) {
        return;
      }
    }
    dispatch(callCompiler());
  }

  const optimizeOptions = [
    {
      label: 'Optimize',
      value: 'O2',
      checked: optimize.includes('O2'),
      clickCallback: () => dispatch(toggleOptimizeFlag('O2')),
    },
    {
      label: 'Rename registers',
      value: 'rename',
      checked: optimize.includes('rename'),
      clickCallback: () => dispatch(toggleOptimizeFlag('rename')),
    },
    {
      label: 'Unroll loops',
      value: 'unroll',
      checked: optimize.includes('unroll'),
      clickCallback: () => dispatch(toggleOptimizeFlag('unroll')),
    },
    {
      label: 'Peel loops',
      value: 'peel',
      checked: optimize.includes('peel'),
      clickCallback: () => dispatch(toggleOptimizeFlag('peel')),
    },
    {
      label: 'Inline functions',
      value: 'inline',
      checked: optimize.includes('inline'),
      clickCallback: () => dispatch(toggleOptimizeFlag('inline')),
    },
    {
      label: 'Omit frame pointer',
      value: 'omit-frame-pointer',
      checked: optimize.includes('omit-frame-pointer'),
      clickCallback: () => dispatch(toggleOptimizeFlag('omit-frame-pointer')),
    },
  ];

  const editorModeChanged = (newVal: string) => {
    if (newVal !== 'c' && newVal !== 'asm') {
      console.error(`Unknown mode '${newVal}' while changing editor mode`);
      return;
    }
    dispatch(enterEditorMode(newVal));
  };

  // TODO: bug: The first render does not show the mode selected
  return (
    <div className='flex flex-col items-stretch gap-4'>
      <RadioInput
        choices={['c', 'asm']}
        texts={['C', 'ASM']}
        value={mode}
        onNewValue={editorModeChanged}
      />
      <ExamplesButton />
      <div className='rounded border flex flex-col gap-2 p-2'>
        {optimizeOptions.map((option) => (
          <div key={option.value} className='flex items-center space-x-2'>
            <Checkbox
              id={option.value}
              checked={option.checked}
              onCheckedChange={option.clickCallback}
            />
            <label
              htmlFor={option.value}
              className='text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
      <CompileButton handleCompile={handleCompile} />
    </div>
  );
}

function ExamplesButton() {
  const dispatch = useAppDispatch();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='outline'>Examples</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent side='right'>
        <DropdownMenuLabel>Examples</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {examples.map((example) => {
          // Wrapped in two divs, to not lose hover when the mouse moves over
          return (
            <DropdownMenuItem
              key={example.name}
              onClick={() => {
                dispatch(openExampleAndCompile(example));
              }}
              className='flex'
            >
              <div className='flex-grow'>{example.name}</div>
              <div className='flex justify-center w-8 rounded px-0.5 mr-1 bg-amber-300 text-[#694848] text-xs'>
                {example.type}
              </div>
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

const COMPILE_SHORTCUT = 'ctrl+enter';

function CompileButton({ handleCompile }: { handleCompile: () => void }) {
  const compileStatus = useAppSelector((state) => state.compiler.compileStatus);
  const statusStyle = statusToClass(compileStatus);

  useHotkeys(
    COMPILE_SHORTCUT,
    () => {
      // TODO: Show warning if the user is about to lose data
      // https://www.reddit.com/r/reactjs/comments/5xnien/reactredux_and_displaying_a_confirmation_dialog/
      handleCompile();
    },
    undefined,
    [handleCompile],
  );

  return (
    <Button className={statusStyle} onClick={handleCompile}>
      Compile
      <Tooltip text='Compile' shortcut={COMPILE_SHORTCUT} />
    </Button>
  );
}

function statusToClass(status: 'idle' | 'success' | 'loading' | 'failed') {
  switch (status) {
    case 'idle':
      return 'ring-gray-200';
    case 'loading':
      return 'ring-yellow-200';
    case 'success':
      return 'ring-green-200';
    case 'failed':
      return 'ring-red-200';
    default:
      throw new Error(`Unknown status '${status}'`);
  }
}
