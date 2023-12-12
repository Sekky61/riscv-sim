/**
 * @file    MemoryForm.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Component for defining a memory location
 *
 * @date    28 November 2023, 22:00 (created)
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

import { Button, buttonVariants } from '@/components/base/ui/button';
import { Card } from '@/components/base/ui/card';
import { Input } from '@/components/base/ui/input';
import { Label } from '@/components/base/ui/label';
import { FormInput } from '@/components/form/FormInput';
import { RadioInputWithTitle } from '@/components/form/RadioInput';
import { parseCsv } from '@/lib/csv';
import {
  dataTypes,
  dataTypesText,
  memoryLocation,
  memoryLocationDefaultValue,
  DataChunk,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  addMemoryLocation,
  selectActiveIsa,
  updateMemoryLocation,
} from '@/lib/redux/isaSlice';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import {
  Control,
  FieldErrors,
  Resolver,
  useForm,
  useWatch,
} from 'react-hook-form';
import { z } from 'zod';

/**
 * Expand the memoryLocation form with a data input
 */
export const memoryLocationWithSource = memoryLocation.extend({
  dataType: z.enum(dataTypes),
  dataSource: z.enum(['constant', 'random', 'file']),
  file: z.any().transform((val) => {
    if (!val || val.length === 0) {
      return undefined;
    }
    return val[0];
  }),
  constant: z.number().optional(),
  dataLength: z.number().min(1).optional(),
});
export type MemoryLocationForm = z.infer<typeof memoryLocationWithSource>;

export const memoryLocationFormDefaultValue: MemoryLocationForm = {
  ...memoryLocationDefaultValue,
  dataType: 'kInt',
  dataSource: 'constant',
  constant: 0,
  dataLength: 1,
  file: [],
};

/**
 * Filter out fields not belonging to memory location
 */
export function memoryLocationFormToIsa(
  memoryLocation: MemoryLocationForm,
): object {
  return {
    name: memoryLocation.name,
    alignment: memoryLocation.alignment,
    dataChunks: memoryLocation.dataChunks.map((dataChunk) => ({
      dataType: dataChunk.dataType,
      values: dataChunk.values,
    })),
    dataType: memoryLocation.dataType,
    dataSource: memoryLocation.dataSource,
    constant: memoryLocation.constant,
    dataLength: memoryLocation.dataLength,
  };
}

async function readFromFile(file: File): Promise<string[]> {
  return file.text().then((text) => {
    return parseCsv(text);
  });
}

// props
interface MemoryFormProps {
  /**
   * True if the memory location already exists (vs new one)
   */
  existing: boolean;
  memoryLocationName: string;
  deleteCallback: () => void;
}

function isNotEmpty<TValue extends object>(
  // biome-ignore lint/complexity/noBannedTypes: this is what zod returns...
  value: TValue | {},
): value is TValue {
  return Object.keys(value).length > 0;
}

/**
 * Component for defining a memory location
 */
