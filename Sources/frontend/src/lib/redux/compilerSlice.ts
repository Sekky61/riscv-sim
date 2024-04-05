/**
 * @file    compilerSlice.ts
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

import {
  type PayloadAction,
  createAsyncThunk,
  createSelector,
  createSlice,
} from '@reduxjs/toolkit';

import { defaultAsmCode } from '@/constant/defaults';
import { transformErrors } from '@/lib/editor/transformErrors';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import type { RootState } from '@/lib/redux/store';
import {
  ServerErrorException,
  callCompilerImpl,
  callParseAsmImpl,
} from '@/lib/serverCalls';
import type { CodeExample } from '@/lib/types/codeExamples';
import type {
  CompileResponse,
  ComplexErrorItem,
  ParseAsmResponse,
  SimpleParseError,
} from '@/lib/types/simulatorApi';
import { saveAsFile } from '@/lib/utils';
import { toast } from 'sonner';

export type OptimizeOption = 'O0' | 'O2' | 'O3' | 'Os';

export interface CompilerOptions {
  optimizeFlag: OptimizeOption;
}

// Define a type for the slice state
interface CompilerState extends CompilerOptions {
  cCode: string;
  asmCode: string;
  // Mapping from c_line to asm_line(s) and back
  asmToC: number[];
  compileStatus: 'idle' | 'loading' | 'success' | 'failed';
  entryPoint: string | number;
  // True if the c_code or asm code has been changed since the last call to the compiler
  cDirty: boolean;
  asmDirty: boolean;
  asmManuallyEdited: boolean;
  cErrors: Array<ComplexErrorItem>;
  asmErrors: Array<SimpleParseError>;
}

// Define the initial state using that type
const initialState: CompilerState = {
  cCode:
    'int add(int a, int b) {\n  int d = 2*a + 2;\n  int x = d + b;\n  for(int i = 0; i < a; i++) {\n    x += b;\n  }\n  return x;\n} ',
  asmCode: defaultAsmCode,
  asmToC: [
    0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4,
    4, 4, 4, 7, 8, 8, 8, 8,
  ],
  cDirty: false,
  asmDirty: false,
  compileStatus: 'idle',
  optimizeFlag: 'O2',
  entryPoint: 0,
  asmManuallyEdited: false,
  cErrors: [],
  asmErrors: [],
};

/**
 * Call the compiler on the C code.
 *
 * Example: dispatch(callCompiler());
 */
export const callCompiler = createAsyncThunk<CompileResponse>(
  'compiler/callCompiler',
  async (arg, { getState, dispatch }) => {
    // @ts-ignore
    const state: RootState = getState();
    const activeIsa = selectActiveConfig(state);
    const request = {
      code: state.compiler.cCode,
      optimizeFlags: [state.compiler.optimizeFlag], // todo api allows multiple flags
      memoryLocations: activeIsa.memoryLocations,
    };
    const response = await callCompilerImpl(request)
      .then((res) => {
        if (res.success) {
          toast.success('Compilation successful');
        } else {
          // Show the short error message
          let message = res.error;
          if (res.asmErrors && res.asmErrors.length > 0) {
            let msg = res.asmErrors[0]?.message;
            if (!msg) {
              msg = 'Unknown error';
            }
            message = msg;
          }
          toast.error(`Compilation failed: ${message}`, {
            dismissible: true,
            duration: 0,
          });
        }
        return res;
      })
      .catch((err) => {
        if (err instanceof ServerErrorException) {
          toast.error(`Server error: ${err.message}`);
        } else {
          toast.error('Compilation failed: server error');
        }
        // Rethrow
        throw err;
      });
    return response;
  },
);

/**
 * Call the compiler on the asm code. Detects errors in the asm code.
 *
 * Example: dispatch(callParseAsm());
 */
export const callParseAsm = createAsyncThunk<ParseAsmResponse>(
  'compiler/callParseAsm',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const request = {
      code: state.compiler.asmCode,
      memoryLocations: selectActiveConfig(state).memoryLocations,
    };
    const response = await callParseAsmImpl(request)
      .then((res) => {
        if (res.success) {
          toast.success('The assembly code is valid');
        } else {
          // Show the short error message
          toast.error('The assembly code is invalid');
        }
        return res;
      })
      .catch((err) => {
        if (err instanceof ServerErrorException) {
          toast.error(`Server error: ${err.message}`);
        } else {
          toast.error('Compilation failed: server error');
        }
        // Rethrow
        throw err;
      });
    return response;
  },
);

