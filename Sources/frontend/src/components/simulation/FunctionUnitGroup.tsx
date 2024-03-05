/**
 * @file    FunctionUnitGroup.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying a group of functional units (e.g. ALU)
 *
 * @date    11 November 2023, 17:00 (created)
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
  selectArithmeticFunctionUnitBlocks,
  selectBranchFunctionUnitBlocks,
  selectFpFunctionUnitBlocks,
  selectMemoryAccessUnitBlocks,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { Badge } from '@/components/base/ui/badge';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { AbstractFunctionUnitBlock } from '@/lib/types/cpuApi';
import { DividedBadge } from '@/components/DividedBadge';

type FUType = 'alu' | 'fp' | 'branch' | 'memory';

export type FunctionUnitGroupProps = {
  type: FUType;
};

/**
 * A component for displaying all functional units of a given type.
 */
export default function FunctionUnitGroup({ type }: FunctionUnitGroupProps) {
  const fus = useAppSelector(selectors[type]);
  if (!fus) return null;

  // TODO: has no limit
  return (
    <>
      {fus.map((fu) => {
        return <FU fu={fu} key={fu.functionUnitId} type={type} />;
      })}
    </>
  );
}

type FUProps = {
  type: FUType;
  fu: AbstractFunctionUnitBlock;
};

function FU({ type, fu }: FUProps) {
  const { name, className } = fuInfo[type];
  const displayCounter = fu.counter;

  return (
    <Block
      title={fu.description.name || name}
      stats={
        <div className='flex gap-2'>
          <div className='w-6 shrink-0'>{`${displayCounter}/${fu.delay}`}</div>
          <div className='flex gap-1 overflow-auto snap-x'>
            {fu.description.operations?.map((op) => (
              <DividedBadge key={op.name}>
                <div className='capitalize'>{op.name}</div>
                <div>{op.latency}</div>
              </DividedBadge>
            ))}
          </div>
        </div>
      }
      className={clsx(className, 'w-issue')}
    >
      <InstructionField instructionId={fu.simCodeModel} />
    </Block>
  );
}

const selectors = {
  alu: selectArithmeticFunctionUnitBlocks,
  fp: selectFpFunctionUnitBlocks,
  branch: selectBranchFunctionUnitBlocks,
  memory: selectMemoryAccessUnitBlocks,
} as const;

const fuInfo = {
  alu: {
    name: 'ALU',
    className: 'aluFu',
  },
  fp: {
    name: 'FP',
    className: 'fpFu',
  },
  branch: {
    name: 'Branch',
    className: 'branchFu',
  },
  memory: {
    name: 'Memory Access',
    className: 'memoryFu',
  },
} as const;
