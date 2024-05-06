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
import { useTour } from '@reactour/tour';
import { useRouter } from 'next/navigation';

/**
 * Global settings - clear localStorage, retake the tour, export configuration.
 */
export function SettingsForm() {
  const activeConfig = useAppSelector(selectActiveConfig);
  const router = useRouter();
  const tour = useTour();

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

  const startTour = () => {
    router.push('/');
    tour.setCurrentStep(0);
    tour.setIsOpen(true);
  };

  return (
    <div>
      <section className='pb-10'>
        <h2>General</h2>
        <div className='flex justify-around gap-4'>
          <Card className='flex-auto'>
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
          <Card className='flex-auto'>
            <CardHeader>
              <CardTitle>Retake the Tour</CardTitle>
              <CardDescription>
                Review the basics of how to operate the simulator.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button onClick={startTour}>Start the Tour</Button>
            </CardContent>
          </Card>
        </div>
      </section>
      <section className='pb-10'>
        <h2>Storage</h2>
        <div className='flex flex-wrap gap-4'>
          <Card className='flex-auto'>
            <CardHeader>
              <CardTitle>Local Storage</CardTitle>
              <CardDescription>
                You can empty the local storage as a first step in
                troubleshooting in the event of an issue. Make sure you refresh
                the page afterwards.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button onClick={clearLocalMemory}>Clear Local Memory</Button>
            </CardContent>
          </Card>
          <Card className='flex-auto'>
            <CardHeader>
              <CardTitle>Export Current Configuration</CardTitle>
              <CardDescription>
                The current settings can be exported to a file. This is useful
                for utilizing the simulator's CLI version, backing up data, and
                sharing the settings with others.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className='flex gap-4 flex-wrap'>
                <Button onClick={saveCpuConfig}>
                  Export CPU Configuration
                </Button>
                <Button onClick={saveCode}>Export Program in Assembly</Button>
                <Button onClick={saveMemory}>Export Memory Objects</Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
}
