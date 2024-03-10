/**
 * @file    RegisterBlock.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Display the register file
 *
 * @date    08 March 2024, 18:00 (created)
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
  DecodedCacheLine,
  selectAllRegisters,
  selectCache,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import { hexPadEven } from '@/lib/utils';
import clsx from 'clsx';
import { DividedBadge } from '@/components/DividedBadge';

/**
 * Display the cache, lines are grouped by the index.
 * Valid lines are highlighted.
 */
export default function RegisterBlock() {
  // including speculative
  const registers = useAppSelector(selectAllRegisters);
  const descriptions = useBlockDescriptions();

  if (!registers) return null;

  // first dimension is the index, second is the associativity

  return (
    <Block
      title='Register File'
      stats={
        <div className='flex'>
          <DividedBadge>
            <div>nal</div>
          </DividedBadge>
        </div>
      }
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Register File</DialogTitle>
            <DialogDescription>
              {descriptions.registerFile?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
    >
      <div>
        {Object.values(registers).map((reg, index) => {
          return (
            <div key={index}>
              {reg.name}: {hexPadEven(reg.value.bits)}
            </div>
          );
        })}
      </div>
    </Block>
  );
}

/**
 * Display a single row of the memory
 */
function CacheLane({
  lanes,
}: {
  lanes: DecodedCacheLine[];
}) {
  const nOfLanes = lanes.length;
  return (
    <div
      className='cache-line font-mono'
      style={{
        gridRow: `span ${nOfLanes}`,
        gridTemplateRows: `repeat(${nOfLanes}, 1fr)`,
      }}
    >
      <div
        className='flex justify-center items-center font-bold border-x border-b box-border p-1'
        style={{
          gridRow: `span ${nOfLanes} / span ${nOfLanes}`,
        }}
      >
        {hexPadEven(lanes[0]?.index || 0)}
      </div>
      {lanes.map((lane, index) => {
        const cls = clsx(
          'border-r border-b p-1',
          lane.valid && 'bg-green-200',
          !lane.valid && 'text-gray-400',
        );
        return (
          <div key={index} className='cache-lines-cont'>
            <div className={cls}>{hexPadEven(lane.tag)}</div>
            <div className='flex gap-1 border-b border-r p-1'>
              {lane.decodedLine.map((byte, index) => {
                return (
                  <div key={index}>
                    <div>{byte.toString(16).padStart(2, '0')}</div>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
