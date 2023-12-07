/**
 * @file    FormInput.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Generic input for forms
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

import {
  FieldError,
  FieldValues,
  Path,
  UseFormRegister,
} from 'react-hook-form';

import { Input } from '@/components/base/ui/input';
import { Label } from '@/components/base/ui/label';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';

export type FormInputProps<T extends FieldValues> = {
  name: Path<T>;
  title: string;
  register: UseFormRegister<T>;
  type?: string;
  error?: FieldError;
  hint?: string;
};

export function FormInput<T extends FieldValues>({
  name,
  register,
  title,
  type,
  error,
  hint,
}: FormInputProps<T>) {
  const isError = error !== undefined;
  let regOptions = {};
  if (type === 'number') {
    regOptions = { valueAsNumber: true };
  }

  return (
    <div>
      <Tooltip>
        <TooltipTrigger>
          <Label htmlFor={name}>
            {title}&nbsp;
            {hint ? <span>&#9432;</span> : null}
          </Label>
        </TooltipTrigger>
        <TooltipContent>
          <p>{hint}</p>
        </TooltipContent>
      </Tooltip>
      <Input
        {...register(name, regOptions)}
        type='text'
        name={name}
        id={name}
        className={isError ? 'error' : ''}
      />
      <div className='h-6'>
        {error?.message && (
          <span className='mt-1 text-sm text-red-600'>{error?.message}</span>
        )}
      </div>
    </div>
  );
}
