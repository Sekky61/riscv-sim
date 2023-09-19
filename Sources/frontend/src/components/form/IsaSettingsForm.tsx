'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { X } from 'lucide-react';
import { MouseEventHandler, useState } from 'react';
import React from 'react';
import {
  Control,
  Controller,
  FieldError,
  useController,
  useForm,
  UseFormReturn,
} from 'react-hook-form';

import {
  cacheReplacementTypes,
  FUnitConfig,
  fUnitSchema,
  FuOps,
  fuOps,
  fuTypes,
  IsaConfig,
  IsaNamedConfig,
  isArithmeticUnitConfig,
  predictorDefaults,
  predictorTypes,
  storeBehaviorTypes,
} from '@/lib/forms/Isa';

import { FormInput } from './FormInput';
import { RadioInput, RadioInputWithTitle } from './RadioInput';

type IsaArrayFields = 'fUnits';
type IsaSimpleFields = keyof Omit<IsaConfig, IsaArrayFields>;
type IsaKeys = IsaSimpleFields | IsaArrayFields;

type IsaFormMetadata = {
  [key in IsaKeys]: {
    title: string;
    hint?: string;
  };
};

const isaFormMetadata: IsaFormMetadata = {
  robSize: {
    title: 'Re-order buffer size',
    hint: 'Instruction capacity of re-order buffer (ROB).',
  },
  lbSize: {
    title: 'Load buffer size',
    hint: 'Instruction capacity of load buffer (LB).',
  },
  sbSize: {
    title: 'Store buffer size',
    hint: 'Instruction capacity of store buffer (SB).',
  },
  fetchWidth: {
    title: 'Fetched instructions per cycle',
    hint: 'The maximum number of instructions loaded from memory in a single clock.',
  },
  commitWidth: {
    title: 'Commited instructions per cycle',
    hint: 'The maximum number of instructions commited in a single clock.',
  },
  btbSize: {
    title: 'Branch target buffer size',
    hint: 'Instruction capacity of branch target buffer (BTB).',
  },
  phtSize: {
    title: 'Pattern history table size',
    hint: 'Instruction capacity of pattern history table (PHT).',
  },
  predictorType: {
    title: 'Predictor type in PHT',
  },
  predictorDefault: {
    title: 'Predictor default value',
  },
  cacheLines: {
    title: 'Cache lines',
    hint: 'Number of cache lines in the cache.',
  },
  cacheLineSize: {
    title: 'Cache line size (B)',
    hint: 'Size of a cache line in bytes.',
  },
  cacheAssoc: {
    title: 'Cache associativity',
    hint: 'Number of cache lines per set.',
  },
  cacheReplacement: {
    title: 'Cache replacement policy',
  },
  storeBehavior: {
    title: 'Store behavior',
  },
  storeLatency: {
    title: 'Store latency',
    hint: 'Number of cycles a store takes to be written to memory.',
  },
  loadLatency: {
    title: 'Load latency',
    hint: 'Number of cycles a load takes to be read from memory.',
  },
  laneReplacementDelay: {
    title: 'Lane replacement delay',
    hint: 'Number of cycles a cache line replacement takes.',
  },
  addRemainingDelay: {
    title: 'Should remaining line replacement delay be added to store?',
  },
  fUnits: {
    title: 'Functional units',
  },
};

export type IsaSettingsFormProps = {
  disabled?: boolean;
  form: UseFormReturn<IsaNamedConfig>;
};

