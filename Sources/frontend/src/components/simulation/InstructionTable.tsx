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

import { useState } from 'react';

import { selectSimCodeModel } from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';
import { Reference } from '@/lib/types/cpuApi';

import { Button } from '@/components/base/ui/button';

type InstructionTableProps = {
  instructions: Reference[];
  pageSize?: number;
};

/**
 * Instruction table component.
 * One row for each instruction.
 * Pagination with configurable page size.
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
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mnemonic</th>
            <th>Type</th>
            <th>PC (Location)</th>
          </tr>
        </thead>
        <tbody>
          {instructions
            .slice(page * pageSize, (page + 1) * pageSize)
            .map((id) => (
              <InstructionRow key={id} instructionId={id} />
            ))}
        </tbody>
      </table>
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
  const dispatch = useAppDispatch();
  if (!q) throw new Error('Instruction not found');
  const { simCodeModel, inputCodeModel } = q;

  let instructionType;
  switch (inputCodeModel.instructionTypeEnum) {
    case 'kIntArithmetic':
      instructionType = 'Arithmetic (int)';
      break;
    case 'kFloatArithmetic':
      instructionType = 'Arithmetic (float)';
      break;
    case 'kLoadstore':
      instructionType = 'Load/Store';
      break;
    case 'kJumpbranch':
      instructionType = 'Branch';
      break;
  }

  const showDetail = () => {
    dispatch(
      openModal({
        modalType: 'SIMCODE_DETAILS_MODAL',
        modalProps: { simCodeId: instructionId },
      }),
    );
  };

  return (
    <tr
      onClick={showDetail}
      onKeyUp={showDetail}
      className='hover:bg-gray-100 hover:cursor-pointer'
    >
      <td>{simCodeModel.id}</td>
      <td>{inputCodeModel.instructionName}</td>
      <td>{instructionType}</td>
      <td>{simCodeModel.savedPc}</td>
    </tr>
  );
}
