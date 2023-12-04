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
  MemoryLocationForm,
  MemoryLocationApi,
  dataTypes,
  dataTypesText,
  memoryLocation,
  memoryLocationDefaultValue,
  memoryLocationFormDefaultValue,
  memoryLocationWithSource,
  DataChunk,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  addMemoryLocation,
  removeMemoryLocation,
  selectActiveIsa,
  updateMemoryLocation,
} from '@/lib/redux/isaSlice';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

interface FileMetadata {
  name: string;
  size: number;
  type: string;
  data: string[];
}

async function readFromFile(
  file: File,
  callback: (result: FileMetadata) => void,
) {
  const reader = new FileReader();
  reader.onload = (e) => {
    const content = e.target?.result;
    if (typeof content === 'string') {
      // parse file content as csv
      const numbers = parseCsv(content);
      if (numbers) {
        // if csv is valid, set the value
        callback({
          name: file.name,
          size: file.size,
          type: file.type,
          data: numbers,
        });
      }
    } else {
      console.warn('File content is not string');
    }
  };
  reader.readAsText(file);
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

export default function MemoryForm({
  existing,
  memoryLocationName,
  deleteCallback,
}: MemoryFormProps) {
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveIsa);
  const form = useForm<MemoryLocationForm>({
    resolver: zodResolver(memoryLocationWithSource),
    defaultValues: memoryLocationFormDefaultValue,
    mode: 'onChange',
  });
  const [fileMetadata, setFileMetadata] = useState<FileMetadata>({
    name: '',
    size: 0,
    type: '',
    data: [],
  });
  const [fileChanged, setFileChanged] = useState(false);
  const watchFields = form.watch();
  const { register, handleSubmit, formState, reset } = form;

  // watch for changes in the active memory location
  useEffect(() => {
    // load the memory location
    const memoryLocation = activeIsa.memoryLocations.find(
      (ml) => ml.name === memoryLocationName,
    );
    if (memoryLocation) {
      reset(memoryLocation);
    } else if (memoryLocationName === 'new') {
      reset(memoryLocationFormDefaultValue);
    } else {
      throw new Error(`Memory location ${memoryLocationName} not found`);
    }
  }, [memoryLocationName, activeIsa, reset]);

  const canSubmit =
    formState.isValid &&
    (activeIsa.memoryLocations.find((ml) => ml.name === watchFields.name) ===
      undefined ||
      existing) &&
    (formState.isDirty || fileChanged);

  const onSubmit = (data: MemoryLocationForm) => {
    // random data
    if (data.dataSource === 'random') {
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
      const chunk: DataChunk = {
        dataType: data.dataType,
        values: fileMetadata.data,
      };
      data.dataChunks = [chunk];
    }

    setFileChanged(false);
    if (existing) {
      dispatch(
        updateMemoryLocation({
          oldName: memoryLocationName,
          memoryLocation: data,
        }),
      );
    } else {
      dispatch(addMemoryLocation(data));
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      readFromFile(file, (result) => {
        setFileMetadata(result);
        setFileChanged(true);
      });
    } else {
      console.warn('No file selected');
    }
  };

  let valueMetadata = {
    count: 0,
  };
  if (watchFields.dataSource === 'constant') {
    valueMetadata = {
      count: watchFields.dataLength || 0,
    };
  }
  if (watchFields.dataSource === 'random') {
    valueMetadata = {
      count: watchFields.dataLength || 0,
    };
  }
  if (watchFields.dataSource === 'file') {
    valueMetadata = {
      count: watchFields.dataChunks?.[0]?.values.length || 0,
    };
  }

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
        <div className='h-20'>
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
                type='file'
                name='file'
                title='File'
                id='file'
                className='hidden'
                accept='.csv'
                onChange={(e) => {
                  handleFileChange(e);
                }}
              />
              <Label
                htmlFor='file'
                className={buttonVariants({ variant: 'outline' })}
              >
                {fileMetadata.name ? `${fileMetadata.name} ✓` : 'Select file'}
              </Label>
              <div>Size: {fileMetadata.size} bytes</div>
            </div>
          )}
        </div>
        <div className='flex flex-col'>
          <h3>Loaded values</h3>
          <div className='flex flex-col'>Count: {valueMetadata.count}</div>
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
          disabled={!canSubmit}
        >
          {existing ? 'Update' : 'Create'}
        </Button>
      </div>
    </form>
  );
}
