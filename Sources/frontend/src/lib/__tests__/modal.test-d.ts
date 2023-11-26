/**
 * @file    modal.test-d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Tests for some complex typescript types used in modal slice
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

import { expectNotType, expectType } from 'tsd';

import { ModalProps, ModalType } from '@/components/modals/ModalRoot';
import { RobDetailsModalProps } from '@/components/modals/RobDetailsModal';
import { SaveIsaChangesModalProps } from '@/components/modals/SaveIsaChangesModal';

function f<T extends ModalType>(): ModalProps<T> {
  // @ts-expect-error just a dumb function to test the type
  return {} as unknown;
}

describe('The ModalType and ModalProps type inference', () => {
  expectType<SaveIsaChangesModalProps>(f<'CONFIRM_ISA_CHANGES_MODAL'>());
  expectNotType<RobDetailsModalProps>(f<'CONFIRM_ISA_CHANGES_MODAL'>());

  it('works', () => {
    expect(true).toBe(true);
  });
});
