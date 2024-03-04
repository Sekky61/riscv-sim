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

import { selectStoreBuffer } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { StoreBufferItem } from '@/lib/types/cpuApi';
import { hexPadEven } from '@/lib/utils';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import RegisterReference from '@/components/simulation/RegisterReference';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { useBlockDescriptions } from '@/components/BlockDescriptionContext';

export default function StoreBuffer() {
  const storeBuffer = useAppSelector(selectStoreBuffer);
  const descriptions = useBlockDescriptions();

  if (!storeBuffer) return null;

  return (
    <Block
      title='Store Buffer'
      className='storeBuffer w-issue h-96'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Store Buffer</DialogTitle>
            <DialogDescription>
              {descriptions.storeBuffer?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
    >
      <InstructionListDisplay
        instructions={storeBuffer.storeQueue}
        totalSize={storeBuffer.bufferSize}
        columns={3}
        legend={
          <>
            <div>Instruction</div>
            <div>Address</div>
            <div>Data</div>
          </>
        }
        instructionRenderer={(bufItem, i) => (
          <StoreBufferItemComponent storeItem={bufItem} key={`item_${i}`} />
        )}
      />
    </Block>
  );
}

type StoreBufferItemProps = {
  storeItem: StoreBufferItem | null;
};

/**
 * Displays address and loaded value of a single item in the Load Buffer
 */
export function StoreBufferItemComponent({
  storeItem: item,
}: StoreBufferItemProps) {
  if (!item) {
    return (
      <div className='instruction-bubble flex justify-center px-2 py-1 font-mono col-span-3'>
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
      <div className='instruction-bubble'>
        <RegisterReference
          registerId={item.sourceRegister}
          className='h-full flex justify-center items-center'
          showValue
        />
      </div>
    </>
  );
}
