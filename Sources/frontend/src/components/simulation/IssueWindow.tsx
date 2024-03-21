/**
 * @file    IssueWindow.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Issue Window
 *
 * @date    06 November 2023, 23:00 (created)
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

'use client';

import clsx from 'clsx';

import {
  type ParsedArgument,
  getValue,
  selectAluIssueWindowBlock,
  selectBranchIssueWindowBlock,
  selectFpIssueWindowBlock,
  selectLoadStoreIssueWindowBlock,
  selectSimCodeModel,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import type { Reference } from '@/lib/types/cpuApi';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import { DividedBadge } from '@/components/DividedBadge';
import { useHighlight } from '@/components/HighlightProvider';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import {
  Tooltip,
  TooltipContent,
  TooltipPortal,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import { ShortValueInformation } from '@/components/simulation/ValueTooltip';

type IssueType = 'alu' | 'fp' | 'branch' | 'ls';

export type IssueWindowProps = {
  type: IssueType;
};

function getSelector(type: IssueType) {
  if (type === 'alu') return selectAluIssueWindowBlock;
  if (type === 'fp') return selectFpIssueWindowBlock;
  if (type === 'branch') return selectBranchIssueWindowBlock;
  if (type === 'ls') return selectLoadStoreIssueWindowBlock;
  throw new Error(`Invalid type ${type}`);
}

function getTitle(type: IssueType) {
  if (type === 'alu') return 'ALU Issue Window';
  if (type === 'fp') return 'FP Issue Window';
  if (type === 'branch') return 'Branch Issue Window';
  if (type === 'ls') return 'L/S Issue Window';
  throw new Error(`Invalid type ${type}`);
}

function getGridClassName(type: IssueType) {
  if (type === 'alu') return 'aluIssue';
  if (type === 'fp') return 'fpIssue';
  if (type === 'branch') return 'branchIssue';
  if (type === 'ls') return 'lsIssue';
  throw new Error(`Invalid type ${type}`);
}

export default function IssueWindow({ type }: IssueWindowProps) {
  const issue = useAppSelector(getSelector(type));
  const descriptions = useBlockDescriptions();

  if (!issue) return null;
  const instrCount = issue.issuedInstructions.length;

  const title = getTitle(type);

  const stats = (
    <>
      <div className='flex'>
        <DividedBadge>
          <div>Instructions</div>
          <div>{instrCount}</div>
        </DividedBadge>
      </div>
    </>
  );

  const cls = clsx(getGridClassName(type), 'w-issue h-96');

  // TODO: Is this limit suitable?
  return (
    <Block
      title={title}
      stats={stats}
      className={cls}
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Issue Window</DialogTitle>
            <DialogDescription>
              {descriptions.issue?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
    >
      <InstructionListDisplay
        columns={3}
        instructions={issue.issuedInstructions}
        legend={
          <div className='grid grid-cols-subgrid col-span-3 sticky top-0 bg-inherit'>
            <div>Instruction</div>
            <div>Arg 1</div>
            <div>Arg 2</div>
          </div>
        }
        instructionRenderer={(instruction, i) => (
          <IssueWindowItem simCodeId={instruction} key={`instr_${i}`} />
        )}
      />
    </Block>
  );
}

type IssueWindowItemProps = {
  simCodeId: Reference | null;
};

/**
 * Displays a single item in the Issue Window
 */
export function IssueWindowItem({ simCodeId }: IssueWindowItemProps) {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  if (!q) {
    return (
      <div className='col-span-3'>
        <InstructionField instructionId={simCodeId} />
      </div>
    );
  }

  const { args } = q;

  let arg1 = null;
  let arg2 = null;
  // TODO fmadd has 3 arguments, what to do though?
  for (const arg of args) {
    if (arg.origArg.name !== 'rd') {
      if (arg1 === null) {
        arg1 = arg;
      } else {
        arg2 = arg;
      }
    }
  }

  return (
    <div className='flex gap-1'>
      <InstructionField instructionId={simCodeId} />
      <div className='min-w-[40px] flex justify-center items-center'>
        <ArgumentTableCell arg={arg1} />
      </div>

      <div className='min-w-[40px] flex justify-center items-center'>
        <ArgumentTableCell arg={arg2} />
      </div>
    </div>
  );
}

type ArgumentTableCellProps = {
  arg: ParsedArgument | null;
};

/**
 * Displays a single parameter of a specific instruction.
 * If the value is valid, it shows the value in green.
 * It displays the register name if not valid.
 */
export function ArgumentTableCell({ arg }: ArgumentTableCellProps) {
  const { setHighlightedRegister } = useHighlight();
  const idToHighlight = arg?.register?.name || arg?.origArg.constantValue?.bits;

  const item = arg && getValue(arg);
  const registerName = arg?.register?.name;
  const valid = arg?.valid;

  const itemStyle = clsx(
    'register rounded flex px-2 h-full flex justify-center items-center min-w-[40px]',
    valid && 'text-green-700',
  );

  let text = '-';
  if (item) {
    if (arg.valid) {
      text = item.stringRepresentation;
    } else {
      text = registerName || arg?.origArg.stringValue || '-';
    }
  }

  const handleMouseEnter = () => {
    if (idToHighlight) {
      setHighlightedRegister(idToHighlight as string);
    }
  };

  const handleMouseLeave = () => {
    setHighlightedRegister(null);
  };

  return (
    <Tooltip>
      <TooltipTrigger>
        <div
          className={itemStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          data-register-id={idToHighlight}
        >
          {text}
        </div>
      </TooltipTrigger>
      <TooltipPortal>
        <TooltipContent>
          {item ? (
            <ShortValueInformation
              value={item}
              valid={arg.valid}
              register={arg.register}
            />
          ) : (
            <div className='text-gray-400'>No value</div>
          )}
        </TooltipContent>
      </TooltipPortal>
    </Tooltip>
  );
}
