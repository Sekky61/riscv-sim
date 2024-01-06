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

import Block from '@/components/simulation/Block';
import React, { memo, useEffect } from 'react';
import { openModal } from '@/lib/redux/modalSlice';
import { Label } from '@/lib/types/cpuApi';

/**
 * Display the memory like a hexdump.
 * Displays the whole memory, up to the highes touched address.
 */
export default function MainMemory() {
  const dispatch = useAppDispatch();
  const program = useAppSelector(selectProgram);
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);

  if (!program) {
    return null;
  }

  program.labels;

  const handleMore = () => {
    dispatch(
      openModal({
        modalType: 'MAIN_MEMORY_DETAILS_MODAL',
        modalProps: null,
      }),
    );
  };

  return (
    <Block title='Main Memory' className='' handleMore={handleMore}>
      <div className='overflow-y-auto max-h-80 flex text-sm gap-2 font-mono'>
        {memory.length === 0 ? (
          <div className='text-center text-gray-500'>Empty</div>
        ) : (
          <HexDump memory={memory} labels={program.labels} bytesInRow={8} />
        )}
      </div>
    </Block>
  );
}

export type HexDumpProps = {
  memory: Uint8Array;
  labels: { [k: string]: Label };
  /**
   * Should be a multiple of 4
   */
  bytesInRow: number;
  showAscii?: boolean;
};

export const HexDump = ({
  memory,
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
    const el = bytes[label.address];
    bytes[label.address] = (
      <div
        key={label.address}
        className='relative bg-gray-200 -m-1 p-1 rounded hover:bg-red-500 hover:rounded-l-none duration-150 group'
      >
        <div className='absolute top-0 right-full h-full p-1 rounded-l bg-red-500 invisible opacity-0 group-hover:visible group-hover:opacity-100 duration-150 translate-x-6 group-hover:translate-x-0'>
          {label.name}
        </div>
        {el}
      </div>
    );
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
    <div className='flex text-sm gap-2 font-mono'>
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
