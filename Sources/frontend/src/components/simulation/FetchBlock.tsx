/**
 * @file    Block.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Fetch block
 *
 * @date    24 October 2023, 10:00 (created)
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

import { selectFetch } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import { DividedBadge } from '@/components/DividedBadge';
import { Badge } from '@/components/base/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/base/ui/dialog';
import { PredictorGraphFromCodeId } from '@/components/prediction/PredictorGraph';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import InstructionTable from '@/components/simulation/InstructionTable';
import { BranchDetailDialog } from '@/components/simulation/PredictionBlock';
import { hexPadEven } from '@/lib/utils';
import { Expand } from 'lucide-react';
import { Fragment } from 'react';

/**
 * A component for displaying the Fetch block.
 */
export default function FetchBlock() {
  const fetchObject = useAppSelector(selectFetch);
  const descriptions = useBlockDescriptions();

  if (!fetchObject) return null;

  const fetchStats = (
    <>
      <div className='badge-container'>
        <DividedBadge>
          <div>PC</div>
          <div>{hexPadEven(fetchObject.pc)}</div>
        </DividedBadge>
        {fetchObject.stallFlag ? <DividedBadge>Stalled</DividedBadge> : null}
        <Dialog>
          <DialogTrigger>
            <DividedBadge>
              <div>Prediction</div>
              <Expand size={16} strokeWidth={1.5} />
            </DividedBadge>
          </DialogTrigger>
          <BranchDetailDialog />
        </Dialog>
      </div>
    </>
  );

  return (
    <Block
      title='Fetch Block'
      stats={fetchStats}
      className='fetch-position w-block'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Fetch Block</DialogTitle>
            <DialogDescription>
              {descriptions.fetch?.shortDescription}
            </DialogDescription>
          </DialogHeader>
          <table>
            <thead>
              <tr>
                <th>PC</th>
                <th>Number of ways</th>
                <th>Stall</th>
                <th>Branch follow limit</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{fetchObject.pc}</td>
                <td>{fetchObject.numberOfWays}</td>
                <td>{fetchObject.stallFlag ? 'Stalled' : 'Not stalled'}</td>
                <td>{fetchObject.branchFollowLimit}</td>
              </tr>
            </tbody>
          </table>
          <h2 className='text-xl mt-4 mb-2'>Buffer</h2>
          <InstructionTable instructions={fetchObject.fetchedCode} />
        </DialogContent>
      }
    >
      <InstructionListDisplay
        instructions={fetchObject.fetchedCode}
        totalSize={fetchObject.numberOfWays}
        columns={2}
        instructionRenderer={(codeModel, i) => (
          <div
            className='grid grid-cols-subgrid col-span-2'
            key={`instr_${codeModel}_${i}`}
          >
            <InstructionField instructionId={codeModel} />
            <PredictorGraphFromCodeId simCodeId={codeModel} />
          </div>
        )}
      />
    </Block>
  );
}
