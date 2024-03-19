
/**
 * @file    Trace.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A visual element of a trace between two blocks
 *
 * @date    19 March 2024, 22:00 (created)
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

type TraceProps = {
  /** The origin of the trace relative to the parent element */
  top?: string;
  bottom?: string;
  left?: string;
  right?: string;
  /** The length of the trace */
  length?: string;
  vertical?: boolean;
}

/**
 * A visual element of a trace between two blocks.
 * It must be positioned relative to the parent (absolute positioning).
 * The gap between the blocks is 2.5rem
 */
export function Trace({ top, bottom, left, right, length = '2.5rem', vertical = false }: TraceProps) {

  // Calculate the line endpoints based on the direction of the trace
  const x2 = vertical ? '0' : '100%';
  const y2 = vertical ? '100%' : '0';

  return (
    <svg style={{
      top: top,
      bottom: bottom,
      left: left,
      right: right,
      width: length,
      height: length,
    }} className='absolute -z-10'
      width='100' height='100' 
    >
      <title>Trace</title>
      <line x1='0' y1='0' x2={x2} y2={y2} stroke='#000' strokeWidth='1' />
    </svg>
  );
}

