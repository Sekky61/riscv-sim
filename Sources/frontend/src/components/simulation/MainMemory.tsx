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

'use client';

import { selectMemoryBytes, selectProgram } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import { Label } from '@/lib/types/cpuApi';
import clsx from 'clsx';
import { memo, useEffect, useMemo, useRef } from 'react';

function memoryCompare(a: Uint8Array | null, b: Uint8Array | null) {
  if (a === null || b === null || a.length !== b.length) {
    return false;
  }
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }
  return true;
}

/**
 * Display the memory like a hexdump.
 * Displays the whole memory, up to the highes touched address.
 *
 * TODO filter out program labels
 */
export default function MainMemory() {
  const program = useAppSelector(selectProgram);
  const descriptions = useBlockDescriptions();
  const memory =
    useAppSelector(selectMemoryBytes, {
      equalityFn: memoryCompare,
    }) ?? new Uint8Array(0);
  const oldMemory = usePrevious(memory);

  // Memoize labels. They do not change and trigger re-renders.
  const labelArray = useRef<Label[] | null>(null);
  useEffect(() => {
    if (!program?.labels) {
      return;
    }
    const currentLabels = Object.values(program.labels);
    // Compare the labels
    let changed = false;
    if (labelArray.current === null) {
      changed = true;
    } else if (currentLabels.length !== labelArray.current.length) {
      changed = true;
    } else {
      for (let i = 0; i < currentLabels.length; i++) {
        if (currentLabels[i]?.name !== labelArray.current[i]?.name) {
          changed = true;
          break;
        }
      }
    }
    if (changed) {
      labelArray.current = Object.values(program.labels);
    }
  }, [program]);

  if (!program) {
    return null;
  }

  // TODO Memoize labels, memory

  return (
    <Block
      title='Main Memory'
      className='w-issue'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Main Memory</DialogTitle>
            <DialogDescription>
              {descriptions.mainMemory?.shortDescription}
            </DialogDescription>
          </DialogHeader>
          <table className='mb-4'>
            <thead>
              <tr>
                <th>Label</th>
                <th>Address</th>
              </tr>
            </thead>
            <tbody>
              {labelArray.current?.map((label) => {
                return (
                  <tr key={label.name}>
                    <td>{label.name}</td>
                    <td>{label.address.stringRepresentation}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <div className='py-1 text-md'>
            Memory Inspector shows the memory up to the highest touched address
          </div>
          <div className='max-h-64 overflow-scroll rounded-md border p-2 shadow-inner'>
            <HexDump
              memory={memory}
              oldMemory={oldMemory}
              labels={labelArray.current ?? []}
              bytesInRow={16}
              showAscii
            />
          </div>
        </DialogContent>
      }
    >
      <div className='max-h-80 flex justify-center text-sm gap-2 font-mono'>
        <HexDump
          memory={memory}
          oldMemory={oldMemory}
          labels={labelArray.current ?? []}
          bytesInRow={8}
        />
      </div>
    </Block>
  );
}

export type HexDumpProps = {
  memory: Uint8Array;
  oldMemory: Uint8Array;
  labels: Label[];
  /**
   * Should be a multiple of 4
   */
  bytesInRow: number;
  showAscii?: boolean;
};

/**
 * The spacing between columns is done with the .memory-grid class (see globals.css)
 */
const HexDump = memo(function HexDump({
  memory,
  oldMemory,
  labels,
  bytesInRow,
  showAscii = false,
}: HexDumpProps) {
  const rows = memory.length / bytesInRow;

  const startAddress = 624; // TODO

  if (memory.length === 0) {
    return <div className='text-center text-gray-500'>Empty</div>;
  }

  const addresses = [];
  for (let i = 0; i < rows; i++) {
    addresses.push(
      <div key={i}>{`0x${(i * bytesInRow)
        .toString(16)
        .padStart(4, '0')}`}</div>,
    );
  }

  const bytes = new Array<JSX.Element>(memory.length);
  memory.forEach((byte, index) => {
    const changed = oldMemory[index] !== undefined && oldMemory[index] !== byte;
    const cls = clsx(changed && 'bg-primary rounded-md text-onPrimary');
    bytes[index] = (
      //biome-ignore lint: Index is appropriate here
      <div key={index} className={cls} data-value={byte}>
        {byte.toString(16).padStart(2, '0')}
      </div>
    );
  });

  // Add labels
  // TODO: separate the data labels and code labels using an extra field in the label object
  for (const label of labels) {
    const index = label.address.bits;
    const el = bytes[index];
    bytes[index] = (
      <div
        key={index}
        className='relative bg-secondary-80 -m-1 p-1 rounded hover:bg-red-500 hover:rounded-l-none duration-150 group'
      >
        <div className='absolute top-0 right-full h-full p-1 rounded-l bg-red-500 invisible opacity-0 group-hover:visible group-hover:opacity-100 duration-150 translate-x-6 group-hover:translate-x-0'>
          {label.name}
        </div>
        {el}
      </div>
    );
  }

  let ascii = null;
  if (showAscii) {
    ascii = [];
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
    <div className='overflow-y-auto flex text-sm gap-6 font-mono'>
      <div
        className='grid memory-grid justify-center gap-1'
        style={{
          gridTemplateColumns: `repeat(${bytesInRow + 1}, max-content)`,
          gridTemplateRows: `repeat(${rows}, max-content)`,
        }}
      >
        <div className='grid grid-rows-subgrid row-span-full col-span-1'>
          {addresses}
        </div>
        {bytes}
        {showAscii && (
          <div
            className='grid grid-rows-subgrid row-span-full col-span-1'
            style={{
              gridColumn: '-1',
            }}
          >
            {ascii}
          </div>
        )}
      </div>
    </div>
  );
});

/**
 * Custom hook to keep track of the previous value
 * Source: https://github.com/sergeyleschev/react-custom-hooks
 */
function usePrevious<T>(value: T) {
  const currentRef = useRef(value);
  const previousRef = useRef<T>(value);
  if (currentRef.current !== value) {
    previousRef.current = currentRef.current;
    currentRef.current = value;
  }
  return previousRef.current;
}