export default function IsaSettingsForm({
  form,
  disabled = false,
}: IsaSettingsFormProps) {
  const {
    register,
    formState: { errors },
    control,
  } = form;

  // This function is valid for regular fields, but not arrays
  const simpleRegister = (name: IsaSimpleFields) => {
    const error: FieldError | undefined = errors[name];
    return {
      register,
      error,
      name,
      title: isaFormMetadata[name].title,
      hint: isaFormMetadata[name].hint,
    };
  };

  const radioRegister = (name: IsaSimpleFields) => {
    return {
      name,
      title: isaFormMetadata[name].title,
      hint: isaFormMetadata[name].hint,
      register,
    };
  };

  return (
    <form>
      <div className='grid grid-cols-2 gap-12'>
        <fieldset className='rounded-md border p-4' disabled={disabled}>
          <legend className='mb-2 px-1 text-xl'>Buffers</legend>
          <FormInput {...simpleRegister('robSize')} type='number' />
          <FormInput {...simpleRegister('lbSize')} type='number' />
          <FormInput {...simpleRegister('sbSize')} type='number' />
        </fieldset>
        <fieldset className='rounded-md border p-4' disabled={disabled}>
          <legend className='mb-2 px-1 text-xl'>Fetch</legend>
          <FormInput {...simpleRegister('fetchWidth')} type='number' />
          <FormInput {...simpleRegister('commitWidth')} type='number' />
        </fieldset>
        <FunctionalUnitInput control={control} disabled={disabled} />
        <fieldset className='rounded-md border p-4' disabled={disabled}>
          <legend className='mb-2 px-1 text-xl'>Cache</legend>
          <FormInput {...simpleRegister('cacheLines')} type='number' />
          <FormInput {...simpleRegister('cacheLineSize')} type='number' />
          <FormInput {...simpleRegister('cacheAssoc')} type='number' />
          <div className='mb-6 flex justify-evenly'>
            <RadioInputWithTitle
              {...radioRegister('cacheReplacement')}
              choices={cacheReplacementTypes}
            />
            <RadioInputWithTitle
              {...radioRegister('storeBehavior')}
              choices={storeBehaviorTypes}
            />
          </div>
          <FormInput {...simpleRegister('storeLatency')} type='number' />
          <FormInput {...simpleRegister('loadLatency')} type='number' />
          <FormInput
            {...simpleRegister('laneReplacementDelay')}
            type='number'
          />
          <input
            id='addRemainingDelay'
            type='checkbox'
            {...register('addRemainingDelay')}
            className='mr-2'
          />
          <label htmlFor='addRemainingDelay'>
            {isaFormMetadata.addRemainingDelay.title}
          </label>
        </fieldset>
        <fieldset className='rounded-md border p-4' disabled={disabled}>
          <legend className='mb-2 px-1 text-xl'>Branch</legend>
          <FormInput {...simpleRegister('btbSize')} type='number' />
          <FormInput {...simpleRegister('phtSize')} type='number' />
          <div className='mb-6 flex justify-evenly'>
            <RadioInputWithTitle
              {...radioRegister('predictorType')}
              choices={predictorTypes}
            />
            <RadioInputWithTitle
              {...radioRegister('predictorDefault')}
              choices={predictorDefaults}
            />
          </div>
        </fieldset>
      </div>
    </form>
  );
}

type FuOpMetadata = {
  name: string;
};

// ++,--,!,#,<-,+,-,*,/,%,&,|,>>>,<<,>>,<=,>=,==,<,>,(,)
const fuOpsMetadata: { [key in FuOps]: FuOpMetadata } = {
  '+': { name: 'Add' },
  '-': { name: 'Subtract' },
  '*': { name: 'Multiply' },
  '/': { name: 'Divide' },
  '%': { name: 'Modulo' },
  '&': { name: 'Bitwise and' },
  '|': { name: 'Bitwise or' },
  '>>': { name: 'Shift right' },
  '<<': { name: 'Shift left' },
  '>>>': { name: 'TODO' },
  '<': { name: 'Less than' },
  '>': { name: 'Greater than' },
  '<=': { name: 'Less than or equal' },
  '>=': { name: 'Greater than or equal' },
  '==': { name: 'Equal' },
  '!': { name: 'TODO' },
  '++': { name: 'Increment' },
  '--': { name: 'Decrement' },
  '#': { name: 'TODO' },
  '<-': { name: 'TODO' },
  '(': { name: 'TODO' },
  ')': { name: 'TODO' },
};

interface FunctionalUnitInputProps {
  disabled?: boolean;
  control: Control<IsaNamedConfig>;
}

