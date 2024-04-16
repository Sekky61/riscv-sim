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
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import { Input } from '@/components/base/ui/input';
import { Label } from '@/components/base/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/base/ui/select';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/base/ui/tabs';
import { Textarea } from '@/components/base/ui/textarea';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { ErrorDisplay, FormInput } from '@/components/form/FormInput';
import { parseCsv } from '@/lib/csv';
import {
  type MemoryLocationApi,
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
import {
  dataTypeToSize,
  memoryLocationSizeInElements,
  pluralize,
} from '@/lib/utils';
import { zodResolver } from '@hookform/resolvers/zod';
import clsx from 'clsx';
import { Upload } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  type FieldError,
  type FieldErrors,
  type Resolver,
  type UseControllerProps,
  useController,
  useForm,
} from 'react-hook-form';
import { toast } from 'sonner';

interface MemoryFormProps {
  /**
   * True if the memory location already exists (vs new one)
   */
  existing: boolean;
  memoryLocationName: string;
  deleteCallback: () => void;
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
  const [activeTab, setActiveTab] = useState<'data' | 'constant' | 'random'>(
    memoryLocationDefaultValue.data.kind,
  );

  const {
    register,
    handleSubmit,
    formState,
    reset,
    trigger,
    control,
    watch,
    setValue,
  } = useForm<MemoryLocationApi>({
    resolver,
    defaultValues: memoryLocationDefaultValue,
    mode: 'onChange',
    context: {
      activeIsa,
      memoryLocationName,
    },
  });
  const watchFields = watch();
  const { errors, isDirty, isValid } = formState;

  // watch for changes in the active memory location
  useEffect(() => {
    // load the memory location
    const memoryLocation = activeIsa.memoryLocations.find(
      (ml) => ml.name === memoryLocationName,
    );
    if (memoryLocation) {
      reset(memoryLocation);
      setActiveTab(memoryLocation.data.kind);
    } else if (memoryLocationName === 'new') {
      reset(memoryLocationDefaultValue);
      setActiveTab(memoryLocationDefaultValue.data.kind);
    } else {
      return; // Do not trigger validation
    }
    trigger(); // Validate the form, after reset
  }, [activeIsa, memoryLocationName, reset, trigger]);

  // Called after the form is submitted and validated by the resolver
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

  /**
   * In response to a tab change, set the default values for the fields (if they are empty)
   */
  const onTabChange = (value: 'data' | 'constant' | 'random') => {
    // Save the information about the current tab to a state.
    // When submitting, the fields from the current tab will be used, others will be filtered out.
    setActiveTab(value as 'data' | 'constant' | 'random');
    // Make it dirty manually
    setValue('data.kind', value as 'data' | 'constant' | 'random', {
      shouldDirty: true,
    });

    // Give defaulut values if empty
    if (value === 'constant') {
      //@ts-ignore
      if (watchFields.data.size === undefined) {
        setValue('data.size', 4);
      }
      //@ts-ignore
      if (watchFields.data.constant === undefined) {
        setValue('data.constant', '0');
      }
    }

    if (value === 'random') {
      //@ts-ignore
      if (watchFields.data.size === undefined) {
        setValue('data.size', 4);
      }
      //@ts-ignore
      if (watchFields.data.min === undefined) {
        setValue('data.min', 0);
      }
      //@ts-ignore
      if (watchFields.data.max === undefined) {
        setValue('data.max', 100);
      }
    }

    if (value === 'data') {
      //@ts-ignore
      if (watchFields.data.data === undefined) {
        setValue('data.data', ['1', '2', '3', '4']);
      }
    }
  };

  const dataLengthElements = memoryLocationSizeInElements(watchFields);

