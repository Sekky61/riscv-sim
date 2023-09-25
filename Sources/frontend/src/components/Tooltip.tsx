/**
 * @file    Tooltip.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tooltip component, used for displaying help text
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

export type TooltipProps = {
  text: string;
  shortcut?: string;
};

export default function Tooltip({ text, shortcut }: TooltipProps) {
  return (
    <div className='tooltiptext show-tooltip-right rounded bg-black px-2 py-1 text-white duration-100 flex items-center gap-4'>
      <div>{text}</div>
      {shortcut && <div className='text-xs text-gray-100'>({shortcut})</div>}
    </div>
  );
}
