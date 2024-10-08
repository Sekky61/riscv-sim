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
  down?: boolean;
};

// https://thenewcode.com/1068/Making-Arrows-in-SVG

export default function Line({ length, down = false }: LineProps) {
  const width = down ? 10 : length;
  const height = down ? length : 10;
  return (
    <svg width={width} height={height}>
      <title>wire</title>
      <line
        x1={down ? 5 : 0}
        y1={down ? 0 : 5}
        x2={down ? 5 : length}
        y2={down ? length : 5}
        className='schemaLine'
      />
    </svg>
  );
}
