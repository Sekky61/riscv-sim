/**
 * @file    IsaSettingsForm.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Form for ISA configuration
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

'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { X } from 'lucide-react';
import { MouseEventHandler } from 'react';
import React from 'react';
import {
  Control,
  Controller,
  FieldError,
  RegisterOptions,
  UseFormReturn,
  useController,
  useForm,
} from 'react-hook-form';

import {
  FUnitConfig,
  IsaNamedConfig,
  Operations,
  cacheReplacementTypes,
  fUnitSchema,
  fuTypes,
  isArithmeticUnitConfig,
  operations,
  predictorDefaults,
  predictorTypes,
  storeBehaviorTypes,
} from '@/lib/forms/Isa';

import { Button } from '@/components/base/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import { Checkbox } from '@/components/base/ui/checkbox';
import { Label } from '@/components/base/ui/label';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/base/ui/tabs';

import { FormInput } from './FormInput';
import {
  ControlRadioInput,
  RadioInput,
  RadioInputWithTitle,
} from './RadioInput';

type IsaArrayFields = 'fUnits' | 'memoryLocations';
type IsaSimpleFields = keyof Omit<IsaNamedConfig, IsaArrayFields>;
type IsaKeys = IsaSimpleFields | IsaArrayFields;

type IsaFormMetadata = {
  [key in IsaKeys]: {
    title: string;
    hint?: string;
  };
};

/**
 * Metadata for ISA form fields
 * Displayed as title above the input and an icon with hint on hover
 */
const isaFormMetadata: IsaFormMetadata = {
  name: {
    title: 'Name',
    hint: 'Name of the ISA configuration.',
  },
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
  memoryLocations: {
    title: 'Memory locations',
  },
};

