/**
 * @file    RegisterBlock.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Display the register file
 *
 * @date    08 March 2024, 18:00 (created)
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
  DecodedCacheLine,
  selectAllRegisters,
  selectCache,
  selectRegisterMap,
  selectRenameMap,
  selectSpecRegisterCount,
  selectSpecRegisters,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import { hexPadEven } from '@/lib/utils';
import clsx from 'clsx';
import { DividedBadge } from '@/components/DividedBadge';
import { RegisterModel } from '@/lib/types/cpuApi';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { useHighlight } from '@/components/HighlightProvider';

/**
 * Display the cache, lines are grouped by the index.
 * Valid lines are highlighted.
 */
export default function RegisterBlock() {
  // including speculative
  const registers = useAppSelector(selectRegisterMap);
  const renameBlock = useAppSelector(selectRenameMap);
  const descriptions = useBlockDescriptions();
  const specCount = useAppSelector(selectSpecRegisterCount);
  const specRegs = useAppSelector(selectSpecRegisters);

  if (!registers || !renameBlock || !specRegs) return null;

  // first dimension is the index, second is the associativity

  // TODO: sorts badly
  const specRegsSorted = Object.values(specRegs).sort();

  return (
    <Block
      title='Register File'
      stats={
        <div className='flex'>
          <DividedBadge>
            <div>Allocated Speculative Registers</div>
            <div>
              {renameBlock.allocatedSpeculativeRegistersCount}/{specCount}
            </div>
          </DividedBadge>
        </div>
      }
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Register File</DialogTitle>
            <DialogDescription>
              {descriptions.registerFile?.shortDescription}
            </DialogDescription>
          </DialogHeader>
          <div className='grid grid-cols-5'>
            {Object.values(specRegsSorted).map((tag) => {
              const reg = registers[tag];
              if (!reg) {
                throw new Error(`Register ${tag} not found`);
              }
              return <Register key={tag} register={reg} />;
            })}
          </div>
        </DialogContent>
      }
    >
      <div className='flex gap-12'>
        <div className='grid grid-cols-4 grid-rows-8 grid-flow-col'>
          {architecturalIntRegisters.map((regName, index) => {
            const reg = registers[regName];
            if (!reg) {
              throw new Error(`Register ${regName} not found`);
            }
            return <Register key={index} register={reg} />;
          })}
        </div>
        <div className='grid grid-cols-4 grid-rows-8 grid-flow-col'>
          {architecturalFloatRegisters.map((regName, index) => {
            const reg = registers[regName];
            if (!reg) {
              throw new Error(`Register ${regName} not found`);
            }
            return <Register key={index} register={reg} />;
          })}
        </div>
      </div>
    </Block>
  );
}

function Register({ register }: { register: RegisterModel }) {
  const { setHighlightedRegister } = useHighlight();

  const handleMouseEnter = () => {
    setHighlightedRegister(register.name);
  };

  const handleMouseLeave = () => {
    setHighlightedRegister(null);
  };

  const lastRename = register.renames[register.renames.length - 1];

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className='register px-2 py-1 rounded'
          data-register-id={register.name}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
        >
          <div className='flex justify-between'>
            <div className='font-bold font-mono w-8 py-0.5'>
              {register.name}
            </div>
            <div className='w-11 text-sm rounded border px-1 py-0.5'>
              {lastRename}
            </div>
          </div>
          <div className='w-24 font-mono'>
            {hexPadEven(register.value.bits)}
          </div>
        </div>
      </TooltipTrigger>
      <TooltipContent className='p-4'>
        {register.renames.length === 0 ? 'No renames' : 'Renames: '}
        <p className='w-48'>{register.renames.join(', ')}</p>
      </TooltipContent>
    </Tooltip>
  );
}

const architecturalIntRegisters = Array.from({ length: 32 }, (_, i) => `x${i}`);

const architecturalFloatRegisters = Array.from(
  { length: 32 },
  (_, i) => `f${i}`,
);
