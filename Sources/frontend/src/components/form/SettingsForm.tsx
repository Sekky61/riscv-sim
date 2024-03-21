/**
 * @file    SettingsForm.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Generic settings form component
 *
 * @date    14 December 2023, 16:00 (created)
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

import { ModeToggle } from '@/components/ModeToggle';
import { Button } from '@/components/base/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import { apiBaseUrl } from '@/constant/env';
import { useAppSelector } from '@/lib/redux/hooks';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import { saveAsFile, saveAsJsonFile } from '@/lib/utils';

/**
 * Global settings
 */
export function SettingsForm() {
  const activeConfig = useAppSelector(selectActiveConfig);

  // Log to console the address of API server
  console.info(
    `API server address (that Next.js backend proxies): ${apiBaseUrl}`,
  );

  const clearLocalMemory = () => {
    // confirm
    const doit = confirm('Are you sure you want to clear local memory?');
    if (!doit) return;

    localStorage.clear();
    console.info('Local memory cleared');
    // window.location.reload();
  };

  const saveCpuConfig = () => {
    saveAsJsonFile(activeConfig.cpuConfig, 'cpu-config.json');
  };

  const saveCode = () => {
    saveAsFile(activeConfig.code, 'code.r5');
  };

  const saveMemory = () => {
    saveAsJsonFile(activeConfig.memoryLocations, 'memory.json');
  };

  return (
    <div>
      <section className='pb-10'>
        <h2>Storage</h2>
        <Card className='mb-8'>
          <CardHeader>
            <CardTitle>Local Storage</CardTitle>
            <CardDescription>
              You can empty the local storage as a first step in troubleshooting
              in the event of an issue. Make sure you refresh the page
              afterwards.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={clearLocalMemory}>Clear local memory</Button>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Export Current Configuration</CardTitle>
            <CardDescription>
              The current settings can be exported to a file. This is useful for
              utilizing the simulator's CLI version, backing up data, and
              sharing the settings with others.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className='flex gap-4'>
              <Button onClick={saveCpuConfig}>Export CPU Configuration</Button>
              <Button onClick={saveCode}>Export Program in Assembly</Button>
              <Button onClick={saveMemory}>Export Memory Objects</Button>
            </div>
          </CardContent>
        </Card>
      </section>
      <section className='pb-10'>
        <h2>Appearance</h2>
        <Card>
          <CardHeader>
            <CardTitle>Light/Dark Mode</CardTitle>
            <CardDescription>
              You can toggle the light/dark mode of the application.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className='flex'>
              <ModeToggle />
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
