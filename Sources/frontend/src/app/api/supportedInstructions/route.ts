/**
 * @file    supportedInstructions.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Static data for the app (JSON files)
 *
 * @date    31 January 2024, 10:00 (created)
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

import { getSimulatorServerUrl } from '@/lib/serverCalls';

export async function GET() {
  // Fetch from the simulation server
  // TODO: must be a post with empty object
  const serverUrl = getSimulatorServerUrl();
  const response = await fetch(`${serverUrl}/instructionDescription`, {
    method: 'POST',
    body: '{}',
  });

  return new Response(response.body, {
    headers: {
      'content-type': 'application/json',
    },
  });
}
