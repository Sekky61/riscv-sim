/**
 * @file    simConfigSlice.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Slice for simulation configuration
 *
 * @date    27 January 2024, 23:00 (created)
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

import { type SimulationConfig, defaultSimulationConfig } from '@/lib/forms/Isa';
import { selectAsmCode, selectEntryPoint } from '@/lib/redux/compilerSlice';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import {
  type Action,
  type PayloadAction,
  type ThunkAction,
  createSlice,
} from '@reduxjs/toolkit';

interface SimConfigSlice {
  /**
   * Simulation config that is currently being simulated. Pulled from the ISA state and the compiler state on request.
   */
  config: SimulationConfig;
}

const initialState: SimConfigSlice = {
  config: defaultSimulationConfig,
};

/**
 * Copy the code, config and entry point from code editor and config to the simulation.
 * This decouples the settings and what is being simulated at the moment.
 */
export const pullSimConfig = (): ThunkAction<
  void,
  RootState,
  unknown,
  Action<string>
> => {
  return async (dispatch, getState) => {
    const state: RootState = getState();
    const code = selectAsmCode(state);
    const config = selectActiveConfig(state);
    const entryPoint = selectEntryPoint(state);
    dispatch(
      setSimConfig({
        code,
        entryPoint,
        cpuConfig: config.cpuConfig,
        memoryLocations: config.memoryLocations,
      }),
    );
  };
};

export const simConfigSlice = createSlice({
  name: 'cpu',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    setSimConfig: (state, action: PayloadAction<SimulationConfig>) => {
      state.config = action.payload;
    },
  },
});
export type SimConfigReducer = ReturnType<typeof simConfigSlice.reducer>;

export const { setSimConfig } = simConfigSlice.actions;

export const selectRunningConfig = (state: RootState) => state.simConfig.config;
export const selectSimulatedCode = (state: RootState) =>
  state.simConfig.config.code;

export default simConfigSlice.reducer;
