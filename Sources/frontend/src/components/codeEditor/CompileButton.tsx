/**
 * @file    CompileButton.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Button calling the compiler
 *
 * @date    26 February 2024, 21:00 (created)
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

import { Button } from '@/components/base/ui/button';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { COMPILE_SHORTCUT } from '@/components/shortcuts/CompilerShortcuts';
import {
  callCompiler,
  selectAsmManuallyEdited,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';
import { Hammer } from 'lucide-react';
import { useHotkeys } from 'react-hotkeys-hook';

/**
 * The compile button. It calls the compiler and mutates the state with the result.
 * TODO: shortcut is not working when the editor is focused
 */
export function CompileButton() {
  const compileStatus = useAppSelector((state) => state.compiler.compileStatus);
  const asmEdited = useAppSelector(selectAsmManuallyEdited);
  const dispatch = useAppDispatch();

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

  useHotkeys(COMPILE_SHORTCUT, () => {
    handleCompile();
  });

  const statusStyle = statusToClass(compileStatus);
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <Button
            className={clsx(statusStyle, 'ring flex gap-2')}
            onClick={handleCompile}
          >
            <Hammer size={20} strokeWidth={1.75} />
            Compile
          </Button>
        </TooltipTrigger>
        <TooltipContent>{COMPILE_SHORTCUT} to Compile</TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}

/**
 * Convert the compile status to a ring color class
 */
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
