/**
 * @file    SimGrid.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The grid of the CPU blocks
 *
 * @date    30 January 2024, 22:00 (created)
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

import BranchBlock from '@/components/simulation/BranchBlock';
import CacheBlock from '@/components/simulation/CacheBlock';
import DecodeBlock from '@/components/simulation/DecodeBlock';
import FetchBlock from '@/components/simulation/FetchBlock';
import FunctionUnitGroup from '@/components/simulation/FunctionUnitGroup';
import IssueWindow from '@/components/simulation/IssueWindow';
import LoadBuffer from '@/components/simulation/LoadBuffer';
import MainMemory from '@/components/simulation/MainMemory';
import Program from '@/components/simulation/Program';
import ReorderBuffer from '@/components/simulation/ReorderBuffer';
import StoreBuffer from '@/components/simulation/StoreBuffer';

export function SimGrid() {
  return (
    <div className='global-grid'>
      <div className='col-grid'>
        <Program />
        <div className='flex flex-col gap-4'>
          <BranchBlock />
          <FetchBlock />
          <DecodeBlock />
        </div>
        <ReorderBuffer />
      </div>
      <div className='sim-grid justify-items-center'>
        <IssueWindow type='alu' />
        <FunctionUnitGroup type='alu' />
        <IssueWindow type='fp' />
        <FunctionUnitGroup type='fp' />
        <IssueWindow type='branch' />
        <FunctionUnitGroup type='branch' />
      </div>
      <div className='sim-grid'>
        <div className='flex justify-center'>
          <FunctionUnitGroup type='memory' />
        </div>
        <div className='flex gap-4'>
          <StoreBuffer />
          <LoadBuffer />
        </div>
      </div>
      <div>
        <div className='flex gap-4 mb-4 items-start'>
          <MainMemory />
          <CacheBlock />
        </div>
      </div>
    </div>
  );
}