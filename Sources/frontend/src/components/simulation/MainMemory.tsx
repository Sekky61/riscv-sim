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
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import React from 'react';

/**
 * Display the memory like a hexdump.
 * Displays the whole memory, up to the highes touched address.
 */
export default function MainMemory() {
  const program = useAppSelector(selectProgram);
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);
  const containerRef = React.useRef<HTMLDivElement>(null);

  if (!program) {
    return null;
  }

  const bytesInRow = 8;
  const rows = memory.length / bytesInRow;

  const labels = program.labels;
  console.dir(labels);

  const startAddress = 624;

  const addresses = [];
  for (let i = 0; i < rows; i++) {
    addresses.push(
      <div>{`0x${(i * bytesInRow).toString(16).padStart(4, '0')}`}</div>,
    );
  }

  const bytes = [];
  for (const byte of memory) {
    bytes.push(<div>{byte.toString(16).padStart(2, '0')}</div>);
  }
  // Add labels
  for (const label of Object.values(labels)) {
    const el = bytes[label.address];
    bytes[label.address] = (
      <div className='relative bg-gray-200 -m-1 p-1 rounded hover:bg-red-500 hover:rounded-l-none duration-150 group'>
        <div className='absolute top-0 right-full h-full p-1 rounded-l bg-red-500 opacity-0 group-hover:opacity-100 duration-150 translate-x-6 group-hover:translate-x-0'>
          {label.name}
        </div>
        {el}
      </div>
    );
  }

  return (
    <Block title='Main Memory' className=''>
      <div
        className='overflow-y-auto max-h-80 flex text-sm gap-2 font-mono'
        ref={containerRef}
      >
        <div className='flex flex-col gap-1'>{addresses}</div>
        <div className='grid memory-grid justify-center gap-1'>{bytes}</div>
      </div>
    </Block>
  );
}

/**
 * Display a single row of the memory
 */
function MemoryRow({
  memory,
  startAddress,
  count,
}: {
  memory: Uint8Array;
  startAddress: number;
  count: number;
}) {
  const bytesInRow = 8;

  const data = memory.slice(startAddress, startAddress + count);

  // Pad the address with zeros
  const address = `0x${startAddress.toString(16).padStart(4, '0')}`;

  return (
    <>
      <div className=''>{address}</div>
      {Array.from(Array(bytesInRow).keys()).map((index) => (
        <div key={index} className=''>
          {(data[index] ?? 0).toString(16).padStart(2, '0')}
        </div>
      ))}
    </>
  );
}
