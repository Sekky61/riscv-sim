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
import { Dialog, DialogTrigger } from '@radix-ui/react-dialog';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';

export type BlockProps = {
  title: string;
  stats?: ReactChildren;
  children: ReactChildren;
  detailDialog?: ReactChildren; // a DialogContent component to be displayed in a modal after clicking on the More button
} & ReactClassName;

/**
 * If you want to use a fixed width, specify it outside of the component
 */
export default function Block({
  children,
  className,
  title,
  stats,
  detailDialog,
}: BlockProps) {
  const classes = clsx(
    className,
    'p-2 flex gap-2 flex-col surface-container rounded-[12px] border sim-shadow',
  );

  return (
    <div className={classes}>
      <div className='flex justify-between items-center pl-2'>
        <span className='font-bold '>{title}</span>
        {detailDialog && (
          <Dialog>
            <DialogTrigger>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div className='iconHighlight h-8 w-8 p-1 rounded-full text-primary'>
                    <MoreVertical strokeWidth={1.5} />
                  </div>
                </TooltipTrigger>
                <TooltipContent side='bottom'>See details</TooltipContent>
              </Tooltip>
            </DialogTrigger>
            {detailDialog}
          </Dialog>
        )}
      </div>
      {stats && <div className='text-sm'>{stats}</div>}
      {children}
    </div>
  );
}
