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
  type ParsedArgument,
  getValue,
  selectSimCodeModel,
  selectStatistics,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import type { InstructionFunctionModel, Reference } from '@/lib/types/cpuApi';

import { useHighlight } from '@/components/HighlightProvider';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/base/ui/dialog';
import {
  Tooltip,
  TooltipContent,
  TooltipPortal,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { BranchTable } from '@/components/prediction/BranchTable';
import {
  ShortValueInformation,
  ValueInformation,
} from '@/components/simulation/ValueTooltip';
import {
  formatFracPercentage,
  hexPadEven,
  instructionTypeName,
  isValidRegisterValue,
} from '@/lib/utils';

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
  const { setHighlightedInstruction } = useHighlight();
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const statistics = useAppSelector(selectStatistics);
  if (!q || simCodeId === null || statistics === undefined) {
    // Empty field
    // aria-disabled is set to true to prevent the button from being focused and to suppress the contrast warning
    return <EmptyInstructionField />;
  }

  const { args, inputCodeModel, functionModel } = q;

  const handleMouseEnter = () => {
    setHighlightedInstruction({
      simcode: simCodeId,
      inputcode: inputCodeModel.codeId,
    });
  };

  const handleMouseLeave = () => {
    setHighlightedInstruction(null);
  };

  // Tabindex and button for accessibility
  return (
    <Dialog>
      <DialogTrigger asChild>
        <button
          type='button'
          className='group instruction rounded-[6px] h-8 w-full font-mono px-2 text-left whitespace-nowrap overflow-hidden'
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          data-instruction-id={simCodeId}
        >
          <InstructionSyntax functionModel={functionModel} args={args} />
        </button>
      </DialogTrigger>
      <InstructionDetailPopup simCodeId={simCodeId} />
    </Dialog>
  );
}

/**
 * A reusable element for empty instruction field.
 * Displays a disabled button with the text 'empty'.
 */
export function EmptyInstructionField() {
  return (
    <button
      type='button'
      className='pointer-events-none group instruction rounded-[6px] h-8 w-full font-mono px-2 text-left whitespace-nowrap overflow-hidden'
      aria-disabled='true'
      tabIndex={-1}
    >
      <span className='text-gray-500'>empty</span>
    </button>
  );
}

export function InstructionDetailPopup({
  simCodeId,
}: { simCodeId: Reference | null }) {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const statistics = useAppSelector(selectStatistics);
  if (!q || simCodeId === null || statistics === undefined) {
    // Empty field
    return (
      <div className='instruction-bubble flex justify-center items-center px-2 font-mono'>
        <span className='text-gray-400' aria-disabled='true'>
          empty
        </span>
      </div>
    );
  }

  const { simCodeModel, args, inputCodeModel, functionModel } = q;
  const isBranch = functionModel.instructionType === 'kJumpbranch';
  const pc = inputCodeModel.codeId * 4;

  const instructionStats = statistics.instructionStats[inputCodeModel.codeId];

  // This can be null because of NOP, so clicking NOP does not show the dialog
  if (!instructionStats) {
    return null;
  }

  return (
    <DialogContent className='max-w-6xl max-h-screen'>
      <DialogHeader>
        <DialogTitle className='text-4xl'>
          <InstructionSyntax functionModel={functionModel} args={args} />
        </DialogTitle>
        <DialogDescription className='surface-variant'>
          Detailed view of instruction #{simCodeModel.id}
        </DialogDescription>
      </DialogHeader>
      <div
        className={clsx('grid gap-4', isBranch ? 'grid-cols-3' : 'grid-cols-2')}
      >
        <div>
          <h2>Operands</h2>
          <ul className='flex flex-col gap-4'>
            {args.map((operand) => {
              const value = getValue(operand);
              const valid = operand.register
                ? isValidRegisterValue(operand.register)
                : true;
              return (
                <li
                  key={operand.origArg.name}
                  className='text-sm border rounded-md p-4'
                >
                  {value && (
                    <ValueInformation
                      value={value}
                      valid={valid}
                      register={operand.register}
                    />
                  )}
                </li>
              );
            })}
          </ul>
        </div>
        <div className='flex flex-col gap-2'>
          <ul>
            <li>ID: {simCodeModel.id}</li>
            <li>Type: {instructionTypeName(inputCodeModel)}</li>
            <li>
              Address: {hexPadEven(pc)} ({pc})
            </li>
            <li>Committed: {instructionStats.committedCount} times</li>
            <li>
              Exception Raised: {simCodeModel.exception ? 'Yes -' : 'No'}{' '}
              {simCodeModel.exception?.exceptionMessage}
            </li>
          </ul>
          <h2>Timestamps</h2>
          <table>
            <thead>
              <tr>
                <th>Stage</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Fetch</td>
                <td>{renderTimestamp(simCodeModel.fetchId)}</td>
              </tr>
              <tr>
                <td>Issue</td>
                <td>{renderTimestamp(simCodeModel.issueWindowId)}</td>
              </tr>
              <tr>
                <td>Result Ready</td>
                <td>{renderTimestamp(simCodeModel.readyId)}</td>
              </tr>
              <tr>
                <td>Commit</td>
                <td>{renderTimestamp(simCodeModel.commitId)}</td>
              </tr>
            </tbody>
          </table>
          <h2>Flags</h2>
          <table>
            <thead>
              <tr>
                <th>Flag</th>
                <th>Value</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Speculative</td>
                <td>{renderFlag(simCodeModel.isSpeculative)}</td>
              </tr>
              <tr>
                <td>Busy</td>
                <td>{renderFlag(simCodeModel.isBusy)}</td>
              </tr>
              <tr>
                <td>Ready To Execute</td>
                <td>{renderFlag(simCodeModel.readyToExecute)}</td>
              </tr>
              <tr>
                <td>Ready To Commit</td>
                <td>{renderFlag(simCodeModel.readyToBeCommitted)}</td>
              </tr>
              <tr>
                <td>Finished</td>
                <td>{renderFlag(simCodeModel.isFinished)}</td>
              </tr>
              <tr>
                <td>Failed</td>
                <td>{renderFlag(simCodeModel.hasFailed)}</td>
              </tr>
            </tbody>
          </table>
        </div>
        {isBranch && (
          <div className='flex flex-col gap-2'>
            <h2>Branch</h2>
            <table>
              <thead>
                <tr>
                  <th>Flag</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Branch</td>
                  <td>
                    {functionModel.unconditionalJump
                      ? 'Unconditional'
                      : 'Conditional'}
                  </td>
                </tr>
                <tr>
                  <td>Prediction Accuracy</td>
                  <td>
                    {formatFracPercentage(
                      instructionStats.correctlyPredicted,
                      instructionStats.committedCount,
                    )}
                  </td>
                </tr>
              </tbody>
            </table>
            {simCodeModel.branchInfo && (
              <BranchTable branchInfo={simCodeModel.branchInfo} />
            )}
          </div>
        )}
      </div>
    </DialogContent>
  );
}

