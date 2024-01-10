/**
 * @file    svg.test.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   SVG parser test
 *
 * @date    30 November 2023, 20:00 (created)
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

import { parseCsv } from '@/lib/csv';

describe('The CSV parser', () => {
  it('Reads 1D integers', () => {
    const result = parseCsv('1,2,3,4,5');
    expect(result).toEqual(['1', '2', '3', '4', '5']);
  });

  it('Reads 2D integers', () => {
    const result = parseCsv('1,2,3,4,5\n6,7,8,9,10');
    expect(result).toEqual(['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']);
  });

  it('Reads floats', () => {
    const result = parseCsv('1.1,2.2,3.3,4.4,5.5');
    expect(result).toEqual(['1.1', '2.2', '3.3', '4.4', '5.5']);
  });

  it('Handles last empty line', () => {
    const result = parseCsv('1,2,3,4,5\n6,7,8,9,10\n');
    expect(result).toEqual(['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']);
  });
});
