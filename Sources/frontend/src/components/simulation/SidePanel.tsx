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

import {
  callSimulation,
  selectDebugLog,
  selectStatistics,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';
import { ChevronLeft } from 'lucide-react';
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

  const gridCls = clsx(
    'grid gap-2',
    isExpanded && 'grid-cols-3',
    !isExpanded && 'grid-cols-1',
  );

  return (
    <div className='h-screen p-2 relative flex flex-col gap-2'>
      <button
        type='button'
        className='w-full h-[40px] secondary-container rounded-[12px] flex justify-center items-center'
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <ChevronLeft className={clsx(isExpanded && 'rotate-180')} />
      </button>
      <div className={gridCls}>
        <SmallBubble label='Cycles' value={statistics.clockCycles.toString()} />
        <SmallBubble
          label='Commit #'
          value={statistics.committedInstructions.toString()}
        />
        <SmallBubble label='IPC' value={statistics.ipc.toFixed(2)} />
        <SmallBubble label='Branch accuracy' value={branchAccuracy} />
        {isExpanded && (
          <>
            <SmallBubble label='FLOPS' value={statistics.flops.toFixed(2)} />
            <SmallBubble
              label='Cache Hit Rate'
              value={statistics.cache.hitRate.toFixed(2)}
            />
          </>
        )}
      </div>
      {isExpanded && <h3 className='mt-2'>Debug log</h3>}
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
    <div className='w-20 surface border border-outlineVariant rounded-[12px] text-wrap font-bold aspect-square p-1 text-sm flex flex-col justify-center gap-1 items-center'>
      <span className='text-center'>{label}</span>
      <span className='font-normal'>{value}</span>
    </div>
  );
}

/**
 * Render the debug log.
 */
function DebugLog() {
  const dispatch = useAppDispatch();
  const debugLog = useAppSelector(selectDebugLog);

  function loadSim(cycle: number) {
    dispatch(callSimulation(cycle));
  }

  // w-0 to make the div shrink to fit the parent
  // the key can be the cycle, because at most one entry is added per cycle
  return (
    <div
      className='w-0 min-w-full h-0 min-h-full overflow-x-clip overflow-y-scroll surface-container-highest rounded-md border p-2 text-nowrap font-mono text-sm grid content-start'
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
            <button
              type='button'
              className='self-start font-bold text-end underline'
              onClick={() => loadSim(entry.cycle)}
            >
              {entry.cycle}
            </button>
            <div className='text-wrap'>: {entry.message}</div>
          </div>
        );
      })}
    </div>
  );
}
