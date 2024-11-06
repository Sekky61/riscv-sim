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

import { ModeToggle } from '@/components/ModeToggle';
import { Button } from '@/components/base/ui/button';
import { Dialog, DialogContent } from '@/components/base/ui/dialog';
import { useTour } from '@reactour/tour';
import { useLocalStorage } from '@uidotdev/usehooks';
import Link from 'next/link';
import Image from 'next/image';
import FitLogo from '../../../public/FIT_color_EN.png';

/**
 * The welcome message dialog.
 * Shows the welcome message to the user on the first visit.
 * The information about visitedness is stored in the local storage :)
 */
export function WelcomeMessage() {
  const [visited, saveVisited] = useLocalStorage('visited', false);
  const { setIsOpen } = useTour();
  const open = !visited;

  function startTour() {
    setIsOpen(true);
    saveVisited(true);
  }

  return (
    <Dialog open={open} onOpenChange={() => saveVisited(true)}>
      <DialogContent>
        <div className='p-4'>
          <div>
            <Image className='w-2/3 mb-12' src={FitLogo} alt='BUT FIT' />
          </div>
          <h1 className='text-2xl font-bold'>
            Welcome to the RISC-V Superscalar Simulator!
          </h1>

          <p className='mt-2'>
            Get hands-on with superscalar processing. Step through each
            execution phase to understand processor behavior in detail.
          </p>
          <p className='mt-2'>
            Start by loading an example program in the{' '}
            <Link href='/compiler' className='link'>
              code editor
            </Link>
            . For a full overview of features, visit the{' '}
            <Link href='/help' className='link'>
              help section
            </Link>
            .
          </p>
        </div>
        <div className='flex gap-4 items-center justify-center mb-6'>
          <div className='relative'>
            <span className='absolute right-full font-semibold text-nowrap mr-4 mt-[6px] h-full'>
              Choose a Theme
            </span>
            <ModeToggle />
          </div>
        </div>
        <Button onClick={startTour} className='py-8 mx-8'>
          Start the Tour!
        </Button>
      </DialogContent>
    </Dialog>
  );
}
