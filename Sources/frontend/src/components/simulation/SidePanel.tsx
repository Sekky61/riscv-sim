/**
 * @file    SidePanel.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Side panel with simulation stats
 *
 * @date    31 January 2024, 21:00 (created)
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

import { selectDebugLog, selectStatistics } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';
import React, { useState } from 'react';

/**
 * When the panel is narrow, show only some stats.
 * When the panel is wide, show more stats and the debug prints.
 */
export function SidePanel() {
  const statistics = useAppSelector(selectStatistics);
  const [isExpanded, setIsExpanded] = useState(false);

  if (!statistics) {
    return null;
  }

  const branchAccuracy = `${(statistics.predictionAccuracy * 100).toFixed(2)}%`;

  const cls = clsx('h-screen p-2 relative flex flex-col gap-2');

  const gridCls = clsx(
    'grid gap-2',
    isExpanded && 'grid-cols-3',
    !isExpanded && 'grid-cols-1',
  );

  return (
    <div className={cls}>
      <div
        className='absolute top-1/2 right-full mr-5 expand-area w-8 h-16'
        data-expanded={isExpanded}
      >
        <button
          type='button'
          className='w-full h-full'
          onClick={() => setIsExpanded(!isExpanded)}
        >
          <div className='w-full h-full flex justify-center items-center'>
            <div className='w-6 h-6 flex flex-col items-center'>
              <div className='h-3 w-1 rounded-full bg-black expand-arrow-up' />
              <div className='h-3 w-1 rounded-full bg-black expand-arrow-down' />
            </div>
          </div>
        </button>
      </div>
      <div className={gridCls}>
        <SmallBubble label='Cycles' value={statistics.clockCycles.toString()} />
        <SmallBubble
          label='Commit #'
          value={statistics.committedInstructions.toString()}
        />
        <SmallBubble label='IPC' value={statistics.ipc.toFixed(2)} />
        <SmallBubble
          label={
            <>
              <span>Branch</span>
              <span>Accuracy</span>
            </>
          }
          value={branchAccuracy}
        />
      </div>
      {isExpanded && <h3 className='my-2'>Debug log</h3>}
      <div className='flex-grow w-full'>
        <DebugLog />
      </div>
    </div>
  );
}

interface StatProps {
  label: string | JSX.Element;
  value: string;
}

function SmallBubble({ label, value }: StatProps) {
  return (
    <div className='w-20 text-wrap font-bold aspect-square p-1 rounded-md text-sm flex flex-col justify-center gap-1 items-center border-2 border-primary'>
      {label}
      <span className='font-normal'>{value}</span>
    </div>
  );
}

/**
 * Render the debug log.
 */
function DebugLog() {
  const debugLog = useAppSelector(selectDebugLog);

  // w-0 to make the div shrink to fit the parent
  // the key can be the cycle, because at most one entry is added per cycle
  return (
    <div
      className='w-0 min-w-full overflow-clip rounded-md border p-2 text-nowrap font-mono text-sm grid'
      style={{
        gridTemplateColumns: 'max-content 1fr',
      }}
    >
      {debugLog?.entries.map((entry, index) => {
        return (
          <div
            key={entry.cycle}
            className='grid grid-rows-subgrid grid-cols-subgrid col-span-2'
          >
            <div className='font-bold text-end'>{entry.cycle}:</div>
            <div className=''>{entry.message}</div>
          </div>
        );
      })}
    </div>
  );
}
