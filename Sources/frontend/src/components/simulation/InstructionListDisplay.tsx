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

import { SimCodeModel } from '@/lib/types/cpuDeref';

import InstructionField from '@/components/simulation/InstructionField';

export type InstructionListDisplayProps = {
  instructions: Array<SimCodeModel>;
  limit?: number;
  pad?: boolean;
};

export function InstructionListDisplay({
  instructions,
  limit,
  pad = false,
}: InstructionListDisplayProps) {
  const displayLimit = limit || instructions.length;
  const displayCount = Math.min(displayLimit, instructions.length);

  return (
    <div className='flex flex-col gap-1'>
      {instructions.slice(0, displayCount).map((codeModel) => (
        <InstructionField key={codeModel.id} instruction={codeModel} />
      ))}
      {pad && displayCount < displayLimit
        ? [...Array(displayLimit - displayCount)].map((_, i) => (
            <InstructionField key={`pad-${i}`} instruction={undefined} />
          ))
        : null}
    </div>
  );
}
