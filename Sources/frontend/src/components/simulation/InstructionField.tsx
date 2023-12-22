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
  ParsedArgument,
  getValue,
  highlightSimCode,
  selectHighlightedSimCode,
  selectSimCodeModel,
  unhighlightSimCode,
} from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';
import {
  InputCodeArgument,
  Reference,
  RegisterDataContainer,
} from '@/lib/types/cpuApi';

import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import RegisterReference from '@/components/simulation/RegisterReference';
import ValueInformation from '@/components/simulation/ValueTooltip';

export type InstructionFieldProps = {
  instructionId: Reference | null;
};

/**
 * A component for displaying instruction information.
 * If no instruction is present, displays empty field.
 * The instruction info is loaded from the redux store based on the instructionId.
 */
export default function InstructionField({
  instructionId: simCodeId,
}: InstructionFieldProps) {
  const dispatch = useAppDispatch();
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const highlightedId = useAppSelector((state) =>
    selectHighlightedSimCode(state),
  );
  if (!q || simCodeId === undefined) {
    // Empty field
    return (
      <div className='instruction-bubble flex justify-center items-center px-2 font-mono'>
        <span className='text-gray-400'>empty</span>
      </div>
    );
  }

  const { simCodeModel, args } = q;
  const highlighted = highlightedId === simCodeId;

  const handleMouseEnter = () => {
    dispatch(highlightSimCode(simCodeId));
  };

  const handleMouseLeave = () => {
    dispatch(unhighlightSimCode(simCodeId));
  };

  const showDetail = () => {
    dispatch(
      openModal({
        modalType: 'SIMCODE_DETAILS_MODAL',
        modalProps: { simCodeId },
      }),
    );
  };

  function renderInstructionSyntax() {
    // simCodeModel.renamedCodeLine contains the instruction with renamed arguments, e.g. addi r1, r2, 5
    // Wrap the arguments in a tooltip and make them highlightable
    const formatSplit = simCodeModel.renamedCodeLine.split(/( |,|\)|\()/g);
    // if a part matches an argument, wrap it in a tooltip
    return formatSplit.map((part, i) => {
      // This may cause problems in the future, if the argument is not unique (e.g. addi sp, sp, -40)
      const arg = args.find((a) => a.origArg.stringValue === part);
      if (arg) {
        return (
          <InstructionArgument
            arg={arg}
            key={`${simCodeModel.renamedCodeLine}-${i}`}
          />
        );
      }
      // Add z-index to make the argument highlight below the parentheses etc.
      return <span className='relative z-10'>{part}</span>;
    });
  }

  const cls = clsx(
    'group instruction-bubble w-full font-mono px-2 text-left whitespace-nowrap',
    highlighted ? 'bg-gray-200' : '',
  );

  // Tabindex and button for accessibility
  return (
    <button
      type='button'
      className={cls}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      onClick={showDetail}
      tabIndex={0}
    >
      {renderInstructionSyntax()}
    </button>
  );
}

export interface InstructionArgumentProps {
  arg: ParsedArgument;
}

/**
 * Displays a single argument of an instruction.
 * Delegaltes to RegisterReference if the argument is a register.
 * Highlights the argument on hover.
 */
function InstructionArgument({ arg }: InstructionArgumentProps) {
  const value = getValue(arg);

  // Add negative margin so the highlight is bigger
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span className='relative rounded hover:bg-gray-300 -m-1 p-1'>
          {arg.origArg.stringValue}
        </span>
      </TooltipTrigger>
      <TooltipContent>
        <ValueInformation value={value} valid={arg.valid} />
      </TooltipContent>
    </Tooltip>
  );
}
