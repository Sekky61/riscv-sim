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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/base/ui/select';
import { FormInput } from '@/components/form/FormInput';
import {
  MemoryLocation,
  dataTypes,
  memoryLocation,
  memoryLocationDefaultValue,
} from '@/lib/forms/Isa';
import { useAppDispatch } from '@/lib/redux/hooks';
import { addMemoryLocation } from '@/lib/redux/isaSlice';
import { DataTypeEnum } from '@/lib/types/cpuApi';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';

export default function MemoryForm() {
  const dispatch = useAppDispatch();
  const form = useForm<MemoryLocation>({
    resolver: zodResolver(memoryLocation),
    defaultValues: memoryLocationDefaultValue,
    mode: 'onChange',
  });

  const { register, handleSubmit, formState } = form;

  const onSubmit = (data: MemoryLocation) => {
    dispatch(addMemoryLocation(data));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col'>
        <div className='flex flex-row'>
          <FormInput
            name='name'
            title='Name'
            register={register}
            error={formState.errors.name}
          />
          <Select
            onValueChange={(e: DataTypeEnum) => {
              form.setValue('dataType', e);
            }}
            defaultValue={'kBool'}
          >
            <SelectTrigger>
              <SelectValue placeholder='Select a verified email to display' />
            </SelectTrigger>
            <SelectContent>
              {dataTypes.map((item) => (
                <SelectItem key={item} value={item}>
                  {item}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className='flex flex-row'>
          <FormInput
            type='number'
            name='alignment'
            title='Alignment'
            register={register}
            error={formState.errors.alignment}
          />
        </div>
      </div>
      <div className='flex flex-row justify-end'>
        <Button type='submit' disabled={!formState.isValid}>
          Save
        </Button>
      </div>
    </form>
  );
}
