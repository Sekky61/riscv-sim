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

import { type ParsedArgument, selectLoadBuffer } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import type { LoadBufferItem, RegisterDataContainer } from '@/lib/types/cpuApi';
import { hexPadEven } from '@/lib/utils';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import { ArgumentTableCell } from '@/components/simulation/IssueWindow';
import RegisterReference from '@/components/simulation/RegisterReference';

export default function LoadBuffer() {
  const loadBuffer = useAppSelector(selectLoadBuffer);
  const descriptions = useBlockDescriptions();

  if (!loadBuffer) return null;

  return (
    <Block
      title='Load Buffer'
      className='loadBuffer w-ls h-[600px]'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Load Buffer</DialogTitle>
            <DialogDescription>
              {descriptions.loadBuffer?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
    >
      <InstructionListDisplay
        instructions={loadBuffer.loadQueue}
        totalSize={loadBuffer.bufferSize}
        columns={4}
        legend={
          <div className='grid grid-cols-subgrid col-span-4 sticky top-0 bg-inherit'>
            <div>Instruction</div>
            <div>Address</div>
            <div>Data</div>
            <div>Bypass</div>
          </div>
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
      <div className='col-span-4 pointer-events-none w-full font-mono px-2 text-left whitespace-nowrap'>
        <button type='button' className='text-gray-400 h-8'>
          empty
        </button>
      </div>
    );
  }

  // If address is -1, it is not known yet
  const addressContainer: RegisterDataContainer = {
    bits: item.address,
    currentType: 'kInt',
    stringRepresentation: hexPadEven(item.address),
  };

  const address: ParsedArgument = {
    register: null,
    valid: item.address !== -1,
    origArg: {
      name: 'address',
      constantValue: addressContainer,
      stringValue: '',
      registerValue: null,
    },
  };

  return (
    <div className='grid grid-cols-subgrid col-span-4'>
      <InstructionField instructionId={item.simCodeModel} />
      <ArgumentTableCell arg={address} />
      <RegisterReference registerId={item.destinationRegister} />
      <div className='instruction-bubble h-full flex justify-center items-center'>
        {item.hasBypassed ? 'True' : 'False'}
      </div>
    </div>
  );
}
