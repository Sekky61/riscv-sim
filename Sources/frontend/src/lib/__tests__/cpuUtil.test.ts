/**
 * @file    cpuUtil.test.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for utility functions for CPU state
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

import { hasId, isReference, resolveRefs } from '@/lib/cpuState/util';

describe('The isReference type guard', () => {
  it('Should return true for Reference', () => {
    expect(isReference({ '@ref': 1 })).toBe(true);
  });

  it('Should return false for non-Reference', () => {
    expect(isReference({})).toBe(false);
    expect(isReference({ '@id': 200 })).toBe(false);
  });

  it('Should return false for null', () => {
    expect(isReference(null)).toBe(false);
  });

  it('Should return false for undefined', () => {
    expect(isReference(undefined)).toBe(false);
  });

  it('Should return false for number', () => {
    expect(isReference(1)).toBe(false);
  });
});

describe('The hasId type guard', () => {
  it('Should return true for WithId', () => {
    expect(hasId({ '@id': 1 })).toBe(true);
  });

  it('Should return false for non-WithId', () => {
    expect(hasId({})).toBe(false);
    expect(hasId({ '@ref': 200 })).toBe(false);
  });

  it('Should return false for null', () => {
    expect(hasId(null)).toBe(false);
  });

  it('Should return false for undefined', () => {
    expect(hasId(undefined)).toBe(false);
  });

  it('Should return false for number', () => {
    expect(hasId(1)).toBe(false);
  });
});

describe('The resolveRefs function', () => {
  it('Should resolve all references in the object', () => {
    const obj = {
      '@id': 1,
      a: {
        '@ref': 2,
      },
    };

    const map = {
      1: {
        '@id': 1,
        a: {
          '@ref': 2,
        },
      },
      2: {
        '@id': 2,
        bar: 'baz',
      },
    };

    const resolved = resolveRefs(obj, map);

    expect(resolved).toEqual({
      '@id': 1,
      a: {
        '@id': 2,
        bar: 'baz',
      },
    });
  });

  it('Should resolve recursive references', () => {
    const obj = {
      '@id': 1,
      a: {
        '@ref': 2,
      },
    };

    const map = {
      1: {
        '@id': 1,
        a: {
          '@ref': 2,
        },
      },
      2: {
        '@id': 2,
        bar: 'baz',
        b: {
          '@ref': 3,
        },
      },
      3: {
        '@id': 3,
        foo: 'bar',
      },
    };

    const resolved = resolveRefs(obj, map);

    expect(resolved).toEqual({
      '@id': 1,
      a: {
        '@id': 2,
        bar: 'baz',
        b: {
          '@id': 3,
          foo: 'bar',
        },
      },
    });
  });
});
