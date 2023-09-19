/**
 * @file    callCompiler.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   [TODO]
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

import { CompilerOptions } from './redux/compilerSlice';

export type APIResponse =
  | {
      '@type': string;
      success: true;
      program: string[];
      cLines: number[];
      asmToC: number[];
    }
  | {
      '@type': string;
      success: false;
      compilerError?: string;
    };

export async function callCompilerImpl(code: string, options: CompilerOptions) {
  // fetch from :8000/compile
  // payload:
  // {
  //   "@type": "com.gradle.superscalarsim.server.compile.CompileRequest",
  //   "code": string
  //   "optimize": boolean
  // }

  const response = await fetch('http://localhost:8000/compile', {
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
  const json: APIResponse = await response.json();
  return json;
}
