/**
 * @file    useSimWasm.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   React hook for running simulation wasm
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

import type { SimulationConfig } from "../forms/Isa";
import type { ParseAsmRequest, ParseAsmResponse } from "../types/simulatorApi";
import { useWrappedWasm } from "./useWrappedWasm";

/**
 * The simulation API
 */
export type SimulationApi = {
  add: (x: number, y: number) => number;
  /**
   * @param sizeBytes the requested buffer size
   * @returns pointer to slice
   */
  allocRequestSpace: (sizeBytes: number) => { ptr: number; len: number };
  getDefaultCpuConfig: () => SimulationConfig;
  parseAsm: (request: ParseAsmRequest) => ParseAsmResponse;

  /**
   * For testing error messages
   */
  returnError: (allocationError: boolean) => unknown;
};

/**
 * This hook is the interface between WASM simulator and
 * frontend code. Call functions and get results from it.
 * TODO: make calling wasm async
 */
export function useSimWasm() {
  return useWrappedWasm<SimulationApi>("allocRequestSpace");
}
