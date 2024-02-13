/**
 * @file    isa.test.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for ISA configuration
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

import {
  MemoryLocationApi,
  defaultCpuConfig,
  isaFormSchema,
  memoryLocationDefaultValue,
  memoryLocationSchema,
} from '@/lib/forms/Isa';

describe('The Default ISA configuration', () => {
  it('Should pass the validation', () => {
    // Throws ZodError if not valid
    const _result = isaFormSchema.parse(defaultCpuConfig);
  });
});

describe('The MemoryLocation schema', () => {
  it('Default value passes', () => {
    // Throws ZodError if not valid
    const _result = memoryLocationSchema.parse(memoryLocationDefaultValue);
  });

  it('constant passes', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataType: 'kInt',
      data: {
        kind: 'constant',
        constant: '1',
        size: 4,
      },
    };

    const _result = memoryLocationSchema.parse(obj);
  });

  it('random passes', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataType: 'kInt',
      data: {
        kind: 'random',
        min: 0,
        max: 10,
        size: 4,
      },
    };

    const _result = memoryLocationSchema.parse(obj);
  });

  it('data passes', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataType: 'kInt',
      data: {
        kind: 'data',
        data: ['1'],
      },
    };

    const _result = memoryLocationSchema.parse(obj);
  });

  it('mixed does not pass', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataTypes: [{ startOffset: 0, dataType: 'kInt' }],
      data: {
        kind: 'data',
        //@ts-ignore This is wrong on purpose
        constant: '9',
        data: ['8'],
        size: 21,
      },
    };

    expect(() => memoryLocationSchema.parse(obj)).toThrow();
  });

  it('kind must be present', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataTypes: [{ startOffset: 0, dataType: 'kInt' }],
      //@ts-ignore This is wrong on purpose
      data: {
        constant: '12',
        size: 12,
      },
    };

    expect(() => memoryLocationSchema.parse(obj)).toThrow();
  });

  it('bad random range does not pass', () => {
    // Throws ZodError if not valid

    const obj: MemoryLocationApi = {
      name: 'Array',
      alignment: 4,
      dataType: 'kInt',
      data: {
        kind: 'random',
        min: 8,
        max: 7,
        size: 4,
      },
    };

    expect(() => memoryLocationSchema.parse(obj)).toThrow();
  });
});
