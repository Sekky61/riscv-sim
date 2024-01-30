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

import { reloadSimulation, selectCpu } from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import AnimatedButton from '@/components/AnimatedButton';
import CanvasWindow from '@/components/CanvasWindow';
import { Button } from '@/components/base/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import BranchBlock from '@/components/simulation/BranchBlock';
import CacheBlock from '@/components/simulation/CacheBlock';
import DecodeBlock from '@/components/simulation/DecodeBlock';
import FetchBlock from '@/components/simulation/FetchBlock';
import FunctionUnitGroup from '@/components/simulation/FunctionUnitGroup';
import IssueWindow from '@/components/simulation/IssueWindow';
import LoadBuffer from '@/components/simulation/LoadBuffer';
import MainMemory from '@/components/simulation/MainMemory';
import Program from '@/components/simulation/Program';
import ReorderBuffer from '@/components/simulation/ReorderBuffer';
import StoreBuffer from '@/components/simulation/StoreBuffer';
import Timeline from '@/components/simulation/Timeline';
import { selectAsmCode, selectEntryPoint } from '@/lib/redux/compilerSlice';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import { pullSimConfig, selectRunningConfig } from '@/lib/redux/simConfigSlice';
import { notify } from 'reapop';

export default function HomePage() {
  const [scale, setScale] = useState(1);
  const dispatch = useAppDispatch();
  const cpu = useAppSelector(selectCpu);

  const same = useAreSame();
  const [openModal, setOpenModal] = useState(!same);

  // On page load, check if the simulation config is up to date, show modal to warn and offer to reload
  // biome-ignore lint: supposed to run only once after page load
  useEffect(() => {
    if (cpu === null) {
      reload();
    }
  }, []);

  const reload = () => {
    dispatch(pullSimConfig());
    dispatch(reloadSimulation());
    dispatch(
      notify({
        title: 'Simulation reloaded',
        status: 'success',
      }),
    );
  };

  const scaleUp = () => {
    setScale(scale + 0.2);
  };

  const scaleDown = () => {
    setScale(scale - 0.2);
  };

  return (
    <>
      <CanvasWindow scale={scale}>
        <div className='global-grid'>
          <div className='col-grid'>
            <Program />
            <div className='flex flex-col gap-4'>
              <BranchBlock />
              <FetchBlock />
              <DecodeBlock />
            </div>
            <ReorderBuffer />
          </div>
          <div className='sim-grid justify-items-center'>
            <IssueWindow type='alu' />
            <FunctionUnitGroup type='alu' />
            <IssueWindow type='fp' />
            <FunctionUnitGroup type='fp' />
            <IssueWindow type='branch' />
            <FunctionUnitGroup type='branch' />
          </div>
          <div className='sim-grid'>
            <div className='flex justify-center'>
              <FunctionUnitGroup type='memory' />
            </div>
            <div className='flex gap-4'>
              <StoreBuffer />
              <LoadBuffer />
            </div>
          </div>
          <div>
            <div className='flex gap-4 mb-4 items-start'>
              <MainMemory />
              <CacheBlock />
            </div>
          </div>
        </div>
      </CanvasWindow>
      <div className='pointer-events-none absolute top-0 flex w-full justify-center pt-2'>
        <Timeline className='pointer-events-auto' />
      </div>
      <div className='absolute bottom-0 right-0 flex flex-col gap-4 p-4'>
        <ScaleButtons scaleUp={scaleUp} scaleDown={scaleDown} />
      </div>
      <Dialog open={openModal} onOpenChange={setOpenModal}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Config not up to date</DialogTitle>
            <DialogDescription>
              The code or the configuration of the simulation has changed. Do
              you want to reload the simulation with the new configuration and
              code?
            </DialogDescription>
          </DialogHeader>
          <div className='flex gap-4'>
            <Button
              onClick={() => {
                reload();
                setOpenModal(false);
              }}
            >
              Yes, reload simulation
            </Button>
            <Button
              onClick={() => {
                setOpenModal(false);
              }}
            >
              No, keep current simulation
            </Button>
          </div>
        </DialogContent>
      </Dialog>
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

/**
 * Compare differences between the current simulation config and the code editor and config page.
 * If there are any, ask the user if they want to reload the simulation.
 */
const useAreSame = () => {
  const config = useAppSelector(selectActiveConfig);
  const runningConfig = useAppSelector(selectRunningConfig);
  const code = useAppSelector(selectAsmCode);
  const entryPoint = useAppSelector(selectEntryPoint);

  const memoryEqual =
    JSON.stringify(config.memoryLocations) ===
    JSON.stringify(runningConfig.memoryLocations);

  const cpuConfigEqual =
    JSON.stringify(config.cpuConfig) ===
    JSON.stringify(runningConfig.cpuConfig);

  const same =
    code === runningConfig.code &&
    entryPoint === runningConfig.entryPoint &&
    memoryEqual &&
    cpuConfigEqual;

  return same;
};
