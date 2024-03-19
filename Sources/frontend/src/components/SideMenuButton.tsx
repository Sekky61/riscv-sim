/**
 * @file    SideMenuButton.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Side menu button component
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

'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { ReactNode } from 'react';

import { IconButton } from '@/components/IconButton';
import clsx from 'clsx';
import { useHotkeys } from 'react-hotkeys-hook';

export type SideMenuButtonProps = {
  Icon: ReactNode;
  href: string;
  shortcut: string;
  hoverText: string;
};

/**
 * The Icon prop is expected to be 24px in size
 */
export default function SideMenuButton({
  Icon,
  href,
  shortcut,
  hoverText,
}: SideMenuButtonProps) {
  const path = usePathname();
  const isActive = path === href;
  const router = useRouter();

  const cls = clsx(
    'sidemenu-button h-12 flex items-center rounded-full',
    isActive && 'secondary-container',
    !isActive && 'surface-variant hover:bg-neutral-20/[0.08]',
  );

  useHotkeys(shortcut, () => {
    // redirect to the page
    router.push(href);
  });

  return (
    <Link href={href} className={cls}>
      {Icon}
      <div className='nav-text text-nowrap ml-[8px]'>{hoverText}</div>
    </Link>
  );
}
