/**
 * @file    LoadBuffer.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Load Buffer
 *
 * @date    06 November 2023, 23:00 (created)
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

import Block from '@/components/simulation/Block';
import { InstructionBubble } from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';

export default function LoadBuffer() {
  // const decode = useAppSelector(selectDecode);

  return (
    <Block title='Load Buffer'>
      <InstructionListDisplay
        instructions={[]}
        limit={4}
        instructionRenderer={(_instruction) => <LoadBufferItem />}
      />
    </Block>
  );
}

/**
 * Displays address and loaded value of a single item in the Load Buffer
 */
export function LoadBufferItem() {
  return (
    <InstructionBubble className='flex divide-x'>
      <div className='flex-grow flex justify-center items-center'>0x01</div>
      <div className='flex-grow flex justify-center items-center'>value</div>
    </InstructionBubble>
  );
}
