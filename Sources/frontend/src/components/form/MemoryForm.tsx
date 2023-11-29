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

import { Button } from '@/components/base/ui/button';
import { Card } from '@/components/base/ui/card';
import { Input } from '@/components/base/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/base/ui/select';
import { FormInput } from '@/components/form/FormInput';
import { RadioInput, RadioInputWithTitle } from '@/components/form/RadioInput';
import {
  MemoryLocationFormValue,
  dataTypes,
  dataTypesText,
  memoryLocation,
  memoryLocationDefaultValue,
} from '@/lib/forms/Isa';
import { useAppDispatch } from '@/lib/redux/hooks';
import { addMemoryLocation } from '@/lib/redux/isaSlice';
import { DataTypeEnum } from '@/lib/types/cpuApi';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

/**
 * Expand the memoryLocation form with a data input
 */
const memoryLocationWithDataType = memoryLocation.extend({
  dataSource: z.enum(['constant', 'random', 'file']),
  constant: z.number().optional(),
});
type MemoryLocationForm = z.infer<typeof memoryLocationWithDataType>;

export default function MemoryForm() {
  const dispatch = useAppDispatch();
  const form = useForm<MemoryLocationForm>({
    resolver: zodResolver(memoryLocationWithDataType),
    defaultValues: {
      ...memoryLocationDefaultValue,
      dataSource: 'constant',
    },
    mode: 'onChange',
  });
  const watchDataSource = form.watch('dataSource');

  const { register, handleSubmit, formState } = form;

  const onSubmit = (data: MemoryLocationFormValue) => {
    dispatch(addMemoryLocation(data));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col'>
          <FormInput
            name='name'
            title='Name'
            register={register}
            error={formState.errors.name}
          />
          <RadioInputWithTitle name='dataType' register={register} title='Data Type' choices={dataTypes} texts={dataTypesText} />
          <FormInput
            type='number'
            name='alignment'
            title='Alignment'
            register={register}
            error={formState.errors.alignment}
          />
          <RadioInputWithTitle name='dataSource' register={register} title='Data Source' choices={['constant', 'random', 'file']} texts={['Constant', 'Random', 'File']} />
      </div>
      <Card>
        <h2>Value</h2>
        {watchDataSource === 'constant'  && (
          <FormInput
            type='number'
            name='constant'
            title='Constant'
            register={register}
            error={formState.errors.constant}
          />
        )}
        {watchDataSource === 'random' && (
          <div className='flex flex-col'>
            -
          </div>
        )}
        {watchDataSource === 'file' && (
          <div className='flex flex-col'>
            <Input
              type='file'
              name='file'
              title='File'
            />
          </div>
        )}
      </Card>
      <div className='flex flex-row justify-end'>
        <Button type='submit' disabled={!formState.isValid}>
          Save
        </Button>
      </div>
    </form>
  );
}
