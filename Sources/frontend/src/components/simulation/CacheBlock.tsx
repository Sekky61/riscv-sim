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

'use client';

import { type DecodedCacheLine, selectCache } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import { DividedBadge } from '@/components/DividedBadge';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { Block } from '@/components/simulation/Block';
import { hexPadEven } from '@/lib/utils';
import clsx from 'clsx';

/**
 * Display the cache, lines are grouped by the index.
 * Valid lines are highlighted.
 * Note: cache _line_ is the correct term (not lane)
 */
export default function CacheBlock() {
  const cache = useAppSelector(selectCache);
  const descriptions = useBlockDescriptions();

  if (!cache) return null;

  // first dimension is the index, second is the associativity

  const policy = cache.replacementPolicyType;

  return (
    <Block
      title='Cache'
      stats={
        <div className='badge-container'>
          <DividedBadge>
            <div>Lane Size</div>
            <div>{cache.lineSize}B</div>
          </DividedBadge>
          <DividedBadge>
            <div>Address</div>
            <div>{cache.indexBits} index bits</div>
            <div>{cache.offsetBits} offset bits</div>
          </DividedBadge>
          <DividedBadge>
            <div>Eviction Policy</div>
            <div>{policy}</div>
          </DividedBadge>
        </div>
      }
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cache</DialogTitle>
            <DialogDescription>
              {descriptions.cache?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
    >
      <div className='cache-grid'>
        <div className='grid grid-cols-subgrid col-span-3'>
          <div>Index</div>
          <div>Tag</div>
          <div>Line</div>
        </div>
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
    <div
      className='cache-line font-mono'
      style={{
        gridRow: `span ${nOfLanes}`,
        gridTemplateRows: `repeat(${nOfLanes}, 1fr)`,
      }}
    >
      <div
        className='flex justify-center items-center font-bold border-x border-b rounded-l box-border px-2'
        style={{
          gridRow: `span ${nOfLanes} / span ${nOfLanes}`,
        }}
      >
        {hexPadEven(lanes[0]?.index || 0)}
      </div>
      {lanes.map((lane, index) => {
        const cls = clsx('cache-lines-content', lane.valid && 'valid-line');
        return (
          <div key={index} className={cls} aria-disabled={!lane.valid}>
            <div className='line-tag border-r border-b p-1'>
              {hexPadEven(lane.tag)}
            </div>
            <div className='flex gap-1 items-center border-b border-r p-1 cache-line-bytes'>
              {lane.decodedLine.map((byte, index) => {
                return (
                  <span key={index}>{byte.toString(16).padStart(2, '0')}</span>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
