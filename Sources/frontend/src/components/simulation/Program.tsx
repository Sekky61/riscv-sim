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

import React, { useEffect } from 'react';

import { getArrayItems } from '@/lib/cpuState/util';
import { selectFetch, selectProgram } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { InputCodeModel } from '@/lib/types/cpuApi';

import Block from '@/components/simulation/Block';

export default function Program() {
  const pcRef = React.useRef<HTMLDivElement>(null);
  const program = useAppSelector(selectProgram);
  const fetch = useAppSelector(selectFetch);

  useEffect(() => {
    if (!pcRef.current) {
      return;
    }
    pcRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });

  if (!program || !fetch) return null;

  const code = program.code;
  const pc = fetch.pc / 4;

  // Sort instructions and labels together to a single array

  // COPY code
  const codeOrder: Array<InputCodeModel | string> = [...code];

  // A thin red line
  const pcPointer = (
    <div ref={pcRef} className='relative mr-8 flex items-center'>
      <div className='absolute w-full h-0.5 bg-red-500 rounded-full' />
      <div
        className='absolute bg-red-500 text-white text-xs rectangle h-4 pl-1 -ml-2'
        title={`PC: ${fetch.pc}`}
      >
        <div className='relative rectangle'>PC</div>
      </div>
    </div>
  );

  // For each label, insert it before the instruction it points to
  Object.entries(program.labels).forEach(([labelName, idx]) => {
    let insertIndex = codeOrder.findIndex(
      (instruction) =>
        typeof instruction !== 'string' && instruction.codeId === idx,
    );
    if (insertIndex === -1) {
      insertIndex = codeOrder.length;
    }
    codeOrder.splice(insertIndex, 0, labelName);
  });

  return (
    <Block title='Program'>
      <div className='flex h-[600px] flex-col gap-1 overflow-y-auto'>
        {codeOrder.map((instructionOrLabel) => {
          if (typeof instructionOrLabel === 'string') {
            return (
              <div key={instructionOrLabel} className='font-bold'>
                {instructionOrLabel}:
              </div>
            );
          }
          const isPointedTo = instructionOrLabel.codeId === pc;
          // Instruction
          return (
            <div key={instructionOrLabel.codeId}>
              {isPointedTo && pcPointer}
              <ProgramInstruction instruction={instructionOrLabel} />
            </div>
          );
        })}
      </div>
    </Block>
  );
}

function ProgramInstruction({ instruction }: { instruction: InputCodeModel }) {
  const model = instruction.instructionFunctionModel;

  const argValues = getArrayItems(instruction.arguments);
  const modelArgs = getArrayItems(model.arguments);

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

  return (
    <div className='ml-5 font-mono text-sm'>
      {model.name}
      {argsValues.map((arg, idx) => {
        return (
          <span key={arg.name}>
            {idx === 0 ? ' ' : ','}
            {arg.value}
          </span>
        );
      })}
    </div>
  );
}
