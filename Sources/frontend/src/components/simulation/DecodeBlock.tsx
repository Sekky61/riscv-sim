/**
 * @file    DecodeBlock.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Decode block
 *
 * @date    26 October 2023, 16:00 (created)
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

import { selectDecode } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';

export default function DecodeBlock() {
  const decode = useAppSelector(selectDecode);

  if (!decode) return null;

  const before = decode.beforeRenameCodeList;
  const after = decode.afterRenameCodeList;

  return (
    <Block title='Decode Block'>
      <div className='my-2 text-sm'>
        <div>Stalled: {decode.stallFlag}</div>
      </div>
      <div className='flex flex-col gap-1'>
        {after.map((instruction) => {
          return (
            <InstructionField key={instruction.id} instruction={instruction} />
          );
        })}
      </div>
    </Block>
  );
}
