/**
 * @file    SimCodeDetailModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Modal for displaying details about the simcodemodel
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

import {
  getValue,
  selectRegisterById,
  selectSimCodeModel,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import ValueInformation from '@/components/simulation/ValueTooltip';
import { instructionTypeName, isValidRegisterValue } from '@/lib/utils';

type SimCodeDetailModalProps = {
  simCodeId: number;
};

export const SimCodeDetailModal = ({ simCodeId }: SimCodeDetailModalProps) => {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  if (!q) throw new Error(`InstructionId ${simCodeId} not found`);
  const { simCodeModel, inputCodeModel, functionModel, args } = q;
  const isBranch = functionModel.instructionType === 'kJumpbranch';

  return (
    <>
      <CardHeader>
        <CardTitle>
          Instruction {inputCodeModel.instructionName} #{simCodeModel.id}
        </CardTitle>
        <CardDescription>Detailed view</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='grid grid-cols-2 gap-4'>
          <div>
            <h1 className='text-2xl'>
              {inputCodeModel.instructionName.toUpperCase()}
            </h1>
            <ul>
              <li>Type: {instructionTypeName(inputCodeModel)}</li>
            </ul>
            <h2 className='text-xl mt-2'>Operands</h2>
            <ul className='flex flex-col gap-4'>
              {args.map((operand) => {
                const value = getValue(operand);
                const valid = operand.register
                  ? isValidRegisterValue(operand.register)
                  : true;
                return (
                  <li
                    key={operand.origArg.name}
                    className='text-sm border rounded-md p-4'
                  >
                    <span className='text-lg'>
                      {operand.origArg.name}: {value.stringRepresentation}
                    </span>
                    {value && <ValueInformation value={value} valid={valid} />}
                  </li>
                );
              })}
            </ul>
          </div>
          <div>
            <h1 className='text-2xl'>Runtime</h1>
            <ul>
              <li>ID: {simCodeModel.id}</li>
            </ul>
            <h2 className='text-xl mt-2'>Timestamps</h2>
            <ul>
              <li>Issued: {simCodeModel.issueWindowId}</li>
              <li>
                Commited:{' '}
                {simCodeModel.commitId === -1
                  ? 'No'
                  : `Yes (at ${simCodeModel.commitId})`}
              </li>
              <li>Function unit: {simCodeModel.functionUnitId}</li>
              <li>PC: {simCodeModel.savedPc}</li>
            </ul>
            <h2 className='text-xl mt-2'>Flags</h2>
            <ul>
              <li>Finished: {simCodeModel.finished ? 'Yes' : 'No'}</li>
              <li>Failed: {simCodeModel.hasFailed ? 'Yes' : 'No'}</li>
            </ul>
            {isBranch && (
              <>
                <h2 className='text-xl mt-2'>Branch</h2>
                <ul>
                  <li>
                    {functionModel.unconditionalJump
                      ? 'Unconditional'
                      : 'Conditional'}
                  </li>
                  <li>
                    Predicted: {simCodeModel.branchPredicted ? 'Yes' : 'No'}
                  </li>
                  <li>
                    Prediction target offset: {simCodeModel.branchTargetOffset}
                  </li>
                </ul>
              </>
            )}
          </div>
        </div>
      </CardContent>
    </>
  );
};
