/**
 * @file    StoreBuffer.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Store Buffer
 *
 * @date    06 November 2023, 23:00 (created)
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

import {
  selectStoreBuffer,
  selectStoreBufferItemById,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { Reference } from '@/lib/types/cpuApi';

import Block from '@/components/simulation/Block';
import { InstructionBubble } from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';

export default function StoreBuffer() {
  const storeBuffer = useAppSelector(selectStoreBuffer);

  if (!storeBuffer) return null;

  const limit = Math.min(16, storeBuffer.bufferSize);

  return (
    <Block title='Store Buffer'>
      <InstructionListDisplay
        instructions={storeBuffer.storeQueue}
        limit={limit}
        instructionRenderer={(storeItemId) => (
          <StoreBufferItem storeItemId={storeItemId} />
        )}
      />
    </Block>
  );
}

/**
 * Displays address and loaded value of a single item in the Load Buffer
 */
export function StoreBufferItem({
  storeItemId: id,
}: {
  storeItemId?: Reference;
}) {
  const item = useAppSelector((state) => selectStoreBufferItemById(state, id));

  if (!item) {
    return (
      <InstructionBubble className='flex justify-center px-2 py-1 font-mono'>
        <span className='text-gray-400'>empty</span>
      </InstructionBubble>
    );
  }

  // If address is -1, it is not known yet
  const displayAddress = item.address === -1 ? '???' : item.address;

  return (
    <InstructionBubble className='flex divide-x'>
      <div className='flex-grow flex justify-center items-center'>
        {displayAddress}
      </div>
      <div className='flex-grow flex justify-center items-center'>
        {item.sourceRegister}
      </div>
    </InstructionBubble>
  );
}
