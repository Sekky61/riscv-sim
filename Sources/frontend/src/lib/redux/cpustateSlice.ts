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
  Action,
  PayloadAction,
  ThunkAction,
  createAsyncThunk,
  createSelector,
  createSlice,
} from '@reduxjs/toolkit';
import { toByteArray } from 'base64-js';
import { notify } from 'reapop';

import { selectAsmCode, selectEntryPoint } from '@/lib/redux/compilerSlice';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import { callSimulationImpl } from '@/lib/serverCalls';
import type {
  Cache,
  CacheLineModel,
  CpuState,
  InputCodeArgument,
  InputCodeModel,
  InstructionFunctionModel,
  Label,
  Reference,
  RegisterDataContainer,
  RegisterModel,
  SimCodeModel,
} from '@/lib/types/cpuApi';
import { isValidReference, isValidRegisterValue } from '@/lib/utils';

/**
 * Redux state for CPU
 */
interface CpuSlice {
  /**
   * CPU state, loaded from the server
   */
  state: CpuState | null;
  /**
   * Code that is currently being simulated. Pulled from the compiler state.
   */
  code: string;
  /**
   * Reference to the currently highlighted line in the input code.
   * Used to highlight the corresponding objects in visualizations.
   */
  highlightedInputCode: Reference | null;
  /**
   * Reference to the currently highlighted line in the simulation code.
   * Used to highlight the corresponding objects in visualizations.
   */
  highlightedSimCode: Reference | null;
  /**
   * Reference to the currently highlighted register.
   */
  highlightedRegister: string | null;
}

/**
 * The initial state
 */
const initialState: CpuSlice = {
  state: null,
  code: '',
  highlightedInputCode: null,
  highlightedSimCode: null,
  highlightedRegister: null,
};

/**
 * The result of a simulation API call
 */
type SimulationParsedResult = {
  state: CpuState;
};

/**
 * Copy the code from code editor to the simulation
 */
export const pullCodeFromCompiler = (): ThunkAction<
  void,
  RootState,
  unknown,
  Action<string>
> => {
  return async (dispatch, getState) => {
    const state: RootState = getState();
    const code = selectAsmCode(state);
    dispatch(setSimulationCode(code));
  };
};

/**
 * Reload the simulation (reset it to the step 0)
 */
export const reloadSimulation = (): ThunkAction<
  void,
  RootState,
  unknown,
  Action<string>
> => {
  return async (dispatch) => {
    dispatch(pullCodeFromCompiler());
    dispatch(callSimulation(0));
  };
};

/**
 * Step the simulation forward by one tick
 */
export const simStepForward = (): ThunkAction<
  void,
  RootState,
  unknown,
  Action<string>
> => {
  return async (dispatch, getState) => {
    const state: RootState = getState();
    const currentTick = selectTick(state);
    dispatch(callSimulation(currentTick + 1));
  };
};

/**
 * Step the simulation backward by one tick
 */
export const simStepBackward = (): ThunkAction<
  void,
  RootState,
  unknown,
  Action<string>
> => {
  return async (dispatch, getState) => {
    const state: RootState = getState();
    const currentTick = selectTick(state);
    const cappedTick = Math.max(0, currentTick - 1);
    dispatch(callSimulation(cappedTick));
  };
};

/**
 * Call the simulation API
 * Call example: dispatch(callSimulation(5));
 *
 * @param tick The tick to simulate to
 */
export const callSimulation = createAsyncThunk<SimulationParsedResult, number>(
  'cpu/callSimulation',
  async (arg, { getState, dispatch }) => {
    // @ts-ignore
    const state: RootState = getState();
    const config = selectActiveConfig(state);
    const entryPoint = selectEntryPoint(state);
    const code = state.cpu.code;
    const tick = arg;
    try {
      const response = await callSimulationImpl(tick, {
        ...config,
        code,
        entryPoint,
      });
      return { state: response.state };
    } catch (err) {
      // Log error and show simple error message to the user
      console.error(err);
      console.warn(
        'Try clearing the local storage (application tab) and reloading the page',
      );
      let message = 'See the console for more details';
      if (err instanceof SyntaxError) {
        // Unexpected token < in JSON
        message = 'Invalid response from the server';
      } else if (err instanceof TypeError) {
        message = 'Server not reachable';
      } else if (err instanceof Error) {
        message = err.message;
      }
      dispatch(
        notify({
          title: 'API call failed',
          message: `Simulation failed: ${message}`,
          status: 'error',
        }),
      );
      throw err;
    }
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
      if (action.payload === null) {
        throw new Error('highlightSimCode: action.payload === null');
      }
      state.highlightedSimCode = action.payload;
      const simCodeModel =
        state.state?.managerRegistry.simCodeManager[action.payload];
      if (simCodeModel) {
        state.highlightedInputCode = simCodeModel.inputCodeModel;
      }
    },
    unhighlightSimCode: (state, action: PayloadAction<Reference | null>) => {
      // Do not unhighlight somebody else's highlight
      if (state.highlightedSimCode === action.payload) {
        state.highlightedSimCode = null;
        state.highlightedInputCode = null;
      }
    },
    highlightRegister: (state, action: PayloadAction<string | null>) => {
      state.highlightedRegister = action.payload;
    },
    unhighlightRegister: (state, action: PayloadAction<string | null>) => {
      // Do not unhighlight somebody else's highlight
      if (state.highlightedRegister === action.payload) {
        state.highlightedRegister = null;
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(callSimulation.fulfilled, (state, action) => {
        state.state = action.payload.state;
      })
      .addCase(callSimulation.rejected, (state, _action) => {
        state.state = null;
      })
      .addCase(callSimulation.pending, (_state, _action) => {
        // nothing
      });
  },
});

