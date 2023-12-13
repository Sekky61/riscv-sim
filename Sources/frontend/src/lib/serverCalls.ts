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

import { CpuConfiguration, MemoryLocationApi } from '@/lib/forms/Isa';
import {
  AsyncEndpointFunction,
  CompileRequest,
  CompileResponse,
  EndpointName,
  ParseAsmRequest,
  ParseAsmResponse,
  SimulateRequest,
  SimulateResponse,
} from '@/lib/types/simulatorApi';

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
): Promise<ParseAsmResponse> {
  const body: ParseAsmRequest = {
    code,
  };
  return await callApi('parseAsm' as const, body);
}

export async function callSimulationImpl(
  tick: number,
  cfg: CpuConfiguration,
): Promise<SimulateResponse> {
  // Take just some fields from memory locations
  const memoryLocations: MemoryLocationApi[] = cfg.memoryLocations.map(
    (loc) => ({
      name: loc.name,
      alignment: loc.alignment,
      dataChunks: loc.dataChunks,
    }),
  );
  // todo: cfg type
  const body: SimulateRequest = {
    tick,
    config: {
      ...cfg,
      memoryLocations,
    },
  };
  return await callApi('simulate' as const, body);
}

async function callApi<T extends EndpointName>(
  ...args: Parameters<AsyncEndpointFunction<T>>
): Promise<ReturnType<AsyncEndpointFunction<T>>> {
  const endpoint = args[0];
  const request = args[1];

  const serverUrl = getSimulatorServerUrl();

  const response = await fetch(`${serverUrl}/${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  return response.json();
}

/**
 * The default is the same host as the app is running on, but on port 8000.
 * Can be overridden by setting the NEXT_PUBLIC_SIMSERVER_PORT and NEXT_PUBLIC_SIMSERVER_HOST env variables (see .env.example, Dockerfile).
 * @returns The URL of the simulator server
 */
export function getSimulatorServerUrl(): string {
  const hostName =
    process.env.NEXT_PUBLIC_SIMSERVER_HOST ?? window.location.hostname;
  const port = process.env.NEXT_PUBLIC_SIMSERVER_PORT ?? '8000';
  return `http://${hostName}:${port}`;
}