// Uses its own subform
function FunctionalUnitInput({
  control,
  disabled = false,
}: FunctionalUnitInputProps) {
  const { field } = useController({
    control,
    name: 'fUnits',
  });

  const funits = field.value;

  const removeUnit = (index: number) => {
    const funitsClone = [...funits];
    funitsClone.splice(index, 1);
    field.onChange(funitsClone);
  };

  return (
    <fieldset className='flex flex-col rounded-md border' disabled={disabled}>
      <legend className='mb-2 ml-4 px-1 text-xl'>Functional Units</legend>
      <div className='h-0 flex-grow overflow-y-auto bg-gray-100'>
        <div className='neutral-bg grid auto-rows-fr grid-cols-[fit-content(0px)_fit-content(0px)_1fr_fit-content(0px)] divide-y'>
          <div className='neutral-bg sticky top-0 border-t px-2 py-1'>Name</div>
          <div className='neutral-bg sticky top-0 px-2 py-1'>Latency</div>
          <div className='neutral-bg sticky top-0 flex-grow px-2 py-1'>
            Operations
          </div>
          <div className='neutral-bg sticky top-0' />
          {funits.map((fu, i) => {
            let third = null;
            if (isArithmeticUnitConfig(fu)) {
              third = fu.operations.map((op) => {
                const meta = fuOpsMetadata[op];
                return (
                  <div
                    key={op}
                    title={meta.name}
                    className='rounded bg-gray-200 px-1 py-0.5'
                  >
                    {op}
                  </div>
                );
              });
            }
            return (
              <React.Fragment key={fu.id}>
                <div className='px-2 py-1'>{fu.fuType}</div>
                <div className='px-2 py-1'>{fu.latency}</div>
                <div className='flex flex-grow gap-1 truncate px-2 py-1 text-sm'>
                  {third}
                </div>
                <button onClick={() => removeUnit(i)} className='shrink-0 px-1'>
                  <X className='rounded-full stroke-red-400 duration-100 hover:bg-red-500/60 hover:stroke-red-600' />
                </button>
              </React.Fragment>
            );
          })}
        </div>
      </div>
      <div className='border-t p-4'>
        <FUAdder control={control} />
      </div>
    </fieldset>
  );
}

// Subform for adding a new FU
// Controls the fUnits field
function FUAdder({ control }: { control: Control<IsaNamedConfig> }) {
  const { field } = useController({
    control,
    name: 'fUnits',
  });

  const {
    register,
    formState: subFormState,
    control: subControl,
    watch,
    setValue,
    getValues,
  } = useForm<FUnitConfig>({
    resolver: zodResolver(fUnitSchema),
    defaultValues: {
      fuType: 'FX',
      latency: 1,
      operations: [],
    },
    mode: 'onChange',
  });
  const { errors } = subFormState;

  const selectedUnitType = watch('fuType');
  const enableOps = selectedUnitType === 'FX' || selectedUnitType === 'FP';

  const addFU: MouseEventHandler = (_e) => {
    // generate integer ID
    const id = Math.floor(Math.random() * 1000000);
    setValue('id', id);
    const data = getValues();
    field.onChange([...field.value, data]);
  };

  return (
    <div>
      <div className=''>
        <RadioInput register={register} name='fuType' choices={fuTypes} />
      </div>
      <div className='mt-4 flex justify-between'>
        <div>
          <FormInput
            register={register}
            name='latency'
            title='Latency'
            type='number'
            error={errors.latency}
          />
        </div>
        <div className={enableOps ? '' : 'invisible'}>
          <label className='mb-1 text-sm font-medium'>
            Supported operations
          </label>
          <div className='flex gap-1'>
            <OpPicker control={subControl} />
            <button type='button' onClick={addFU} className='small-button'>
              Select all
            </button>
          </div>
        </div>
      </div>
      <button type='button' onClick={addFU} className='button'>
        Add Unit
      </button>
    </div>
  );
}

function OpPicker({ control }: { control: Control<FUnitConfig> }) {
  const [isOpen, setIsOpen] = useState(false);

  const toggleDropdown: MouseEventHandler = (e) => {
    if (e.target !== e.currentTarget) return;
    setIsOpen(!isOpen);
  };

  return (
    <Controller
      control={control}
      name='operations'
      render={({ field: { onChange, value } }) => {
        return (
          <div
            className='relative flex w-48 items-center justify-center rounded-md border bg-gray-100 p-1'
            onClick={toggleDropdown}
          >
            <div className='pointer-events-none'>
              <span>Selected: {value.length}</span>
              <span>{isOpen ? '▲' : '▼'}</span>
            </div>
            {isOpen && (
              <div className='absolute top-full flex h-32 flex-col overflow-y-scroll rounded-b-md border bg-white'>
                {fuOps.map((op) => {
                  const meta = fuOpsMetadata[op];
                  return (
                    <label
                      key={op}
                      className='grid grid-cols-[20px_40px_1fr] items-center gap-2 hover:bg-gray-200 active:bg-gray-300'
                    >
                      <input
                        className='ml-1 mr-2 h-4 w-4'
                        type='checkbox'
                        value={op}
                        checked={value.includes(op)}
                        onChange={(e) => {
                          const val = e.target.value as FuOps;
                          if (value.includes(val)) {
                            onChange(value.filter((item) => item !== val));
                          } else {
                            onChange([...value, val]);
                          }
                        }}
                      />
                      <span className='font-bold'>{op}</span>
                      <span>{meta.name}</span>
                    </label>
                  );
                })}
              </div>
            )}
          </div>
        );
      }}
    />
  );
}
