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
import {
  createAsyncThunk,
  createSelector,
  createSlice,
  PayloadAction,
} from '@reduxjs/toolkit';

import { getArrayItems, hasId, IdMap, resolveRefs } from '@/lib/cpuState/util';
import { selectActiveIsa } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import { callSimulationImpl } from '@/lib/serverCalls/callSimulation';
import { CpuState } from '@/lib/types/cpuApi';
import {
  DecodeAndDispatchBlock,
  InstructionFetchBlock,
  ReorderBufferState,
} from '@/lib/types/cpuDeref';

// Define a type for the slice state
interface CpuSlice {
  state: CpuState | null;
}

// Define the initial state using that type
const initialState: CpuSlice = {
  state: null,
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
    return { state: response.state };
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
      })
      .addCase(callSimulation.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(callSimulation.pending, (_state, _action) => {
        // nothing
      })
      .addCase(simStepForward.fulfilled, (state, action) => {
        state.state = action.payload.state;
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

/**
 * Collects all objects with ID into a map
 */
export const selectIdMap = createSelector([selectCpu], (state) => {
  if (!state) {
    return null;
  }
  return collectIds(state);
});

export const selectInputCodeModels = createSelector(
  [selectCpu, selectIdMap],
  (state, map) => {
    if (!state || !map) {
      return null;
    }
    return resolveRefs(state.instructionMemoryBlock, map);
  },
);

export const selectProgram = createSelector(
  [selectCpu, selectIdMap],
  (state, map) => {
    if (!state || !map) {
      return null;
    }
    return resolveRefs(state.instructionMemoryBlock, map);
  },
);

export const selectFetch = createSelector(
  [selectCpu, selectIdMap],
  (state, map): InstructionFetchBlock | null => {
    if (!state || !map) {
      return null;
    }

    const fetch = state.instructionFetchBlock;
    const collectedFetchedCode = resolveRefs(
      getArrayItems(fetch.fetchedCode),
      map,
    );
    return {
      numberOfWays: fetch.numberOfWays,
      fetchedCode: collectedFetchedCode,
      pc: fetch.pc,
      stallFlag: fetch.stallFlag,
      cycleId: fetch.cycleId,
    };
  },
);

export const selectDecode = createSelector(
  [selectCpu, selectIdMap],
  (state, map): DecodeAndDispatchBlock | null => {
    if (!state || !map) {
      return null;
    }

    const decode = state.decodeAndDispatchBlock;
    const before = resolveRefs(getArrayItems(decode.beforeRenameCodeList), map);
    const after = resolveRefs(getArrayItems(decode.afterRenameCodeList), map);
    return {
      beforeRenameCodeList: before,
      afterRenameCodeList: after,
      idCounter: decode.idCounter,
      flush: decode.flush,
      stallFlag: decode.stallFlag,
      stalledPullCount: decode.stalledPullCount,
      decodeBufferSize: decode.decodeBufferSize,
    };
  },
);

export const selectROB = createSelector(
  [selectCpu, selectIdMap],
  (state, map): ReorderBufferState | null => {
    if (!state || !map) {
      return null;
    }

    const rob = state.reorderBufferState;
    const robQueue = resolveRefs(rob.reorderQueue, map);
    return {
      reorderQueue: robQueue,
      commitLimit: rob.commitLimit,
      commitId: rob.commitId,
      speculativePulls: rob.speculativePulls,
      bufferSize: rob.bufferSize,
    };
  },
);

export default cpuSlice.reducer;
