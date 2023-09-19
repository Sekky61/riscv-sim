/* eslint-disable @typescript-eslint/ban-ts-comment */
import {
  createAsyncThunk,
  createSelector,
  createSlice,
  PayloadAction,
} from '@reduxjs/toolkit';
import { notify } from 'reapop';

import type { RootState } from '@/lib/redux/store';

import { APIResponse, callCompilerImpl } from '../callCompiler';

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
  c_code: string;
  asm_code: string;
  // Mapping from c_line to asm_line(s) and back
  c_lines: number[];
  asm_to_c: number[];
  compileStatus: 'idle' | 'loading' | 'success' | 'failed';
  // Editor options
  editorMode: 'c' | 'asm';
  // True if the c_code or asm code has been changed since the last call to the compiler
  dirty: boolean;
  asm_manually_edited: boolean;
  compilerError?: string;
}

// Define the initial state using that type
const initialState: CompilerState = {
  c_code:
    'int add(int a, int b) {\n  int d = 2*a  + 2;\n  int x = d + b;\n  for(int i = 0; i < a; i++) {\n    x += b;\n  }\n  return x;\n} ',
  asm_code:
    'add:\n\taddi sp,sp,-48\n\tsw s0,44(sp)\n\taddi s0,sp,48\n\tsw a0,-36(s0)\n\tsw a1,-40(s0)\n\tlw a5,-36(s0)\n\taddi a5,a5,1\n\tslli a5,a5,1\n\tsw a5,-28(s0)\n\tlw a4,-28(s0)\n\tlw a5,-40(s0)\n\tadd a5,a4,a5\n\tsw a5,-20(s0)\n.LBB2:\n\tsw zero,-24(s0)\n\tj .L2\n.L3:\n\tlw a4,-20(s0)\n\tlw a5,-40(s0)\n\tadd a5,a4,a5\n\tsw a5,-20(s0)\n\tlw a5,-24(s0)\n\taddi a5,a5,1\n\tsw a5,-24(s0)\n.L2:\n\tlw a4,-24(s0)\n\tlw a5,-36(s0)\n\tblt a4,a5,.L3\n.LBE2:\n\tlw a5,-20(s0)\n\tmv a0,a5\n\tlw s0,44(sp)\n\taddi sp,sp,48\n\tjr ra',
  c_lines: [0, 1, 2, 3, 4, 5, 0, 7, 8],
  asm_to_c: [
    0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 4, 4,
    4, 4, 4, 4, 4, 4, 7, 8, 8, 8, 8,
  ],
  dirty: false,
  compileStatus: 'idle',
  optimize: false,
  editorMode: 'c',
  asm_manually_edited: false,
  compilerError: undefined,
};

// Call example: dispatch(callCompiler());
export const callCompiler = createAsyncThunk<APIResponse>(
  'compiler/callCompiler',
  async (arg, { getState, dispatch }) => {
    // @ts-ignore
    const code: string = getState().compiler.c_code;
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
          dispatch(
            notify({
              message: `Compilation failed: ${res.compilerError}`,
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
      state.c_code = action.payload;
      state.dirty = true;
    },
    asmFieldTyping: (state, action: PayloadAction<string>) => {
      state.asm_code = action.payload;
      state.dirty = true;
      state.asm_manually_edited = true;
    },
    setCCode: (state, action: PayloadAction<string>) => {
      state.c_code = action.payload;
    },
    setAsmCode: (state, action: PayloadAction<string>) => {
      state.asm_code = action.payload;
    },
    setOptimize: (state, action: PayloadAction<boolean>) => {
      state.optimize = action.payload;
    },
    enterEditorMode: (state, action: PayloadAction<'c' | 'asm'>) => {
      state.editorMode = action.payload;
    },
    openExample: (state, action: PayloadAction<Example>) => {
      state.c_code = action.payload.type === 'c' ? action.payload.code : '';
      state.asm_code = action.payload.type === 'asm' ? action.payload.code : '';
      state.c_lines = [];
      state.asm_to_c = [];
      state.editorMode = action.payload.type;
      state.dirty = false;
    },
    openFile: (
      state,
      action: PayloadAction<{ code: string; type: 'c' | 'asm' }>,
    ) => {
      if (action.payload.type === 'c') {
        state.c_code = action.payload.code;
        state.asm_code = '';
      } else {
        state.c_code = '';
        state.asm_code = action.payload.code;
      }
      state.c_lines = [];
      state.asm_to_c = [];
      state.editorMode = action.payload.type;
      state.dirty = false;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(callCompiler.fulfilled, (state, action) => {
        if (!action.payload.success) {
          state.compileStatus = 'failed';
          state.compilerError = action.payload.compilerError;
          return;
        }
        state.asm_code = action.payload.program.join('\n');
        state.compileStatus = 'success';
        state.compilerError = undefined;
        state.dirty = false;
        state.asm_manually_edited = false;
        state.c_lines = action.payload.cLines;
        state.asm_to_c = action.payload.asmToC;
      })
      .addCase(callCompiler.rejected, (state, _action) => {
        state.compileStatus = 'failed';
      })
      .addCase(callCompiler.pending, (state, _action) => {
        state.compileStatus = 'loading';
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

export const selectCCode = (state: RootState) => state.compiler.c_code;
export const selectCCodeMappings = (state: RootState) => state.compiler.c_lines;
export const selectAsmMappings = (state: RootState) => state.compiler.asm_to_c;
export const selectCompileStatus = (state: RootState) =>
  state.compiler.compileStatus;
export const selectCompilerError = (state: RootState) =>
  state.compiler.compilerError;
export const selectAsmCode = (state: RootState) => state.compiler.asm_code;
export const selectOptimize = (state: RootState) => state.compiler.optimize;
export const selectEditorMode = (state: RootState) => state.compiler.editorMode;
export const selectDirty = (state: RootState) => state.compiler.dirty;
export const selectAsmManuallyEdited = (state: RootState) =>
  state.compiler.asm_manually_edited;

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

export default compilerSlice.reducer;
