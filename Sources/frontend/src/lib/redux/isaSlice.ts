/**
 * @file    isaSlice.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Redux state for saved configurations
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

import { PayloadAction, createSelector, createSlice } from '@reduxjs/toolkit';

// Import as type to avoid circular dependency
import type { RootState } from '@/lib/redux/store';

import {
  CpuConfig,
  MemoryLocationIsa,
  SimulationConfig,
  defaultCpuConfig,
  defaultSimulationConfig,
} from '../forms/Isa';

/**
 * The slice state type
 */
interface IsaState {
  /**
   * List of saved ISAs
   */
  isas: Array<SimulationConfig>;
  /**
   * Name of the active isa. References one of the isas in the list
   */
  activeIsaName: string;
}

/**
 * Define the initial state.
 * One ISA is always present, called "Default". It is selected.
 */
const initialState: IsaState = {
  isas: [defaultSimulationConfig],
  activeIsaName: defaultSimulationConfig.cpuConfig.name,
};

/**
 * Helper function to find an ISA by name
 */
function findIsaByName(
  isas: Array<SimulationConfig>,
  name: string,
): SimulationConfig | undefined {
  return isas.find((isa) => isa.cpuConfig.name === name);
}

export const isaSlice = createSlice({
  name: 'isaSl',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    newActiveIsa: (state, action: PayloadAction<string>) => {
      if (findIsaByName(state.isas, action.payload) === undefined)
        throw new Error('ISA not found');
      state.activeIsaName = action.payload;
    },
    /**
     * Create a new ISA. Make it active.
     * Fill code and memory locations with default values.
     */
    createIsa: (state, action: PayloadAction<CpuConfig>) => {
      if (findIsaByName(state.isas, action.payload.name) !== undefined) {
        throw new Error('ISA already exists');
      }
      // todo: check if assigning from defaultSimulationConfig is ok (references)
      const newIsa: SimulationConfig = {
        cpuConfig: action.payload,
        code: defaultSimulationConfig.code,
        memoryLocations: defaultSimulationConfig.memoryLocations,
        entryPoint: 0,
      };
      state.isas.push(newIsa);
      state.activeIsaName = action.payload.name;
    },
    updateIsa: (
      state,
      action: PayloadAction<{ oldName: string; isa: CpuConfig }>,
    ) => {
      if (action.payload.oldName === 'Default') {
        throw new Error('Cannot edit the default ISA');
      }
      // Update the ISA
      for (const isa of state.isas) {
        if (isa.cpuConfig.name === action.payload.oldName) {
          isa.cpuConfig = action.payload.isa;
        }
      }
    },
    /**
     * Enforces unique memory location names
     */
    addMemoryLocation: (state, action: PayloadAction<MemoryLocationIsa>) => {
      const activeIsa = findIsaByName(state.isas, state.activeIsaName);
      if (activeIsa === undefined) throw new Error('Active ISA not found');
      // Check if the name is unique
      if (
        activeIsa.memoryLocations.find(
          (loc) => loc.name === action.payload.name,
        ) !== undefined
      ) {
        throw new Error('Memory location name must be unique');
      }
      activeIsa.memoryLocations.push(action.payload);
    },
    /**
     * Update existing memory location. Name can be changed, but must be unique.
     */
    updateMemoryLocation: (
      state,
      action: PayloadAction<{
        oldName: string;
        memoryLocation: MemoryLocationIsa;
      }>,
    ) => {
      const activeIsa = findIsaByName(state.isas, state.activeIsaName);
      if (activeIsa === undefined) throw new Error('Active ISA not found');
      const oldLoc = activeIsa.memoryLocations.find(
        (loc) => loc.name === action.payload.oldName,
      );
      if (oldLoc === undefined) {
        throw new Error('Updating memory location that is undefined');
      }
      activeIsa.memoryLocations = activeIsa.memoryLocations.map((loc) => {
        if (loc.name === action.payload.oldName) {
          return action.payload.memoryLocation;
        }
        return loc;
      });
    },
    removeMemoryLocation: (state, action: PayloadAction<string>) => {
      const activeIsa = findIsaByName(state.isas, state.activeIsaName);
      if (activeIsa === undefined) throw new Error('Active ISA not found');
      activeIsa.memoryLocations = activeIsa.memoryLocations.filter(
        (loc) => loc.name !== action.payload,
      );
    },
    removeIsa: (state, action: PayloadAction<string>) => {
      // Do not allow to remove the first ISA (called "Default")
      if (action.payload === defaultCpuConfig.name) {
        throw new Error('Cannot remove the default configuration');
      }
      state.isas = state.isas.filter(
        (isa) => isa.cpuConfig.name !== action.payload,
      );
      // If the active ISA was removed, make the first one active
      if (state.activeIsaName === action.payload) {
        const defaultIsa = state.isas[0];
        if (defaultIsa === undefined) throw new Error('No default ISA found');
        state.activeIsaName = defaultIsa.cpuConfig.name;
      }
    },
  },
});

export type IsaReducer = ReturnType<typeof isaSlice.reducer>;

export const {
  newActiveIsa,
  createIsa,
  updateIsa,
  removeIsa,
  addMemoryLocation,
  removeMemoryLocation,
  updateMemoryLocation,
} = isaSlice.actions;

export const selectActiveIsaName = (state: RootState) =>
  state.isa.activeIsaName;
export const selectIsas = (state: RootState) => state.isa.isas;

/**
 * Select the active cpu config
 */
export const selectActiveConfig = createSelector(
  [selectIsas, selectActiveIsaName],
  (isas, name): SimulationConfig => {
    const isa = isas.find((isaItem) => isaItem.cpuConfig.name === name);
    if (isa === undefined) throw new Error('Active ISA not found');
    // reference the ISA config
    return isa;
  },
);

export default isaSlice.reducer;