const capabilitiesMetadata: {
  [key in Operations]: { name: string; additional: string };
} = {
  addition: {
    name: 'Addition',
    additional: 'Addition (+)',
  },
  bitwise: {
    name: 'Bitwise',
    additional: 'Bitwise (and, or, xor)',
  },
  division: {
    name: 'Division',
    additional: 'Division (/)',
  },
  multiplication: {
    name: 'Multiplication',
    additional: 'Multiplication (*)',
  },
  special: {
    name: 'Special',
    additional: 'Special (sqrt, ...)',
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
  const simpleRegister = (
    name: IsaSimpleFields,
    regOptions?: RegisterOptions,
  ) => {
    const error: FieldError | undefined = errors[name];
    return {
      error,
      title: isaFormMetadata[name].title,
      hint: isaFormMetadata[name].hint,
      ...register(name, regOptions),
    };
  };

  const radioRegister = (name: IsaSimpleFields) => {
    return {
      name,
      title: isaFormMetadata[name].title,
      hint: isaFormMetadata[name].hint,
      control,
    };
  };

  return (
    <form>
      <Tabs defaultValue='buffers' className='w-[600px]'>
        <TabsList className='w-full'>
          <TabsTrigger value='name'>Name</TabsTrigger>
          <TabsTrigger value='buffers'>Buffers</TabsTrigger>
          <TabsTrigger value='fetch'>Fetch</TabsTrigger>
          <TabsTrigger value='functional'>Functional Units</TabsTrigger>
          <TabsTrigger value='cache'>Cache</TabsTrigger>
          <TabsTrigger value='branch'>Branch</TabsTrigger>
        </TabsList>
        <TabsContent value='name'>
          <Card>
            <CardHeader>
              <CardTitle>Name</CardTitle>
            </CardHeader>
            <CardContent>
              <fieldset disabled={disabled}>
                <FormInput {...simpleRegister('name')} />
              </fieldset>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='buffers'>
          <Card>
            <CardHeader>
              <CardTitle>Buffers</CardTitle>
            </CardHeader>
            <CardContent>
              <fieldset disabled={disabled}>
                <FormInput
                  {...simpleRegister('robSize', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('lbSize', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('sbSize', { valueAsNumber: true })}
                />
              </fieldset>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='fetch'>
          <Card>
            <CardHeader>
              <CardTitle>Fetch</CardTitle>
            </CardHeader>
            <CardContent>
              <fieldset disabled={disabled}>
                <FormInput
                  {...simpleRegister('fetchWidth', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('commitWidth', { valueAsNumber: true })}
                />
              </fieldset>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='functional'>
          <FunctionalUnitInput control={control} disabled={disabled} />
        </TabsContent>
        <TabsContent value='cache'>
          <Card>
            <CardHeader>
              <CardTitle>Cache</CardTitle>
            </CardHeader>
            <CardContent>
              <fieldset disabled={disabled}>
                <FormInput
                  {...simpleRegister('cacheLines', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('cacheLineSize', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('cacheAssoc', { valueAsNumber: true })}
                />
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
                <FormInput
                  {...simpleRegister('storeLatency', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('loadLatency', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('laneReplacementDelay', {
                    valueAsNumber: true,
                  })}
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
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='branch'>
          <Card>
            <CardHeader>
              <CardTitle>Predictors</CardTitle>
            </CardHeader>
            <CardContent>
              <fieldset disabled={disabled}>
                <FormInput
                  {...simpleRegister('btbSize', { valueAsNumber: true })}
                />
                <FormInput
                  {...simpleRegister('phtSize', { valueAsNumber: true })}
                />
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
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </form>
  );
}

type OpMetadata = {
  name: string;
};

const opsMetadata: { [op in Operations]: OpMetadata } = {
  addition: { name: 'Addition' },
  bitwise: { name: 'Bitwise' },
  division: { name: 'Division' },
  multiplication: { name: 'Multiplication' },
  special: { name: 'Special' },
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
    <Card>
      <CardHeader>
        <CardTitle>Functional Units</CardTitle>
      </CardHeader>
      <CardContent>
        <fieldset disabled={disabled}>
          <div className='fu-grid'>
            <div className='contents font-bold'>
              <div>Name</div>
              <div>Latency</div>
              <div>Operations</div>
              <div />
            </div>
            <div className='contents'>
              {funits.map((fu, i) => {
                let third = null;
                if (isArithmeticUnitConfig(fu)) {
                  third = fu.operations.map((op) => {
                    const meta = opsMetadata[op];
                    return (
                      <div
                        key={op}
                        title={meta.name}
                        className='rounded bg-gray-200 px-1 py-0.5'
                      >
                        {capabilitiesMetadata[op].name}
                      </div>
                    );
                  });
                }
                return (
                  <div className='contents' key={fu.id}>
                    <div className='whitespace-nowrap'>
                      {fu.name || fu.fuType}
                    </div>
                    <div>{fu.latency}</div>
                    <div className='flex flex-grow gap-1 items-center truncate text-sm overflow-x-auto'>
                      {third}
                    </div>
                    <div>
                      <button
                        type='button'
                        onClick={() => removeUnit(i)}
                        className='shrink-0 px-1'
                      >
                        <X className='rounded-full stroke-red-400 duration-100 hover:bg-red-500/60 hover:stroke-red-600' />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
          <div className='border-t p-8 mt-6'>
            <FUAdder control={control} />
          </div>
        </fieldset>
      </CardContent>
    </Card>
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
        <ControlRadioInput
          control={subControl}
          name='fuType'
          choices={fuTypes}
        />
      </div>
      <div className='mt-4 flex justify-evenly'>
        <div>
          <FormInput
            {...register('name')}
            name='name'
            title='Name'
            error={errors.name}
          />
          <FormInput
            {...register('latency', { valueAsNumber: true })}
            name='latency'
            title='Latency'
            error={errors.latency}
          />
        </div>
        <div className={enableOps ? '' : 'invisible'}>
          <Label>Supported operations</Label>
          <div className='flex gap-1'>
            <Controller
              control={subControl}
              name='operations'
              render={({ field: { onChange, value } }) => {
                return (
                  <div className='flex flex-col gap-2'>
                    {operations.map((op) => {
                      const id = `chkbx-${op}`;
                      return (
                        <div key={op} className='flex items-center space-x-2'>
                          <Checkbox
                            id={id}
                            value={op}
                            checked={value.includes(op)}
                            onCheckedChange={(checked) => {
                              if (checked) {
                                onChange([...value, op]);
                              } else {
                                onChange(value.filter((item) => item !== op));
                              }
                            }}
                          />
                          <Label
                            htmlFor={id}
                            className='text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
                          >
                            {capabilitiesMetadata[op].additional}
                          </Label>
                        </div>
                      );
                    })}
                  </div>
                );
              }}
            />
          </div>
        </div>
      </div>
      <Button type='button' onClick={addFU}>
        Add Unit
      </Button>
    </div>
  );
}
