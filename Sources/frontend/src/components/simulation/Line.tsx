/**
 * @file    Line.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   SVG line component
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

// Renders a svg line

export type LineProps = {
  length: number;
};

// https://thenewcode.com/1068/Making-Arrows-in-SVG

export default function Line({ length }: LineProps) {
  return (
    <svg
      viewBox={`0 0 ${length} 100`}
      xmlns='http://www.w3.org/2000/svg'
      style={{ width: `${length}px`, height: '100px' }}
    >
      <line x1='0' y1='0' x2={length} y2='0' className='schemaLine' />
    </svg>
  );
}
