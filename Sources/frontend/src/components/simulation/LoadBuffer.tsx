/**
 * @file    LoadBuffer.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Load Buffer
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

import { selectLoadBuffer } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { LoadBufferItem } from '@/lib/types/cpuApi';
import { hexPadEven } from '@/lib/utils';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import RegisterReference from '@/components/simulation/RegisterReference';

export default function LoadBuffer() {
  const loadBuffer = useAppSelector(selectLoadBuffer);

  if (!loadBuffer) return null;

  const limit = Math.min(16, loadBuffer.bufferSize);

  return (
    <Block title='Load Buffer' className='loadBuffer w-96'>
      <InstructionListDisplay
        instructions={loadBuffer.loadQueue}
        limit={limit}
        columns={4}
        legend={
          <>
            <div>Instruction</div>
            <div>Address</div>
            <div>Data</div>
            <div>Bypass</div>
          </>
        }
        instructionRenderer={(buffItem, i) => (
          <LoadBufferItemComponent loadItem={buffItem} key={`item_${i}`} />
        )}
      />
    </Block>
  );
}

type LoadBufferItemProps = {
  loadItem: LoadBufferItem | null;
};

/**
 * Displays address and loaded value of a single item in the Load Buffer
 */
export function LoadBufferItemComponent({
  loadItem: item,
}: LoadBufferItemProps) {
  if (!item) {
    return (
      <div className='instruction-bubble flex justify-center px-2 py-1 font-mono col-span-4'>
        <span className='text-gray-400'>empty</span>
      </div>
    );
  }

  // If address is -1, it is not known yet
  const displayAddress = item.address === -1 ? '???' : hexPadEven(item.address);

  return (
    <>
      <InstructionField instructionId={item.simCodeModel} />
      <div className='instruction-bubble h-full flex justify-center items-center'>
        {displayAddress}
      </div>
      <div>
        <RegisterReference
          registerId={item.destinationRegister}
          className='h-full flex justify-center items-center'
          showValue
        />
      </div>
      <div className='instruction-bubble h-full flex justify-center items-center'>
        {item.hasBypassed ? 'True' : 'False'}
      </div>
    </>
  );
}