export const {
  setSimulationCode,
  highlightSimCode,
  unhighlightSimCode,
  highlightRegister,
  unhighlightRegister,
} = cpuSlice.actions;

//
// Selectors
//

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

export const selectAllSimCodeModels = (state: RootState) =>
  state.cpu.state?.managerRegistry.simCodeManager;
export const selectSimCodeModelById = (state: RootState, id: Reference) =>
  state.cpu.state?.managerRegistry.simCodeManager[id];

export const selectMemory = (state: RootState) =>
  state.cpu.state?.simulatedMemory;

/**
 * Parse the base64 string from the API into a Uint8Array.
 */
export const selectMemoryBytes = createSelector([selectMemory], (memory) => {
  if (!memory) {
    return null;
  }
  const arr = toByteArray(memory.memoryBase64 ?? '');
  return arr;
});

export const selectProgram = (state: RootState) =>
  state.cpu.state?.instructionMemoryBlock;

export const selectHighlightedSimCode = (state: RootState) =>
  state.cpu.highlightedSimCode;

export const selectHighlightedInputCode = (state: RootState) =>
  state.cpu.highlightedInputCode;

export const selectHighlightedRegister = (state: RootState) =>
  state.cpu.highlightedRegister;

export const selectLabels = (state: RootState) =>
  state.cpu.state?.instructionMemoryBlock?.labels;

/**
 * Returns program code with labels inserted before the instruction they point to.
 */
export const selectProgramWithLabels = createSelector(
  [selectProgram],
  (program): Array<Reference | string> | null => {
    if (!program) {
      return null;
    }

    // Collect labels that are not after the end of the program
    const labels: Array<Label & { labelName: string }> = [];
    for (const [labelName, label] of Object.entries(program.labels)) {
      // Do not insert labels that are well after the end of the program
      if (label.address >= (program.code.length + 1) * 4) {
        continue;
      }
      labels.push({ ...label, labelName });
    }

    // Sort labels by address, ascending
    labels.sort((a, b) => a.address - b.address);

    // Upsert labels into the code
    let offset = 0;
    const codeOrder: Array<Reference | string> = [];
    for (let i = 0; i < program.code.length; i++) {
      const address = i * 4;
      // Insert labels before the instruction they point to
      let lab = labels[offset];
      while (lab !== undefined && lab.address === address) {
        codeOrder.push(lab.labelName);
        offset++;
        lab = labels[offset];
      }
      const instruction = program.code[i];
      if (instruction === undefined) {
        throw new Error(`Instruction at ${address} not found`);
      }
      codeOrder.push(instruction);
    }

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
    for (const [alias, id] of Object.entries(map)) {
      const register = registers[id];
      if (!register) {
        console.warn(`Register ${id} not found`, alias, id);
        throw new Error(`Register ${id} not found`);
      }
      registerMap[alias] ??= register;
    }

    return registerMap;
  },
);

export type ParsedArgument = {
  /**
   * Register model, if the argument is a register. Useful for name aliases, not speculatives.
   */
  register: RegisterModel | null;
  /**
   * True if the current argument value is valid
   */
  valid: boolean;
  origArg: InputCodeArgument;
};

/**
 * Extract the value of an argument.
 * TODO: move to utils
 */
export function getValue(arg: ParsedArgument): RegisterDataContainer {
  if (arg.origArg.constantValue !== null) {
    return arg.origArg.constantValue;
  }

  // Must be a register
  if (!arg.register) {
    throw new Error(`Argument ${arg.origArg.name} has no value`);
  }

  return arg.register.value;
}

type DetailedSimCodeModel = {
  simCodeModel: SimCodeModel;
  inputCodeModel: InputCodeModel;
  functionModel: InstructionFunctionModel;
  args: Array<ParsedArgument>;
};

/**
 * Select simcodemodel, inputcodemodel and instructionfunctionmodel for a given simcode id.
 * Resolves references to registers.
 */
