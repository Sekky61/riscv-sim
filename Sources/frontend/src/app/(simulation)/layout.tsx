/**
 * @file    layout.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The main layout of the app
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

import { type ReactNode } from 'react';

import '@/styles/globals.css';

import Navbar from '@/components/Navbar';

/**
 * Layout of the simualtion page.
 *
 * The div of navbar sets new stacking context to render over the main content.
 */
export default function Layout({ children }: { children: ReactNode }) {
  return (
    <>
      <div className='w-14 relative z-30'>
        <Navbar />
      </div>
      <div className='relative flex-grow overflow-y-auto'>{children}</div>
    </>
  );
}
