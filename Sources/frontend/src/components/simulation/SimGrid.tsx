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

import { Trace } from '@/app/(simulation)/Trace';
import { HighlightProvider } from '@/components/HighlightProvider';
import CacheBlock from '@/components/simulation/CacheBlock';
import DecodeBlock from '@/components/simulation/DecodeBlock';
import FetchBlock from '@/components/simulation/FetchBlock';
import FunctionUnitGroup from '@/components/simulation/FunctionUnitGroup';
import IssueWindow from '@/components/simulation/IssueWindow';
import LoadBuffer from '@/components/simulation/LoadBuffer';
import MainMemory from '@/components/simulation/MainMemory';
import Program from '@/components/simulation/Program';
import RegisterBlock from '@/components/simulation/RegisterBlock';
import ReorderBuffer from '@/components/simulation/ReorderBuffer';
import StoreBuffer from '@/components/simulation/StoreBuffer';

export function SimGrid() {
  return (
    <HighlightProvider>
      <div className='global-grid'>
        <div className='top-grid'>
          <Program />
          <div className='block-stack'>
            <div className='relative'>
              <Trace top='50%' right='100%' />
              <FetchBlock />
            </div>
            <div className='relative'>
              <Trace bottom='100%' left='50%' vertical />
              <Trace top='50%' left='100%' />
              <DecodeBlock />
            </div>
          </div>
          <div className='w-[1px] relative'>
            <Trace
              top='-2.5rem'
              left='0'
              length='calc(100% + 5rem)'
              vertical
            />
          </div>
          <div className='w-block h-full relative'>
            <Trace bottom='100%' left='50%' vertical />
            <Trace top='-2.5rem' left='-2.5rem' length='calc(100% + 2.5rem)' />
            <ReorderBuffer />
          </div>
          <div className='issue relative'>
            <Trace top='-2.5rem' left='-2.5rem' length='calc(100% + 2.5rem)' />
            <div className='relative'>
              <IssueWindow type='alu' />
              <Trace bottom='100%' left='50%' vertical />
              <Trace top='100%' left='50%' vertical />
            </div>
            <FunctionUnitGroup type='alu' />
          </div>
          <div className='issue relative'>
            <Trace top='-2.5rem' left='-2.5rem' length='calc(100% + 2.5rem)' />
            <div className='relative'>
              <IssueWindow type='fp' />
              <Trace bottom='100%' left='50%' vertical />
              <Trace top='100%' left='50%' vertical />
            </div>
            <FunctionUnitGroup type='fp' />
          </div>
          <div className='issue relative'>
            <Trace top='-2.5rem' left='-2.5rem' length='calc(50% + 2.5rem)' />
            <div className='relative'>
              <IssueWindow type='branch' />
              <Trace bottom='100%' left='50%' vertical />
              <Trace top='100%' left='50%' vertical />
            </div>
            <FunctionUnitGroup type='branch' />
          </div>
        </div>
        <div />
        <div className='bottom-grid pb-8'>
          <div className='bottom-grid-mem'>
            <div className='relative'>
              <Trace top='-2.5rem' left='50%' length='50%' />
              <Trace bottom='100%' left='50%' vertical />
            
              <StoreBuffer />
            </div>
            <div className='relative'>
              <Trace top='-2.5rem' left='-2.5rem' length='calc(50% + 2.5rem)' />
              <Trace bottom='100%' left='50%' vertical />
              <LoadBuffer />
            </div>
            <div className='block-stack'>
              <div className='relative'>
                <Trace top='50%' right='100%' length='10rem' />
                <FunctionUnitGroup type='ls' />
              </div>
              <div className='relative'>
                <Trace top='100%' left='50%' vertical />
                <Trace top='50%' left='100%' />
                <Trace top='50%' right='100%' length='10rem' />
                <FunctionUnitGroup type='memory' />
              </div>
              <MainMemory />
            </div>
            <div className=' col-span-2 flex'>
              <RegisterBlock />
            </div>
          </div>
          <CacheBlock />
        </div>
      </div>
    </HighlightProvider>
  );
}