export default function MemoryForm({
  existing,
  memoryLocationName,
  deleteCallback,
}: MemoryFormProps) {
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveIsa);

  // Custom resolver
  const resolver: Resolver<MemoryLocationForm> = async (
    values: MemoryLocationForm,
    ...args
  ) => {
    // Let zod do its thing
    const r = zodResolver(memoryLocationWithSource);
    const val = await r(values, ...args);

    if (isNotEmpty(val.errors)) {
      return val;
    }
    // Additional checks
    const errors: FieldErrors<MemoryLocationForm> = {};

    // Name is unique. If we are updating, the name can stay the same
    const memoryLocation = activeIsa.memoryLocations.find(
      (ml) => ml.name === values.name,
    );
    // found, but ignore if we are updating
    // todo: bug when updating memory
    if (
      memoryLocation &&
      !(existing && memoryLocation.name === memoryLocationName)
    ) {
      console.log('name already exists', memoryLocation, memoryLocationName);
      errors.name = {
        type: 'manual',
        message: 'Name already exists',
      };
    }

    console.log(errors);

    return {
      values: val.values,
      errors,
    };
  };

  const form = useForm<MemoryLocationForm>({
    resolver,
    defaultValues: memoryLocationFormDefaultValue,
    mode: 'onChange',
  });
  const { register, handleSubmit, formState, reset, trigger } = form;
  const watchFields = form.watch();
  const { errors, isDirty, isValid } = formState;

  // load the memory location
  const memoryLocation = activeIsa.memoryLocations.find(
    (ml) => ml.name === memoryLocationName,
  );
  // get file metadata
  const file = watchFields.file?.[0];

  // watch for changes in the active memory location
  useEffect(() => {
    trigger(); // Validate the form
    if (memoryLocation) {
      reset(memoryLocation);
    } else if (memoryLocationName === 'new') {
      reset(memoryLocationFormDefaultValue);
    } else {
      throw new Error(`Memory location ${memoryLocationName} not found`);
    }
  }, [memoryLocation, memoryLocationName, reset, trigger]);

  const onSubmit = async (data: MemoryLocationForm) => {
    // random data - generate random values
    if (data.dataSource === 'random') {
      // todo: random floats, chars
      const chunk: DataChunk = {
        dataType: data.dataType,
        values: Array.from({ length: data.dataLength ?? 0 }, () =>
          Math.floor(Math.random() * 256).toString(),
        ),
      };
      data.dataChunks = [chunk];
    }
    // constant data
    if (data.dataSource === 'constant') {
      const constant = data.constant?.toString() || '0';
      const chunk: DataChunk = {
        dataType: data.dataType,
        values: Array.from({ length: data.dataLength ?? 0 }, () => constant),
      };
      data.dataChunks = [chunk];
    }
    if (data.dataSource === 'file') {
      const file = data.file;
      if (!file) {
        throw new Error('File not selected');
      }
      const values = await readFromFile(file);

      const chunk: DataChunk = {
        dataType: data.dataType,
        values,
      };
      data.dataChunks = [chunk];
    }

    // Filter just fields for ISA
    const filtered = memoryLocationFormToIsa(data);

    if (existing) {
      dispatch(
        updateMemoryLocation({
          oldName: memoryLocationName,
          memoryLocation: filtered,
        }),
      );
    } else {
      dispatch(addMemoryLocation(filtered));
    }
  };

  console.log(errors);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col gap-4'>
        <FormInput
          name='name'
          title='Name'
          register={register}
          error={formState.errors.name}
        />
        <RadioInputWithTitle
          name='dataType'
          register={register}
          title='Data Type'
          choices={dataTypes}
          texts={dataTypesText}
        />
        <FormInput
          type='number'
          name='alignment'
          title='Alignment'
          register={register}
          error={formState.errors.alignment}
        />
        <RadioInputWithTitle
          name='dataSource'
          register={register}
          title='Data Source'
          choices={['constant', 'random', 'file']}
          texts={['Constant', 'Random', 'File']}
        />
      </div>
      <Card className='my-4 p-4'>
        <h2 className='text-xl mb-4'>Data</h2>
        <div className='h-24'>
          {watchFields.dataSource === 'constant' && (
            <div className='flex justify-evenly'>
              <FormInput
                type='number'
                name='constant'
                title='Constant'
                register={register}
                error={formState.errors.constant}
              />
              <FormInput
                type='number'
                name='dataLength'
                title='Data Size'
                register={register}
                error={formState.errors.dataLength}
              />
            </div>
          )}
          {watchFields.dataSource === 'random' && (
            <div className='flex flex-col'>
              <FormInput
                type='number'
                name='dataLength'
                title='Data Size'
                register={register}
                error={formState.errors.dataLength}
              />
            </div>
          )}
          {watchFields.dataSource === 'file' && (
            <div className='flex flex-col'>
              <Input
                title='File'
                id='file'
                type='file'
                className='hidden'
                accept='.csv'
                {...register('file')}
              />
              <Label
                htmlFor='file'
                className={buttonVariants({ variant: 'outline' })}
              >
                {file?.name ? `${file.name} ✓` : 'Select file'}
              </Label>
              <div className='mt-4'>Size: {file?.size || '-'} bytes</div>
            </div>
          )}
        </div>
        <div className='flex flex-col'>
          <h3>Loaded values</h3>
          <div className='flex flex-col'>Count: lul</div>
        </div>
      </Card>
      <div className='relative'>
        {existing && (
          <Button
            className='absolute left-0'
            type='button'
            variant='destructive'
            onClick={deleteCallback}
          >
            Delete
          </Button>
        )}
        <Button
          className='absolute right-0'
          type='submit'
          disabled={!isDirty || !isValid}
        >
          {existing ? 'Update' : 'Create'}
        </Button>
      </div>
    </form>
  );
}
