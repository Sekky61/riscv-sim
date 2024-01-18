/**
 * @file    InstructionListDisplay.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Codemirror extension for line coloring based on mapping
 *
 * @date    28 October 2023, 13:00 (created)
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

import type { ReactElement } from 'react';

export type InstructionListDisplayProps<T> = {
  instructions: Array<T>;
  limit: number;
  instructionRenderer: (item: T | null, id: number) => ReactElement<{ key: string }>;
  legend?: ReactElement;
  columns?: number;
};

/**
 * A component for displaying a list.
 * It pads or truncates the list to the specified limit.
 * Calls renderer for each item.
 *
 * Can render into multiple columns.
 */
export function InstructionListDisplay<T>({
  instructions,
  limit,
  instructionRenderer,
  legend,
  columns = 1,
}: InstructionListDisplayProps<T>) {
  const displayLimit = Math.min(limit, instructions.length);
  const emptyCount = limit - instructions.length;

  // Pad the array with nulls, limit the length
  const displayArray: Array<T | null> = instructions.slice(0, displayLimit);
  if (emptyCount > 0) {
    displayArray.push(...new Array(emptyCount).fill(null));
  }

  return (
    <ol
      className='grid gap-1'
      style={{ gridTemplateColumns: `repeat(${columns}, auto)` }}
    >
      {legend && <li className='contents'>{legend}</li>}
      {displayArray.map((inst, id) => instructionRenderer(inst, id))}
    </ol>
  );
}
