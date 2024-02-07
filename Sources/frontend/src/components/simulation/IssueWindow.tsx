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

import clsx from 'clsx';

import {
  ParsedArgument,
  getValue,
  selectAluIssueWindowBlock,
  selectBranchIssueWindowBlock,
  selectFpIssueWindowBlock,
  selectLoadStoreIssueWindowBlock,
  selectRegisterById,
  selectSimCodeModel,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { Reference, RegisterDataContainer } from '@/lib/types/cpuApi';

import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import ValueInformation from '@/components/simulation/ValueTooltip';

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

  if (!issue) return null;
  const instrCount = issue.issuedInstructions.length;

  const title = getTitle(type);

  const stats = (
    <>
      <div>
        {instrCount} {instrCount === 1 ? 'instruction' : 'instructions'}
      </div>
    </>
  );

  const cls = clsx(getGridClassName(type), 'w-80 h-96');

  // TODO: Is this limit suitable?
  return (
    <Block title={title} stats={stats} className={cls}>
      <InstructionListDisplay
        columns={3}
        instructions={issue.issuedInstructions}
        legend={
          <>
            <div>Instruction</div>
            <div>Arg 1</div>
            <div>Arg 2</div>
          </>
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

  let item1: RegisterDataContainer | null = null;
  let item1Valid = false;
  let item2: RegisterDataContainer | null = null;
  let item2Valid = false;
  for (const arg of args) {
    if (arg.origArg.name !== 'rd') {
      if (item1 === null) {
        item1 = getValue(arg);
        item1Valid = arg.valid;
      } else {
        item2 = getValue(arg);
        item2Valid = arg.valid;
      }
    }
  }

  const item1Style = clsx(
    'instruction-bubble flex px-2',
    item1Valid && 'text-green-500',
    item1 === null && 'invisible',
  );
  const item2Style = clsx(
    'instruction-bubble flex px-2',
    item2Valid && 'text-green-500',
    item2 === null && 'invisible',
  );

  return (
    <>
      <InstructionField instructionId={simCodeId} />
      <Tooltip>
        <TooltipTrigger>
          <div className={item1Style}>{item1?.stringRepresentation ?? '-'}</div>
        </TooltipTrigger>
        <TooltipContent>
          {item1 ? (
            <ValueInformation value={item1} valid={item1Valid} />
          ) : (
            <div className='text-gray-400'>No value</div>
          )}
        </TooltipContent>
      </Tooltip>
      <Tooltip>
        <TooltipTrigger>
          <div className={item2Style}>{item2?.stringRepresentation ?? '-'}</div>
        </TooltipTrigger>

        <TooltipContent>
          {item2 ? (
            <ValueInformation value={item2} valid={item2Valid} />
          ) : (
            <div className='text-gray-400'>No value</div>
          )}
        </TooltipContent>
      </Tooltip>
    </>
  );
}
