/**
 * @file    DevTools.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Debugging tools
 *
 * @date    10 April 2024, 19:00 (created)
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

'use client';

import { Button } from '@/components/base/ui/button';
import { Card, CardTitle } from '@/components/base/ui/card';
import { loadFile } from '@/lib/utils';

/**
 * Menu with developer tools
 */
export function DevTools() {
  // Do not render in production
  if (process.env.NODE_ENV === 'production') {
    return null;
  }

  // load a file picked by the user, set it as localStorage
  const handleFileLoad = async () => {
    loadFile(loadLocalStorage);
  };

  return (
    <Card>
      <CardTitle>Developer tools</CardTitle>
      <Button onClick={handleFileLoad}>Load localStorage</Button>
    </Card>
  );
}

/**
 * Given a file, parse it as json and set it as localStorage keys
 */
function loadLocalStorage(content: string) {
  const data = JSON.parse(content);
  for (const key in data) {
    const value = data[key];
    console.log('Loading key:', key, 'value:', value);
    // try to unescape the value (\\, \n, etc.)
    try {
      const interm = JSON.parse(value);
      data[key] = JSON.stringify(interm);
    } catch (e) {
      // if it fails, just use the original value
      data[key] = value;
    }
    localStorage.setItem(key, data[key]);
  }

  console.log('Loading new localStorage:', data);
}