const selectDetailedSimCodeModels = createSelector(
  [
    selectAllInputCodeModels,
    selectAllInstructionFunctionModels,
    selectAllSimCodeModels,
    selectRegisterMap,
  ],
  (
    inputCodeModels,
    instructionFunctionModels,
    simCodeModels,
    registers,
  ): Record<Reference, DetailedSimCodeModel> | null => {
    if (
      !inputCodeModels ||
      !instructionFunctionModels ||
      !simCodeModels ||
      !registers
    ) {
      return null;
    }

    // Create a lookup table with entry for each simcode
    const lookup: Record<Reference, DetailedSimCodeModel> = {};
    for (const [id, simCodeModel] of Object.entries(simCodeModels)) {
      const reference = parseInt(id, 10);
      if (Number.isNaN(reference)) {
        throw new Error(`Invalid simcode id: ${id}`);
      }
      const inputCodeModel = inputCodeModels[simCodeModel.inputCodeModel];
      if (!inputCodeModel) {
        throw new Error(`Invalid simcode id: ${id}`);
      }
      const functionModel =
        instructionFunctionModels[inputCodeModel.instructionFunctionModel];
      if (!functionModel || !inputCodeModel || !simCodeModel) {
        throw new Error(`Invalid simcode id: ${id}`);
      }
      const detail: DetailedSimCodeModel = {
        simCodeModel,
        inputCodeModel,
        functionModel,
        args: [],
      };
      for (const renamedArg of simCodeModel.renamedArguments) {
        const arg: ParsedArgument = {
          register: null,
          valid: false,
          origArg: renamedArg,
        };
        const regName = renamedArg.registerValue;
        if (regName !== null) {
          const arch = registers[regName];
          if (arch === undefined) {
            console.warn(`Register ${regName} not found ()`);
            throw new Error(`Register ${regName} not found`);
          }
          arg.register = arch;
        }
        arg.valid = arg.register === null || isValidRegisterValue(arg.register);
        detail.args.push(arg);
      }
      lookup[reference] = detail;
    }
    return lookup;
  },
);

export const selectSimCodeModel = (state: RootState, id: Reference | null) => {
  if (!isValidReference(id)) {
    return null;
  }
  return selectDetailedSimCodeModels(state)?.[id];
};

/**
 * ID is a register name or alias.
 */
export const selectRegisterById = (state: RootState, regName: string) =>
  selectRegisterMap(state)?.[regName] ?? null;

/**
 * Get architectural register for a given speculative register.
 */
export const selectArchRegisterBySpeculative = (
  state: RootState,
  regName: string,
): RegisterModel | null => {
  const map = state.cpu.state?.renameMapTableBlock.registerMap;
  if (!map) {
    return null;
  }

  const register = map[regName];
  const archName = register?.architecturalRegister;
  if (!archName) {
    return null;
  }

  return selectRegisterById(state, archName);
};

// Stages

export const selectFetch = (state: RootState) =>
  state.cpu.state?.instructionFetchBlock;

export const selectDecode = (state: RootState) =>
  state.cpu.state?.decodeAndDispatchBlock;

export const selectROB = (state: RootState) =>
  state.cpu.state?.reorderBufferBlock;

// Branch prediction

export const selectBranchTargetBuffer = (state: RootState) =>
  state.cpu.state?.branchTargetBuffer;

export const selectGlobalHistoryRegister = (state: RootState) =>
  state.cpu.state?.globalHistoryRegister;

export const selectPatternHistoryTable = (state: RootState) =>
  state.cpu.state?.patternHistoryTable;

// Issue window blocks

export const selectAluIssueWindowBlock = (state: RootState) =>
  state.cpu.state?.aluIssueWindowBlock;

export const selectFpIssueWindowBlock = (state: RootState) =>
  state.cpu.state?.fpIssueWindowBlock;

export const selectBranchIssueWindowBlock = (state: RootState) =>
  state.cpu.state?.branchIssueWindowBlock;

export const selectLoadStoreIssueWindowBlock = (state: RootState) =>
  state.cpu.state?.loadStoreIssueWindowBlock;

// Function units

export const selectArithmeticFunctionUnitBlocks = (state: RootState) =>
  state.cpu.state?.arithmeticFunctionUnitBlocks;

export const selectFpFunctionUnitBlocks = (state: RootState) =>
  state.cpu.state?.fpFunctionUnitBlocks;

export const selectBranchFunctionUnitBlocks = (state: RootState) =>
  state.cpu.state?.branchFunctionUnitBlocks;

export const selectMemoryAccessUnitBlocks = (state: RootState) =>
  state.cpu.state?.memoryAccessUnits; // todo: inconsistent name

// Load store

export const selectStoreBuffer = (state: RootState) =>
  state.cpu.state?.storeBufferBlock;

export const selectLoadBuffer = (state: RootState) =>
  state.cpu.state?.loadBufferBlock;

// cache

const selectCacheInternal = (state: RootState) => state.cpu.state?.cache;

// Statistics

export const selectStatistics = (state: RootState) =>
  state.cpu.state?.statistics;

export interface DecodedCacheLine extends CacheLineModel {
  decodedLine: number[];
}

export interface DecodedCache extends Cache {
  cache: DecodedCacheLine[][];
}

export const selectCache = createSelector(
  [selectCacheInternal],
  (cache): DecodedCache | null => {
    if (!cache) {
      return null;
    }

    // deep copy the cache
    const copy: DecodedCache = JSON.parse(JSON.stringify(cache));

    // decode the base64 string in each cache line
    for (const isl of copy.cache) {
      for (const line of isl) {
        const ba = toByteArray(line.line ?? '');
        line.decodedLine = Array.from(ba);
      }
    }
    return copy;
  },
);

export default cpuSlice.reducer;