  const dataLengthBytes =
    dataLengthElements * dataTypeToSize(watchFields.dataType);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className='flex flex-col gap-4 justify-start'>
        <FormInput
          autoComplete='off'
          title='Pointer Name'
          hint='Name of the memory location. Will be used as the pointer name in C/assembler.'
          {...register('name')}
          error={formState.errors.name}
        />
        <div className='flex gap-8'>
          <div>
            <Tooltip>
              <TooltipTrigger asChild>
                <Label id='dataType-label' htmlFor='dataType'>
                  Data Type&nbsp;&#9432;
                </Label>
              </TooltipTrigger>
              <TooltipContent>
                <p className='max-w-64 p-2'>
                  Data type dictates the interpretation of provided values. For
                  example, choosing Integer will allocate 4 bytes for each
                  value.
                </p>
              </TooltipContent>
            </Tooltip>
            <SelectInput control={control} name='dataType' />
          </div>
          <FormInput
            type='number'
            title='Alignment (log Bytes)'
            hint="Allocates the array, so that its address is a multiple of a certain number. For instance, an alignment of three will align the array's start to 2^3 = 8 bytes. In assembler, the .align directive performs exactly the same thing."
            {...register('alignment', { valueAsNumber: true })}
            error={formState.errors.alignment}
          />
        </div>
      </div>
      <h2 className='text-xl mb-4'>Values</h2>
      <Tabs
        className='md:w-[600px]'
        onValueChange={(value) => {
          onTabChange(value as 'data' | 'constant' | 'random');
        }}
        value={activeTab}
      >
        <TabsList className='w-full'>
          <TabsTrigger value='data'>List</TabsTrigger>
          <TabsTrigger value='constant'>Repeated Constant</TabsTrigger>
          <TabsTrigger value='random'>Random Values</TabsTrigger>
        </TabsList>
        <TabsContent value='data'>
          <Card>
            <CardHeader className='text-sm'>
              You can import the values from a CSV file or manually enter them.
              As a separator, use a comma.
            </CardHeader>
            <CardContent>
              <DataTextArea
                control={control}
                name='data.data'
                memoryLocationName={memoryLocationName}
              />
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value='constant'>
          <Card>
            <CardHeader className='text-sm'>
              A single value will be repeated.
            </CardHeader>
            <CardContent>
              <div className='flex gap-4'>
                <FormInput
                  type='number'
                  title='Number of Elements'
                  {...register('data.size', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'size'])}
                />
                <FormInput
                  title='Constant'
                  {...register('data.constant')}
                  error={getError(errors, ['data', 'constant'])}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent
          value='random'
          onChange={() => {
            trigger();
          }}
        >
          <Card>
            <CardHeader className='text-sm'>
              Random values will be generated in an inclusive range.
            </CardHeader>
            <CardContent>
              <div className='flex gap-4'>
                <FormInput
                  type='number'
                  title='Number of Elements'
                  {...register('data.size', { valueAsNumber: true })}
                  error={getError(errors, ['data', 'size'])}
                />
                <div>
                  <Label>Inclusive Range</Label>
                  <div className='flex gap-2 items-center'>
                    <Input
                      type='number'
                      className='w-20'
                      {...register('data.min', { valueAsNumber: true })}
                    />
                    <span>-</span>
                    <Input
                      type='number'
                      className='w-20'
                      {...register('data.max', { valueAsNumber: true })}
                    />
                  </div>
                  <div>
                    <ErrorDisplay
                      error={
                        getError(errors, ['data', 'min']) ||
                        getError(errors, ['data', 'max'])
                      }
                    />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      <Card className='mt-4'>
        <CardHeader>
          <CardTitle>Summary</CardTitle>
        </CardHeader>
        <CardContent>
          Array called <b>{watchFields.name}</b> of {dataLengthElements}{' '}
          {pluralize('element', dataLengthElements)} ({dataLengthBytes}{' '}
          {pluralize('byte', dataLengthBytes)}). Alignment is 2
          <sup>{watchFields.alignment}</sup> = {2 ** watchFields.alignment}{' '}
          {pluralize('byte', 2 ** watchFields.alignment)}.
          <div className='flex gap-4 mt-4'>
            <Button type='submit' disabled={!isDirty || !isValid}>
              {existing ? 'Update' : 'Create'}
            </Button>
            {existing && (
              <Button
                type='button'
                variant='destructive'
                onClick={deleteCallback}
              >
                Delete
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </form>
  );
}