/**
 * Open an example and compile it.
 *
 * Example: dispatch(openExampleAndCompile("example1"));
 */
export const openExampleAndCompile = createAsyncThunk<void, CodeExample>(
  'compiler/openExampleAndCompile',
  async (example, { dispatch }) => {
    dispatch(openExample(example));
    if (example.type === 'c') {
      dispatch(callCompiler());
    }
  },
);

/**
 * Save the code to a file. A dialog is shown to the user, they can choose the file name.
 *
 * Example: dispatch(saveToFile('c'));
 */
export const saveCodeToFile = createAsyncThunk<void, 'c' | 'asm'>(
  'compiler/saveToFile',
  async (mode, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const suggestedFileName = mode === 'c' ? 'code.c' : 'code.asm';
    const code = mode === 'c' ? state.compiler.cCode : state.compiler.asmCode;
    saveAsFile(code, suggestedFileName);
  },
);

export const compilerSlice = createSlice({
  name: 'compiler',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    // Use the PayloadAction type to declare the contents of `action.payload`
    cFieldTyping: (state, action: PayloadAction<string>) => {
      state.cCode = action.payload;
      if (state.cDirty === false) {
        // First typing, remove the mappings
        state.asmToC = [];
        state.cErrors = [];
      }
      state.cDirty = true;
    },
    asmFieldTyping: (state, action: PayloadAction<string>) => {
      state.asmCode = action.payload;
      if (state.asmDirty === false) {
        // First typing, remove the mappings
        state.asmToC = [];
        state.asmErrors = [];
      }
      state.asmDirty = true;
      state.asmManuallyEdited = true;
    },
    setCCode: (state, action: PayloadAction<string>) => {
      state.cCode = action.payload;
    },
    setAsmCode: (state, action: PayloadAction<string>) => {
      state.asmCode = action.payload;
      state.entryPoint = 0;
    },
    setEntryPoint: (state, action: PayloadAction<number | string>) => {
      state.entryPoint = action.payload;
    },
    setOptimizeFlag: (state, action: PayloadAction<OptimizeOption>) => {
      state.optimizeFlag = action.payload;
    },
    openExample: (state, action: PayloadAction<CodeExample>) => {
      state.cCode = action.payload.type === 'c' ? action.payload.code : '';
      state.asmCode = action.payload.type === 'asm' ? action.payload.code : '';
      state.asmToC = [];
      state.cDirty = false;
      state.asmDirty = false;
      state.asmErrors = [];
      state.cErrors = [];
      state.asmManuallyEdited = false;
      state.entryPoint = action.payload.entryPoint ?? 0;
    },
    openFile: (
      state,
      action: PayloadAction<{ code: string; type: 'c' | 'asm' }>,
    ) => {
      if (action.payload.type === 'c') {
        state.cCode = action.payload.code;
        state.asmCode = '';
      } else {
        state.cCode = '';
        state.asmCode = action.payload.code;
      }
      state.asmToC = [];
      state.cDirty = false;
      state.asmDirty = false;
      state.entryPoint = 0;
    },
  },
  extraReducers: (builder) => {
    builder
      // /compile
      .addCase(callCompiler.fulfilled, (state, action) => {
        state.cDirty = false;
        state.asmDirty = false;
        if (!action.payload.success) {
          state.compileStatus = 'failed';
          state.cErrors = action.payload.compilerError ?? [];
          state.asmErrors = action.payload.asmErrors ?? [];
          // Delete mapping
          state.asmToC = [];
          state.asmCode = '';
          return;
        }
        state.asmCode = action.payload.program;
        state.compileStatus = 'success';
        state.cErrors = [];
        state.asmManuallyEdited = false;
        state.asmToC = action.payload.asmToC;
      })
      .addCase(callCompiler.rejected, (state, _action) => {
        state.compileStatus = 'failed';
        state.asmCode = '';
        state.asmDirty = false;
        state.asmManuallyEdited = false;
        state.asmToC = [];
        state.entryPoint = 0;
      })
      .addCase(callCompiler.pending, (state, _action) => {
        state.compileStatus = 'loading';
      })
      // /parseAsm
      .addCase(callParseAsm.fulfilled, (state, action) => {
        state.asmDirty = false;
        if (!action.payload.success) {
          state.asmErrors = action.payload.errors || [];
          return;
        }
        state.asmErrors = [];
      })
      .addCase(callParseAsm.rejected, (state, _action) => {
        state.asmErrors = [];
      });
  },
});
export type CompilerReducer = ReturnType<typeof compilerSlice.reducer>;

