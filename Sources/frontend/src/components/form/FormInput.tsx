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

import type { FieldError } from 'react-hook-form';

import { Label } from '@/components/base/ui/label';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { cn } from '@/lib/utils';
import React from 'react';

type InputProps = React.InputHTMLAttributes<HTMLInputElement>;

export type FormInputProps = {
  name: string;
  title?: string;
  type?: string;
  error?: FieldError;
  hint?: string;
} & InputProps;

/**
 * Generic input for forms
 * It is a forwardRef component, so it can be used with react-hook-form.
 * It displays error message, title, hint.
 * See example of usage in {@link src/components/form/MemoryForm.tsx}
 */
export const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ name, title, error, hint, ...inputProps }: FormInputProps, ref) => {
    const isError = error !== undefined;

    return (
      <div>
        {hint ? (
          <Tooltip>
            <TooltipTrigger>
              <Label htmlFor={name}>{title}&nbsp;&#9432;</Label>
            </TooltipTrigger>
            <TooltipContent>
              <p className='max-w-64 p-2'>{hint}</p>
            </TooltipContent>
          </Tooltip>
        ) : (
          <Label htmlFor={name}>{title}</Label>
        )}
        <input
          {...inputProps}
          type={inputProps.type || 'text'}
          name={name}
          id={name}
          className={cn(
            'input',
            isError && 'form-input-error',
            !isError && 'focus-visible:ring-ring',
          )}
          ref={ref}
        />
        <ErrorDisplay error={error} />
      </div>
    );
  },
);

FormInput.displayName = 'FormInput';

/**
 * Displays a zod error
 */
export function ErrorDisplay({ error }: { error?: FieldError }) {
  return (
    <div className='h-6'>
      {error?.message && (
        <span className='mt-1 text-sm text-red-600'>{error?.message}</span>
      )}
    </div>
  );
}
