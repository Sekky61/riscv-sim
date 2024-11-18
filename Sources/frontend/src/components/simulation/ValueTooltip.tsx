/**
 * @file    ValueInformation.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying a tooltip with a value of a register or constant
 *
 * @date    27 November 2023, 19:00 (created)
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

import type { RegisterDataContainer, RegisterModel } from '@/lib/types/cpuApi';
import { binPad32, hexPad } from '@/lib/utils';

export type ValueInformationProps = {
  value: RegisterDataContainer;
  valid: boolean;
  register: RegisterModel | null;
};

/**
 * Value of a register or constant.
 * Used in the instruction modals.
 */
export function ValueInformation({
  value,
  valid,
  register,
}: ValueInformationProps) {
  return (
    <div className='flex flex-col'>
      <span className='text-lg'>
        {register?.architecturalRegister
          ? `${register.name}: renamed from ${register.architecturalRegister}`
          : value.stringRepresentation}
      </span>
      <div className='flex flex-row'>
        <div className='flex flex-col'>
          <div>Value</div>
          <div>Type</div>
          <div>Valid</div>
          <div>Bin</div>
          <div>Hex</div>
        </div>
        <div className='flex flex-col ml-2'>
          <div>{value.stringRepresentation}</div>
          <div>{value.currentType}</div>
          <div>{valid ? 'Yes' : 'No'}</div>
          <div>{binPad32(value.bits)}</div>
          <div>{hexPad(value.bits)}</div>
        </div>
      </div>
    </div>
  );
}

/**
 * Shorter version for hover
 */
export function ShortValueInformation({
  value,
  valid,
  register,
}: ValueInformationProps) {
  return (
    <div
      className='z-50 grid gap-1 secondary-container px-3 py-1.5 font-mono'
      style={{
        gridTemplateColumns: 'auto auto',
      }}
    >
      {register && (
        <div className='col-span-2'>
          {register.name}
          {register.architecturalRegister && (
            <div className='text-xs font-normal'>
              renamed from {register.architecturalRegister}
            </div>
          )}
        </div>
      )}
      {valid ? (
        <>
          <div className=' font-bold'>Value</div>
          <div>{value.stringRepresentation}</div>
          <div>Type</div>
          <div>{value.currentType}</div>
        </>
      ) : (
        <div className='col-span-2'>Unassigned value</div>
      )}
    </div>
  );
}
