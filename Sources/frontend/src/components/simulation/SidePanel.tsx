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

import { selectStatistics } from '@/lib/redux/cpustateSlice';
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

  const cls = clsx('h-screen p-2 relative', isExpanded && 'w-80');

  const gridCls = clsx(
    'grid gap-2',
    isExpanded && 'grid-cols-2',
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
    </div>
  );
}

interface StatProps {
  label: React.ReactNode;
  value: string;
}

function SmallBubble({ label, value }: StatProps) {
  return (
    <div className='text-wrap font-bold aspect-square p-1 rounded-md text-sm flex flex-col justify-center gap-1 items-center border-2 border-primary'>
      {label}
      <span className='font-normal'>{value}</span>
    </div>
  );
}
