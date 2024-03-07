/**
 * @file    layout.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Layout of the compiler page
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

import Navbar from '@/components/Navbar';

/**
 * This layout is mused inside the root layout for the pages in this directory.
 * It contains the expanded navbar.
 */
export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className='flex w-screen h-screen overflow-hidden'>
      <div className='navbar-slot'>
        <Navbar />
      </div>
      <div className='shrink-0 flex-grow py-10 overflow-y-auto'>
        <div className='small-container main-pane'>{children}</div>
      </div>
    </div>
  );
}
