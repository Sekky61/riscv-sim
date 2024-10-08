/**
 * @file    RadioInput.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Generic radio input component
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

// Custom radio input component
// Does not support multiple selections
//
// Works either with
// - react-hook-form (using `register` and `name` props), or
// - a simple `value` and `onNewValue` props

import React from 'react';
import {
  type Control,
  type FieldValues,
  type Path,
  useController,
} from 'react-hook-form';

import { cn } from '@/lib/utils';

import { Label } from '@/components/base/ui/label';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import * as RadioGroup from '@radix-ui/react-radio-group';

/**
 * Uses radix ui for accessibility
 */

interface RadioInputBaseProps<T extends string> {
  choices: readonly T[];
  texts?: readonly string[];
  className?: string;
}

type RadioInputProps<T extends string> = RadioInputBaseProps<T> & {
  value: T;
  onNewValue: (newValue: RadioInputBaseProps<T>['choices'][number]) => void;
};

/**
 * Uncontrolled radio input.
 * Calls onNewValue callback on value change.
 */
export function RadioInput<T extends string>({
  choices,
  texts,
  value,
  onNewValue,
}: RadioInputProps<T>) {
  return (
    <RadioGroup.Root
      className={cn(
        'radio-field flex h-10 items-center justify-evenly rounded-lg surface border p-1',
      )}
      value={value}
      onValueChange={onNewValue}
    >
      {choices.map((choice, i) => {
        const inputId = `${choice}`;
        const text = texts ? texts[i] : choice;
        return (
          <RadioGroup.Item key={choice} value={choice} id={inputId}>
            {/* <RadioGroup.Indicator className="flex items-center justify-center w-full h-full relative after:content-[''] after:block after:w-[11px] after:h-[11px] after:rounded-[50%] after:bg-violet11" /> */}
            <label
              data-state={value === choice ? 'active' : 'inactive'}
              className={cn(
                'cursor-pointer flex-grow inline-flex items-center justify-center whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 data-[state=active]:bg-primary data-[state=active]:text-onPrimary data-[state=active]:shadow-sm',
              )}
              htmlFor={inputId}
            >
              {text}
            </label>
          </RadioGroup.Item>
        );
      })}
    </RadioGroup.Root>
  );
}

type ControlledRadioInputProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
} & RadioInputBaseProps<T[string]>;

/**
 * Controlled version of RadioInput.
 * See react-hook-form docs for more info.
 */
export function ControlRadioInput<T extends FieldValues>({
  choices,
  texts,
  control,
  name,
  className = '',
}: ControlledRadioInputProps<T>) {
  const { field } = useController({
    name,
    control,
    rules: { required: true },
  });

  return (
    <RadioInput
      choices={choices}
      texts={texts}
      value={field.value}
      onNewValue={field.onChange}
      className={className}
    />
  );
}

// Added title and hint
export type RadioInputWithTitleProps<T extends FieldValues> = {
  title: string;
  hint?: string;
} & ControlledRadioInputProps<T>;

/**
 * Controlled version of RadioInput with title and hint. Works with control only.
 * See react-hook-form docs for more info.
 */
export function RadioInputWithTitle<T extends FieldValues>({
  choices,
  texts,
  control,
  name,
  className = '',
  title,
  hint,
}: RadioInputWithTitleProps<T>) {
  return (
    <div className={className}>
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
        <ControlRadioInput
          choices={choices}
          texts={texts}
          control={control}
          name={name}
        />
      </div>
    </div>
  );
}
