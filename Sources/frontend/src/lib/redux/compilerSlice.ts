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
  createAsyncThunk,
  createSelector,
  createSlice,
  PayloadAction,
} from '@reduxjs/toolkit';
import { notify } from 'reapop';

import { transformErrors } from '@/lib/editor/transformErrors';
import type { RootState } from '@/lib/redux/store';
import {
  callParseAsmImpl,
  ParseAsmAPIResponse,
} from '@/lib/serverCalls/callParseAsm';

import {
  callCompilerImpl,
  CompilerAPIResponse,
  ErrorItem,
} from '../serverCalls/callCompiler';

export interface CompilerOptions {
  optimize: boolean;
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
  cLines: number[];
  asmToC: number[];
  compileStatus: 'idle' | 'loading' | 'success' | 'failed';
  // Editor options
  editorMode: 'c' | 'asm';
  // True if the c_code or asm code has been changed since the last call to the compiler
  cDirty: boolean;
  asmDirty: boolean;
  asmManuallyEdited: boolean;
  cErrors: Array<ErrorItem>;
  asmErrors: Array<ErrorItem>;
}

// Define the initial state using that type
const initialState: CompilerState = {
  cCode:
    'int add(int a, int b) {\n  int d = 2*a + 2;\n  int x = d + b;\n  for(int i = 0; i < a; i++) {\n    x += b;\n  }\n  return x;\n} ',
  asmCode:
    'add:\n\taddi sp,sp,-48\n\tsw s0,44(sp)\n\taddi s0,sp,48\n\tsw a0,-36(s0)\n\tsw a1,-40(s0)\n\tlw a5,-36(s0)\n\taddi a5,a5,1\n\tslli a5,a5,1\n\tsw a5,-28(s0)\n\tlw a4,-28(s0)\n\tlw a5,-40(s0)\n\tadd a5,a4,a5\n\tsw a5,-20(s0)\n.LBB2:\n\tsw zero,-24(s0)\n\tj .L2\n.L3:\n\tlw a4,-20(s0)\n\tlw a5,-40(s0)\n\tadd a5,a4,a5\n\tsw a5,-20(s0)\n\tlw a5,-24(s0)\n\taddi a5,a5,1\n\tsw a5,-24(s0)\n.L2:\n\tlw a4,-24(s0)\n\tlw a5,-36(s0)\n\tblt a4,a5,.L3\n.LBE2:\n\tlw a5,-20(s0)\n\tmv a0,a5\n\tlw s0,44(sp)\n\taddi sp,sp,48\n\tjr ra',
  cLines: [0, 1, 2, 3, 4, 5, 0, 7, 8],
  asmToC: [
    0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 4, 4,
    4, 4, 4, 4, 4, 4, 7, 8, 8, 8, 8,
  ],
  cDirty: false,
  asmDirty: false,
  compileStatus: 'idle',
  optimize: false,
  editorMode: 'c',
  asmManuallyEdited: false,
  cErrors: [],
  asmErrors: [],
};

// Call example: dispatch(callCompiler());
export const callCompiler = createAsyncThunk<CompilerAPIResponse>(
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
            message: `Compilation failed: server error`,
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

// Call example: dispatch(callParseAsm());
export const callParseAsm = createAsyncThunk<ParseAsmAPIResponse>(
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
              message: `Check failed`,
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
            message: `Compilation failed: server error`,
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
        state.cLines = [];
        state.asmToC = [];
        state.cErrors = [];
      }
      state.cDirty = true;
    },
    asmFieldTyping: (state, action: PayloadAction<string>) => {
      state.asmCode = action.payload;
      if (state.asmDirty === false) {
        // First typing, remove the mappings
        state.cLines = [];
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
    setOptimize: (state, action: PayloadAction<boolean>) => {
      state.optimize = action.payload;
    },
    enterEditorMode: (state, action: PayloadAction<'c' | 'asm'>) => {
      state.editorMode = action.payload;
    },
    openExample: (state, action: PayloadAction<Example>) => {
      state.cCode = action.payload.type === 'c' ? action.payload.code : '';
      state.asmCode = action.payload.type === 'asm' ? action.payload.code : '';
      state.cLines = [];
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
      state.cLines = [];
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
          state.cErrors = action.payload.compilerError['@items'];
          // Delete mapping
          state.cLines = [];
          state.asmToC = [];
          return;
        }
        state.asmCode = action.payload.program.join('\n');
        state.compileStatus = 'success';
        state.cErrors = [];
        state.asmManuallyEdited = false;
        state.cLines = action.payload.cLines;
        state.asmToC = action.payload.asmToC;
      })
      .addCase(callCompiler.rejected, (state, _action) => {
        state.compileStatus = 'failed';
      })
      .addCase(callCompiler.pending, (state, _action) => {
        state.compileStatus = 'loading';
      })
      // /parseAsm
      .addCase(callParseAsm.fulfilled, (state, action) => {
        state.asmDirty = false;
        if (!action.payload.success) {
          state.asmErrors = action.payload.errors['@items'];
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
  setOptimize,
  enterEditorMode,
  cFieldTyping,
  asmFieldTyping,
  openExample,
  openFile,
} = compilerSlice.actions;

export const selectCCode = (state: RootState) => state.compiler.cCode;
export const selectCCodeMappings = (state: RootState) => state.compiler.cLines;
export const selectAsmMappings = (state: RootState) => state.compiler.asmToC;
export const selectCompileStatus = (state: RootState) =>
  state.compiler.compileStatus;
export const selectCErrors = (state: RootState) => state.compiler.cErrors;
export const selectAsmErrors = (state: RootState) => state.compiler.asmErrors;
export const selectAsmCode = (state: RootState) => state.compiler.asmCode;
export const selectOptimize = (state: RootState) => state.compiler.optimize;
export const selectEditorMode = (state: RootState) => state.compiler.editorMode;
export const selectCDirty = (state: RootState) => state.compiler.cDirty;
export const selectAsmDirty = (state: RootState) => state.compiler.asmDirty;
export const selectAsmManuallyEdited = (state: RootState) =>
  state.compiler.asmManuallyEdited;

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
    line = line.trim();
    const [mnemonic, ...args] = line.split(' ');
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
    return transformErrors(errors, asmCode);
  },
);

export default compilerSlice.reducer;
