/**
 * @file    serverCalls.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Call compiler API implementation
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

import { SimulationConfig } from '@/lib/forms/Isa';
import {
  AsyncEndpointFunction,
  CompileRequest,
  CompileResponse,
  EndpointMap,
  EndpointName,
  InstructionDescriptionResponse,
  ParseAsmRequest,
  ParseAsmResponse,
  ServerError,
  SimulateRequest,
  SimulateResponse,
} from '@/lib/types/simulatorApi';

import { apiBaseUrl, apiServerHost } from '@/constant/env';
import { CompilerOptions } from './redux/compilerSlice';

export async function callCompilerImpl(
  code: string,
  options: CompilerOptions,
): Promise<CompileResponse> {
  const body: CompileRequest = {
    code,
    optimizeFlags: options.optimizeFlags,
  };
  return await callApi('compile' as const, body);
}

export async function callParseAsmImpl(
  code: string,
  cfg: SimulationConfig, // Does not need code
): Promise<ParseAsmResponse> {
  const body: ParseAsmRequest = {
    code,
    config: cfg,
  };
  return await callApi('parseAsm' as const, body);
}

export async function callSimulationImpl(
  tick: number | null,
  cfg: SimulationConfig,
): Promise<SimulateResponse> {
  const body: SimulateRequest = {
    tick,
    config: cfg,
  };
  return await callApi('simulate' as const, body);
}

export async function callInstructionDescriptionImpl(): Promise<InstructionDescriptionResponse> {
  return await callApi('instructionDescription' as const, {});
}

/**
 * A custom exception for the case when the server returns an error.
 */
export class ServerErrorException extends Error {
  constructor(obj: ServerError) {
    super(`Server error: ${obj.message}`);
  }
}

/**
 * Call the simulator server API. Parse the response as JSON.
 * Throws an error if the response is not ok, it should be caught by the caller.
 *
 * Next.js proxies the backend simulator. Set the NEXT_PUBLIC_SIMSERVER_PORT and NEXT_PUBLIC_SIMSERVER_HOST env variables (see .env.example, Dockerfile).
 * The default is the same host as the app is running on, but on port 8000.
 */
const callApi: AsyncEndpointFunction = async <T extends EndpointName>(
  endpoint: T,
  request: EndpointMap[T]['request'],
) => {
  let apiUrl: string;
  if (typeof window === 'undefined') {
    // Running on server
    const host = apiBaseUrl || 'localhost:3000';
    apiUrl = `${host}/`;
  } else {
    // Running in browser
    apiUrl = '/api/sim/';
  }

  const url = `${apiUrl}${endpoint}`;

  console.info(`Calling API: ${url}`);

  // In browser, the absolute path works (origin is defined), but on server (node.js) it needs a full URL.
  // The only way to know the url at build time is to use an environment variable.
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (response.ok) {
    // Deserialize the response. It is either the requested object or an error message.
    return response.json();
  }

  if (response.status === 400) {
    const error = await response.json();
    throw new ServerErrorException(error);
  }

  throw new Error(`Network response was not ok: ${response.status}`);
};
