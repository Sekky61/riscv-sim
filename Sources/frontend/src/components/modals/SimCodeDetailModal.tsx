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
  selectSimCodeModel,
  selectStatistics,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import ValueInformation from '@/components/simulation/ValueTooltip';
import {
  hexPadEven,
  instructionTypeName,
  isValidRegisterValue,
} from '@/lib/utils';

type SimCodeDetailModalProps = {
  simCodeId: number;
};

export const SimCodeDetailModal = ({ simCodeId }: SimCodeDetailModalProps) => {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const statistics = useAppSelector(selectStatistics);
  if (!q) throw new Error(`InstructionId ${simCodeId} not found`);
  if (!statistics) throw new Error('Statistics not found');
  const { simCodeModel, inputCodeModel, functionModel, args } = q;
  const isBranch = functionModel.instructionType === 'kJumpbranch';
  const pc = inputCodeModel.codeId * 4;

  const instructionStats = statistics.instructionStats[simCodeId];
  if (!instructionStats) throw new Error('Instruction stats not found');

  const predictionAccuracy =
    instructionStats.correctlyPredicted /
    (instructionStats.committedCount === 0
      ? 1
      : instructionStats.committedCount);

  return (
    <>
      <CardHeader>
        <CardTitle>{simCodeModel.renamedCodeLine}</CardTitle>
        <CardDescription>
          Detailed view of instruction #{simCodeModel.id}
        </CardDescription>
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
              <li>
                Address: {hexPadEven(pc)} ({pc})
              </li>
              <li>Committed: {instructionStats.committedCount} times</li>
            </ul>
            <h2 className='text-xl mt-2'>Timestamps</h2>
            <table>
              <thead>
                <tr>
                  <th>Stage</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Fetch</td>
                  <td>{renderTimestamp(simCodeModel.fetchId)}</td>
                </tr>
                <tr>
                  <td>Issue</td>
                  <td>{renderTimestamp(simCodeModel.issueWindowId)}</td>
                </tr>
                <tr>
                  <td>Result Ready</td>
                  <td>{renderTimestamp(simCodeModel.readyId)}</td>
                </tr>
                <tr>
                  <td>Commit</td>
                  <td>{renderTimestamp(simCodeModel.commitId)}</td>
                </tr>
              </tbody>
            </table>
            <h2 className='text-xl mt-2'>Flags</h2>
            <table>
              <thead>
                <tr>
                  <th>Flag</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Speculative</td>
                  <td>{renderFlag(simCodeModel.isSpeculative)}</td>
                </tr>
                <tr>
                  <td>Busy</td>
                  <td>{renderFlag(simCodeModel.isBusy)}</td>
                </tr>
                <tr>
                  <td>Ready To Execute</td>
                  <td>{renderFlag(simCodeModel.readyToExecute)}</td>
                </tr>
                <tr>
                  <td>Ready To Commit</td>
                  <td>{renderFlag(simCodeModel.readyToBeCommitted)}</td>
                </tr>
                <tr>
                  <td>Finished</td>
                  <td>{renderFlag(simCodeModel.isFinished)}</td>
                </tr>
                <tr>
                  <td>Failed</td>
                  <td>{renderFlag(simCodeModel.hasFailed)}</td>
                </tr>
              </tbody>
            </table>
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
                    Branch Result:{' '}
                    {simCodeModel.branchPredicted ? 'Branch' : 'Do not branch'}
                  </li>
                  <li>Prediction target: {simCodeModel.branchTarget}</li>
                  <li>
                    Prediction Accuracy: {instructionStats.committedCount}
                  </li>
                </ul>
              </>
            )}
            <h2 className='text-xl mt-2'>Exception Raised</h2>
            <p>
              {simCodeModel.exception ? 'Yes' : 'No'}
              {simCodeModel.exception?.exceptionMessage}
            </p>
          </div>
        </div>
      </CardContent>
    </>
  );
};

/**
 * Render timestamp. If the timestamp is negative, render a 'N/A' string.
 */
const renderTimestamp = (timestamp: number) =>
  timestamp >= 0 ? timestamp : 'N/A';

/**
 * Render a flag.
 */
const renderFlag = (flag: boolean) => (flag ? 'Yes' : 'No');
