/**
 * @file    Timeline.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Control ticks of simulation
 *
 * @date    19 September 2023, 22:00 (created)
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

import clsx from 'clsx';
import {
  ChevronFirst,
  ChevronLast,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

import {
  reloadSimulation,
  selectStopReason,
  selectTick,
  simStepBackward,
  simStepEnd,
  simStepForward,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import AnimatedButton from '@/components/AnimatedButton';
import { stopReasonToShortString } from '@/lib/utils';

export type TimelineProps = Pick<
  React.HTMLAttributes<HTMLDivElement>,
  'className'
>;

// Control ticks of simulation
// Go forward, back, finish
export default function Timeline({ className = '' }: TimelineProps) {
  const dispatch = useAppDispatch();
  const tick = useAppSelector(selectTick);
  const stopReason = useAppSelector(selectStopReason);

  const cls = clsx('timeline-grid drop-shadow', className);

  let state = 0;
  let message = '';
  if (tick > 0) {
    state = 1;
  }
  if (stopReason !== 'kNotStopped') {
    state = 2;
    message = stopReasonToShortString(stopReason);
  }

  // The .controls is rotated, see the css file.
  // todo make buttons unselectable in certain states
  return (
    <div className={cls} data-state={state} data-reset={false}>
      <div className='controls rounded-full h-full box-content border bg-gray-100 flex flex-row-reverse justify-end items-center'>
        <AnimatedButton
          shortCut='left'
          clickCallback={() => dispatch(simStepBackward())}
          description='Step backward'
          className='left-arrow m-1 rotate-180'
        >
          <ChevronLeft strokeWidth={1.5} />
        </AnimatedButton>
        <AnimatedButton
          shortCut='right'
          clickCallback={() => dispatch(simStepForward())}
          description='Step forward'
          className='right-arrow m-1 rotate-180'
        >
          <ChevronRight strokeWidth={1.5} />
        </AnimatedButton>
        <AnimatedButton
          clickCallback={() => dispatch(simStepEnd())}
          shortCut='ctrl+enter'
          description='Skip to the end of simulation'
          className='m-1 rotate-180'
        >
          <ChevronLast strokeWidth={1.5} />
        </AnimatedButton>
      </div>
      <div className='reset h-full box-content border flex items-center justify-between bg-[#ff7171] rounded-full'>
        <AnimatedButton
          clickCallback={() => dispatch(reloadSimulation())}
          shortCut='r'
          description='Reload simulation'
          className='m-1 rotate-180'
        >
          <ChevronFirst strokeWidth={1.5} />
        </AnimatedButton>
        <div
          className={`${
            state === 2 ? 'opacity-100' : 'opacity-0'
          } flex-grow text-center rotate-180 mr-4`}
        >
          {message}
        </div>
      </div>
    </div>
  );
}
