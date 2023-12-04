/**
 * @file    Cache.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A block displaying the cache
 *
 * @date    01 December 2023, 20:10 (created)
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

import { DecodedCacheLine, selectCache } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import { CacheLineModel } from '@/lib/types/cpuApi';
import { hexPadEven } from '@/lib/utils';
import clsx from 'clsx';

/**
 * Display the memory like a hexdump
 */
export default function CacheBlock() {
  const cache = useAppSelector(selectCache);

  if (!cache) return null;

  // first dimension is the index, second is the associativity

  const policy = cache.replacementPolicyType;

  return (
    <Block title='Cache' stats={policy}>
      <div className='cache-grid'>
        <div className='border-b'>Index</div>
        <div className='border-b'>Tag</div>
        <div className='border-b'>Line</div>
        {cache.cache.map((row, index) => (
          <CacheLane key={index} lanes={row} />
        ))}
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
    <div className='cache-line font-mono' style={{
      gridRow: `span ${nOfLanes}`,
      gridTemplateRows: `repeat(${nOfLanes}, 1fr)`,
    }}>
      <div className='flex justify-center items-center font-bold border-x border-b box-border p-1' style={{
        gridRow: `span ${nOfLanes} / span ${nOfLanes}`
      }}>
        {hexPadEven(lanes[0]?.index || 0)}
      </div>
      {lanes.map((lane, index) => {
        const cls = clsx('border-r border-b p-1', lane.valid && 'bg-green-200', !lane.valid && 'text-gray-400');
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
      )
          })}
    </div>
  );
}
