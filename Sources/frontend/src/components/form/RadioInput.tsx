// Custom radio input component
// Does not support multiple selections
//
// Works either with
// - react-hook-form (using `register` and `name` props), or
// - a simple `value` and `onNewValue` props

import clsx from 'clsx';
import React from 'react';
import { useId } from 'react';
import { FieldValues, Path, UseFormRegister } from 'react-hook-form';

import { ReactClassName } from '@/lib/reactTypes';

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
      className={
        className + ' radio-field flex overflow-hidden rounded border divide-x'
      }
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
              className={clsx(
                'button-interactions flex-1 whitespace-nowrap button-shape text-center',
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
