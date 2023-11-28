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
import { useAppSelector } from '@/lib/redux/hooks';
import { selectActiveIsa } from '@/lib/redux/isaSlice';
import Head from 'next/head';

export default function HomePage() {
  // Load the active ISA
  const activeIsa = useAppSelector(selectActiveIsa);

  const memoryLocations = activeIsa?.memoryLocations;

  return (
    <main className='h-full'>
      <Head>
        <title>Memory</title>
      </Head>
      <h1 className='m-2 mb-6 text-2xl'>Memory Editor</h1>
      <div className='flex h-full flex-col'>
        <div className='flex divide-x'>
          <div className='w-48 p-4 flex flex-col gap-4'>
            {memoryLocations.map((memoryLocation) => {
              return <Button variant='ghost'>{memoryLocation.name}</Button>;
            })}
            <div className='mt-4 pt-4 border-t'>
              <Button variant='ghost' className='w-full'>
                New
              </Button>
            </div>
          </div>
          <div className='p-4'>
            <MemoryForm />
          </div>
        </div>
      </div>
    </main>
  );
}
