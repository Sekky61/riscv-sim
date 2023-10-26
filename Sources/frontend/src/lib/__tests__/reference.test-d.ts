/**
 * @file    reference.test-d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for some complex typescript types
 *
 * @date    26 September 2023, 17:00 (created)
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

import { expectType } from 'tsd';

import { Resolved } from '@/lib/cpuState/util';
import { MaybeReference } from '@/lib/types/cpuApi';

function f<T>(): Resolved<T> {
  // @ts-expect-error just a dumb function to test the type
  return {} as unknown;
}

describe('The Resolved type inference', () => {
  expectType<string>(f<string>());
  expectType<null>(f<null>());
  expectType<undefined>(f<undefined>());
  expectType<number>(f<number>());
  expectType<boolean>(f<boolean>());

  expectType<string[]>(f<string[]>());

  expectType<{ a: string }>(f<{ a: string }>());

  expectType<number>(f<MaybeReference<number>>());

  type A = {
    b: number;
    c: MaybeReference<number>;
  };

  expectType<{ b: number; c: number }>(f<A>());
  expectType<{ b: number; c: number } | number>(f<A | number>());

  expectType<Array<{ b: number; c: number }>>(f<Array<A>>());

  it('works', () => {
    expect(true).toBe(true);
  });
});
