/**
 * @file    transformErrors.test.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for error conversion
 *
 * @date    09 April 2024, 8:00 (created)
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

import { transformErrors } from '../editor/transformErrors';

describe('The error transformation', () => {
  it('Should transforrm one character error', () => {
    const diagnostics = transformErrors(
      [
        {
          kind: 'error',
          message: 'Error',
          locations: [
            {
              caret: { line: 1, 'display-column': 2 },
            },
          ],
        },
      ],
      'a',
    );

    expect(diagnostics).toEqual([
      {
        from: 1,
        to: 1,
        severity: 'error',
        message: 'Error',
      },
    ]);
  });

  it('Should transforrm wider error', () => {
    const diagnostics = transformErrors(
      [
        {
          kind: 'error',
          message: 'Error',
          locations: [
            {
              caret: { line: 1, 'display-column': 6 },
              finish: { line: 1, 'display-column': 10 },
            },
          ],
        },
      ],
      'good error',
    );

    expect(diagnostics).toEqual([
      {
        from: 5,
        to: 10,
        severity: 'error',
        message: 'Error',
      },
    ]);
  });

  it('should not crash on invalid line', () => {
    const diagnostics = transformErrors(
      [
        {
          kind: 'error',
          message: 'Error',
          locations: [
            {
              caret: { line: 4, 'display-column': 1 },
            },
          ],
        },
      ],
      'a',
    );

    expect(diagnostics).toEqual([
      {
        from: 0,
        to: 0,
        severity: 'error',
        message: 'Error',
      },
    ]);
  });

  it('should handle multiline errors', () => {
    const diagnostics = transformErrors(
      [
        {
          kind: 'error',
          message: 'Error',
          locations: [
            {
              caret: { line: 3, 'display-column': 4 }, // the 'o' in 'error'
            },
          ],
        },
      ],
      'good\n\nerror',
    );

    expect(diagnostics).toEqual([
      {
        from: 9,
        to: 9,
        severity: 'error',
        message: 'Error',
      },
    ]);
  });
});
