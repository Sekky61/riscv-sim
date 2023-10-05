/**
 * @file    callParseAsm.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Call /parseAsm API implementation
 *
 * @date    29 September 2023, 22:20 (created)
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

import { ErrorItem } from '@/lib/serverCalls/callCompiler';

// TODO: rename to ParseAsmApiResponse
export type ParseAsmAPIResponse =
  | {
      success: true;
    }
  | {
      success: false;
      errors: {
        '@items': Array<ErrorItem>;
      };
    };

export async function callParseAsmImpl(code: string) {
  // fetch from :8000/parseAsm
  // payload:
  // {
  //   "@type": "com.gradle.superscalarsim.server.parseAsm.ParseAsmRequest",
  //   "code": string
  // }

  const serverUrl =
    process.env.NEXT_PUBLIC_SIMSERVER_URL || 'http://localhost:8000';

  const response = await fetch(`${serverUrl}/parseAsm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      '@type': 'com.gradle.superscalarsim.server.parseAsm.ParseAsmRequest',
      code,
    }),
  });
  const json: ParseAsmAPIResponse = await response.json();
  return json;
}
