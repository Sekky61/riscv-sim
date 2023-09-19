import {
  FieldError,
  FieldValues,
  Path,
  UseFormRegister,
} from 'react-hook-form';

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
    <div className=''>
      <label
        htmlFor={name}
        className={
          'mb-1 text-sm font-medium ' + (isError ? 'text-red-700' : '')
        }
      >
        {title}
      </label>
      {hint ? (
        <span className='tooltip ml-1 text-xs hover:cursor-help'>
          &#9432;
          <div className='tooltiptext ml-2 rounded bg-gray-100 p-1'>{hint}</div>
        </span>
      ) : null}
      <input
        {...register(name, regOptions)}
        type='text'
        name={name}
        id={name}
        className={'form-input ' + (isError ? 'error' : '')}
      />
      <div className='h-6'>
        {error?.message && (
          <span className='mt-1 text-sm text-red-600'>{error?.message}</span>
        )}
      </div>
    </div>
  );
}
