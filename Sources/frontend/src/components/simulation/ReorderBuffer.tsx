/**
 * @file    ReorderBuffer.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Reorder Buffer component
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

import { selectROB } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';

export default function ReorderBuffer() {
  const rob = useAppSelector(selectROB);

  if (!rob) return null;

  const used = rob.reorderQueue.length;
  const showLimit = 16;
  const showMore = used > showLimit;

  return (
    <Block title='Reorder Buffer'>
      <div>
        <span>
          {used}/{rob.bufferSize}
        </span>
      </div>
      <div className='flex flex-col gap-1'>
        {rob.reorderQueue.slice(0, showLimit).map((item) => (
          <InstructionField
            key={item.simCodeModel.id}
            instruction={item.simCodeModel}
          />
        ))}
      </div>
      {showMore && (
        <div className='flex justify-center'>
          And {used - showLimit} more...
        </div>
      )}
    </Block>
  );
}
