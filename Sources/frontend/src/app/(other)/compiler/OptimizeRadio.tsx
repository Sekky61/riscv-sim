/**
 * @file    OptimizeRadio.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The radio buttons for selecting the optimization level
 *
 * @date    26 February 2024, 21:00 (created)
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

import { Label } from '@/components/base/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/base/ui/radio-group';
import {
  OptimizeOption,
  selectOptimize,
  setOptimizeFlag,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

type OptimizeMeta = {
  label: string;
  value: OptimizeOption;
};

/**
 * The optimization options. Label is displayed in the UI, value is the value passed to the compiler (API).
 * One flag is selected at a time.
 */
const optimizeOptions: OptimizeMeta[] = [
  {
    label: 'Do not optimize',
    value: 'O0',
  },
  {
    label: 'Optimize (O2)',
    value: 'O2',
  },
  {
    label: 'Optimize (O3)',
    value: 'O3',
  },
  {
    label: 'Optimize for size',
    value: 'Os',
  },
] as const;

export function OptimizeRadio() {
  const dispatch = useAppDispatch();
  const optimizeFlag = useAppSelector(selectOptimize);

  const setIt = (val: string) => {
    dispatch(setOptimizeFlag(val as OptimizeOption));
  };

  return (
    <div className='rounded border flex flex-col gap-2 p-2'>
      <RadioGroup defaultValue={optimizeFlag} onValueChange={setIt}>
        {optimizeOptions.map((option) => (
          <div className='flex items-center space-x-2' key={option.label}>
            <RadioGroupItem value={option.value} id={option.value} />
            <Label htmlFor={option.value}>{option.label}</Label>
          </div>
        ))}
      </RadioGroup>
    </div>
  );
}
