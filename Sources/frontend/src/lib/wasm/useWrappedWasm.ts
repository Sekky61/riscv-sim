/**
 * @file    useWrappedWasm.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   React hook for mapping and unwrapping api calls from wasm
 *
 * @date    23 March 2025, 19:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2025  Michal Majer
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

import { isError, type ApiResult } from "./ApiResult";
import { useWasm } from "./useWasm";

/** Every api should satisfy this */
// biome-ignore lint/suspicious/noExplicitAny: lib code
export type BaseWasmApi = Record<string, (...args: any[]) => any>;

/**
 * The actual API of WASM (glue that we don't want the end user to see)
 */
export type GlueApi<Api> = {
  [F in keyof Api]: WasmWrap<Api[F]>;
};

/**
 * The API consumed by frontend code
 */
export type WasmApi<Api> = {
  [F in keyof Api]: ResponseWrap<Api[F]>;
};

type ResponseWrap<F> = F extends (...args: infer A) => infer Res
  ? (...args: A) => ApiResult<Res>
  : never;

type Pointer = number;

// biome-ignore lint/suspicious/noExplicitAny: lib code
type WasmWrap<F> = F extends (...args: infer A) => any
  ? (...args: A) => Pointer
  : never;

function readSliceAt(memory: WebAssembly.Memory, ptrOffset: number) {
  const dataView = new DataView(memory.buffer);
  // Read the pointer (first 4 bytes)
  const ptr = dataView.getUint32(ptrOffset, true);
  // Read the length (next 4 bytes)
  const len = dataView.getUint32(ptrOffset + 4, true);
  console.log("read slice at", ptrOffset, "ptr", ptr, "len", len);
  return { ptr, len };
}

/**
 * Function to read a string from WebAssembly memory.
 * This is how data serialization is designed.
 * Assumes little-endian
 */
function readWasmString(memory: WebAssembly.Memory, ptrOffset: number) {
  const { ptr, len } = readSliceAt(memory, ptrOffset);
  const bytes = new Uint8Array(memory.buffer, ptr, len);
  return new TextDecoder("utf-8").decode(bytes);
}

/** Parse the serialized response */
function readWasmResponse(memory: WebAssembly.Memory, ptrOffset: number) {
  try {
    const string = readWasmString(memory, ptrOffset);
    const obj = JSON.parse(string);
    return obj as ApiResult<unknown>;
  } catch (e) {
    console.error("Could not parse WASM response", e);
    return null;
  }
}

function wasmFunctionFactory<F extends (arg?: unknown) => Pointer>(
  fn: F,
  options: {
    memory: WebAssembly.Memory;
    argWrapper?: (arg: unknown) => Pointer | null;
  },
) {
  return (arg: unknown): ApiResult<unknown> => {
    const argWrapper = options?.argWrapper ?? ((x) => x);
    const wrappedArg = arg ? argWrapper(arg) : null;
    const ptr = fn(wrappedArg);
    const response = readWasmResponse(options.memory, ptr);
    if (!response) {
      return {
        type: "error",
        message:
          "Could not parse WASM response on the client side. This is a bug.",
      };
    }
    return response;
  };
}

export function useWrappedWasm<Api extends BaseWasmApi>(
  allocationFnName: keyof Api,
) {
  const wasm = useWasm<GlueApi<Api>>("/wasm/bin/riscvsim.wasm", {});

  const serializeRequestArg = (arg: unknown) => {
    let serializedArg = null;
    try {
      serializedArg = JSON.stringify(arg);
    } catch (e) {
      console.error("Could not JSON serialize arg", arg);
      return null;
    }
    const argBytes = new TextEncoder().encode(serializedArg);
    const allocFn = wasmFunctionFactory(wasm.fn[allocationFnName], {
      memory: wasm.memory,
    });
    console.log(
      `Requesting allocation of ${argBytes.length}B for '${serializedArg}'`,
    );
    const allocResult = allocFn(argBytes.length);
    if (isError(allocResult)) {
      console.error(`Allocation failed: ${allocResult.message}`);
      return null;
    }

    console.log("request of allocation returned data", allocResult.data);
    const sliceAddress = allocResult.data as Pointer;
    const { ptr, len } = readSliceAt(wasm.memory, sliceAddress);
    console.log("request of allocation pointed to", ptr, len);

    if (len !== argBytes.length) {
      console.error(
        `Lengths of input do not match; allocated: ${len}, message: ${argBytes.length}`,
      );
    }
    // Copy the serialized JSON bytes into WebAssembly memory
    const u8Array = new Uint8Array(wasm.memory.buffer, ptr, len);
    argBytes.forEach((element, i) => {
      u8Array[i] = element;
    });
    return sliceAddress;
  };

  // todo: cache it
  const apiEntries = Object.entries(wasm.fn).map(([fnName, fn]) => [
    fnName,
    wasmFunctionFactory(fn, {
      memory: wasm.memory,
      argWrapper: serializeRequestArg,
    }),
  ]);
  const api = Object.fromEntries(apiEntries) as WasmApi<Api>;

  return [api, wasm] as const;
}
