/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The main page of the application with the simulation
 *
 * @date    19 September 2023, 22:00 (created)
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

import { CanvasWindow } from '@/components/CanvasWindow';
import { ReloadSimModal } from '@/components/ReloadSimModal';
import { SidePanel } from '@/components/simulation/SidePanel';
import { SimGrid } from '@/components/simulation/SimGrid';
import Timeline from '@/components/simulation/Timeline';
import { AutoPlay } from './AutoPlay';
import { ErrorMessage } from './ErrorMessage';
import { WelcomeMessage } from './WelcomeMessage';

export const metadata = {
  title: 'RISC-V simulator',
  description: 'Interactive RISC-V superscalar simulator',
};

export default function HomePage() {
  return (
    <div className='flex'>
      <div className='flex-grow h-screen'>
        <div className='relative w-full h-full shadow-inner'>
          <ErrorMessage />
          <CanvasWindow>
            <SimGrid />
          </CanvasWindow>
          <div className='absolute top-3 left-1/2'>
            <Timeline />
          </div>
          <div className='absolute top-3 right-[280px]'>
            <AutoPlay />
          </div>
          <SidePanel />
        </div>
      </div>
      <ReloadSimModal />
      <WelcomeMessage />
    </div>
  );
}
