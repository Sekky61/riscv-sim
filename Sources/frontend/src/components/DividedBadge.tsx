/**
 * @file    DividedBadge.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Badge or a tag with a dividing line between elements
 *
 * @date    05 March 2024, 13:00 (created)
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

type DividedBadgeProps = {
  children: React.ReactNode;
  title?: string;
};

/**
 * Pill-like badge
 */
export function DividedBadge({ children, title }: DividedBadgeProps) {
  return (
    <div
      title={title}
      className='inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold transition-colors secondary-container divide-pad snap-start divide-x divide-primary-50 gap-1'
    >
      {children}
    </div>
  );
}
