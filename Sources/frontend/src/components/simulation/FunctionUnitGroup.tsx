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
  selectLoadStoreFunctionUnitBlocks,
  selectMemoryAccessUnitBlocks,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { DividedBadge } from '@/components/DividedBadge';
import { Block } from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import type {
  AbstractFunctionUnitBlock,
  MemoryAccessUnit,
} from '@/lib/types/cpuApi';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '../base/ui/dialog';
import InstructionTable from './InstructionTable';
import { useBlockDescriptions } from '../BlockDescriptionContext';

type FUType = 'alu' | 'fp' | 'branch' | 'ls' | 'memory';

export type FunctionUnitGroupProps = {
  type: FUType;
  children?: React.ReactNode;
};

/**
 * A component for displaying all functional units of a given type.
 */
export default function FunctionUnitGroup({
  type,
  children,
}: FunctionUnitGroupProps) {
  const fus = useAppSelector(selectors[type]);
  if (!fus) return null;

  // TODO: has no limit
  return (
    <>
      {fus.map((fu) => {
        return (
          <FU fu={fu} key={fu.functionUnitId} type={type}>
            {children}
          </FU>
        );
      })}
    </>
  );
}

type FUProps = {
  type: FUType;
  fu: AbstractFunctionUnitBlock;
  children?: React.ReactNode;
};

function FU({ type, fu, children }: FUProps) {
  const descriptions = useBlockDescriptions();
  const { name, className } = fuInfo[type];

  const handledBy = (fu as MemoryAccessUnit)?.transaction?.handledBy;

  return (
    <div className='relative'>
      {children}
      <Block
        title={fu.description.name || name}
        className={clsx(className, 'w-issue relative')}
        detailDialog={
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                Functional Unit {fu.description.name || name}
              </DialogTitle>
              <DialogDescription>
                {descriptions.functionUnit?.shortDescription}
              </DialogDescription>
            </DialogHeader>
            <InstructionTable
              instructions={fu.simCodeModel !== null ? [fu.simCodeModel] : []}
            />
          </DialogContent>
        }
      >
        <div className='flex gap-4 items-center'>
          <div className='flex gap-2 items-center'>
            <DividedBadge>
              <div>Cycle</div>
              <div>{`${fu.counter}/${fu.delay}`}</div>
            </DividedBadge>
            {handledBy && (
              <DividedBadge>
                <div className='whitespace-nowrap'>
                  {handledPretty[handledBy]}
                </div>
              </DividedBadge>
            )}
          </div>
          <InstructionField instructionId={fu.simCodeModel} />
        </div>
      </Block>
    </div>
  );
}

const handledPretty = {
  main_memory: 'Memory',
  cache: 'Cache',
  cache_with_miss: 'Cache Miss',
};

const selectors = {
  alu: selectArithmeticFunctionUnitBlocks,
  fp: selectFpFunctionUnitBlocks,
  branch: selectBranchFunctionUnitBlocks,
  ls: selectLoadStoreFunctionUnitBlocks,
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
  ls: {
    name: 'Load/Store',
    className: 'lsFu',
  },
  memory: {
    name: 'Memory Access',
    className: 'memoryFu',
  },
} as const;
