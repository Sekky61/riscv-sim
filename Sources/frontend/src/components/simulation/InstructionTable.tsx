/**
 * @file    InstructionTable.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying a table of instructions with details
 *
 * @date    14 November 2023, 12:00 (created)
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

import { useState } from 'react';

import { selectSimCodeModel } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import type { Reference } from '@/lib/types/cpuApi';

import { Button } from '@/components/base/ui/button';
import { Dialog, DialogTrigger } from '@/components/base/ui/dialog';
import { InstructionDetailPopup } from '@/components/simulation/InstructionField';
import { instructionTypeName } from '@/lib/utils';

type InstructionTableProps = {
  instructions: Reference[];
  pageSize?: number;
};

/**
 * Instruction table component.
 * One row for each instruction.
 * Pagination with configurable page size.
 *
 * Grid is used instead of table for better responsiveness and ability to use the row as a link.
 */
export default function InstructionTable({
  instructions,
  pageSize = 10,
}: InstructionTableProps) {
  const [page, setPage] = useState(0);

  const pages = Math.ceil(instructions.length / pageSize);

  const handlePrev = () => {
    setPage(Math.max(page - 1, 0));
  };

  const handleNext = () => {
    setPage(Math.min(page + 1, pages - 1));
  };

  const showButtons = pages > 1;

  return (
    <div className='flex flex-col'>
      <div className='grid grid-cols-4 gap-1 border instruction-table'>
        <div className='grid grid-cols-subgrid col-span-4 bg-slate-200'>
          <div>ID</div>
          <div>Mnemonic</div>
          <div>Type</div>
          <div>PC (Location)</div>
        </div>
        {instructions
          .slice(page * pageSize, (page + 1) * pageSize)
          .map((id) => (
            <InstructionRow key={id} instructionId={id} />
          ))}
      </div>
      {showButtons && (
        <div className='flex flex-row justify-between mt-2'>
          <div className='flex flex-row'>
            <Button onClick={handlePrev} disabled={page === 0}>
              {'<'}
            </Button>
          </div>
          <div className='flex flex-row'>
            <Button onClick={handleNext} disabled={page === pages - 1}>
              {'>'}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

type InstructionRowProps = {
  instructionId: Reference;
};

/**
 * Instruction row component.
 * Displays instruction details.
 */
function InstructionRow({ instructionId }: InstructionRowProps) {
  const q = useAppSelector((state) => selectSimCodeModel(state, instructionId));
  if (!q) throw new Error('Instruction not found');
  const { simCodeModel, inputCodeModel, functionModel } = q;

  const instructionType = instructionTypeName(functionModel);

  return (
    <Dialog>
      <DialogTrigger asChild>
        <div className='hover:bg-gray-200 hover:cursor-pointer grid grid-cols-subgrid col-span-4 odd:bg-slate-100'>
          <div>{simCodeModel.id}</div>
          <div>{functionModel.name}</div>
          <div>{instructionType}</div>
          <div>{inputCodeModel.codeId * 4}</div>
        </div>
      </DialogTrigger>
      <InstructionDetailPopup simCodeId={instructionId} />
    </Dialog>
  );
}
