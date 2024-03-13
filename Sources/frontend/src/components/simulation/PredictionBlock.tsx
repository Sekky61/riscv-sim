/**
 * @file    BranchBlock.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Branch Block - BTB and PHT
 *
 * @date    21 November 2023, 12:00 (created)
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

import {
  selectBranchTargetBuffer,
  selectFetch,
  selectGShare,
  selectGlobalHistoryRegister,
  selectPatternHistoryTable,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { PredictorIcon } from '@/components/prediction/PredictorIcon';
import Block from '@/components/simulation/Block';
import type { GlobalHistoryRegister } from '@/lib/types/cpuApi';
import { useBlockDescriptions } from '@/components/BlockDescriptionContext';

export function BranchDetailDialog() {
  const btb = useAppSelector(selectBranchTargetBuffer);
  const ghr = useAppSelector(selectGlobalHistoryRegister);
  const pht = useAppSelector(selectPatternHistoryTable);
  const fetch = useAppSelector(selectFetch);
  const gshare = useAppSelector(selectGShare);
  const descriptions = useBlockDescriptions();

  if (!btb || !ghr || !pht || !fetch || !gshare) return null;

  // array of size, all elements pointing to defaultPredictor
  const predictors = Array.from(
    { length: pht.size },
    () => pht.defaultPredictor,
  );
  for (const [key, value] of Object.entries(pht.predictorMap)) {
    predictors[key as unknown as number] = value;
  }

  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{descriptions.branchPredictor?.name}</DialogTitle>
        <DialogDescription>
          {descriptions.branchPredictor?.shortDescription}
        </DialogDescription>
      </DialogHeader>
      <div>
        <div className='flex pb-8'>
          <GhrVector ghr={ghr} used={gshare.useGlobalHistory} />
        </div>
        <div
          className='grid gap-4'
          style={{
            gridTemplateColumns: 'repeat(10, 2.5rem)',
          }}
        >
          {predictors.map((predictor, i) => (
            <div key={i}>
              <div className='flex justify-center'>{i}</div>
              <PredictorIcon
                state={predictor.state}
                width={predictor.bitWidth}
              />
            </div>
          ))}
        </div>
      </div>
    </DialogContent>
  );
}

type GhrVectorProps = {
  ghr: GlobalHistoryRegister;
  used: boolean;
};

function GhrVector({ ghr, used }: GhrVectorProps) {
  const latest = ghr.shiftRegisters[ghr.shiftRegisters.length - 1];

  if (!latest) {
    throw new Error('GHR error state');
  }

  // Take the last size bits
  const vector = latest.shiftRegister
    .toString(2)
    .padStart(ghr.size, '0')
    .split('');

  return (
    <div className='flex gap-2'>
      <span>
        GHR - {used ? 'Used in prediction' : 'Not used in prediction'}
      </span>
      <div className='flex items-center border font-mono text-xs divide-x justify-start rounded-sm'>
        {vector.map((bit, i) => (
          <span key={i} className='py-0.5 px-1 text-center'>
            {bit}
          </span>
        ))}
      </div>
    </div>
  );
}