type DataTextAreaProps = UseControllerProps<MemoryLocationApi> & {
  memoryLocationName: string;
};

/**
 * Text area for entering data. Can be filled from a CSV file.
 * Forgets the file when the tab is changed.
 */
function DataTextArea({ memoryLocationName, ...props }: DataTextAreaProps) {
  const { field } = useController(props);

  // File selected to fill the memory
  const [file, setFile] = useState<File | undefined>();

  // biome-ignore lint: reset the file when the memory location changes
  useEffect(() => {
    setFile(undefined);
  }, [memoryLocationName]);

  return (
    <div>
      <Label htmlFor='data-textarea'>Values</Label>
      <Textarea
        id='data-textarea'
        onChange={(e) => {
          // Convert the string to an array of strings
          const data = e.target.value.split(',');
          field.onChange(data);
        }}
        onKeyDown={(e) => {
          // Disable new lines
          if (e.key === 'Enter') {
            e.preventDefault();
          }
        }}
        value={field.value as string}
        className='w-full font-mono border border-gray-300 rounded-md mb-2'
        placeholder='Enter values, separated by a comma. For example: 1,2,3,4.20'
      />
      <Label
        htmlFor='fileId'
        className={clsx(
          'relative focus-within:bg-secondary/80',
          buttonVariants({ variant: 'secondary' }),
        )}
      >
        <Input
          title='File'
          id='fileId'
          type='file'
          className='absolute inset-0 opacity-0'
          accept='.csv'
          onChange={(e) => {
            const file = getFileFromFileList(e.target.files);
            if (file && file instanceof File) {
              setFile(file);
              readFromFile(file).then((data) => {
                field.onChange(data);
              });
            }
          }}
        />
        {file?.name ? (
          `${file.name} ✓`
        ) : (
          <span className='flex items-center gap-4'>
            Fill from a CSV file
            <Upload size={20} strokeWidth={1.5} />
          </span>
        )}
      </Label>
    </div>
  );
}

/**
 * A select input (dropdown). Used for choosing the data type.
 * Can be used for any textual input, if generalized.
 */
function SelectInput(props: UseControllerProps<MemoryLocationApi>) {
  const { field } = useController(props);

  return (
    <Select onValueChange={field.onChange} value={field.value as string}>
      <SelectTrigger
        className='w-[180px]'
        id={field.name}
        aria-labelledby={`${field.name}-label`}
      >
        <SelectValue placeholder='Select a Type' />
      </SelectTrigger>
      <SelectContent>
        {dataTypes.map((dt, i) => {
          const name = dataTypesText[i];
          return (
            <SelectItem value={dt} key={dt}>
              {name}
            </SelectItem>
          );
        })}
      </SelectContent>
    </Select>
  );
}

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
  return current as FieldError;
}

// Custom resolver
const resolver: Resolver<MemoryLocationApi> = async (
  values: MemoryLocationApi,
  ctx,
  options,
) => {
  // Let zod do its thing
  const r = zodResolver(memoryLocationSchema);
  const val = await r(values, ctx, options);

  const empty = Object.keys(val.errors).length === 0;
  if (!empty) {
    return val;
  }
  // Additional checks

  const activeIsa = ctx.activeIsa as ReturnType<typeof selectActiveConfig>;
  const memoryLocationName = ctx.memoryLocationName as string;

  // Name is unique. If we are updating, the name can stay the same
  const memoryLocation = activeIsa.memoryLocations.find(
    (ml) => ml.name === values.name,
  );
  // found, but ignore if we are updating
  // todo: bug when updating memory
  const errors: FieldErrors<MemoryLocationApi> = {};
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
