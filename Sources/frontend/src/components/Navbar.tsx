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
  Info,
  MemoryStick,
  Settings,
} from 'lucide-react';

import SideMenuButton from '@/components/SideMenuButton';
import clsx from 'clsx';

type NavbarProps = {
  expanded: boolean;
};

export default function Navbar({ expanded }: NavbarProps) {
  return (
    <nav
      className={clsx(
        'p-2 navbar flex flex-col justify-between',
        expanded && 'navbar-expanded',
      )}
    >
      <div className='flex flex-col gap-3'>
        <SideMenuButton
          Icon={<BrainCircuit strokeWidth={1.5} />}
          href='/'
          hoverText='Simulation'
          shortcut='Digit1'
        />
        <SideMenuButton
          Icon={<Code strokeWidth={1.5} />}
          href='/compiler'
          hoverText='Code editor'
          shortcut='Digit2'
        />
        <SideMenuButton
          Icon={<MemoryStick strokeWidth={1.5} />}
          href='/memory'
          hoverText='Memory settings'
          shortcut='Digit3'
        />
        <SideMenuButton
          Icon={<Cpu strokeWidth={1.5} />}
          href='/isa'
          hoverText='Architecture settings'
          shortcut='Digit4'
        />
        <SideMenuButton
          Icon={<BarChart3 strokeWidth={1.5} />}
          href='/stats'
          hoverText='Runtime statistics'
          shortcut='Digit5'
        />
      </div>
      <div className='flex flex-col gap-3'>
        <SideMenuButton
          Icon={<BookOpen strokeWidth={1.5} />}
          href='/docs'
          hoverText='RISC-V documentation'
          shortcut='Digit6'
        />
        <SideMenuButton
          Icon={<Settings strokeWidth={1.5} />}
          href='/settings'
          hoverText='App settings'
          shortcut='Digit7'
        />
        <SideMenuButton
          Icon={<Info strokeWidth={1.5} />}
          href='/help'
          hoverText='Help'
          shortcut='Digit8'
        />
      </div>
    </nav>
  );
}
