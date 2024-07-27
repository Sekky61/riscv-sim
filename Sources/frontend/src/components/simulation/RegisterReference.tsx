/**
 * @file    RegisterReference.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying register name with additional information
 *
 * @date    27 November 2023, 18:00 (created)
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
  type ParsedArgument,
  selectRegisterById,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { ArgumentTableCell } from '@/components/simulation/IssueWindow';
import { isValidRegisterValue } from '@/lib/utils';

export type RegisterReferenceProps = {
  registerId: string;
};

export default function RegisterReference({
  registerId,
}: RegisterReferenceProps) {
  const register = useAppSelector((state) =>
    selectRegisterById(state, registerId),
  );

  if (!register) return null;

  const valid = isValidRegisterValue(register);

  let displayValue = registerId;
  if (valid) {
    displayValue = register.value.stringRepresentation ?? '???';
  }

  const arg: ParsedArgument = {
    register,
    valid,
    origArg: {
      constantValue: null,
      name: registerId,
      stringValue: displayValue,
      registerValue: register.name,
    },
    value: register.value,
  };

  return <ArgumentTableCell arg={arg} />;
}
