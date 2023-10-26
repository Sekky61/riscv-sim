/**
 * @file    Program.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Block with program instructions
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

import { getArrayItems } from '@/lib/cpuState/util';
import { selectProgram } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';

export default function Program() {
  const program = useAppSelector(selectProgram);

  if (!program) return null;

  const code = getArrayItems(program.code);

  return (
    <Block title='Program'>
      <div className='flex h-[600px] flex-col gap-1 overflow-y-scroll'>
        {code.map((instruction) => {
          return (
            <div key={instruction.codeId}>{instruction.instructionName}</div>
          );
        })}
      </div>
    </Block>
  );
}
