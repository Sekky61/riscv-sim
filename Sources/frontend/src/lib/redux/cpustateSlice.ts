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

import {
  collectIds,
  getArrayItems,
  resolveRefsCopy,
} from '@/lib/cpuState/util';
import { selectAsmCode } from '@/lib/redux/compilerSlice';
import { selectActiveIsa } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import { callSimulationImpl } from '@/lib/serverCalls/callSimulation';
import type { CpuState, InputCodeModel } from '@/lib/types/cpuApi';
import type {
  DecodeAndDispatchBlock,
  InstructionFetchBlock,
  InstructionMemoryBlock,
  ReorderBufferState,
} from '@/lib/types/cpuDeref';

// Define a type for the slice state
interface CpuSlice {
  state: CpuState | null;
  code: string;
}

// Define the initial state using that type
const initialState: CpuSlice = {
  state: null,
  code: '',
};

type SimulationParsedResult = {
  state: CpuState;
};

// Call example: dispatch(reloadSimulation());
export const reloadSimulation = createAsyncThunk<SimulationParsedResult>(
  'cpu/reloadSimulation',
  async (_, { getState, dispatch }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const code = selectAsmCode(state);
    dispatch(setSimulationCode(code));
    const response = await callSimulationImpl(0, { ...config, code });
    return { state: response.state };
  },
);

// Call example: dispatch(callSimulation());
export const callSimulation = createAsyncThunk<SimulationParsedResult, number>(
  'cpu/callSimulation',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const code = state.cpu.code;
    const tick = arg;
    const response = await callSimulationImpl(tick, { ...config, code });
    return { state: response.state };
  },
);

// Call example: dispatch(simStepForward());
export const simStepForward = createAsyncThunk<SimulationParsedResult>(
  'cpu/simStepForward',
  async (_, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const code = state.cpu.code;
    const currentTick = selectTick(state);
    const response = await callSimulationImpl(currentTick + 1, {
      ...config,
      code,
    });
    return { state: response.state };
  },
);

// Call example: dispatch(simStepForward());
export const simStepBackward = createAsyncThunk<SimulationParsedResult>(
  'cpu/simStepBackward',
  async (_, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveIsa(state);
    const code = state.cpu.code;
    const currentTick = selectTick(state);
    const tick = Math.max(0, currentTick - 1);
    const response = await callSimulationImpl(tick, {
      ...config,
      code,
    });
    return { state: response.state };
  },
);

export const cpuSlice = createSlice({
  name: 'cpu',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    setSimulationCode: (state, action: PayloadAction<string>) => {
      state.code = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(reloadSimulation.fulfilled, (state, action) => {
        state.state = action.payload.state;
      })
      .addCase(reloadSimulation.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(reloadSimulation.pending, (_state, _action) => {
        // nothing
      })
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

export const { setSimulationCode } = cpuSlice.actions;

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
    return resolveRefsCopy(state.instructionMemoryBlock, map);
  },
);

export const selectProgram = createSelector(
  [selectCpu, selectIdMap],
  (state, map): InstructionMemoryBlock | null => {
    if (!state || !map) {
      return null;
    }

    const program = resolveRefsCopy(state.instructionMemoryBlock, map);

    const code = getArrayItems(program.code);

    const labels: Record<string, number> = {};
    Object.entries(program.labels).forEach(([key, value]) => {
      // Filter out @type entry, TODO handle generally
      if (key === '@type') {
        return;
      }
      labels[key] = value.value;
    });

    return {
      nop: program.nop,
      code,
      labels,
    };
  },
);

/**
 * Returns program code with labels inserted before the instruction they point to.
 */
export const selectProgramWithLabels = createSelector(
  [selectProgram],
  (program): Array<InputCodeModel | string> | null => {
    if (!program) {
      return null;
    }

    // COPY code
    const codeOrder: Array<InputCodeModel | string> = [...program.code];

    // For each label, insert it before the instruction it points to
    Object.entries(program.labels).forEach(([labelName, idx]) => {
      let insertIndex = codeOrder.findIndex(
        (instruction) =>
          typeof instruction !== 'string' && instruction.codeId === idx,
      );
      if (insertIndex === -1) {
        insertIndex = codeOrder.length;
      }
      codeOrder.splice(insertIndex, 0, labelName);
    });

    return codeOrder;
  },
);

export const selectFetch = createSelector(
  [selectCpu, selectIdMap],
  (state, map): InstructionFetchBlock | null => {
    if (!state || !map) {
      return null;
    }

    const fetch = state.instructionFetchBlock;
    const collectedFetchedCode = resolveRefsCopy(
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
    const before = resolveRefsCopy(
      getArrayItems(decode.beforeRenameCodeList),
      map,
    );
    const after = resolveRefsCopy(
      getArrayItems(decode.afterRenameCodeList),
      map,
    );
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
    const robQueue = resolveRefsCopy(rob.reorderQueue, map);
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
