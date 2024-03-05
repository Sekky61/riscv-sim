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

'use client';

import React from 'react';

import {
  selectFetch,
  selectInputCodeModelById,
  selectInstructionFunctionModelById,
  selectProgram,
  selectProgramWithLabels,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import {
  InputCodeArgument,
  InstructionFunctionModel,
  Reference,
} from '@/lib/types/cpuApi';
import { hexPadEven, inputCodeAddress } from '@/lib/utils';

import Block from '@/components/simulation/Block';
import { selectEntryPoint } from '@/lib/redux/compilerSlice';
import { DividedBadge } from '@/components/DividedBadge';

/**
 * A block displaying the program instructions.
 * Labels are displayed more prominently. PC is rendered as a red line pointing before the instruction.
 */
export default function Program() {
  const pcRef = React.useRef<HTMLDivElement>(null);
  const containerRef = React.useRef<HTMLDivElement>(null);
  const program = useAppSelector(selectProgram);
  const fetch = useAppSelector(selectFetch);
  const codeOrder = useAppSelector(selectProgramWithLabels);
  const entryPoint = useAppSelector(selectEntryPoint);

  // Scroll to PC on every render using scrollTop, because scrollIntoView makes the whole page jump
  if (pcRef.current && containerRef.current) {
    const pcTop = pcRef.current.offsetTop;
    const containerTop = containerRef.current.offsetTop;
    const containerHeight = containerRef.current.offsetHeight;
    containerRef.current.scrollTop = pcTop - containerTop - containerHeight / 2;
  }

  if (!program || !fetch || !codeOrder) return null;

  const pc = fetch.pc / 4;

  // A thin red line
  const pcPointer = (
    <div ref={pcRef} className='relative w-full flex items-center'>
      <div className='absolute w-full h-0.5 bg-tertiary rounded-full' />
      <div className='absolute -left-6 bg-tertiary text-xs rectangle h-4 pl-1'>
        <div className='relative rectangle text-onTertiary pt-[1px]'>PC</div>
      </div>
    </div>
  );

  let entryPointPretty = entryPoint;
  if (typeof entryPoint === 'number') {
    entryPointPretty = hexPadEven(entryPoint);
  }

  return (
    <Block
      title='Program'
      className='program justify-self-stretch self-stretch w-block h-full'
      stats={
        <div className='flex'>
          <DividedBadge>
            <div>Entry Point</div>
            <div>{entryPointPretty}</div>
          </DividedBadge>
        </div>
      }
    >
      <div className='flex-1 relative surface-container-lowest rounded-[8px]'>
        <div
          className='absolute inset-x-0 top-0 max-h-full grid gap-y-1 gap-x-7 overflow-y-auto p-[4px] pt-4 font-mono'
          style={{ gridTemplateColumns: 'auto auto' }}
          ref={containerRef}
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
              >
                {isPointedTo && pcPointer}
              </ProgramInstruction>
            );
          })}
        </div>
      </div>
    </Block>
  );
}

type ProgramInstructionProps = {
  instructionId: Reference;
  showAddress?: boolean;
  children?: React.ReactNode;
};

/**
 * Used here in program block and in stats
 */
export function ProgramInstruction({
  instructionId,
  children,
  showAddress = true,
}: ProgramInstructionProps) {
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

  // Id is mappable to address
  const address = inputCodeAddress(instructionId);

  return (
    <>
      {showAddress && (
        <div className='text-xs text-gray-600 justify-self-end self-center'>
          {address}
        </div>
      )}
      <div
        className='rounded-sm text-sm inputcodemodel'
        data-inputcode-id={instructionId}
      >
        {children}
        <InstructionSyntax functionModel={model} args={instruction.arguments} />
      </div>
    </>
  );
}

/**
 * Renders the syntax of an instruction.
 * Bit of code duplication, but ok.
 */
function InstructionSyntax({
  functionModel,
  args,
}: {
  functionModel: InstructionFunctionModel;
  args: InputCodeArgument[];
}) {
  // syntaxTemplate is an array of strings. Some of them are arguments, some are not. Example: ['addi ', 'rd', ', ', 'rs1', ', ', 'imm'].
  const formatSplit = functionModel.syntaxTemplate;
  // if a part matches an argument, wrap it in a tooltip
  return formatSplit.map((part, i) => {
    const arg = args.find((a) => a.name === part);
    const key = `${part}-${i}`;
    const token = arg?.stringValue ?? part;
    return <span key={key}>{token}</span>;
  });
}
