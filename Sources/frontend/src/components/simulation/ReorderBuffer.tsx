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

import { selectROB } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import InstructionTable from '@/components/simulation/InstructionTable';

export default function ReorderBuffer() {
  const rob = useAppSelector(selectROB);

  if (!rob) return null;

  const used = rob.reorderQueue.length;

  const robStats = (
    <>
      <div>
        Capacity: {used}/{rob.bufferSize}
      </div>
    </>
  );

  return (
    <Block
      title='Reorder Buffer'
      stats={robStats}
      className='rob w-block h-96'
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reorder Buffer</DialogTitle>
            <DialogDescription>
              Detailed view of the Reorder Buffer
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
          if (simCodeModel === null) {
            return <InstructionField instructionId={null} key={`item_${i}`} />;
          }
          return (
            <div className='relative' key={`item_${i}`}>
              <InstructionField instructionId={simCodeModel} showSpeculative />
            </div>
          );
        }}
      />
    </Block>
  );
}
