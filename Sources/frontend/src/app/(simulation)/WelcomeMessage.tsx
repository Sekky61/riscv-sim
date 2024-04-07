/**
 * @file    WelcomeMessage.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The welcome message dialog.
 *
 * @date    02 April 2024, 22:00 (created)
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

import { Dialog, DialogContent } from '@/components/base/ui/dialog';
import { useLocalStorage } from '@uidotdev/usehooks';
import Link from 'next/link';

/**
 * The welcome message dialog.
 * Shows the welcome message to the user on the first visit.
 * The information about visitedness is stored in the local storage :)
 */
export function WelcomeMessage() {
  const [visited, saveVisited] = useLocalStorage('visited', false);
  const open = !visited;

  return (
    <Dialog open={open} onOpenChange={() => saveVisited(true)}>
      <DialogContent>
        <div className='p-4'>
          <h1 className='text-2xl font-bold'>
            Welcome to the RISC-V Superscalar simulator!
          </h1>
          <p className='mt-2'>
            This app allows you to simulate the execution of a superscalar
            processor step by step.
          </p>
          <p className='mt-2'>
            To get started, navigate to the{' '}
            <Link href='/compiler' className='link'>
              code editor
            </Link>{' '}
            and load an example program.
          </p>
          <p className='mt-2'>
            You can find more information about the app in the{' '}
            <Link href='/help' className='link'>
              help section
            </Link>
            .
          </p>
        </div>
      </DialogContent>
    </Dialog>
  );
}
