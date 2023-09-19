/**
 * @file    simulatorApi.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Type definitions for compiler API
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

// Send a request to the server to compile the code
export type CompilerInput = {
  c_code: string;
  optimize: boolean;
};

export type CompilerOutput = {
  // Textual representation of the compiled code
  asm: string;
  // A mapping from C code to assembly code
  blocks: Block[];
};

export type Block = {
  // Line in the C code
  c_line: number;
  // Lines in the assembly code
  asm_lines: number[];
};

// State of the simulator
export type SimulatorState = {
  // PC - The current line of code being executed
  pc: number;
  // The current value of the registers
  registers: number[];
  // The current value of the memory
  memory: number[];
  // The current value of the flags
  flags: boolean[];
};

export type SimulationStartInput = {
  // Code to be simulated
  asm: string;
};
