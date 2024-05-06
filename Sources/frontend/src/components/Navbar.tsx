/**
 * @file    Navbar.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Side bar navigation menu
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

import {
  BarChart3,
  BookOpen,
  BrainCircuit,
  Code,
  Cpu,
  GraduationCap,
  Info,
  MemoryStick,
  Settings,
} from 'lucide-react';

import { SideMenuButton } from '@/components/SideMenuButton';

/**
 * Navbar with ~8 buttons. Numerical shortcuts are available.
 * The menu can be expanded or not, see the navbar class in css.
 */
export function Navbar() {
  return (
    <nav className='navbar'>
      <div className='navbar-inner surface w-14 h-full flex flex-col justify-between py-12'>
        <div className='flex flex-col'>
          <SideMenuButton
            icon={<BrainCircuit strokeWidth={1.5} />}
            href='/'
            hoverText='Simulation'
            shortcut='Digit1'
          />
          <SideMenuButton
            icon={<Code strokeWidth={1.5} />}
            href='/compiler'
            hoverText='Code Editor'
            shortcut='Digit2'
          />
          <SideMenuButton
            icon={<MemoryStick strokeWidth={1.5} />}
            href='/memory'
            hoverText='Memory Settings'
            shortcut='Digit3'
          />
          <SideMenuButton
            icon={<Cpu strokeWidth={1.5} />}
            href='/isa'
            hoverText='Architecture Settings'
            shortcut='Digit4'
          />
          <SideMenuButton
            icon={<BarChart3 strokeWidth={1.5} />}
            href='/stats'
            hoverText='Runtime Statistics'
            shortcut='Digit5'
          />
        </div>
        <div className='flex flex-col'>
          <SideMenuButton
            icon={<GraduationCap strokeWidth={1.5} />}
            href='/learn'
            hoverText='Architecture Overview'
            shortcut='Digit6'
          />
          <SideMenuButton
            icon={<BookOpen strokeWidth={1.5} />}
            href='/docs'
            hoverText='RISC-V Documentation'
            shortcut='Digit7'
          />
          <SideMenuButton
            icon={<Info strokeWidth={1.5} />}
            href='/help'
            hoverText='Help'
            shortcut='Digit8'
          />
          <SideMenuButton
            icon={<Settings strokeWidth={1.5} />}
            href='/settings'
            hoverText='App Settings'
            shortcut='Digit9'
          />
        </div>
      </div>
    </nav>
  );
}
