/**
 * @file    ExamplesButton.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The button for the examples. Pop up.
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/base/ui/dropdown-menu';
import { openExampleAndCompile } from '@/lib/redux/compilerSlice';
import { useAppDispatch } from '@/lib/redux/hooks';
import type { CodeExample } from '@/lib/types/codeExamples';
import { FileCode2 } from 'lucide-react';

/**
 * The button to reveal available examples and load them into the editor.
 * The examples are loaded after the page load (from client).
 * The examples are fetched from the Next.js backend, not Java simulator backend.
 * After selecting an example, it is loaded into the editor and compiled (if the code is C).
 */
export function ExamplesButton({ examples }: { examples: CodeExample[] }) {
  const dispatch = useAppDispatch();

  if (!examples) {
    throw new Error('Examples not loaded');
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='outline' className='load-example-button flex gap-2'>
          <FileCode2 size={20} strokeWidth={1.75} />
          Load Example
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent side='right'>
        <DropdownMenuLabel>Examples</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {examples.map((example) => {
          // Wrapped in two divs, to not lose hover when the mouse moves over
          return (
            <DropdownMenuItem
              key={example.name}
              data-example-name={example.name}
              onClick={() => {
                dispatch(openExampleAndCompile(example));
              }}
              className='flex gap-1'
            >
              <div className='flex-grow'>{example.name}</div>
              <div className='font-bold flex justify-center w-8 rounded px-1 py-0.5 mr-1 bg-amber-300 text-[#694848] text-xs'>
                {example.type}
              </div>
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
