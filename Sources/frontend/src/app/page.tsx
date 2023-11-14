/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The main page of the application with the simulation
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

import { ZoomIn, ZoomOut } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

import {
  pullCodeFromCompiler,
  reloadSimulation,
  selectCpu,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import AnimatedButton from '@/components/AnimatedButton';
import CanvasWindow from '@/components/CanvasWindow';
import Placement from '@/components/Placement';
import DecodeBlock from '@/components/simulation/DecodeBlock';
import FetchBlock from '@/components/simulation/FetchBlock';
import FunctionUnitGroup from '@/components/simulation/FunctionUnitGroup';
import IssueWindow from '@/components/simulation/IssueWindow';
import Line from '@/components/simulation/Line';
import LoadBuffer from '@/components/simulation/LoadBuffer';
import Program from '@/components/simulation/Program';
import ReorderBuffer from '@/components/simulation/ReorderBuffer';
import StoreBuffer from '@/components/simulation/StoreBuffer';
import Timeline from '@/components/simulation/Timeline';

export default function HomePage() {
  const [scale, setScale] = useState(1);
  const dispatch = useAppDispatch();
  const state = useAppSelector(selectCpu);

  useEffect(() => {
    if (!state) {
      // TODO: calls multiple times unnecessarily
      dispatch(pullCodeFromCompiler());
      dispatch(reloadSimulation());
    }
  }, [state, dispatch]);

  const scaleUp = () => {
    setScale(scale + 0.2);
  };

  const scaleDown = () => {
    setScale(scale - 0.2);
  };

  const blockWidth = 184;
  const spaceBetweenBlocks = 50;

  const firstCol = 50;
  const secondCol = firstCol + blockWidth + spaceBetweenBlocks;
  const thirdCol = secondCol + blockWidth + spaceBetweenBlocks;
  const fourthCol = thirdCol + blockWidth + spaceBetweenBlocks;

  return (
    <>
      <CanvasWindow scale={scale}>
        <Placement x={firstCol} y={50}>
          <Program />
        </Placement>

        {/* Fetch block, line program--fetch */}
        <Placement x={firstCol + blockWidth} y={100}>
          <Line length={50} />
        </Placement>

        {/* Fetch, Decode block, line fetch--decode */}
        <Placement x={secondCol} y={50} className='flex flex-col items-center'>
          <FetchBlock />
          <Line length={50} down />
          <DecodeBlock />
        </Placement>

        <Placement x={thirdCol} y={50}>
          <ReorderBuffer />
        </Placement>

        <Placement x={thirdCol + blockWidth} y={145}>
          <Line length={610} />
        </Placement>

        <Placement x={fourthCol} y={150}>
          <div className='flex gap-[50px]'>
            <div className='flex flex-col items-center'>
              <Line length={50} down />
              <IssueWindow type='alu' />
              <Line length={10} down />
              <IssueWindow type='fp' />
              <Line length={50} down />
              <div className='flex gap-2 flex-col'>
                <FunctionUnitGroup type='alu' />
              </div>
            </div>
            <div className='flex flex-col items-center'>
              <Line length={50} down />
              <IssueWindow type='branch' />
              <Line length={50} down />
              <div className='flex gap-2 flex-col'>
                <FunctionUnitGroup type='branch' />
              </div>
            </div>
            <div className='flex flex-col items-center'>
              <Line length={50} down />
              <IssueWindow type='ls' />
            </div>
          </div>
        </Placement>

        <Placement x={fourthCol} y={600}>
          <div className='flex gap-[50px]'>
            <LoadBuffer />
            <StoreBuffer />
          </div>
        </Placement>
      </CanvasWindow>
      <div className='pointer-events-none absolute top-0 flex w-full justify-center pt-2'>
        <Timeline className='pointer-events-auto' />
      </div>
      <div className='absolute bottom-0 right-0 flex flex-col gap-4 p-4'>
        <ScaleButtons scaleUp={scaleUp} scaleDown={scaleDown} />
      </div>
    </>
  );
}

export type ScaleButtonsProps = {
  scaleUp: () => void;
  scaleDown: () => void;
};

/**
 * also provides shortcuts
 */
const ScaleButtons = ({ scaleUp, scaleDown }: ScaleButtonsProps) => {
  useHotkeys(
    'ctrl-+',
    () => {
      scaleUp();
    },
    { combinationKey: '-', preventDefault: true },
    [scaleUp],
  );

  useHotkeys(
    'ctrl+-',
    () => {
      scaleDown();
    },
    { preventDefault: true },
    [scaleDown],
  );

  return (
    <>
      <AnimatedButton
        shortCut='ctrl-+'
        shortCutOptions={{ combinationKey: '-', preventDefault: true }}
        clickCallback={scaleUp}
        className='bg-gray-100 rounded-full drop-shadow'
        description='Zoom in'
      >
        <ZoomIn strokeWidth={1.5} />
      </AnimatedButton>
      <AnimatedButton
        shortCut='ctrl+-'
        shortCutOptions={{ preventDefault: true }}
        clickCallback={scaleDown}
        className='bg-gray-100 rounded-full drop-shadow'
        description='Zoom out'
      >
        <ZoomOut strokeWidth={1.5} />
      </AnimatedButton>
    </>
  );
};
