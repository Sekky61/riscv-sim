/**
 * @file    BranchTable.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the state of a branch instruction
 *
 * @date    03 March 2024, 10:00 (created)
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

import { selectPredictorWidth } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { BranchInfo } from '@/lib/types/cpuApi';
import { hexPadEven } from '@/lib/utils';

type BranchTableProps = {
  branchInfo: BranchInfo;
};

export function BranchTable({ branchInfo }: BranchTableProps) {
  const predictorWidth = useAppSelector(selectPredictorWidth);

  if (predictorWidth === undefined) {
    return null;
  }

  return (
    <div>
      <table className='w-full'>
        <thead>
          <tr>
            <th colSpan={2}>Branch Prediction</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Predictor</td>
            <td>
              {predictorWidthToName(predictorWidth)} with state{' '}
              {branchInfo.predictorStateBeforePrediction}
            </td>
          </tr>
          <tr>
            <td>Condition</td>
            <td>{boolToTake(branchInfo.predictorVerdict)}</td>
          </tr>
          <tr>
            <td>Address</td>
            <td>{numberToUnknown(branchInfo.predictedTarget)}</td>
          </tr>
        </tbody>
      </table>
      <table className='w-full'>
        <thead>
          <tr>
            <th colSpan={2}>Branch result</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Condition</td>
            <td>{boolToTake(branchInfo.branchCondition)}</td>
          </tr>
          <tr>
            <td>Address</td>
            <td>{numberToUnknown(branchInfo.branchTarget)}</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

function predictorWidthToName(width: number) {
  if (width === 1) {
    return '1-bit';
  }
  if (width === 2) {
    return '2-bit';
  }
  return '0-bit';
}

function numberToUnknown(value: number | null) {
  if (value === null || value === -1) {
    return 'Unknown';
  }

  return `${hexPadEven(value ?? 0)} (${value})`;
}

function boolToTake(value?: boolean) {
  return value === undefined ? 'Unknown' : value ? 'Take' : 'Do not take';
}
