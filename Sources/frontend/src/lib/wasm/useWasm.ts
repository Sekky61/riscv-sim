/**
 * @file    useWasm.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   React hook for running wasm
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

// Source: https://github.com/Romainlg29/use-wasm/tree/main

import { useEffect, useState } from "react";

export interface useWasmReturn<T> {
  /**
   * The wasm exports
   */
  fn: T;
  isLoading: boolean;
  module: WebAssembly.Module;
  instance: WebAssembly.Instance;
  memory: WebAssembly.Memory;
}

export interface useWasmOptions {
  /**
   * Streaming instantiation
   * Make sure that the server returns the correct MIME type to enable this.
   * @default false
   */
  streaming?: boolean;

  /**
   * Fetch options
   * @default undefined
   */
  fetchOptions?: RequestInit;

  /**
   * Memory
   * Default: 256 initial, 512 maximum
   */
  memory?: WebAssembly.Memory;

  /**
   * Wasm environment
   */
  env?: (memory: WebAssembly.Memory) => { [key: string]: any };
}

/**
 * Hook to load a wasm file and use it.
 * @param path Path to the wasm file
 * @param options Options
 * @returns The wasm exports
 */
export const useWasm = <T>(path: string, options?: useWasmOptions) => {
  const [wasm, setWasm] = useState<useWasmReturn<T>>({
    isLoading: true,
    fn: {} as T,
    module: {} as WebAssembly.Module,
    instance: {} as WebAssembly.Instance,
    memory: options?.memory ?? ({} as WebAssembly.Memory),
  });

  useEffect(() => {
    if (!path) {
      throw new Error("Path is required");
    }

    /**
     * Abort controller
     */
    const controller = new AbortController();

    /**
     * Load the wasm file
     */
    setWasm((p: useWasmReturn<T>) => ({
      ...p,
      isLoading: true,
    }));

    /**
     * Create the memory
     */
    const memory =
      options?.memory ?? new WebAssembly.Memory({ initial: 256, maximum: 512 });

    /**
     * Create the env
     */
    const env = options?.env?.(memory) ?? {};

    (async () => {
      /**
       * Fetch the wasm file
       */
      const file = await fetch(path, {
        signal: controller.signal,
        ...options?.fetchOptions,
        headers: {
          ...options?.fetchOptions?.headers,
          "Content-Type": "application/wasm",
        },
      });

      if (!file.ok) {
        throw new Error(`Failed to fetch the WASM file: ${file.statusText}`);
      }

      /**
       * Instantiate the wasm file
       */
      let wa: WebAssembly.WebAssemblyInstantiatedSource;

      if (options?.streaming) {
        /**
         * Streaming instantiation
         */
        wa = await WebAssembly.instantiateStreaming(file, {
          js: {
            mem: memory,
          },
          env: {
            ...env,
          },
        });
      } else {
        /**
         * Buffer instantiation
         */
        const buffer = await file.arrayBuffer();
        wa = await WebAssembly.instantiate(buffer, {
          js: {
            mem: memory,
          },
          env: {
            ...env,
          },
        });
      }

      /**
       * Type the exports
       */
      type Exports<T extends {}> = T & { memory: WebAssembly.Memory };

      /**
       * Force the type of the exports to Exports (with memory)
       */
      const exports: Exports<{}> = wa.instance.exports as Exports<{}>;

      /**
       * Set the exports as the function
       */
      setWasm({
        isLoading: false,
        fn: exports as T,
        module: wa.module,
        instance: wa.instance,
        memory: exports.memory,
      });
    })();

    return () => {
      /**
       * Clean up
       */
      controller.abort();

      /**
       * Clear the memory
       */
      memory.grow(0);
    };
  }, [path]);

  return wasm;
};
