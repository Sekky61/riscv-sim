/**
 * @file    cpustateSlice.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Redux state for compiler
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

/* eslint-disable @typescript-eslint/ban-ts-comment */
import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';

import {
  getArrayItems,
  hasId,
  IdMap,
  isReference,
  isSimCodeModel,
  resolveRefs,
} from '@/lib/cpuState/util';
import { selectActiveIsa } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import { callSimulationImpl } from '@/lib/serverCalls/callSimulation';
import { CpuState } from '@/lib/types/cpuApi';
import { InstructionFetchBlock, SimCodeModel } from '@/lib/types/cpuDeref';

// Define a type for the slice state
interface CpuSlice {
  state: CpuState | null;
  idMap: IdMap;
}

// Define the initial state using that type
const initialState: CpuSlice = {
  state: null,
  idMap: {},
};

/**
 * Recusively collect all ids from cpuState, put the ids and objects in a map
 */
function collectIds(state: CpuState): IdMap {
  const idMap: IdMap = {};
  const queue: Array<unknown> = [state];
  while (queue.length > 0) {
    const obj = queue.pop();
    if (hasId(obj)) {
      // Add to map
      const id = obj['@id'];
      if (idMap[id] !== undefined) {
        throw new Error(`Duplicate id ${id}`);
      }
      idMap[id] = obj;
    }

    if (typeof obj === 'object' && obj !== null) {
      Object.values(obj).forEach((val) => {
        if (typeof val === 'object') {
          queue.push(val);
        }
      });
    }
  }
  return idMap;
}

type SimulationParsedResult = {
  state: CpuState;
  idMap: IdMap;
};

// Call example: dispatch(callSimulation());
export const callSimulation = createAsyncThunk<SimulationParsedResult, number>(
  'cpu/callSimulation',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const tick = arg;
    const response = await callSimulationImpl(tick, config);
    const map = collectIds(response.state);
    return { state: response.state, idMap: map };
  },
);

// Call example: dispatch(simStepForward());
export const simStepForward = createAsyncThunk<SimulationParsedResult>(
  'cpu/simStepForward',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const currentTick = selectTick(state);
    const response = await callSimulationImpl(currentTick + 1, config);
    const map = collectIds(response.state);
    return { state: response.state, idMap: map };
  },
);

// Call example: dispatch(simStepForward());
export const simStepBackward = createAsyncThunk<SimulationParsedResult>(
  'cpu/simStepBackward',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const currentTick = selectTick(state);
    const response = await callSimulationImpl(currentTick - 1, config);
    const map = collectIds(response.state);
    return { state: response.state, idMap: map };
  },
);

export const cpuSlice = createSlice({
  name: 'cpu',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    // Use the PayloadAction type to declare the contents of `action.payload`
    cFieldTyping: (state, _action: PayloadAction<string>) => {
      state.state = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // /simulation
      .addCase(callSimulation.fulfilled, (state, action) => {
        state.state = action.payload.state;
        state.idMap = action.payload.idMap;
      })
      .addCase(callSimulation.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(callSimulation.pending, (_state, _action) => {
        // nothing
      })
      .addCase(simStepForward.fulfilled, (state, action) => {
        state.state = action.payload.state;
        state.idMap = action.payload.idMap;
      })
      .addCase(simStepForward.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(simStepForward.pending, (_state, _action) => {
        // nothing
      })
      .addCase(simStepBackward.fulfilled, (state, action) => {
        state.state = action.payload.state;
      })
      .addCase(simStepBackward.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(simStepBackward.pending, (_state, _action) => {
        // nothing
      });
  },
});

export const { cFieldTyping } = cpuSlice.actions;

export const selectCpu = (state: RootState) => state.cpu.state;
export const selectTick = (state: RootState) => state.cpu.state?.tick ?? 0;

export const selectInstructionMemoryBlock = (state: RootState) =>
  state.cpu.state?.instructionMemoryBlock;

export const selectFetch = (state: RootState): InstructionFetchBlock | null => {
  const fetch = state.cpu.state?.instructionFetchBlock;
  if (!fetch) {
    return null;
  }
  const fetchedCode = getArrayItems(fetch.fetchedCode);
  const collectedFetchedCode: Array<SimCodeModel> = [];
  for (const code of fetchedCode) {
    if (!isReference(code)) {
      throw new Error(`Unexpected object ${code}`);
    }
    const id = code['@ref'];
    const resolvedObject = state.cpu.idMap[id];
    if (!resolvedObject) {
      throw new Error(`Id ${id} not found in idMap`);
    }
    const obj = resolveRefs(resolvedObject, state.cpu.idMap);
    // Check type
    if (!isSimCodeModel(obj)) {
      throw new Error(`Unexpected object ${obj}`);
    }
    collectedFetchedCode.push(obj);
  }
  return {
    numberOfWays: fetch.numberOfWays,
    fetchedCode: collectedFetchedCode,
    pc: fetch.pc,
    stallFlag: fetch.stallFlag,
    cycleId: fetch.cycleId,
  };
};

export default cpuSlice.reducer;
