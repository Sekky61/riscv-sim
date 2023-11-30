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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/base/ui/select';
import { FormInput } from '@/components/form/FormInput';
import { RadioInput, RadioInputWithTitle } from '@/components/form/RadioInput';
import { parseCsv } from '@/lib/csv';
import {
  MemoryLocationFormValue,
  dataTypes,
  dataTypesText,
  memoryLocation,
  memoryLocationDefaultValue,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  addMemoryLocation,
  removeMemoryLocation,
  selectActiveIsa,
} from '@/lib/redux/isaSlice';
import { DataTypeEnum } from '@/lib/types/cpuApi';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { set, useForm } from 'react-hook-form';
import { z } from 'zod';

/**
 * Expand the memoryLocation form with a data input
 */
const memoryLocationWithDataType = memoryLocation.extend({
  dataSource: z.enum(['constant', 'random', 'file']),
  constant: z.number().optional(),
  dataLength: z.number().min(1).optional(),
});
type MemoryLocationForm = z.infer<typeof memoryLocationWithDataType>;

const memoryLocationFormDefaultValue: MemoryLocationForm = {
  ...memoryLocationDefaultValue,
  dataSource: 'constant',
  constant: 0,
  dataLength: 1,
};

// props
interface MemoryFormProps {
  memoryLocationName: string;
}

export default function MemoryForm({ memoryLocationName }: MemoryFormProps) {
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveIsa);
  const form = useForm<MemoryLocationForm>({
    resolver: zodResolver(memoryLocationWithDataType),
    defaultValues: memoryLocationFormDefaultValue,
    mode: 'onChange',
  });
  const [fileMetadata, setFileMetadata] = useState({
    name: '',
    size: 0,
    type: '',
  });
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
    activeIsa.memoryLocations.find((ml) => ml.name === watchFields.name) ===
      undefined;

  const onSubmit = (data: MemoryLocationFormValue) => {
    dispatch(addMemoryLocation(data));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target?.result;
        if (typeof content === 'string') {
          // parse file content as csv
          const numbers = parseCsv(content);
          if (numbers) {
            // if csv is valid, set the value
            form.setValue('bytes', numbers);
            setFileMetadata({
              name: file.name,
              size: file.size,
              type: file.type,
            });
          }
        } else {
          console.warn('File content is not string');
        }
      };
      reader.readAsText(file);
    } else {
      console.warn('No file selected');
    }
  };

  const handleDelete = () => {
    // remove from redux
    dispatch(removeMemoryLocation(watchFields.name));
    // Reset form
    reset(memoryLocationFormDefaultValue);
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
      count: watchFields.bytes?.length || 0,
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
      <div className='flex flex-row justify-between'>
        <Button type='button' variant='destructive' onClick={handleDelete}>
          Delete
        </Button>
        <Button type='submit' disabled={!canSubmit}>
          Save
        </Button>
      </div>
    </form>
  );
}
