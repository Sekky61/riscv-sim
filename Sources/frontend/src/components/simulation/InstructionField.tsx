/**
 * @file    InstructionField.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   [TODO]
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

import { Instruction } from '@/lib/redux/compilerSlice';

export type InstructionFieldProps = {
  instruction?: Instruction;
};

export default function InstructionField({
  instruction,
}: InstructionFieldProps) {
  if (!instruction) {
    return (
      <div className='w-full rounded-sm border p-0.5'>
        <span className='text-gray-400'>empty</span>
      </div>
    );
  }
  return (
    <div className='w-full rounded-sm border p-0.5'>{instruction.mnemonic}</div>
  );
}
