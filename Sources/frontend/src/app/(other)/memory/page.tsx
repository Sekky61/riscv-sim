/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology 

 *          Brno University of Technology 

 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The memory page
 *
 * @date    27 November 2023, 22:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023  Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use client';

import { Button } from '@/components/base/ui/button';
import MemoryForm from '@/components/form/MemoryForm';
import type { MemoryLocationApi } from '@/lib/forms/Isa';
import { isMemoryLocations } from '@/lib/forms/validators';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  removeMemoryLocation,
  selectActiveConfig,
  setMemoryLocations,
} from '@/lib/redux/isaSlice';
import { loadFile, saveAsJsonFile } from '@/lib/utils';
import clsx from 'clsx';
import Head from 'next/head';
import { useState } from 'react';
import { toast } from 'sonner';

/**
 * This is the main page for the memory editor.
 * It allows the user to create, edit, and delete memory locations.
 * It also allows the user to import and export memory locations.
 */
export default function HomePage() {
  // Load the active ISA
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);
  const [activeMemoryLocation, setActiveMemoryLocation] = useState('new');
  const memoryLocations = activeIsa?.memoryLocations;

  const handleDelete = () => {
    // remove from redux
    dispatch(removeMemoryLocation(activeMemoryLocation));
    setActiveMemoryLocation('new');
  };

  const doImport = () => {
    loadFile((json_string) => {
      // TODO: resolve issue with extra fields on the form
      let newMemoryLocations: unknown;
      try {
        newMemoryLocations = JSON.parse(json_string);
      } catch (e) {
        toast.error('Invalid JSON. Nothing got imported.');
        return;
      }
      if (!isMemoryLocations(newMemoryLocations)) {
        toast.error('Invalid memory locations. Nothing got imported.');
        return;
      }
      // they could be added, but a check for existing names would be needed
      dispatch(setMemoryLocations(newMemoryLocations));
      toast.success(`Imported ${newMemoryLocations.length} memory locations`);
    });
  };

  const doExport = () => {
    saveAsJsonFile(memoryLocations, 'memory.json');
  };

  return (
    <main className='h-full'>
      <Head>
        <title>Memory</title>
      </Head>
      <h1>Memory Editor</h1>
      <div className='flex divide-x justify-center'>
        <div className='w-48 p-4 flex flex-col gap-4 divide-y'>
          <Button
            variant='secondary'
            className={clsx(
              'new' === activeMemoryLocation && 'bg-secondary',
              'w-full',
            )}
            onClick={() => setActiveMemoryLocation('new')}
          >
            New Object
          </Button>
          <div className='pt-4 flex justify-around gap-3'>
            <Button onClick={doImport} variant='secondary'>
              Import
            </Button>
            <Button onClick={doExport} variant='secondary'>
              Export
            </Button>
          </div>
          <div>
            <h2 className='text-lg mt-4 font-semibold text-center'>
              Memory Objects
            </h2>
            {memoryLocations?.length === 0 && (
              <div className='text-gray-400 text-sm text-center'>
                No memory locations
              </div>
            )}
            {memoryLocations.map((memoryLocation) => {
              const isActive = memoryLocation.name === activeMemoryLocation;
              const style = clsx(
                isActive && 'bg-primary text-onPrimary',
                'w-full mt-4',
              );
              return (
                <Button
                  variant='text'
                  className={style}
                  onClick={() => setActiveMemoryLocation(memoryLocation.name)}
                >
                  {memoryLocation.name}
                </Button>
              );
            })}
          </div>
        </div>
        <div className='p-4'>
          <MemoryForm
            existing={activeMemoryLocation !== 'new'}
            memoryLocationName={activeMemoryLocation}
            deleteCallback={handleDelete}
          />
        </div>
      </div>
    </main>
  );
}
