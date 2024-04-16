/**
 * @file    AutoPlay.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Codemirror extension for line coloring based on mapping
 *
 * @date    10 March 2024, 19:00 (created)
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

import { IconButton } from '@/components/IconButton';
import {
  selectAutoplay,
  selectAutoplayInterval,
  setAutoplay,
  setAutoplayInterval,
  simStepForward,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';
import { Pause, Play } from 'lucide-react';
import { useEffect } from 'react';

export function AutoPlay() {
  const dispatch = useAppDispatch();
  const autoplayRunning = useAppSelector(selectAutoplay);
  const ms = useAppSelector(selectAutoplayInterval);

  // Autoplay simulation.
  // TODO: may cause trouble if simulation is too slow (>=1s per tick)
  useEffect(() => {
    if (autoplayRunning) {
      const interval = setInterval(() => {
        dispatch(simStepForward());
      }, ms);
      return () => clearInterval(interval);
    }
  }, [autoplayRunning, ms, dispatch]);

  return (
    <div className='timeline-grid drop-shadow hidden sm:block'>
      <div
        className={clsx(
          'autoplay rounded-[16px] h-full box-content tertiary-container flex flex-row-reverse justify-end items-center',
        )}
      >
        <IconButton
          clickCallback={() => dispatch(setAutoplay(!autoplayRunning))}
          shortCut='a'
          description='Reload simulation'
          className='m-1'
          animate
        >
          {autoplayRunning ? (
            <Pause strokeWidth={1.5} />
          ) : (
            <Play strokeWidth={1.5} />
          )}
        </IconButton>
      </div>
      <div className='autoplay-ms rounded-[16px] px-2 h-full box-content tertiary-container flex justify-center items-center gap-0.5'>
        <input
          id='autoplay-interval'
          type='number'
          className='w-full rounded-[8px] border px-1 py-0.5 bg-white/50 dark:bg-black/50'
          value={ms}
          step={500}
          placeholder='Autoplay interval in ms'
          onChange={(e) =>
            dispatch(setAutoplayInterval(e.target.valueAsNumber))
          }
        />
        <div className='text-xs'>ms</div>
      </div>
    </div>
  );
}
