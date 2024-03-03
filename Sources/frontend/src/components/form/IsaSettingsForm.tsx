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
import { MouseEventHandler, useEffect } from 'react';
import React from 'react';
import {
  Control,
  FieldError,
  RegisterOptions,
  UseFormReturn,
  useController,
  useForm,
} from 'react-hook-form';

import {
  CpuConfig,
  FUnitConfig,
  Operations,
  cacheReplacementTypes,
  fUnitSchema,
  fuTypes,
  isArithmeticUnitConfig,
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

import { formatNumberWithUnit } from '@/lib/utils';
import { FormInput } from './FormInput';
import { ControlRadioInput, RadioInputWithTitle } from './RadioInput';

type IsaArrayFields = 'fUnits' | 'memoryLocations';
type IsaSimpleFields = keyof Omit<CpuConfig, IsaArrayFields>;
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
  branchFollowLimit: {
    title: 'Branch follow limit',
    hint: 'Number of branch instructions that can be evaluated in a single fetch.',
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
  predictorDefaultState: {
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
  fUnits: {
    title: 'Functional units',
  },
  memoryLocations: {
    title: 'Memory locations',
  },
  flushPenalty: {
    title: 'Flush penalty',
    hint: 'Number of clock cycles the CPU will take to flush the pipeline.',
  },
  useGlobalHistory: {
    title: 'Use global history vector',
    hint: 'Use global history vector to address the PHT.',
  },
  useCache: {
    title: 'Use cache',
    hint: 'Enabling cache will add a cache between the CPU and memory.',
  },
  cacheAccessDelay: {
    title: 'Cache access delay',
    hint: 'Cache access delay in cycles.',
  },
  callStackSize: {
    title: 'Call stack size',
    hint: 'Call stack size in bytes.',
  },
  speculativeRegisters: {
    title: 'Number of speculative registers',
  },
  coreClockFrequency: {
    title: 'Core clock frequency (Hz)',
    hint: 'Core clock frequency in Hz.',
  },
  cacheClockFrequency: {
    title: 'Cache clock frequency (Hz)',
    hint: 'Cache clock frequency in Hz.',
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

const twoStatePredictor = ['Not Taken', 'Taken'] as const;
const fourStatePredictor = [
  'Strongly Not Taken',
  'Weakly Not Taken',
  'Weakly Taken',
  'Strongly Taken',
] as const;

export type IsaSettingsFormProps = {
  disabled?: boolean;
  form: UseFormReturn<CpuConfig>;
};

export default function IsaSettingsForm({
  form,
  disabled = false,
}: IsaSettingsFormProps) {
  const {
    register,
    formState: { errors },
    control,
    watch,
    setValue,
  } = form;

  const watchPredictorType = watch('predictorType');
  const coreClockFrequency = watch('coreClockFrequency');
  const cacheClockFrequency = watch('cacheClockFrequency');

  // When predictorType changes, set a new predictorDefault
  useEffect(() => {
    setValue('predictorDefaultState', 0);
  }, [setValue]);

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
          <TabsTrigger value='functional'>Functional Units</TabsTrigger>
          <TabsTrigger value='cache'>Cache</TabsTrigger>
          <TabsTrigger value='memory'>Memory</TabsTrigger>
          <TabsTrigger value='branch'>Branch</TabsTrigger>
        </TabsList>
        <div
          className={
            disabled
              ? 'pointer-events-none opacity-60 hover:cursor-not-allowed'
              : undefined
          }
        >
          <TabsContent value='name'>
            <Card>
              <CardHeader>
                <CardTitle>Name</CardTitle>
              </CardHeader>
              <CardContent>
                <fieldset disabled={disabled}>
                  <FormInput {...simpleRegister('name')} />
                  <div className='grid grid-cols-2 items-center gap-4'>
                    <FormInput
                      {...simpleRegister('coreClockFrequency', {
                        valueAsNumber: true,
                      })}
                    />
                    <p>
                      {coreClockFrequency &&
                        `= ${formatNumberWithUnit(coreClockFrequency)}`}
                    </p>
                    <FormInput
                      {...simpleRegister('cacheClockFrequency', {
                        valueAsNumber: true,
                      })}
                    />
                    <p>
                      {cacheClockFrequency &&
                        `= ${formatNumberWithUnit(cacheClockFrequency)}`}
                    </p>
                  </div>
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
                    {...simpleRegister('commitWidth', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('flushPenalty', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('fetchWidth', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('branchFollowLimit', {
                      valueAsNumber: true,
                    })}
                  />
                </fieldset>
              </CardContent>
            </Card>
          </TabsContent>
          <TabsContent value='memory'>
            <Card>
              <CardHeader>
                <CardTitle>Memory</CardTitle>
              </CardHeader>
              <CardContent>
                <fieldset disabled={disabled}>
                  <FormInput
                    {...simpleRegister('lbSize', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('sbSize', { valueAsNumber: true })}
                  />

                  <FormInput
                    {...simpleRegister('storeLatency', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('loadLatency', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('callStackSize', {
                      valueAsNumber: true,
                    })}
                  />
                  <FormInput
                    {...simpleRegister('speculativeRegisters', {
                      valueAsNumber: true,
                    })}
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
                  <div className='flex gap-2 items-center m-2'>
                    <input
                      {...register('useCache')}
                      type='checkbox'
                      className='m-2'
                    />
                    <Label htmlFor='useCache'>
                      {isaFormMetadata.useCache.title}
                    </Label>
                  </div>
                  <FormInput
                    {...simpleRegister('cacheLines', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('cacheLineSize', {
                      valueAsNumber: true,
                    })}
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
                    {...simpleRegister('laneReplacementDelay', {
                      valueAsNumber: true,
                    })}
                  />
                  <FormInput
                    {...simpleRegister('cacheAccessDelay', {
                      valueAsNumber: true,
                    })}
                  />
                </fieldset>
              </CardContent>
            </Card>
          </TabsContent>
          <TabsContent value='branch'>
            <Card>
              <CardHeader>
                <CardTitle>Branch Prediction</CardTitle>
              </CardHeader>
              <CardContent>
                <fieldset disabled={disabled}>
                  <FormInput
                    {...simpleRegister('btbSize', { valueAsNumber: true })}
                  />
                  <FormInput
                    {...simpleRegister('phtSize', { valueAsNumber: true })}
                  />
                  <RadioInputWithTitle
                    {...radioRegister('predictorType')}
                    choices={predictorTypes}
                    texts={['Zero bit', 'One bit', 'Two bit']}
                  />
                  <RadioInputWithTitle
                    {...radioRegister('predictorDefaultState')}
                    choices={
                      watchPredictorType === 'TWO_BIT_PREDICTOR'
                        ? [0, 1, 2, 3]
                        : [0, 1]
                    }
                    texts={
                      watchPredictorType === 'TWO_BIT_PREDICTOR'
                        ? fourStatePredictor
                        : twoStatePredictor
                    }
                  />
                  <div className='flex gap-2 items-center m-2'>
                    <input
                      {...register('useGlobalHistory')}
                      id='useGlobalHistory'
                      type='checkbox'
                      className='m-2'
                    />
                    <Label htmlFor='useGlobalHistory'>
                      {isaFormMetadata.useGlobalHistory.title}
                    </Label>
                  </div>
                </fieldset>
              </CardContent>
            </Card>
          </TabsContent>
        </div>
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
  control: Control<CpuConfig>;
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
              <div>Base Latency</div>
              <div>Operations</div>
              <div />
            </div>
            <div className='contents'>
              {funits.map((fu, i) => {
                let third = null;
                if (isArithmeticUnitConfig(fu)) {
                  third = fu.operations.map((op) => {
                    const meta = opsMetadata[op.name];
                    return (
                      <div
                        key={op.name}
                        title={meta.name}
                        className='rounded bg-gray-200 px-1 py-0.5 whitespace-nowrap snap-always snap-start'
                      >
                        {capabilitiesMetadata[op.name as Operations].name} (
                        {op.latency})
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
                    <div className='flex flex-grow gap-1 items-center text-sm overflow-x-scroll snap-x'>
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

type CapabilityPicker = {
  [key in Operations]: {
    name: key;
    picked: boolean;
    latency: number;
  };
};

// Subform for adding a new FU
// Controls the fUnits field
function FUAdder({ control }: { control: Control<CpuConfig> }) {
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

  const [capabilities, setCapabilities] = React.useState<CapabilityPicker>({
    addition: { name: 'addition', picked: false, latency: 1 },
    bitwise: { name: 'bitwise', picked: false, latency: 1 },
    division: { name: 'division', picked: false, latency: 1 },
    multiplication: { name: 'multiplication', picked: false, latency: 1 },
    special: { name: 'special', picked: false, latency: 1 },
  });

  const selectedUnitType = watch('fuType');
  const enableOps = selectedUnitType === 'FX' || selectedUnitType === 'FP';

  const addFU: MouseEventHandler = (_e) => {
    // generate integer ID
    const id = Math.floor(Math.random() * 1000000);
    setValue('id', id);
    const data = getValues();
    if (data.fuType === 'FX' || data.fuType === 'FP') {
      data.operations = Object.entries(capabilities)
        .filter(([, value]) => value.picked)
        .map(([key, value]) => {
          return {
            name: key as Operations,
            latency: value.latency,
          };
        });
    }
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
      <div className='mt-4'>
        <div className='flex justify-evenly gap-4'>
          <FormInput
            {...register('name')}
            name='name'
            title='Name'
            error={errors.name}
          />
          <FormInput
            {...register('latency', { valueAsNumber: true })}
            name='latency'
            title={enableOps ? 'Base latency' : 'Latency'}
            error={errors.latency}
          />
        </div>
        <div className={enableOps ? '' : 'hidden'}>
          <Label className='text text-lg'>Supported operations</Label>
          <div>
            {Object.entries(capabilities).map(([op, cap]) => {
              const id = `chkbx-${op}`;
              return (
                <div key={op} className='flex items-center'>
                  <Checkbox
                    className='m-2'
                    id={id}
                    value={op}
                    checked={cap.picked}
                    onCheckedChange={(checked) => {
                      setCapabilities({
                        ...capabilities,
                        [op]: { ...cap, picked: checked },
                      });
                    }}
                  />
                  <Label htmlFor={id} className='flex-grow'>
                    {capabilitiesMetadata[op as Operations].additional}
                  </Label>
                  <FormInput
                    value={cap.latency}
                    onChange={(e) => {
                      setCapabilities({
                        ...capabilities,
                        [op]: {
                          ...cap,
                          latency: e.target.valueAsNumber || 0,
                        },
                      });
                    }}
                    name={`latency-${op}`}
                    type='number'
                    title='Latency'
                    error={errors.latency}
                    disabled={!cap.picked}
                  />
                </div>
              );
            })}
          </div>
        </div>
      </div>
      <Button type='button' onClick={addFU}>
        Add Unit
      </Button>
    </div>
  );
}
