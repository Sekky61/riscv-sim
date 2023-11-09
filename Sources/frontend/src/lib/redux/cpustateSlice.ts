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

import { selectAsmCode } from '@/lib/redux/compilerSlice';
import { selectActiveIsa } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import { callSimulationImpl } from '@/lib/serverCalls/callCompiler';
import type { CpuState, Reference, RegisterModel } from '@/lib/types/cpuApi';

// Define a type for the slice state
interface CpuSlice {
  state: CpuState | null;
  code: string;
  highlightedInputCode: Reference | null;
  highlightedSimCode: Reference | null;
}

// Define the initial state using that type
const initialState: CpuSlice = {
  state: null,
  code: '',
  highlightedInputCode: null,
  highlightedSimCode: null,
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
    highlightSimCode: (state, action: PayloadAction<Reference | null>) => {
      state.highlightedSimCode = action.payload;
    },
    unhighlight: (state, action: PayloadAction<Reference | null>) => {
      // Do not unhighlight somebody else's highlight
      if (state.highlightedSimCode === action.payload) {
        state.highlightedSimCode = null;
        state.highlightedInputCode = null;
      }
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

export const { setSimulationCode, highlightSimCode, unhighlight } =
  cpuSlice.actions;

export const selectCpu = (state: RootState) => state.cpu.state;
export const selectTick = (state: RootState) => state.cpu.state?.tick ?? 0;

export const selectAllInstructionFunctionModels = (state: RootState) =>
  state.cpu.state?.managerRegistry.instructionFunctionManager;
export const selectInstructionFunctionModelById = (
  state: RootState,
  id: Reference,
) => state.cpu.state?.managerRegistry.instructionFunctionManager[id];

export const selectAllInputCodeModels = (state: RootState) =>
  state.cpu.state?.managerRegistry.inputCodeManager;
export const selectInputCodeModelById = (state: RootState, id: Reference) =>
  state.cpu.state?.managerRegistry.inputCodeManager[id];

export const selectSimCodeModelById = (state: RootState, id: Reference) =>
  state.cpu.state?.managerRegistry.simCodeManager[id];

export const selectProgram = (state: RootState) =>
  state.cpu.state?.instructionMemoryBlock;

export const selectHighlightedSimCode = (state: RootState) =>
  state.cpu.highlightedSimCode;

/**
 * Returns program code with labels inserted before the instruction they point to.
 */
export const selectProgramWithLabels = createSelector(
  [selectProgram],
  (program): Array<Reference | string> | null => {
    if (!program) {
      return null;
    }

    // COPY code
    const codeOrder: Array<Reference | string> = [...program.code];

    // For each label, insert it before the instruction it points to
    Object.entries(program.labels).forEach(([labelName, idx]) => {
      let insertIndex = codeOrder.findIndex(
        (instruction) => typeof instruction !== 'string' && instruction === idx,
      );
      if (insertIndex === -1) {
        insertIndex = codeOrder.length;
      }
      codeOrder.splice(insertIndex, 0, labelName);
    });

    return codeOrder;
  },
);

export const selectAllRegisters = (state: RootState) =>
  state.cpu.state?.managerRegistry.registerModelManager;
export const selectRegisterIdMap = (state: RootState) =>
  state.cpu.state?.unifiedRegisterFileBlock.registerMap;

/**
 * Add aliases to the map of registers.
 */
export const selectRegisterMap = createSelector(
  [selectAllRegisters, selectRegisterIdMap],
  (registers, map): Record<string, RegisterModel> | null => {
    // Go through the map of ids and join the register models with the ids
    if (!registers || !map) {
      return null;
    }

    // Copy the registers
    const registerMap: Record<string, RegisterModel> = { ...registers };

    // Assign aliases (not in the map at the moment)
    Object.entries(map).forEach(([alias, id]) => {
      const register = registers[id];
      if (!register) {
        throw new Error(`Register ${id} not found`);
      }
      registerMap[alias] ??= register;
    });

    return registerMap;
  },
);

export const selectRegisterById = (state: RootState, id: Reference) =>
  selectRegisterMap(state)?.[id];

export const selectFetch = (state: RootState) =>
  state.cpu.state?.instructionFetchBlock;

export const selectDecode = (state: RootState) =>
  state.cpu.state?.decodeAndDispatchBlock;

export const selectROB = (state: RootState) =>
  state.cpu.state?.reorderBufferState;

export default cpuSlice.reducer;