export interface InstructionArgumentProps {
  arg: ParsedArgument;
}

/**
 * Displays a single argument of an instruction.
 * Delegaltes to RegisterReference if the argument is a register.
 * Highlights the argument on hover, whether it is a register or not.
 */
function InstructionArgument({ arg }: InstructionArgumentProps) {
  const { setHighlightedRegister } = useHighlight();
  const value = getValue(arg);
  const idToHighlight = arg.register?.name ?? arg.origArg.stringValue;

  const handleMouseEnter = () => {
    setHighlightedRegister(idToHighlight);
  };

  const handleMouseLeave = () => {
    setHighlightedRegister(null);
  };

  // Add negative margin so the highlight is bigger
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span
          className='register rounded -m-1 p-1'
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          data-register-id={idToHighlight}
        >
          {arg.origArg.stringValue}
        </span>
      </TooltipTrigger>
      <TooltipPortal>
      <TooltipContent>
        <ShortValueInformation
          value={value}
          valid={arg.valid}
          register={arg.register}
        />
      </TooltipContent>
      </TooltipPortal>
    </Tooltip>
  );
}

/**
 * Render timestamp. If the timestamp is negative, render a 'N/A' string.
 */
const renderTimestamp = (timestamp: number) =>
  timestamp >= 0 ? timestamp : 'N/A';

/**
 * Render a flag.
 */
const renderFlag = (flag: boolean) => (flag ? 'Yes' : 'No');

/**
 * Render the instruction syntax.
 * Wraps the arguments in a tooltip and makes them highlightable.
 */
function InstructionSyntax({
  functionModel,
  args,
}: {
  functionModel: InstructionFunctionModel;
  args: ParsedArgument[];
}) {
  // syntaxTemplate is an array of strings. Some of them are arguments, some are not. Example: ['addi ', 'rd', ', ', 'rs1', ', ', 'imm'].
  const formatSplit = functionModel.syntaxTemplate;
  // if a part matches an argument, wrap it in a tooltip
  return formatSplit.map((part, i) => {
    const arg = args.find((a) => a.origArg.name === part);
    const key = `${part}-${i}`;
    if (arg) {
      return <InstructionArgument arg={arg} key={key} />;
    }
    // Add z-index to make the argument highlight below the parentheses etc.
    return (
      <span className='relative z-10' key={key}>
        {part}
      </span>
    );
  });
}
