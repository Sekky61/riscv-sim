/**
 * @file    MemoryInfo.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Component to inform about memory locations in the ISA configuration
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

import { DividedBadge } from '@/components/DividedBadge';
import { useAppSelector } from '@/lib/redux/hooks';
import { selectActiveConfig } from '@/lib/redux/isaSlice';
import { pluralize } from '@/lib/utils';
import Link from 'next/link';

/**
 * Component to inform about memory locations
 */
export function MemoryInfo() {
  const activeIsa = useAppSelector(selectActiveConfig);
  const mem = activeIsa.memoryLocations;
  const names = mem.map((m) => m.name);

  const memoryLink = (
    <Link href='/memory' className='link'>
      memory
    </Link>
  );

  return (
    <div className='flex gap-2 pt-4'>
      {mem.length === 0 ? (
        <span>No {memoryLink} locations defined.</span>
      ) : (
        <span>
          {mem.length} {memoryLink} {pluralize('location', mem.length)} defined:
        </span>
      )}
      {names.map((name) => {
        return <DividedBadge key={name}>{name}</DividedBadge>;
      })}
    </div>
  );
}
