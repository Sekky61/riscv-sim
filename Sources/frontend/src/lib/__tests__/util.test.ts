/**
 * @file    util.test.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for utility functions
 *
 * @date    05 January 2023, 14:00 (created)
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

import { formatNumberWithUnit } from '@/lib/utils';

describe('The Unit Formatter', () => {
  it('Should not crash on edge values', () => {
    const res = formatNumberWithUnit(0);
    expect(res).toBe('0 Hz');

    const res2 = formatNumberWithUnit(1023);
    expect(res2).toBe('1.0 kHz');

    // Negative values are not supported
    const res3 = formatNumberWithUnit(-1);
    expect(res3).toBe('-1 Hz');

    // Does not crash
    const _res4 = formatNumberWithUnit(NaN);
    const _res5 = formatNumberWithUnit(Infinity);
  });

  it('Should format GHz', () => {
    const res = formatNumberWithUnit(1e9);
    expect(res).toBe('1 GHz');

    const res2 = formatNumberWithUnit(1.5e9);
    expect(res2).toBe('1.5 GHz');
  });
});
