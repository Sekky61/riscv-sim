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
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';

export default function ReorderBuffer() {
  const dispatch = useAppDispatch();
  const rob = useAppSelector(selectROB);

  if (!rob) return null;

  const used = rob.reorderQueue.length;
  const showLimit = 16;
  const showMore = used > showLimit;

  const robStats = (
    <>
      <div>
        Capacity: {used}/{rob.bufferSize}
      </div>
    </>
  );

  const handleMore = () => {
    dispatch(
      openModal({
        modalType: 'ROB_DETAILS_MODAL',
        modalProps: null,
      }),
    );
  };

  return (
    <Block title='Reorder Buffer' stats={robStats} handleMore={handleMore}>
      <InstructionListDisplay
        instructions={rob.reorderQueue}
        limit={showLimit}
        instructionRenderer={(item) => {
          const isPresent = item !== undefined;
          const isConfirmed = !(item?.reorderFlags.isSpeculative ?? false);
          return (
            <div className='relative'>
              <InstructionField instructionId={item?.simCodeModel} />
              {isPresent && isConfirmed && (
                <span className='absolute right-0 top-0 w-1 h-full bg-green-300' />
              )}
            </div>
          );
        }}
      />
      {showMore && (
        <div className='flex justify-center'>
          And {used - showLimit} more...
        </div>
      )}
    </Block>
  );
}
