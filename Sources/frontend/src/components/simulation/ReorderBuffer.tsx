/**
 * @file    ReorderBuffer.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Reorder Buffer component
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

'use client';

import { selectROB, selectSimCodeModel } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import { DividedBadge } from '@/components/DividedBadge';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { Block } from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import InstructionTable from '@/components/simulation/InstructionTable';

export default function ReorderBuffer() {
  const rob = useAppSelector(selectROB);
  const descriptions = useBlockDescriptions();

  if (!rob) return null;

  const used = rob.reorderQueue.length;

  const robStats = (
    <>
      <div className='flex'>
        <DividedBadge>
          <div>Capacity</div>
          <div>
            {used}/{rob.bufferSize}
          </div>
        </DividedBadge>
      </div>
    </>
  );

  return (
    <Block
      title='Reorder Buffer'
      stats={robStats}
      className='w-block h-[500px]'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reorder Buffer</DialogTitle>
            <DialogDescription>
              {descriptions.rob?.shortDescription}
            </DialogDescription>
          </DialogHeader>
          <h2>Buffer</h2>
          <div>
            Capacity: {rob.reorderQueue.length}/{rob.bufferSize}
          </div>
          <InstructionTable instructions={rob.reorderQueue} />
        </DialogContent>
      }
    >
      <InstructionListDisplay
        instructions={rob.reorderQueue}
        totalSize={rob.bufferSize}
        instructionRenderer={(simCodeModel, i) => {
          return (
            <div className='flex items-center gap-1' key={`item_${i}`}>
              <InstructionField instructionId={simCodeModel} />
              <RobInfo instructionId={simCodeModel} />
            </div>
          );
        }}
      />
    </Block>
  );
}

export function RobInfo({ instructionId }: { instructionId: number | null }) {
  const q = useAppSelector((state) => selectSimCodeModel(state, instructionId));
  if (!q || instructionId === null) {
    // Empty field
    return null;
  }

  const { simCodeModel } = q;

  return (
    <div className='flex gap-0.5 items-start'>
      {simCodeModel.isSpeculative && (
        <DividedBadge title='Speculative'>S</DividedBadge>
      )}
      {simCodeModel.exception && (
        <DividedBadge
          title={`Exception: ${simCodeModel.exception.exceptionMessage}`}
        >
          Ex
        </DividedBadge>
      )}
    </div>
  );
}
