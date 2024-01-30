/**
 * @file    useReloadSim.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Hook for reloading the simulation if needed
 *
 * @date    30 January 2024, 21:00 (created)
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

import { useEffect, useState } from 'react';

import { reloadSimulation, selectCpu } from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import CanvasWindow from '@/components/CanvasWindow';
import { Button } from '@/components/base/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';

import { selectAsmCode, selectEntryPoint } from '@/lib/redux/compilerSlice';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import { pullSimConfig, selectRunningConfig } from '@/lib/redux/simConfigSlice';
import { notify } from 'reapop';

export const useReloadSim = () => {
  const dispatch = useAppDispatch();
  const cpu = useAppSelector(selectCpu);

  const same = useAreSame();

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

  return { same, reload };
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
