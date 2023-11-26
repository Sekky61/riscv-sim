/**
 * @file    Placement.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Places a block in the simulation canvas
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

import clsx from 'clsx';

import { ReactClassName } from '@/lib/types/reactTypes';

// Place component on a canvas

export type PlacementProps = {
  children?: React.ReactNode;
  x: number;
  y: number;
  [key: string]: unknown;
} & ReactClassName;

export default function Placement({
  children,
  x = 0,
  y = 0,
  className,
  ...props
}: PlacementProps) {
  const cls = clsx('absolute', className);
  return (
    <div className={cls} style={{ left: `${x}px`, top: `${y}px` }} {...props}>
      {children}
    </div>
  );
}
