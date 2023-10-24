/**
 * @file    callCompiler.ts
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

import { CompilerOptions } from '../redux/compilerSlice';

export type CompilerAPIResponse =
  | {
      '@type': string;
      success: true;
      program: string;
      cLines: number[];
      asmToC: number[];
    }
  | {
      '@type': string;
      success: false;
      error: string;
      compilerError: {
        '@items': Array<ErrorItem>;
      };
    };

export type ErrorItem = {
  kind: 'error' | 'warning';
  message: string;
  locations: {
    '@items': Array<ErrorSpan>;
  };
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

export async function callCompilerImpl(code: string, options: CompilerOptions) {
  // fetch from :8000/compile
  // payload:
  // {
  //   "@type": "com.gradle.superscalarsim.server.compile.CompileRequest",
  //   "code": string
  //   "optimize": boolean
  // }

  const serverUrl =
    process.env.NEXT_PUBLIC_SIMSERVER_URL || 'http://localhost:8000';

  const response = await fetch(`${serverUrl}/compile`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      '@type': 'com.gradle.superscalarsim.server.compile.CompileRequest',
      code,
      optimize: options.optimize,
    }),
  });
  const json: CompilerAPIResponse = await response.json();
  return json;
}
