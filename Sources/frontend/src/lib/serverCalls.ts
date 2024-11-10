/**
 * @file    serverCalls.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Call API endpoints
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

import { EnvContextType, env } from '@/constant/envProvider';
import type {
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

/**
 * Call the /compile endpoint
 * @param request The compilation request (code, flags)
 * @returns       The response from the server, or throws an error
 */
export async function callCompilerImpl(
  request: CompileRequest,
): Promise<CompileResponse> {
  return callApi('compile' as const, request);
}

/**
 * Call the /parseAsm endpoint
 * @param request The parse request (code, config) (only memory is relevant)
 * @returns    The response from the server, or throws an error
 */
export async function callParseAsmImpl(
  request: ParseAsmRequest,
): Promise<ParseAsmResponse> {
  return callApi('parseAsm' as const, request);
}

/**
 * Call the /simulate endpoint
 * @param request The simulation request (code, memory, config, start address, number of cycles)
 * @returns    The response from the server, or throws an error
 */
export async function callSimulationImpl(
  request: SimulateRequest,
): Promise<SimulateResponse> {
  return callApi('simulate' as const, request);
}

/**
 * Call the /instructionDescription endpoint
 * @returns The response from the server, or throws an error
 */
export async function callInstructionDescriptionImpl(): Promise<InstructionDescriptionResponse> {
  return callApi('instructionDescription' as const, {});
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
 * Get the API hostname, which might be different on client and server
 */
async function getApiPrefix(): Promise<string> {
  // In browser, the absolute path works (origin is defined), but on server (node.js) it needs a full URL.
  // environment variables are pulled from server so that they can be configured at runtime
  
  if(!cachedEnv) {
    cachedEnv = await env();
  }

  if (typeof window === 'undefined') {
    return cachedEnv.simApiInternalPrefix;
  }
  return cachedEnv.simApiExternalPrefix;
}

/**
* The environment variables pulled from the server are cached here.
*/
let cachedEnv: EnvContextType | null = null;

/**
 * Call the simulator server API. Parse the response as JSON.
 * Throws an error if the response is not ok, it should be caught by the caller.
 *
 * See Readmes for how to define the API path
 */
const callApi: AsyncEndpointFunction = async <T extends EndpointName>(
  endpoint: T,
  request: EndpointMap[T]['request'],
) => {
  const url = `${await getApiPrefix()}/${endpoint}`;

  console.info(`Calling API endpoint ${url}`);

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (response.ok) {
    console.info(`API call to ${url} successful`);
    // Deserialize the response. It is either the requested object or an error message.
    return response.json();
  }

  if (response.status === 400) {
    const error = await response.json();
    throw new ServerErrorException(error);
  }

  throw new Error(`Network response was not ok: ${response.status}`);
};
