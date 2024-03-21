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
import type { Label } from '@/lib/types/cpuApi';
import { useEffect, useRef } from 'react';
import { FixedSizeList, type ListChildComponentProps } from 'react-window';
import { useRefDimensions } from '@/lib/hooks/useRefDimensions';

/**
 * Display the memory like a hexdump.
 * Displays the whole memory, up to the highes touched address.
 *
 * TODO filter out program labels
 */
export default function MainMemory() {
  const program = useAppSelector(selectProgram);
  const descriptions = useBlockDescriptions();
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);
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
      className='w-issue h-80'
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
          <div className='h-64 overflow-scroll rounded-md border p-2 shadow-inner'>
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
      <div className='flex-grow justify-center text-sm gap-2 font-mono'>
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
function HexDump({
  memory,
  oldMemory,
  labels,
  bytesInRow,
  showAscii = false,
}: HexDumpProps) {
  const rows = memory.length / bytesInRow;
  const ref = useRef(null);
  const dimensions = useRefDimensions(ref);

  if (memory.length === 0) {
    return <div className='text-center text-gray-500'>Empty</div>;
  }

  // A lookup structure for labels. Key is the address
  const labelsLookup = new Map<number, string>();
  for (const label of labels) {
    labelsLookup.set(label.address.bits, label.name);
  }

  // A lookup structure for memory changes. Key is the address
  const memoryChanges: object = {};
  for (let i = 0; i < memory.length; i++) {
    if (oldMemory[i] !== memory[i]) {
      //@ts-ignore
      memoryChanges[i] = null;
    }
  }

  const Row = ({ index, style }: ListChildComponentProps) => {
    const baseAddress = index * bytesInRow;
    const bytes = [];
    for (let i = baseAddress; i < baseAddress + bytesInRow; i++) {
      const addr = baseAddress + i;
      const byte = memory[i] ?? 0;
      const label = labelsLookup.get(addr);
      const changed = Object.hasOwn(memoryChanges, i);
      const style = `${changed ? 'bg-red-200' : ''} ${
        byte === 0 ? 'text-gray-700' : ''
      }`;
      let cell = (
        <div key={addr} data-value={byte} className={style}>
          {byte.toString(16).padStart(2, '0')}
        </div>
      );
      if (label !== undefined) {
        cell = (
          <div
            key={addr}
            data-value={byte}
            className='relative bg-secondary-80 -m-1 p-1 rounded hover:bg-red-500 hover:rounded-l-none duration-150 group'
          >
            <div className='absolute top-0 right-full h-full p-1 rounded-l bg-red-500 invisible opacity-0 group-hover:visible group-hover:opacity-100 duration-150 translate-x-6 group-hover:translate-x-0'>
              {label}
            </div>
            {cell}
          </div>
        );
      }
      bytes.push(cell);
    }

    return (
      <div style={style} className='flex gap-2'>
        <div>{`0x${baseAddress.toString(16).padStart(4, '0')}`}</div>
        <div className='flex gap-0.5 items-center'>{bytes}</div>
        {showAscii && (
          <Ascii memory={memory} base={baseAddress} bytesInRow={bytesInRow} />
        )}
      </div>
    );
  };

  return (
    <div ref={ref} className='w-full h-full font-mono'>
      <FixedSizeList
        width={dimensions.width}
        height={dimensions.height}
        itemSize={25}
        itemCount={rows}
      >
        {Row}
      </FixedSizeList>
    </div>
  );
}

type AsciiProps = {
  memory: Uint8Array;
  base: number;
  bytesInRow: number;
};

function Ascii({ memory, base, bytesInRow }: AsciiProps) {
  // Build string of bytesInRow bytes
  let str = '';
  for (let j = 0; j < bytesInRow; j++) {
    const byte = memory[base + j];
    if (byte === undefined) {
      break;
    }
    if (byte >= 32 && byte <= 126) {
      str += String.fromCharCode(byte);
    } else {
      str += '.';
    }
  }
  return <div className='flex flex-col gap-2'>{str}</div>;
}

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
