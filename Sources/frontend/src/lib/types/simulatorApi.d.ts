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

import type { SimulationConfig } from '@/lib/forms/Isa';
import type { MemoryLocationApi } from '@/lib/forms/Isa';
import type { OptimizeOption } from '@/lib/redux/compilerSlice';
import type {
  CpuState,
  InstructionFunctionModel,
  StopReason,
} from '@/lib/types/cpuApi';

type EndpointMap = {
  compile: CompileEndpoint;
  parseAsm: ParseAsmEndpoint;
  checkConfig: CheckConfigEndpoint;
  simulate: SimulateEndpoint;
  schema: SchemaEndpoint;
  instructionDescription: InstructionDescriptionEndpoint;
};
export type EndpointName = keyof EndpointMap;

/**
 * The API call
 * As a note, there is a difference between generic type and a type that is a generic function (https://stackoverflow.com/questions/51197819/declaring-const-of-generic-type).
 */
export type AsyncEndpointFunction = <T extends EndpointName>(
  endpoint: T,
  request: EndpointMap[T]['request'],
) => Promise<EndpointMap[T]['response']>;

//
// Error type
//

/**
 * This type is returned on 400 errors.
 * The name is a bit misleading, as it is sent on user errors.
 */
export type ServerError = {
  field: string;
  message: string;
  extra?: unknown;
};

//
// /schema
//

export interface SchemaEndpoint {
  name: 'schema';
  request: SchemaRequest;
  response: SchemaResponse;
}

export interface SchemaRequest {
  endpoint: EndpointName;
  requestResponse: 'request' | 'response';
}

export interface SchemaResponse {
  [k: string]: unknown;
}

//
// /compile
//

export interface CompileEndpoint {
  name: 'compile';
  request: CompileRequest;
  response: CompileResponse;
}

export interface CompileRequest {
  code: string;
  optimizeFlags: OptimizeOption[];
  memoryLocations: MemoryLocationApi[];
}

export type CompileResponse = {
  // common fields
  status: 'success' | 'c' | 'asm' | 'warning' | 'internal' | null;
  message: string;
} & (
  | {
      success: true;
      program: string;
      asmToC: number[];
    }
  | {
      success: false;
      compilerError: ComplexErrorItem[] | null;
      asmErrors: SimpleParseError[] | null;
    }
);

export type ComplexErrorItem = {
  kind: 'error' | 'warning';
  message: string;
  locations: Array<ErrorSpan>;
};

/**
 * Finish is optional, meaning that the error is a single character
 */
export type ErrorSpan = {
  caret: ErrorLocation;
  finish?: ErrorLocation;
};

export type ErrorLocation = {
  line: number;
  'display-column': number;
};

//
// /parseAsm
//

export interface ParseAsmEndpoint {
  name: 'parseAsm';
  request: ParseAsmRequest;
  response: ParseAsmResponse;
}

export type ParseAsmRequest = {
  code: string;
  memoryLocations: MemoryLocation[];
};

export type ParseAsmResponse =
  | {
      success: true;
    }
  | {
      success: false;
      errors: SimpleParseError[];
    };

export interface SimpleParseError {
  kind: 'error' | 'warning';
  message: string;
  line: number;
  columnStart: number;
  columnEnd: number;
}

//
// /checkConfig
//

export interface CheckConfigEndpoint {
  name: 'checkConfig';
  request: CheckConfigRequest;
  response: CheckConfigResponse;
}

export interface CheckConfigRequest {
  config: CpuConfig;
}

export interface CheckConfigResponse {
  valid: boolean;
  messages: string[];
}

//
// /simulate
//

export interface SimulateEndpoint {
  name: 'simulate';
  request: SimulateRequest;
  response: SimulateResponse;
}

/**
 * Specify tick to get the simulation state at this time (tick 0 is the initial state). Tick is optional. If not provided, the simulation will run until the end.
 */
export interface SimulateRequest {
  tick: number | null;
  config: SimulationConfig;
}

export interface SimulateResponse {
  executedSteps: number;
  state: CpuState;
  stopReason: StopReason;
}

//
// /instructionDescription
//

export interface InstructionDescriptionEndpoint {
  name: 'instructionDescription';
  request: InstructionDescriptionRequest;
  response: InstructionDescriptionResponse;
}

export type InstructionDescriptionRequest = Record<string, never>;

export type InstructionDescriptionResponse = {
  models: Record<string, InstructionFunctionModel>;
};
