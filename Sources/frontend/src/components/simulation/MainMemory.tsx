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

import { selectMemoryBytes } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';

/**
 * Display the memory like a hexdump
 */
export default function MainMemory() {
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);

  const bytesInRow = 8;
  const rows = 16;

  const startAddress = 0;

  return (
    <Block title='Main Memory' className=''>
      <div className='grid gap-1 text-xs font-mono memory-grid justify-center'>
        {Array.from(Array(rows).keys()).map((row) => (
          <MemoryRow
            key={row}
            memory={memory}
            startAddress={startAddress + row * bytesInRow}
            count={bytesInRow}
          />
        ))}
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
