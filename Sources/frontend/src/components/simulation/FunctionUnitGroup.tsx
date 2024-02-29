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
import { Fragment } from 'react';

import {
  selectArithmeticFunctionUnitBlocks,
  selectBranchFunctionUnitBlocks,
  selectFpFunctionUnitBlocks,
  selectMemoryAccessUnitBlocks,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { Badge } from '@/components/base/ui/badge';

export default function FunctionUnitGroup({ type }: FunctionUnitGroupProps) {
  const { name, className, selector } = getFuInfo(type);
  const fus = useAppSelector(selector);

  if (!fus) return null;

  // TODO: has no limit
  return (
    <>
      {fus.map((fu, i) => {
        const displayCounter = fu.simCodeModel === null ? 0 : fu.counter + 1;
        const id = fu.simCodeModel ?? null;
        return (
          <Fragment key={`${fu.description.name}-${fu.functionUnitId}`}>
            <Block
              title={fu.description.name || name}
              stats={
                <div className='flex gap-2'>
                  <div className='w-6 shrink-0'>{`${displayCounter}/${fu.delay}`}</div>
                  <div className='flex gap-1 overflow-auto snap-x'>
                    {fu.description.operations?.map((op, i) => (
                      <Badge
                        key={op.name}
                        variant='outline'
                        className='snap-start flex divide-x gap-1'
                      >
                        <div>{op.name}</div>
                        <div className='pl-1'>{op.latency}</div>
                      </Badge>
                    ))}
                  </div>
                </div>
              }
              className={clsx(
                className,
                'row-span-1 w-block',
                rowPosition[i + 1],
              )}
            >
              <InstructionField instructionId={id} />
            </Block>
          </Fragment>
        );
      })}
    </>
  );
}

function FU({ name, className, selector });

type FUType = 'alu' | 'fp' | 'branch' | 'memory';

const rowPosition = [
  'row-start-1',
  'row-start-2',
  'row-start-3',
  'row-start-4',
  'row-start-5',
  'row-start-6',
  'row-start-7',
];

export type FunctionUnitGroupProps = {
  type: FUType;
};

function getFuInfo(type: FUType) {
  switch (type) {
    case 'alu':
      return {
        selector: selectArithmeticFunctionUnitBlocks,
        name: 'ALU',
        className: 'aluFu',
      };
    case 'fp':
      return {
        selector: selectFpFunctionUnitBlocks,
        name: 'FP',
        className: 'fpFu',
      };
    case 'branch':
      return {
        selector: selectBranchFunctionUnitBlocks,
        name: 'Branch',
        className: 'branchFu',
      };
    case 'memory':
      return {
        selector: selectMemoryAccessUnitBlocks,
        name: 'Memory Access',
        className: 'memoryFu',
      };
    default:
      throw new Error(`Invalid type ${type}`);
  }
}
