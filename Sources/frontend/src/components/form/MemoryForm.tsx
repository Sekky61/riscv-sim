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
  DataChunk,
  dataTypes,
  dataTypesText,
  memoryLocationDefaultValue,
  memoryLocationIsa,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  addMemoryLocation,
  selectActiveConfig,
  updateMemoryLocation,
} from '@/lib/redux/isaSlice';
import { ErrorMessage } from '@hookform/error-message';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { FieldErrors, Resolver, useForm } from 'react-hook-form';
import { notify } from 'reapop';
import { z } from 'zod';

/**
 * @param fileList File list is interface of object that is returned by input[type=file]
 * @returns The first file, if it exists
 */
function getFileFromFileList(fileList: unknown): File | undefined {
  if (
    typeof fileList === 'object' &&
    fileList !== null &&
    'item' in fileList &&
    typeof fileList.item === 'function'
  ) {
    return fileList.item(0);
  }
}

function isPowerOfTwo(n: number) {
  return (n & (n - 1)) === 0;
}

/**
 * Expand the memoryLocation form with a data input.
 * These are internal fields, not part of the ISA.
 */
const memoryLocationWithSource = memoryLocationIsa.extend({
  /**
   * File is of type any, because FileList is not a class.
   * The getFileFromFileList transform function will convert it to (File | undefined).
   *
   * There is a test file in `src/lib/__tests__/test.csv`.
   */
  file: z.any(),
  constant: z.number().optional(),
  /**
   * Expose alignment to the user - the isa expects the exponent, not the actual value
   */
  alignment: z
    .number()
    .min(1)
    .max(65536)
    .refine((v) => isPowerOfTwo(v), {
      message: 'Must be a power of 2',
    }),
  dataLength: z.number().min(1).optional(),
});
type MemoryLocationForm = z.infer<typeof memoryLocationWithSource>;

/**
 * Converts the exponent, adds default values if needed
 */
function isaMemLocationToForm(memLoc?: MemoryLocationForm): MemoryLocationForm {
  let memoryLocation = memLoc;
  if (memoryLocation === undefined) {
    memoryLocation = {
      ...memoryLocationDefaultValue,
      dataType: 'kInt',
      dataSource: 'constant',
      constant: 0,
      dataLength: 1,
      file: null,
    };
  }
  const actualAlignment = 2 ** memoryLocation.alignment;

  return {
    ...memoryLocation,
    alignment: actualAlignment,
  };
}

/**
 * Filter out fields not belonging to memory location
 */
