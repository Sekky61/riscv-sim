/**
 * @file    MemorySummary.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Memory summary component
 *
 * @date    04 March 2024, 13:00 (created)
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
import { useAppSelector } from '@/lib/redux/hooks';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import { dataTypeToText, memoryLocationSizeInElements } from '@/lib/utils';
import { MemoryStick } from 'lucide-react';

/**
 * Expandable memory summary component.
 * Shows basic information about defined memory elements.
 */
export function MemorySummary() {
  const activeIsa = useAppSelector(selectActiveConfig);
  const memoryLocations = activeIsa?.memoryLocations;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='outline' className='flex gap-2'>
          <MemoryStick size={20} strokeWidth={1.75} />
          Show Memory
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent side='right'>
        <DropdownMenuLabel>Memory Locations</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {memoryLocations.length === 0 && (
          <DropdownMenuItem className='text-gray-400'>
            No memory locations defined
          </DropdownMenuItem>
        )}
        {memoryLocations.map((loc) => {
          const elements = memoryLocationSizeInElements(loc);
          // Wrapped in two divs, to not lose hover when the mouse moves over
          return (
            <DropdownMenuLabel
              key={loc.name}
              className='flex gap-1 font-normal'
            >
              <div className='flex-grow'>{loc.name}</div>
              <div className='font-bold flex justify-center rounded px-1 py-0.5 mr-1 bg-amber-300 text-[#694848] text-xs'>
                {elements} x {dataTypeToText(loc.dataType)}
              </div>
            </DropdownMenuLabel>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