export const {
  setCCode,
  setAsmCode,
  setOptimizeFlag,
  cFieldTyping,
  asmFieldTyping,
  openExample,
  openFile,
  setEntryPoint,
} = compilerSlice.actions;

export const selectCCode = (state: RootState) => state.compiler.cCode;
export const selectAsmMappings = (state: RootState) => state.compiler.asmToC;
export const selectCompileStatus = (state: RootState) =>
  state.compiler.compileStatus;
export const selectCErrors = (state: RootState) => state.compiler.cErrors;
export const selectAsmErrors = (state: RootState) => state.compiler.asmErrors;
export const selectAsmCode = (state: RootState) => state.compiler.asmCode;
export const selectOptimize = (state: RootState) => state.compiler.optimizeFlag;
export const selectCDirty = (state: RootState) => state.compiler.cDirty;
export const selectAsmDirty = (state: RootState) => state.compiler.asmDirty;
export const selectAsmManuallyEdited = (state: RootState) =>
  state.compiler.asmManuallyEdited;
export const selectEntryPoint = (state: RootState) => state.compiler.entryPoint;

export const selectCCodeMappings = createSelector(
  [selectAsmMappings, selectCCode],
  (asmToC, cCode) => {
    const lines = cCode.split('\n');
    const codeLength = lines.length;
    const arr = new Array(codeLength + 1);
    // Go through the mapping, note that the mapping is 1-indexed
    for (const cLine of asmToC) {
      if (cLine === undefined || cLine === 0) {
        continue;
      }
      // cLine is 1-indexed
      arr[cLine] = cLine;
    }
    return arr;
  },
);

export interface Instruction {
  // Id of instruction in program. ~line number
  id: number;
  mnemonic: string;
  args: string[];
}

// Split the asm code into lines (instructions)
// Parse each instruction into its components
export const parsedInstructions = createSelector([selectAsmCode], (asm) => {
  const lines = asm.split('\n');
  const parsed = lines.map((line, i) => {
    const [mnemonic, ...args] = line.trim().split(' ');
    return { mnemonic, args, id: i } as Instruction;
  });
  return parsed;
});

// True if any of the fields is dirty
export const selectDirty = createSelector(
  [selectCDirty, selectAsmDirty],
  (c, asm) => {
    return c || asm;
  },
);

// Provides errors in the form expected by code mirror
export const selectCCodeMirrorErrors = createSelector(
  [selectCErrors, selectCCode, selectCDirty],
  (errors, cCode, dirty) => {
    // Errors are not displayed if the code is dirty, so skip the expensive computation
    if (dirty) {
      return [];
    }
    return transformErrors(errors, cCode);
  },
);

// Provides errors in the form expected by code mirror
export const selectAsmCodeMirrorErrors = createSelector(
  [selectAsmErrors, selectAsmCode, selectAsmDirty],
  (errors, asmCode, dirty) => {
    // Errors are not displayed if the code is dirty, so skip the expensive computation
    if (dirty) {
      return [];
    }
    // Transform simple to complex errors
    const simpleErrors: Array<ComplexErrorItem> = errors.map((err) => {
      const c: ComplexErrorItem = {
        kind: err.kind,
        message: err.message,
        locations: [
          {
            caret: {
              line: err.line,
              'display-column': err.columnStart,
            },
          },
        ],
      };

      if (err.columnEnd && c.locations.length > 0) {
        const loc = c.locations[0];
        if (!loc) {
          throw new Error('Unexpected error');
        }
        loc.finish = {
          line: err.line,
          'display-column': err.columnEnd,
        };
      }

      return c;
    });

    return transformErrors(simpleErrors, asmCode);
  },
);

export default compilerSlice.reducer;
