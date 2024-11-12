/**
 * @file    umami.ts
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

import { basePath } from '@/constant/env';

export async function registerUmami() {
  // In case it needs to be rewritten
  const apiUrl =
    process.env.UMAMI_API_URL ?? `http://umami:3000${basePath}/api`;
  const username = process.env.UMAMI_USERNAME ?? 'admin';
  const password = process.env.UMAMI_PASSWORD ?? 'umami';
  const domain = process.env.DOMAIN ?? 'localhost';

  console.info(`  Using url ${apiUrl}`);

  try {
    // Get token
    // request to auth/login with username and pw
    const loginresp = await fetch(`${apiUrl}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    // result is json with token
    const { token } = await loginresp.json();
    console.info('  Got token');

    // List all websites. If one exists, use it, else pick first
    const queryWebsitesResponse = await fetch(`${apiUrl}/websites`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const websitesObj = await queryWebsitesResponse.json();

    // Try if id exists, if not create it
    const websites = websitesObj.data;
    const found = websites.length > 0;
    if (found) {
      const picked_website = websites[0];
      console.info(
        '  Already registered. Picked first website: ',
        JSON.stringify(picked_website),
      );
      return picked_website;
    }
    console.info('  No websites found. Registering...');

    const response = await fetch(`${apiUrl}/websites`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: 'RISC-V Simulator',
        domain,
      }),
    });

    console.info(
      '  Registered with Umami. Response: ',
      JSON.stringify(response),
    );
    if (response.ok) {
      const data = await response.json();
      return data;
    }
    console.error(`Failed to register with Umami: ${response.statusText}`);
    return null;
  } catch (error) {
    console.error('Error registering with Umami:', error);
  }
  return null;
}
