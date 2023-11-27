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

/* eslint-disable @typescript-eslint/ban-ts-comment */
import {
  PayloadAction,
  createAsyncThunk,
  createSelector,
  createSlice,
} from '@reduxjs/toolkit';
import { notify } from 'reapop';

import { transformErrors } from '@/lib/editor/transformErrors';
import type { RootState } from '@/lib/redux/store';
import {
  callCompilerImpl,
  callParseAsmImpl,
} from '@/lib/serverCalls/callCompiler';
import {
  CompileResponse,
  ComplexErrorItem,
  ParseAsmResponse,
  SimpleParseError,
} from '@/lib/types/simulatorApi';

export type OptimizeOption =
  | 'O2'
  | 'rename'
  | 'unroll'
  | 'peel'
  | 'inline'
  | 'omit-frame-pointer';

export interface CompilerOptions {
  optimizeFlags: OptimizeOption[];
}

export interface Example {
  name: string;
  type: 'c' | 'asm';
  code: string;
}

// Define a type for the slice state
interface CompilerState extends CompilerOptions {
  cCode: string;
  asmCode: string;
  // Mapping from c_line to asm_line(s) and back
  asmToC: number[];
  compileStatus: 'idle' | 'loading' | 'success' | 'failed';
  // Editor options
  editorMode: 'c' | 'asm';
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
  asmCode:
    'add:\n    addi sp,sp,-48\n    sw s0,44(sp)\n    addi s0,sp,48\n    sw a0,-36(s0)\n    sw a1,-40(s0)\n    lw a5,-36(s0)\n    addi a5,a5,1\n    slli a5,a5,1\n    sw a5,-28(s0)\n    lw a4,-28(s0)\n    lw a5,-40(s0)\n    add a5,a4,a5\n    sw a5,-20(s0)\n    sw zero,-24(s0)\n    j .L2\n.L3:\n    lw a4,-20(s0)\n    lw a5,-40(s0)\n    add a5,a4,a5\n    sw a5,-20(s0)\n    lw a5,-24(s0)\n    addi a5,a5,1\n    sw a5,-24(s0)\n.L2:\n    lw a4,-24(s0)\n    lw a5,-36(s0)\n    blt a4,a5,.L3\n    lw a5,-20(s0)\n    mv a0,a5\n    lw s0,44(sp)\n    addi sp,sp,48\n    jr ra',
  asmToC: [
    0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4,
    4, 4, 4, 7, 8, 8, 8, 8,
  ],
  cDirty: false,
  asmDirty: false,
  compileStatus: 'idle',
  optimizeFlags: [],
  editorMode: 'c',
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
    const code: string = getState().compiler.cCode;
    // @ts-ignore
    const options: CompilerState = getState().compiler;
    const response = await callCompilerImpl(code, options)
      .then((res) => {
        if (res.success) {
          dispatch(
            notify({
              message: 'Compilation successful',
              status: 'success',
            }),
          );
        } else {
          // Show the short error message
          dispatch(
            notify({
              message: `Compilation failed: ${res.error}`,
              status: 'error',
              dismissible: true,
              // Do not automatically dismiss
              dismissAfter: 0,
            }),
          );
        }
        return res;
      })
      .catch((err) => {
        dispatch(
          notify({
            message: 'Compilation failed: server error',
            status: 'error',
            dismissible: true,
            dismissAfter: 2000,
          }),
        );
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
  async (arg, { getState, dispatch }) => {
    // @ts-ignore
    const code: string = getState().compiler.asmCode;
    const response = await callParseAsmImpl(code)
      .then((res) => {
        if (res.success) {
          dispatch(
            notify({
              message: 'Check successful',
              status: 'success',
            }),
          );
        } else {
          // Show the short error message
          dispatch(
            notify({
              message: 'Check failed',
              status: 'error',
              dismissible: true,
              // Do not automatically dismiss
              dismissAfter: 0,
            }),
          );
        }
        return res;
      })
      .catch((err) => {
        dispatch(
          notify({
            message: 'Compilation failed: server error',
            status: 'error',
            dismissible: true,
            dismissAfter: 2000,
          }),
        );
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
export const openExampleAndCompile = createAsyncThunk<void, Example>(
  'compiler/openExampleAndCompile',
  async (example, { dispatch }) => {
    dispatch(openExample(example));
    if (example.type === 'c') {
      dispatch(callCompiler());
    }
  },
);

/**
 * Save the active code to a file. A dialog is shown to the user, they can choose the file name.
 *
 * Example: dispatch(saveToFile());
 */
export const saveToFile = createAsyncThunk<void>(
  'compiler/saveToFile',
  async (arg, { getState }) => {
    // @ts-ignore
    const state: RootState = getState();
    const code = selectActiveCode(state);
    const suggestedFileName =
      selectEditorMode(state) === 'c' ? 'code.c' : 'code.asm';

    const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = suggestedFileName;
    link.click();
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
    },
    toggleOptimizeFlag: (state, action: PayloadAction<OptimizeOption>) => {
      // Special case for -O2
      if (action.payload === 'O2' && !state.optimizeFlags.includes('O2')) {
        state.optimizeFlags = ['O2', 'omit-frame-pointer', 'inline', 'rename'];
        return;
      }
      const idx = state.optimizeFlags.indexOf(action.payload);
      if (idx === -1) {
        state.optimizeFlags.push(action.payload);
      } else {
        state.optimizeFlags.splice(idx, 1);
      }
    },
    enterEditorMode: (state, action: PayloadAction<'c' | 'asm'>) => {
      state.editorMode = action.payload;
    },
    openExample: (state, action: PayloadAction<Example>) => {
      state.cCode = action.payload.type === 'c' ? action.payload.code : '';
      state.asmCode = action.payload.type === 'asm' ? action.payload.code : '';
      state.asmToC = [];
      state.editorMode = action.payload.type;
      state.cDirty = false;
      state.asmDirty = false;
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
      state.editorMode = action.payload.type;
      state.cDirty = false;
      state.asmDirty = false;
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
          state.cErrors = action.payload.compilerError;
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
      })
      .addCase(callCompiler.pending, (state, _action) => {
        state.compileStatus = 'loading';
      })
      // /parseAsm
      .addCase(callParseAsm.fulfilled, (state, action) => {
        state.asmDirty = false;
        if (!action.payload.success) {
          state.asmErrors = action.payload.errors;
          return;
        }
        state.asmErrors = [];
      })
      .addCase(callParseAsm.rejected, (state, _action) => {
        state.asmErrors = [];
      });
  },
});

export const {
  setCCode,
  setAsmCode,
  toggleOptimizeFlag,
  enterEditorMode,
  cFieldTyping,
  asmFieldTyping,
  openExample,
  openFile,
} = compilerSlice.actions;

export const selectCCode = (state: RootState) => state.compiler.cCode;
export const selectAsmMappings = (state: RootState) => state.compiler.asmToC;
export const selectCompileStatus = (state: RootState) =>
  state.compiler.compileStatus;
export const selectCErrors = (state: RootState) => state.compiler.cErrors;
export const selectAsmErrors = (state: RootState) => state.compiler.asmErrors;
export const selectAsmCode = (state: RootState) => state.compiler.asmCode;
export const selectOptimize = (state: RootState) =>
  state.compiler.optimizeFlags;
export const selectEditorMode = (state: RootState) => state.compiler.editorMode;
export const selectCDirty = (state: RootState) => state.compiler.cDirty;
export const selectAsmDirty = (state: RootState) => state.compiler.asmDirty;
export const selectAsmManuallyEdited = (state: RootState) =>
  state.compiler.asmManuallyEdited;

export const selectCCodeMappings = createSelector(
  [selectAsmMappings, selectCCode],
  (asmToC, cCode) => {
    const lines = cCode.split('\n');
    const codeLength = lines.length;
    const arr = new Array(codeLength + 1);
    // Go through the mapping, note that the mapping is 1-indexed
    for (let i = 0; i < asmToC.length; i++) {
      const cLine = asmToC[i];
      if (cLine === undefined || cLine === 0) {
        continue;
      }
      // cLine is 1-indexed
      arr[cLine] = cLine;
    }
    return arr;
  },
);

export const selectActiveCode = (state: RootState) => {
  if (state.compiler.editorMode === 'c') {
    return state.compiler.cCode;
  }
  return state.compiler.asmCode;
};

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