export function memoryLocationFormToIsa(memoryLocation: MemoryLocationForm) {
  const alignmentExponent = Math.log2(memoryLocation.alignment);
  return {
    name: memoryLocation.name,
    alignment: alignmentExponent,
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

/**
 * Read file contents, parse it as csv, return as 1D array of strings
 */
async function readFromFile(file: File): Promise<string[]> {
  return file.text().then((text) => {
    return parseCsv(text);
  });
}

/**
 * Check if obj is a File
 * @param obj argument to check
 * @returns True if obj is a File
 */
function isFile(obj: unknown): obj is File {
  return obj instanceof File;
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
 * Component for defining a memory location.
 *
 * For random data, the data is generated on submit. This means that chnging the name of the memory location
 * will generate new random data.
 */
export default function MemoryForm({
  existing,
  memoryLocationName,
  deleteCallback,
}: MemoryFormProps) {
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);

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
    if (memoryLocation && memoryLocation.name !== memoryLocationName) {
      errors.name = {
        type: 'manual',
        message: 'Name already exists',
      };
    }

    // If file source, check if file is selected
    if (values.dataSource === 'file') {
      // Watch out - the file list is checked, transform has not been applied yet
      const file = getFileFromFileList(values.file);
      if (!isFile(file)) {
        errors.file = {
          type: 'manual',
          message: 'File not selected',
        };
      }
    }

    return {
      values: val.values,
      errors,
    };
  };

  const form = useForm<MemoryLocationForm>({
    resolver,
    defaultValues: isaMemLocationToForm(),
    mode: 'onChange',
  });
  const { register, handleSubmit, formState, reset, trigger, control } = form;
  const watchFields = form.watch();
  const { errors, isDirty, isValid } = formState;

  // load the memory location
  const memoryLocation = activeIsa.memoryLocations.find(
    (ml) => ml.name === memoryLocationName,
  );
  // get file metadata
  const file = getFileFromFileList(watchFields.file);

  // watch for changes in the active memory location
  useEffect(() => {
    if (memoryLocation) {
      reset(isaMemLocationToForm(memoryLocation));
    } else if (memoryLocationName === 'new') {
      reset(isaMemLocationToForm());
    } else {
      return; // Do not trigger validation
    }
    trigger(); // Validate the form, after reset
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
      const file = getFileFromFileList(data.file);
      if (!isFile(file)) {
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
      dispatch(
        notify({
          title: `Memory location ${memoryLocationName} updated.`,
          message: 'Reload the simulation to see the changes.',
          status: 'success',
        }),
      );
    } else {
      dispatch(addMemoryLocation(filtered));
      dispatch(
        notify({
          title: `Memory location ${data.name} created.`,
          message: 'Reload the simulation to see the changes.',
          status: 'success',
        }),
      );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col gap-4'>
        <FormInput
          title='Pointer Name'
          hint='Name of the memory location. Will be used as the pointer name in C/assembler.'
          {...register('name')}
          error={formState.errors.name}
        />
        <RadioInputWithTitle
          name='dataType'
          title='Data type'
          hint='Data type dictates the interpretation of provided values. For example, choosing Integer will allocate 4 bytes for each value.'
          control={control}
          choices={dataTypes}
          texts={dataTypesText}
        />
        <FormInput
          type='number'
          title='Alignment (Bytes)'
          hint='The array will be aligned to this boundary. The .align directive in assembler works differently!'
          {...register('alignment', { valueAsNumber: true })}
          error={formState.errors.alignment}
        />
        <RadioInputWithTitle
          name='dataSource'
          title='Data Source'
          choices={['constant', 'random', 'file']}
          texts={['A Constant', 'Random Numbers', 'File']}
          control={control}
        />
      </div>
      <Card className='my-4 p-4'>
        <h2 className='text-xl mb-4'>Data</h2>
        <div className='h-28'>
          {watchFields.dataSource === 'constant' && (
            <div>
              <p className='text-gray-700'>
                The selected constant value will be duplicated a specified
                number of times.
              </p>
              <div className='flex justify-evenly'>
                <FormInput
                  type='number'
                  title='Constant'
                  {...register('constant', { valueAsNumber: true })}
                  error={formState.errors.constant}
                />
                <FormInput
                  type='number'
                  title='Number of Elements'
                  {...register('dataLength', { valueAsNumber: true })}
                  error={formState.errors.dataLength}
                />
              </div>
            </div>
          )}
          {watchFields.dataSource === 'random' && (
            <div>
              <p className='text-gray-700'>
                Random data will be generated on submit.
              </p>
              <FormInput
                type='number'
                title='Number of Elements'
                {...register('dataLength', { valueAsNumber: true })}
                error={formState.errors.dataLength}
              />
            </div>
          )}
          {watchFields.dataSource === 'file' && (
            <div>
              <p className='text-gray-700'>
                Choose a CSV file as the data source. The table may be of any
                shape.
              </p>
              <Input
                title='File'
                id='fileId'
                type='file'
                className='hidden'
                accept='.csv'
                {...register('file')}
              />
              <Label
                htmlFor='fileId'
                className={buttonVariants({ variant: 'outline' })}
              >
                {file?.name ? `${file.name} ✓` : 'Select file'}
              </Label>
              <div className='mt-4'>
                {errors.file === undefined && (
                  <span>Size: {file?.size || '-'} bytes</span>
                )}
                <ErrorMessage
                  errors={errors}
                  name='file'
                  render={({ message }) => (
                    <span className='text-red-600'>{message}</span>
                  )}
                />
              </div>
            </div>
          )}
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
