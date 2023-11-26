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
import { useId } from 'react';
import { FieldValues, Path, UseFormRegister } from 'react-hook-form';

import { ReactClassName } from '@/lib/types/reactTypes';
import { cn } from '@/lib/utils';

interface RadioInputBaseProps extends ReactClassName {
  choices: readonly string[];
  texts?: readonly string[];
}

// The register function from react-hook-form
interface RadioInputRegisterProps<Form extends FieldValues> {
  name: Path<Form>;
  register: UseFormRegister<Form>;
}

interface SimpleRadioInputProps<Val extends string> {
  value: Val;
  onNewValue: (newVal: Val) => void;
}

// Final type of props
export type RadioInputProps<
  Form extends FieldValues,
  Val extends string = string,
> = RadioInputBaseProps &
  (RadioInputRegisterProps<Form> | SimpleRadioInputProps<Val>);

export function RadioInput<T extends FieldValues, U extends string>({
  choices,
  texts,
  className = '',
  ...rest
}: RadioInputProps<T, U>) {
  const radioId = useId();
  let inputProps = {};
  let activeChoice = '';
  if ('register' in rest) {
    inputProps = { ...rest.register(rest.name) };
  } else {
    const handleClick: React.MouseEventHandler<HTMLInputElement> = (e) => {
      rest.onNewValue(e.currentTarget.value as U);
    };
    inputProps = {
      onClick: handleClick,
      value: rest.value,
      name: radioId, // The radios need to have the same name to be grouped
    };
    activeChoice = rest.value;
  }

  return (
    <div
      className={cn(
        'radio-field flex h-10 items-center justify-stretch rounded-md bg-muted p-1 text-muted-foreground',
        className,
      )}
    >
      {choices.map((choice, i) => {
        const inputId = `${radioId}-${choice}`;
        const text = texts ? texts[i] : choice;
        // Workaround for SimpleRadioInputs - initial value is not highlighted so JS is used
        const active = choice === activeChoice;
        return (
          <React.Fragment key={choice}>
            <input
              type='radio'
              id={inputId}
              className='hidden'
              hidden
              {...inputProps}
              value={choice}
            />
            <label
              htmlFor={inputId}
              className={cn(
                'flex-grow inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
                active && 'radio-selected',
              )}
            >
              {text}
            </label>
          </React.Fragment>
        );
      })}
    </div>
  );
}

// Added title and hint
export type RadioInputWithTitleProps<
  Form extends FieldValues,
  Val extends string,
> = RadioInputProps<Form, Val> & { title: string; hint?: string };

export function RadioInputWithTitle<T extends FieldValues, U extends string>({
  title,
  hint,
  className,
  ...rest
}: RadioInputWithTitleProps<T, U>) {
  return (
    <div className={className}>
      <label className='mb-1 text-sm font-medium '>{title}</label>
      {hint ? (
        <span className='tooltip ml-1 text-xs'>
          &#9432;
          <div className='tooltiptext ml-2 rounded bg-gray-100 p-1'>{hint}</div>
        </span>
      ) : null}
      <div>
        <RadioInput {...rest} />
      </div>
    </div>
  );
}
