/**
 * @file    Program.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Block with program instructions
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
import React, { useEffect } from 'react';

import {
  selectFetch,
  selectInputCodeModelById,
  selectInstructionFunctionModelById,
  selectProgram,
  selectProgramWithLabels,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { Reference } from '@/lib/types/cpuApi';
import { ReactClassName } from '@/lib/types/reactTypes';
import { inputCodeAddress } from '@/lib/utils';

import Block from '@/components/simulation/Block';

/**
 * A block displaying the program instructions.
 * Labels are displayed more prominently. PC is rendered as a red line pointing before the instruction.
 */
export default function Program() {
  const pcRef = React.useRef<HTMLDivElement>(null);
  const program = useAppSelector(selectProgram);
  const fetch = useAppSelector(selectFetch);
  const codeOrder = useAppSelector(selectProgramWithLabels);

  // Scroll to PC on every render
  useEffect(() => {
    if (!pcRef.current) {
      return;
    }
    pcRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });

  if (!program || !fetch || !codeOrder) return null;

  const pc = fetch.pc / 4;

  // A thin red line
  const pcPointer = (
    <div ref={pcRef} className='relative w-full flex items-center'>
      <div className='absolute w-full h-0.5 bg-red-500 rounded-full' />
      <div
        className='absolute -left-6 bg-red-500 text-white text-xs rectangle h-4 pl-1'
        title={`PC: ${fetch.pc}`}
      >
        <div className='relative rectangle'>PC</div>
      </div>
    </div>
  );

  return (
    <Block
      title='Program'
      className='program justify-self-stretch self-stretch'
    >
      <div
        className='h-96 grid gap-1 overflow-y-auto pt-4'
        style={{ gridTemplateColumns: 'auto auto' }}
      >
        {codeOrder.map((instructionOrLabel) => {
          if (typeof instructionOrLabel === 'string') {
            return (
              <div
                key={`lab-${instructionOrLabel}`}
                className='font-bold text-sm col-span-2'
              >
                {instructionOrLabel}:
              </div>
            );
          }
          const isPointedTo = instructionOrLabel === pc;
          // Instruction
          return (
            <ProgramInstruction
              key={`ins-${instructionOrLabel}`}
              instructionId={instructionOrLabel}
              className='ml-6'
            >
              {isPointedTo && pcPointer}
            </ProgramInstruction>
          );
        })}
      </div>
    </Block>
  );
}

function ProgramInstruction({
  instructionId,
  className,
  children,
}: {
  instructionId: Reference;
  children?: React.ReactNode;
} & ReactClassName) {
  const instruction = useAppSelector((state) =>
    selectInputCodeModelById(state, instructionId),
  );

  const model = useAppSelector((state) =>
    selectInstructionFunctionModelById(
      state,
      instruction?.instructionFunctionModel ?? 0,
    ),
  );

  if (!instruction || !model) return null;

  const argValues = instruction.arguments;
  const modelArgs = model.arguments;

  const argsNames = [];
  for (const arg of modelArgs) {
    if (arg.silent) {
      continue;
    }
    argsNames.push(arg.name);
  }

  const argsValues = [];
  for (const argName of argsNames) {
    const arg = argValues.find((a) => a.name === argName);
    if (!arg) {
      throw new Error(
        `Argument ${argName} not found in instruction ${model.name}`,
      );
    }
    argsValues.push(arg);
  }

  // Id is mappable to address
  const address = inputCodeAddress(instructionId);

  const cls = clsx(className, 'font-mono text-sm');
  return (
    <>
      <span className='text-xs text-gray-600'>{address}</span>
      <span className={cls}>
        {children}
        <span title={model.interpretableAs}>{model.name}</span>
        {argsValues.map((arg, idx) => {
          return (
            <span key={arg.name}>
              {idx === 0 ? ' ' : ','}
              {arg.value}
            </span>
          );
        })}
      </span>
    </>
  );
}
