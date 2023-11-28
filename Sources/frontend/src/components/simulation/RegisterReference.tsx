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

import clsx from 'clsx';

import {
  highlightRegister,
  selectHighlightedRegister,
  selectRegisterById,
  unhighlightRegister,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { ReactClassName } from '@/lib/types/reactTypes';

import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import ValueInformation from '@/components/simulation/ValueTooltip';
import { isValidRegisterValue } from '@/lib/utils';

export type RegisterReferenceProps = {
  registerId: string;
  showValue?: boolean;
} & ReactClassName;

export default function RegisterReference({
  registerId,
  className,
  showValue = false,
}: RegisterReferenceProps) {
  const dispatch = useAppDispatch();
  const register = useAppSelector((state) =>
    selectRegisterById(state, registerId),
  );
  const highlightedId = useAppSelector(selectHighlightedRegister);

  if (!register) return null;

  const highlighted = highlightedId === registerId;
  const valid = isValidRegisterValue(register);

  let displayValue = registerId;
  if (showValue && valid) {
    displayValue = register.value.stringRepresentation ?? '???';
  }

  const cls = clsx(highlighted && 'bg-gray-200', className);
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div
            className={cls}
            onMouseEnter={() => {
              dispatch(highlightRegister(registerId));
            }}
            onMouseLeave={() => {
              dispatch(unhighlightRegister(registerId));
            }}
          >
            {displayValue}
          </div>
        </TooltipTrigger>
        <TooltipContent>
          <ValueInformation value={register.value} valid={valid} />
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
