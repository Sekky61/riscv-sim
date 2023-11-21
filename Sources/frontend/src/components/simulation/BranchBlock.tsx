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

import {
  selectBranchTargetBuffer,
  selectFetch,
  selectGlobalHistoryRegister,
  selectPatternHistoryTable,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import { InstructionBubble } from '@/components/simulation/InstructionField';

export default function BranchBlock() {
  const btb = useAppSelector(selectBranchTargetBuffer);
  const ghr = useAppSelector(selectGlobalHistoryRegister);
  const pht = useAppSelector(selectPatternHistoryTable);
  const fetch = useAppSelector(selectFetch);

  if (!btb || !ghr || !pht || !fetch) return null;

  const pc = fetch.pc;
  const addresses = [];
  for (let i = 0; i < fetch.numberOfWays; i++) {
    addresses.push(pc + i * 4);
  }

  const btbEntries: React.ReactNode[] = [];
  // TODO: target label if exists
  addresses.forEach((address) => {
    const entry = btb.buffer[address];
    if (!entry) return null;
    btbEntries.push(
      <InstructionBubble key={address} className='flex'>
        <div className='w-1/2'>Address: {`0x${address.toString(16)}`}</div>
        <div className='w-1/2'>Target: {`0x${entry.target.toString(16)}`}</div>
      </InstructionBubble>,
    );
  });

  return (
    <Block title='Branch Block'>
      {btbEntries.length > 0 ? btbEntries : <div>No relevant entries</div>}
    </Block>
  );
}
