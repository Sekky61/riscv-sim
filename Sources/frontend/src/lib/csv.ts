/**
 * @file    csv.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   simple CSV parser
 *
 * @date    30 November 2023, 19:00 (created)
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

/**
 * Parse contents as CSV. Maps the values to a 1D array of numbers.
 * The rows do not have to be of the same length.
 *
 * @param contents
 */
export function parseCsv(contents: string): number[] {
  const numbers = [];
  for (const line of contents.split('\n')) {
    if (line === '') {
      continue;
    }
    for (const number of line.split(',')) {
      numbers.push(Number(number));
    }
  }

  return numbers;
}
