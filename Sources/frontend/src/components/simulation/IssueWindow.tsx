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

import {
  selectAluIssueWindowBlock,
  selectBranchIssueWindowBlock,
  selectFpIssueWindowBlock,
  selectLoadStoreIssueWindowBlock,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';

type IssueType = 'alu' | 'fp' | 'branch' | 'ls';

export type IssueWindowProps = {
  type: IssueType;
};

function getSelector(type: IssueType) {
  if (type == 'alu') return selectAluIssueWindowBlock;
  if (type == 'fp') return selectFpIssueWindowBlock;
  if (type == 'branch') return selectBranchIssueWindowBlock;
  if (type == 'ls') return selectLoadStoreIssueWindowBlock;
  throw new Error(`Invalid type ${type}`);
}

function getTitle(type: IssueType) {
  if (type == 'alu') return 'ALU Issue Window';
  if (type == 'fp') return 'FP Issue Window';
  if (type == 'branch') return 'Branch Issue Window';
  if (type == 'ls') return 'Load/Store Issue Window';
  throw new Error(`Invalid type ${type}`);
}

export default function IssueWindow({ type }: IssueWindowProps) {
  const issue = useAppSelector(getSelector(type));

  if (!issue) return null;

  const validity = issue.argumentValidityMap;
  const instructionIds = [];
  for (const key in validity) {
    instructionIds.push(key);
  }

  const title = getTitle(type);

  // TODO: has no limit
  return (
    <Block title={title}>
      <InstructionListDisplay
        instructions={instructionIds}
        instructionRenderer={(instruction) => (
          <InstructionField instructionId={instruction} />
        )}
      />
    </Block>
  );
}
