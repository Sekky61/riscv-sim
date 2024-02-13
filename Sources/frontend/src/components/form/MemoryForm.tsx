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
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
} from '@/components/base/ui/card';
import { Input } from '@/components/base/ui/input';
import { Label } from '@/components/base/ui/label';
import { FormInput } from '@/components/form/FormInput';
import { RadioInputWithTitle } from '@/components/form/RadioInput';
import { parseCsv } from '@/lib/csv';
import {
  MemoryLocationApi,
  dataTypes,
  dataTypesText,
  memoryLocationDefaultValue,
  memoryLocationSchema,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  addMemoryLocation,
  selectActiveConfig,
  updateMemoryLocation,
} from '@/lib/redux/isaSlice';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import {
  FieldError,
  FieldErrors,
  FieldValues,
  Resolver,
  UseControllerProps,
  useController,
  useForm,
} from 'react-hook-form';
import { toast } from 'sonner';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/base/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/base/ui/select';
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/base/ui/form';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';

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
 * Convenience function to get an error from a nested object
 */
function getError(
  errors: FieldErrors<MemoryLocationApi>,
  field: string[],
): FieldError | undefined {
  let current: unknown = errors;
  for (const f of field) {
    //@ts-ignore
    if (f in current) {
      //@ts-ignore
      current = current[f];
    } else {
      return undefined;
    }
  }
  return undefined;
}

/**
 * Component for defining a memory location.
 *
 * Data can be defined in three ways:
 * - constant: a single value is repeated
 * - random: random values are generated in an inclusive range
 * - data: a literal array of values. Can be fulled manually or loaded from a CSV file
 *
 * Note: React-hook-form cannot handle the and/or from the zoo schema, so types have to be defined manually.
 * (https://github.com/react-hook-form/react-hook-form/issues/9287)
 */
