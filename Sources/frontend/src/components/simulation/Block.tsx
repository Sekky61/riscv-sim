/**
 * @file    Block.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A generic component for displaying a block in the simulation
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

// Universal block component - used for all blocks in the simulation
// Provides design of the box, content is substituted in

import clsx from 'clsx';
import { MoreVertical } from 'lucide-react';

import { ReactChildren, ReactClassName } from '@/lib/types/reactTypes';

export type BlockProps = {
  title: string;
  stats?: ReactChildren;
  children: ReactChildren;
  handleMore?: () => void;
} & ReactClassName;

export default function Block({
  children,
  className,
  title,
  stats,
  handleMore,
}: BlockProps) {
  const classes = clsx(
    className,
    'rounded border bg-white p-2 flex gap-2 flex-col min-w-[13rem]',
  );

  return (
    <div className={classes}>
      <div className='flex justify-between'>
        <span className='font-bold'>{title}</span>
        {handleMore && (
          <button
            type='button'
            onClick={handleMore}
            className='iconHighlight h-6 w-6 rounded-full'
          >
            <MoreVertical strokeWidth={1.5} />
          </button>
        )}
      </div>
      {stats && <div className='text-sm'>{stats}</div>}
      {children}
    </div>
  );
}
