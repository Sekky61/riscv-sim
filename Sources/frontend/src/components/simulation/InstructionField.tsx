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
  highlightSimCode,
  selectHighlightedSimCode,
  selectSimCodeModel,
  unhighlightSimCode,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';
import { InputCodeArgument, Reference } from '@/lib/types/cpuApi';
import { ReactChildren, ReactClassName } from '@/lib/types/reactTypes';

import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import RegisterReference from '@/components/simulation/RegisterReference';
import ValueInformation from '@/components/simulation/ValueTooltip';

export type InstructionFieldProps = {
  instructionId?: Reference;
};

export default function InstructionField({
  instructionId: simCodeId,
}: InstructionFieldProps) {
  const dispatch = useAppDispatch();
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const highlightedId = useAppSelector((state) =>
    selectHighlightedSimCode(state),
  );
  if (!q || simCodeId === undefined) {
    return (
      <InstructionBubble className='flex justify-center px-2 py-1 font-mono'>
        <span className='text-gray-400'>empty</span>
      </InstructionBubble>
    );
  }
  const { simCodeModel, inputCodeModel } = q;

  const args = simCodeModel.renamedArguments;
  const highlighted = highlightedId === simCodeId;

  const handleMouseEnter = () => {
    dispatch(highlightSimCode(simCodeId));
  };

  const handleMouseLeave = () => {
    dispatch(unhighlightSimCode(simCodeId));
  };

  const cls = clsx(
    'flex justify-between items-center gap-2 font-mono px-2 hover:cursor-pointer',
    highlighted ? 'bg-gray-200' : '',
  );

  const showDetail = () => {
    dispatch(
      openModal({
        modalType: 'SIMCODE_DETAILS_MODAL',
        modalProps: { simCodeId },
      }),
    );
  };

  return (
    <InstructionBubble
      className={cls}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      onClick={showDetail}
    >
      <InstructionName mnemonic={inputCodeModel.instructionName} />
      <div className='flex gap-2'>
        {args.map((arg) => (
          <InstructionArgument arg={arg} key={arg.name} />
        ))}
      </div>
    </InstructionBubble>
  );
}

interface InstructionBubbleProps extends ReactClassName {
  children: ReactChildren;
  [x: string]: unknown;
}

export function InstructionBubble({
  children,
  className,
  ...props
}: InstructionBubbleProps) {
  const cls = clsx('rounded-sm border h-8', className);
  return (
    <div className={cls} {...props}>
      {children}
    </div>
  );
}

function InstructionName({ mnemonic }: { mnemonic: string }) {
  return (
    <div className='font-mono hover:cursor-pointer hover:underline leading-4'>
      {mnemonic}
    </div>
  );
}

export interface InstructionArgumentProps {
  arg: InputCodeArgument;
}

function InstructionArgument({ arg }: InstructionArgumentProps) {
  const isRegister = arg.name.startsWith('r');
  const cls = clsx(
    'rounded hover:bg-gray-300 min-w-[2em] h-6 flex justify-center items-center leading-4',
  );

  if (isRegister) {
    return <RegisterReference registerId={arg.value} className={cls} />;
  }

  const displayText = arg.value;

  if (arg.constantValue === undefined) {
    throw new Error(
      `Constant value of argument ${arg.name} has undefined value`,
    );
  }

  // todo more general 
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div className={cls}>{displayText}</div>
        </TooltipTrigger>
        <TooltipContent>
          <ValueInformation value={arg.constantValue} valid={true} />
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
