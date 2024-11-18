/**
 * @file    route.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Umami ID getter
 *
 * @date    12 November 2024, 10:00 (created)
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
export const dynamic = 'force-dynamic';

import { registerUmami } from '@/lib/umami';

const umamiCache = {
  data: null as { website_uuid: string } | null,
};

export async function GET() {
  if (umamiCache.data === null) {
    let webdata = null;
    try {
      webdata = await registerUmami();
    } catch (error) {
      console.error('Error fetching Umami ID:', error);
      return new Response('Failed to register with Umami', {
        status: 500,
      });
    }
    if (webdata === null) {
      return new Response('Failed to register with Umami', {
        status: 500,
      });
    }
    umamiCache.data = webdata;
  }

  return Response.json(umamiCache.data);
}
