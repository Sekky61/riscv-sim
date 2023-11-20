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

export type InstructionListDisplayProps<T> = {
  instructions: Array<T>;
  limit?: number;
  instructionRenderer: (item?: T) => React.ReactNode;
  legend?: React.ReactNode;
  columns?: number;
};

export function InstructionListDisplay<T>({
  instructions,
  limit,
  instructionRenderer,
  legend,
  columns = 1,
}: InstructionListDisplayProps<T>) {
  let displayLimit = instructions.length;
  let emptyCount = 0;
  if (limit !== undefined) {
    displayLimit = limit;
    emptyCount = limit - instructions.length;
  }

  const codeModels = instructions.slice(0, displayLimit);

  return (
    <ul
      className='grid gap-1'
      style={{ gridTemplateColumns: `repeat(${columns}, auto)` }}
    >
      {legend && <li className='contents'>{legend}</li>}
      {codeModels.map((inst) => instructionRenderer(inst))}
      {emptyCount > 0 &&
        [...Array(emptyCount)].map((_, i) => instructionRenderer(undefined))}
    </ul>
  );
}
