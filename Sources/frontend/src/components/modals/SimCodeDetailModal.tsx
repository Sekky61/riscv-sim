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

import { selectSimCodeModel } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';

type SimCodeDetailModalProps = {
  instructionId: number;
};

export const SimCodeDetailModal = ({
  instructionId,
}: SimCodeDetailModalProps) => {
  const q = useAppSelector((state) => selectSimCodeModel(state, instructionId));
  if (!q) throw new Error('Instruction not found');
  const { simCodeModel, inputCodeModel, functionModel } = q;

  return (
    <>
      <CardHeader>
        <CardTitle>
          Instruction {inputCodeModel.instructionName} #{simCodeModel.id}
        </CardTitle>
        <CardDescription>Detailed view</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='grid grid-cols-2'>
          <div>
            <h1 className='text-2xl'>
              {inputCodeModel.instructionName.toUpperCase()}
            </h1>
            <ul>
              <li>Type: {inputCodeModel.instructionTypeEnum}</li>
            </ul>
            <h2 className='text-xl mt-2'>Operands</h2>
            <ul>
              {simCodeModel.renamedArguments.map((operand) => (
                <li key={operand.name}>
                  {operand.name} {operand.value}
                </li>
              ))}
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
              <li>Commited: {simCodeModel.commitId}</li>
              <li>Function unit: {simCodeModel.functionUnitId}</li>
              <li>Pulled: {simCodeModel.instructionBulkNumber}</li>
              <li>PC: {simCodeModel.savedPc}</li>
            </ul>
            <h2 className='text-xl mt-2'>Flags</h2>
            <ul>
              <li>Finished: {simCodeModel.finished ? 'Yes' : 'No'}</li>
              <li>Failed: {simCodeModel.hasFailed ? 'Yes' : 'No'}</li>
            </ul>
            <h2 className='text-xl mt-2'>Branch</h2>
            <ul>
              <li>
                {functionModel.unconditionalJump
                  ? 'Unconditional'
                  : 'Conditional'}
              </li>
              <li>Predicted: {simCodeModel.branchPredicted ? 'Yes' : 'No'}</li>
              <li>
                Prediction target offset: {simCodeModel.branchTargetOffset}
              </li>
            </ul>
          </div>
        </div>
      </CardContent>
    </>
  );
};
