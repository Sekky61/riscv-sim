/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The ISA configuration page
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
import { Check, ChevronsUpDown } from 'lucide-react';
import { useEffect, useState } from 'react';
import React from 'react';
import { useForm } from 'react-hook-form';
import { notify } from 'reapop';

import {
  IsaNamedConfig,
  isaFormDefaultValues,
  isaNamed,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  IsaSaveChecked,
  createIsa,
  newActiveIsa,
  selectActiveIsa,
  selectIsas,
  updateIsa,
} from '@/lib/redux/isaSlice';
import { openModal } from '@/lib/redux/modalSlice';
import { cn } from '@/lib/utils';

import { Button } from '@/components/base/ui/button';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandSeparator,
} from '@/components/base/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/base/ui/popover';
import IsaSettingsForm from '@/components/form/IsaSettingsForm';
import { SaveIsaChangesModalProps } from '@/components/modals/SaveIsaChangesModal';

// TODO: delete configuration
export default function Page() {
  // Redux
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveIsa);
  const isas = useAppSelector(selectIsas);

  // If the active ISA is the default, we cannot edit it
  const blockEditing = activeIsa.name === 'Default';
  const [savesOpen, setSavesOpen] = useState(false);

  // Lifted state of form
  const form = useForm<IsaNamedConfig>({
    resolver: zodResolver(isaNamed),
    defaultValues: isaFormDefaultValues,
    mode: 'onChange',
  });

  const hasUnsavedChanges = form.formState.isDirty;

  // When the active ISA changes, set the form values
  useEffect(() => {
    form.reset(activeIsa);
  }, [activeIsa, form]);

  const generateIsaName = () => {
    // Iterate over the names of the saved ISAs
    // Find the highest number and add 1
    // If there are no saved ISAs, return "Isa 1"
    // Isa [number] regex
    const regex = /Isa (\d+)/;
    let biggestNum = 0;
    for (const isa of isas) {
      const match = isa.name.match(regex);
      if (match?.[1]) {
        // We have a match
        const num = parseInt(match[1]);
        if (num > biggestNum) {
          biggestNum = num;
        }
      }
    }
    return `Isa ${biggestNum + 1}`;
  };

  // Create a new ISA with default values and generated name
  const createNewIsa = () => {
    // Generate a name
    const nam = generateIsaName();
    dispatch(createIsa({ ...isaFormDefaultValues, name: nam }));
    setSavesOpen(false);
    dispatch(
      notify({
        title: `Created new ISA ${nam}.`,
        status: 'success',
      }),
    );
  };

  // Save name and form values to the active ISA
  const persistIsaChanges = () => {
    const isa = form.getValues();
    dispatch(updateIsa({ isa, oldName: activeIsa.name }));
    dispatch(
      notify({
        title: 'Updates have been saved.',
        status: 'success',
      }),
    );
  };

  const promptForSave = () => {
    const modalProps: SaveIsaChangesModalProps = {
      isa: form.getValues(),
      oldName: activeIsa.name,
    };
    dispatch(
      openModal({
        modalType: 'CONFIRM_ISA_CHANGES_MODAL',
        modalProps,
      }),
    );
    // if (confirm('You have unsaved changes. Do you want to save them?')) {
    //   persistIsaChanges();
    // }
  };

  // Set the form values, dispatch the new active ISA
  const onChangeSelected = (theName: string) => {
    // If there are unsaved changes, prompt the user to save them
    // If they do not want to save them, discard them
    if (hasUnsavedChanges) {
      promptForSave();
    }
    dispatch(newActiveIsa(theName));
    // Close the dropdown
    setSavesOpen(false);
    dispatch(
      notify({
        title: `${theName} is now the active ISA.`,
        status: 'success',
      }),
    );
  };

  return (
    <div>
      <h1 className='mb-8 text-2xl'>ISA Configuration</h1>
      <div className='mb-4 flex justify-center items-center gap-4 border-b pb-4'>
        <span className='font-bold'>Active configuration</span>
        <Popover open={savesOpen} onOpenChange={(op) => setSavesOpen(op)}>
          <PopoverTrigger asChild>
            <Button
              variant='outline'
              role='combobox'
              aria-expanded={savesOpen}
              className='w-[200px] justify-between'
            >
              {activeIsa.name}
              <ChevronsUpDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
            </Button>
          </PopoverTrigger>
          <PopoverContent className='w-[200px] p-0'>
            <Command>
              <CommandInput placeholder='Search...' />
              <CommandEmpty>No ISA found.</CommandEmpty>
              <CommandGroup>
                {isas.map((isa) => (
                  <CommandItem
                    key={isa.name}
                    value={isa.name}
                    onSelect={(_currentValue) => {
                      // _currentValue converts to lowercase
                      onChangeSelected(isa.name);
                    }}
                  >
                    <Check
                      className={cn(
                        'mr-2 h-4 w-4',
                        activeIsa.name === isa.name
                          ? 'opacity-100'
                          : 'opacity-0',
                      )}
                    />
                    {isa.name}
                  </CommandItem>
                ))}
              </CommandGroup>
              <CommandSeparator />
              <CommandGroup>
                <CommandItem onSelect={createNewIsa}>
                  Create new ISA
                </CommandItem>
              </CommandGroup>
            </Command>
          </PopoverContent>
        </Popover>
        <Button onClick={persistIsaChanges} disabled={!hasUnsavedChanges}>
          Save Changes
        </Button>
      </div>
      <div
        className={cn(
          blockEditing &&
            'pointer-events-none opacity-60 hover:cursor-not-allowed',
          'flex justify-center',
        )}
      >
        <IsaSettingsForm form={form} disabled={blockEditing} />
      </div>
    </div>
  );
}

// Configuration picker

export type IsaItemsProps = {
  onIsaSavePicked: (name: string) => void;
};

export type IsaItemProps = IsaItemsProps & { isa: IsaSaveChecked };
