/**
 * @file    InstructionField.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying instruction information
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

import clsx from 'clsx';

import {
  selectInputCodeModelById,
  selectRegisterById,
  selectSimCodeModelById,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { Reference } from '@/lib/types/cpuApi';
import { ReactChildren, ReactClassName } from '@/lib/types/reactTypes';

export type InstructionFieldProps = {
  instructionId?: Reference;
};

export default function InstructionField({
  instructionId,
}: InstructionFieldProps) {
  const simCodeId = instructionId === undefined ? 'INVALID' : instructionId;
  const instruction = useAppSelector((state) =>
    selectSimCodeModelById(state, simCodeId),
  );

  const inputCodeId =
    instruction?.inputCodeModel == undefined
      ? 'INVALID'
      : instruction?.inputCodeModel;
  const inputCodeModel = useAppSelector((state) =>
    selectInputCodeModelById(state, inputCodeId),
  );

  if (!instruction || !inputCodeModel) {
    return (
      <InstructionBubble className='flex justify-center px-2 py-1 font-mono'>
        <span className='text-gray-400'>empty</span>
      </InstructionBubble>
    );
  }

  const args = instruction.renamedArguments;

  return (
    <InstructionBubble className='flex justify-between items-center gap-4 font-mono px-2'>
      <InstructionName mnemonic={inputCodeModel.instructionName} />
      <div className='flex gap-2'>
        {args.map((arg) => (
          <InstructionArgument
            argName={arg.name}
            idOrLiteral={arg.value}
            key={arg.name}
          />
        ))}
      </div>
    </InstructionBubble>
  );
}

interface InstructionBubbleProps extends ReactClassName {
  children: ReactChildren;
}

export function InstructionBubble({
  children,
  className,
}: InstructionBubbleProps) {
  const cls = clsx('rounded-sm border h-8', className);
  return <div className={cls}>{children}</div>;
}

function InstructionName({ mnemonic }: { mnemonic: string }) {
  return (
    <div className='font-mono hover:cursor-pointer hover:underline leading-4'>
      {mnemonic}
    </div>
  );
}

export interface InstructionArgumentProps {
  argName: string;
  idOrLiteral: string;
}

function InstructionArgument({
  argName,
  idOrLiteral,
}: InstructionArgumentProps) {
  const register = useAppSelector((state) =>
    selectRegisterById(state, idOrLiteral),
  );

  const isRegister = register !== undefined;

  let displayText;
  let hoverText;

  if (isRegister) {
    displayText = register?.name;
    hoverText = `${register?.name} (${register?.value.bits})`;
  } else {
    displayText = idOrLiteral;
    hoverText = idOrLiteral;
  }

  return (
    <div
      className='rounded hover:bg-gray-200 w-6 h-6 flex justify-center items-center leading-4'
      title={hoverText}
    >
      {displayText}
    </div>
  );
}