export default function MemoryForm({
  existing,
  memoryLocationName,
  deleteCallback,
}: MemoryFormProps) {
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);
  // File selected to fill the memory
  const [file, setFile] = useState<File | undefined>();
  const [activeTab, setActiveTab] = useState<'data' | 'constant' | 'random'>(
    memoryLocationDefaultValue.data.kind,
  );

  // Custom resolver
  const resolver: Resolver<MemoryLocationApi> = async (
    values: MemoryLocationApi,
    ...args
  ) => {
    // Let zod do its thing
    const r = zodResolver(memoryLocationSchema);
    const val = await r(values, ...args);

    const validation = memoryLocationSchema.safeParse(values);

    console.log('val', val, values);

    if (isNotEmpty(val.errors)) {
      return val;
    }
    // Additional checks
    const errors: FieldErrors<MemoryLocationApi> = {};

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

    return {
      values: val.values,
      errors,
    };
  };

  const form = useForm<MemoryLocationApi>({
    resolver,
    defaultValues: memoryLocationDefaultValue,
    mode: 'onChange',
  });
  const { register, handleSubmit, formState, reset, trigger, control } = form;
  const watchFields = form.watch();
  const { errors, isDirty, isValid } = formState;

  // load the memory location
  const memoryLocation = activeIsa.memoryLocations.find(
    (ml) => ml.name === memoryLocationName,
  );

  // watch for changes in the active memory location
  useEffect(() => {
    if (memoryLocation) {
      reset(memoryLocation);
    } else if (memoryLocationName === 'new') {
      reset(memoryLocationDefaultValue);
    } else {
      return; // Do not trigger validation
    }
    trigger(); // Validate the form, after reset
  }, [memoryLocation, memoryLocationName, reset, trigger]);

  const onSubmit = async (data: MemoryLocationApi) => {
    if (existing) {
      dispatch(
        updateMemoryLocation({
          oldName: memoryLocationName,
          memoryLocation: data,
        }),
      );
      toast.success(`Memory location ${memoryLocationName} updated.`);
    } else {
      dispatch(addMemoryLocation(data));
      toast.success(`Memory location ${data.name} created.`);
    }
  };

  const randomTrigger = () => {
    trigger('data');
  };

  console.log(watchFields);
  console.log('errors', errors);
  console.log(activeTab);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col gap-4 justify-start'>
        <FormInput
          title='Pointer Name'
          hint='Name of the memory location. Will be used as the pointer name in C/assembler.'
          {...register('name')}
          error={formState.errors.name}
        />
        <div className='flex mb-4'>
          <FormInput
            type='number'
            title='Type Start Offset'
            {...register('dataTypes.0.startOffset', { valueAsNumber: true })}
            error={formState.errors.dataTypes?.[0]?.startOffset}
          />
          {/* <RadioInputWithTitle
            name='dataTypes.0.dataType'
            title='Data type'
            hint='Data type dictates the interpretation of provided values. For example, choosing Integer will allocate 4 bytes for each value.'
            control={control}
            choices={dataTypes}
            texts={dataTypesText}
          /> */}
          <SelectInput control={control} name='dataTypes.0.dataType' />
        </div>
        <FormInput
          type='number'
          title='Alignment (log Bytes)'
          hint='The array will be aligned to this boundary. The .align directive in assembler works the same way. For example, 3 will align the array to 2^3 = 8 bytes.'
          {...register('alignment', { valueAsNumber: true })}
          error={formState.errors.alignment}
        />
      </div>
      <h2 className='text-xl mb-4'>Values</h2>
      <Tabs
        defaultValue={activeTab}
        className=''
        onValueChange={(value) => {
          // Save the information about the current tab to a state.
          // When submitting, the fields from the current tab will be used, others will be filtered out.
          setActiveTab(value as 'data' | 'constant' | 'random');
          form.setValue('data.kind', value as 'data' | 'constant' | 'random');
        }}
      >
        <TabsList>
          <TabsTrigger value='data'>A List of Values</TabsTrigger>
          <TabsTrigger value='constant'>A Repeated Constant</TabsTrigger>
          <TabsTrigger value='random'>Random Values</TabsTrigger>
        </TabsList>
        <TabsContent value='data'>
          <Card>
            <CardHeader>
              <CardDescription>
                Fill the values manually or load them from a CSV file.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Input
                title='File'
                id='fileId'
                type='file'
                className='hidden'
                accept='.csv'
                onChange={(e) => {
                  const file = getFileFromFileList(e.target.files);
                  if (file && isFile(file)) {
                    setFile(file);
                    readFromFile(file).then((data) => {
                      form.setValue('data.kind', 'data');
                      form.setValue('data.data', data);
                    });
                  }
                }}
              />
              <Label
                htmlFor='fileId'
                className={buttonVariants({ variant: 'outline' })}
              >
                {file?.name ? `${file.name} ✓` : 'Select file'}
              </Label>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='constant'>
          <Card>
            <CardHeader>
              <CardDescription>
                The selected constant value will be duplicated a specified
                number of times.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className='flex justify-evenly'>
                <FormInput
                  title='Constant'
                  {...register('data.constant')}
                  error={getError(errors, ['data', 'constant'])}
                />
                <FormInput
                  type='number'
                  title='Number of Elements'
                  {...register('data.size', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'size'])}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent
          value='random'
          onClick={randomTrigger}
          onFocus={randomTrigger}
          onBlur={randomTrigger}
        >
          <Card>
            <CardHeader>
              <CardDescription>
                Random data will be generated after each time the memory
                location is saved.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className='flex gap-4 justify-evenly'>
                <FormInput
                  type='number'
                  title='Lower bound of random range'
                  {...register('data.min', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'min'])}
                />
                <FormInput
                  type='number'
                  title='Upper bound of random range'
                  {...register('data.max', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'max'])}
                />
                <FormInput
                  type='number'
                  title='Number of Elements'
                  {...register('data.size', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'size'])}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      <div className='flex justify-between p-4'>
        <Button type='submit' disabled={!isDirty || !isValid}>
          {existing ? 'Update' : 'Create'}
        </Button>
        {existing && (
          <Button type='button' variant='destructive' onClick={deleteCallback}>
            Delete
          </Button>
        )}
      </div>
    </form>
  );
}

/**
 * Can be used for textual input
 */
function SelectInput(props: UseControllerProps<MemoryLocationApi>) {
  const { field, fieldState } = useController(props);

  const name = 'dataTypes.0.dataType';
  const title = 'Data Type';
  const hint =
    'Data type dictates the interpretation of provided values. For example, choosing Integer will allocate 4 bytes for each value.';

  return (
    <div>
      {hint ? (
        <Tooltip>
          <TooltipTrigger>
            <Label htmlFor={name}>{title}&nbsp;&#9432;</Label>
          </TooltipTrigger>
          <TooltipContent>
            <p>{hint}</p>
          </TooltipContent>
        </Tooltip>
      ) : (
        <Label htmlFor={name}>{title}</Label>
      )}
      <Select onValueChange={field.onChange} value={field.value}>
        <SelectTrigger className='w-[180px]'>
          <SelectValue placeholder='Select a Type' />
        </SelectTrigger>
        <SelectContent>
          {dataTypes.map((dt, i) => {
            const name = dataTypesText[i];
            return <SelectItem value={dt}>{name}</SelectItem>;
          })}
        </SelectContent>
      </Select>
    </div>
  );
}
