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

import clsx from 'clsx';
import {
  ArrowBigLeft,
  ArrowBigRight,
  RefreshCcw,
  SkipForward,
} from 'lucide-react';

import {
  reloadSimulation,
  simStepBackward,
  simStepForward,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch } from '@/lib/redux/hooks';

import AnimatedButton from '@/components/AnimatedButton';

export type TimelineProps = Pick<
  React.HTMLAttributes<HTMLDivElement>,
  'className'
>;

// Control ticks of simulation
// Go forward, back, finish
export default function Timeline({ className = '' }: TimelineProps) {
  const dispatch = useAppDispatch();

  const cls = clsx('flex bg-red-300 rounded-full h-10', className);

  return (
    <div className={cls}>
      <div className='flex gap-2 rounded-full border bg-gray-100 p-1 drop-shadow'>
        <AnimatedButton
          shortCut='left'
          clickCallback={() => dispatch(simStepBackward())}
        >
          <ArrowBigLeft strokeWidth={1.5} />
        </AnimatedButton>
        <AnimatedButton
          shortCut='right'
          clickCallback={() => dispatch(simStepForward())}
        >
          <ArrowBigRight strokeWidth={1.5} />
        </AnimatedButton>
        <AnimatedButton shortCut='ctrl+enter'>
          <SkipForward strokeWidth={1.5} />
        </AnimatedButton>
      </div>
      <div className='p-1 border border-transparent'>
        <AnimatedButton clickCallback={() => dispatch(reloadSimulation())}>
          <RefreshCcw strokeWidth={1.5} />
        </AnimatedButton>
      </div>
    </div>
  );
}
