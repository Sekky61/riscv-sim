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

import { selectFetch } from '@/lib/redux/cpustateSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';

/**
 * A component for displaying the Fetch block.
 */
export default function FetchBlock() {
  const dispatch = useAppDispatch();
  const fetchObject = useAppSelector(selectFetch);

  if (!fetchObject) return null;

  const handleMore = () => {
    dispatch(
      openModal({
        modalType: 'FETCH_DETAILS_MODAL',
        modalProps: null,
      }),
    );
  };

  const fetchStats = (
    <>
      <div>PC: {fetchObject.pc}</div>
      <div>{fetchObject.stallFlag ? 'Stalled' : null}</div>
    </>
  );

  return (
    <Block
      title='Fetch Block'
      stats={fetchStats}
      handleMore={handleMore}
      className='fetch-position'
    >
      <InstructionListDisplay
        instructions={fetchObject.fetchedCode}
        limit={fetchObject.numberOfWays}
        instructionRenderer={(codeModel) => (
          <InstructionField instructionId={codeModel} />
        )}
      />
    </Block>
  );
}
