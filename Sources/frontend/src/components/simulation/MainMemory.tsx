/**
 * @file    MainMemory.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A block displaying the main memory
 *
 * @date    26 November 2023, 16:00 (created)
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

import { selectMemoryBytes, selectProgram } from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import { Label } from '@/lib/types/cpuApi';
import React, { memo, useDeferredValue } from 'react';

/**
 * Get the indexes of the memory that are different
 */
function getChangedIndexes(
  oldMemory: Uint8Array,
  newMemory: Uint8Array,
): number[] {
  const indexes: number[] = [];
  const length = Math.min(oldMemory.length, newMemory.length);
  for (let i = 0; i < length; i++) {
    if (oldMemory[i] !== newMemory[i]) {
      indexes.push(i);
    }
  }
  // Add the rest of the bytes
  const rest = oldMemory.length > newMemory.length ? oldMemory : newMemory;
  for (let i = length; i < rest.length; i++) {
    indexes.push(i);
  }
  return indexes;
}

/**
 * Display the memory like a hexdump.
 * Displays the whole memory, up to the highes touched address.
 */
export default function MainMemory() {
  const program = useAppSelector(selectProgram);
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);
  const oldMemory = useDeferredValue(memory);

  if (!program) {
    return null;
  }

  const labelTable = [];
  for (const label of Object.values(program.labels)) {
    labelTable.push(
      <tr key={label.name}>
        <td>{label.name}</td>
        <td>{label.address.stringRepresentation}</td>
      </tr>,
    );
  }

  return (
    <Block
      title='Main Memory'
      className=''
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Main Memory</DialogTitle>
            <DialogDescription>
              Detailed view of the Main Memory
            </DialogDescription>
          </DialogHeader>
          <table className='mb-4'>
            <thead>
              <tr>
                <th>Label</th>
                <th>Address</th>
              </tr>
            </thead>
            <tbody>{labelTable}</tbody>
          </table>
          <div className='py-1'>
            Memory Inspector - shows the memory up to the highest touched
            address
          </div>
          <div className='max-h-64 flex'>
            <HexDump
              memory={memory}
              labels={program.labels}
              bytesInRow={16}
              showAscii
            />
          </div>
        </DialogContent>
      }
    >
      <div className='max-h-80 flex text-sm gap-2 font-mono'>
        {memory.length === 0 ? (
          <div className='text-center text-gray-500'>Empty</div>
        ) : (
          <HexDump
            memory={memory}
            labels={program.labels}
            bytesInRow={8}
            oldMemory={oldMemory}
          />
        )}
      </div>
    </Block>
  );
}

export type HexDumpProps = {
  memory: Uint8Array;
  oldMemory?: Uint8Array;
  labels: { [k: string]: Label };
  /**
   * Should be a multiple of 4
   */
  bytesInRow: number;
  showAscii?: boolean;
};

export const HexDump = ({
  memory,
  oldMemory,
  labels,
  bytesInRow,
  showAscii = false,
}: HexDumpProps) => {
  const rows = memory.length / bytesInRow;

  const startAddress = 624;

  const addresses = [];
  for (let i = 0; i < rows; i++) {
    addresses.push(
      <div key={i}>{`0x${(i * bytesInRow)
        .toString(16)
        .padStart(4, '0')}`}</div>,
    );
  }

  const bytes = [];
  for (const [index, byte] of memory.entries()) {
    bytes.push(
      <div key={index} className={byte === 0 ? 'text-gray-500' : undefined}>
        {byte.toString(16).padStart(2, '0')}
      </div>,
    );
  }
  // Add labels
  // TODO: separate the data labels and code labels using an extra field in the label object
  for (const label of Object.values(labels)) {
    const el = bytes[Number(label.address.stringRepresentation)];
    bytes[Number(label.address.stringRepresentation)] = (
      <div
        key={label.address.bits}
        className='relative bg-gray-200 -m-1 p-1 rounded hover:bg-red-500 hover:rounded-l-none duration-150 group'
      >
        <div className='absolute top-0 right-full h-full p-1 rounded-l bg-red-500 invisible opacity-0 group-hover:visible group-hover:opacity-100 duration-150 translate-x-6 group-hover:translate-x-0'>
          {label.name}
        </div>
        {el}
      </div>
    );
  }
  // Highlight changed bytes
  // todo scroll to changed bytes
  if (oldMemory) {
    const changedIndexes = getChangedIndexes(oldMemory, memory);
    for (const index of changedIndexes) {
      const el = bytes[index];
      bytes[index] = (
        <div key={index} className='bg-yellow-300'>
          {el}
        </div>
      );
    }
  }

  const ascii = [];
  if (showAscii) {
    // Build string of bytesInRow bytes
    for (let i = 0; i < memory.length; i += bytesInRow) {
      let str = '';
      for (let j = 0; j < bytesInRow; j++) {
        const byte = memory[i + j];
        if (byte === undefined) {
          break;
        }
        if (byte >= 32 && byte <= 126) {
          str += String.fromCharCode(byte);
        } else {
          str += '.';
        }
      }
      ascii.push(<div key={i}>{str}</div>);
    }
  }

  return (
    <div className='overflow-y-auto flex text-sm gap-2 font-mono'>
      <div className='flex flex-col gap-1'>{addresses}</div>
      <div
        className='grid memory-grid justify-center gap-1'
        style={{
          gridTemplateColumns: `repeat(${bytesInRow}, max-content)`,
        }}
      >
        {bytes}
      </div>
      {showAscii && <div className='flex flex-col gap-1'>{ascii}</div>}
    </div>
  );
};
