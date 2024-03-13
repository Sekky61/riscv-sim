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

import { CpuConfig, isaFormSchema } from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  createIsa,
  newActiveIsa,
  selectActiveConfig,
  selectIsas,
  updateIsa,
} from '@/lib/redux/isaSlice';
import { cn, loadFile, pluralize, saveAsJsonFile } from '@/lib/utils';

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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/base/ui/popover';
import IsaSettingsForm from '@/components/form/IsaSettingsForm';
import Link from 'next/link';
import { toast } from 'sonner';
import { DividedBadge } from '@/components/DividedBadge';
import { defaultCpuConfig } from '@/constant/defaults';

// TODO: delete configuration
// TODO: prevent from leaving the page with unsaved changes
export default function Page() {
  // Redux
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);
  const isas = useAppSelector(selectIsas);
  const [saveModalOpen, setSaveModalOpen] = useState(false);
  const [switchingName, setSwitchingName] = useState('');

  // If the active ISA is the default, we cannot edit it
  const blockEditing = activeIsa.cpuConfig.name === defaultCpuConfig.name;
  const [savesOpen, setSavesOpen] = useState(false);

  // Lifted state of form
  const form = useForm<CpuConfig>({
    resolver: zodResolver(isaFormSchema),
    defaultValues: defaultCpuConfig,
    mode: 'onChange',
  });

  const hasUnsavedChanges = form.formState.isDirty;

  // When the active ISA changes, set the form values
  useEffect(() => {
    form.reset(activeIsa.cpuConfig);
  }, [activeIsa, form]);

  const generateIsaName = () => {
    // Iterate over the names of the saved ISAs
    // Find the highest number and add 1
    // If there are no saved ISAs, return "Isa 1"
    // Isa [number] regex
    const regex = /Isa (\d+)/;
    let biggestNum = 0;
    for (const isa of isas) {
      const match = isa.cpuConfig.name.match(regex);
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
    dispatch(createIsa({ ...defaultCpuConfig, name: nam }));
    setSavesOpen(false);
    toast.success(`Created new ISA ${nam}.`);
  };

  // Save name and form values to the active ISA
  const persistIsaChanges = () => {
    const isa = form.getValues();
    // Merge the form values with the name
    const mergedIsa: CpuConfig = { ...activeIsa, ...isa };
    dispatch(updateIsa({ isa: mergedIsa, oldName: activeIsa.cpuConfig.name }));
    toast('Updates have been saved.');
  };

  // todo switching without saving does not switch
  const saveChanges = () => {
    const oldName = activeIsa.cpuConfig.name;
    dispatch(
      updateIsa({
        isa: form.getValues(),
        oldName,
      }),
    );
    toast('Updates have been saved.');
  };

  const switchIsa = (theName: string) => {
    dispatch(newActiveIsa(theName));
    // Close the dropdown
    setSavesOpen(false);
    toast(`ISA configuration has been switched to ${theName}.`);
  };

  // Set the form values, dispatch the new active ISA
  const onChangeSelected = (theName: string) => {
    // If there are unsaved changes, prompt the user to save them
    // If they do not want to save them, discard them
    if (hasUnsavedChanges) {
      // prompt for saving
      setSaveModalOpen(true);
      setSwitchingName(theName);
    } else {
      // no unsaved changes, just switch
      switchIsa(theName);
    }
  };

  const doImport = () => {
    loadFile((json_string) => {
      // TODO: resolve issue with extra fields on the form
      const newConfig = JSON.parse(json_string) as CpuConfig;
      // Fill a name if not present
      if (!newConfig.name) {
        newConfig.name = generateIsaName();
      }
      dispatch(createIsa(newConfig));
      setSavesOpen(false);
      toast.success(`Imported ISA: ${newConfig.name}`);
    });
  };

  const doExport = () => {
    saveAsJsonFile(activeIsa.cpuConfig, 'cpuConfig.json');
  };

  return (
    <div>
      <h1>Architecture Settings</h1>
      <div className='border-b mb-4 pb-4 pt-6'>
        <div className='flex justify-center items-center gap-4'>
          <span className='font-bold'>Active configuration</span>
          <Popover open={savesOpen} onOpenChange={(op) => setSavesOpen(op)}>
            <PopoverTrigger asChild>
              <Button
                variant='outline'
                role='combobox'
                aria-expanded={savesOpen}
                className='w-[200px] justify-between'
              >
                {activeIsa.cpuConfig.name}
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
                      key={isa.cpuConfig.name}
                      value={isa.cpuConfig.name}
                      onSelect={(_currentValue) => {
                        // _currentValue converts to lowercase
                        onChangeSelected(isa.cpuConfig.name);
                      }}
                    >
                      <Check
                        className={cn(
                          'mr-2 h-4 w-4',
                          activeIsa.cpuConfig.name === isa.cpuConfig.name
                            ? 'opacity-100'
                            : 'opacity-0',
                        )}
                      />
                      {isa.cpuConfig.name}
                    </CommandItem>
                  ))}
                </CommandGroup>
                <CommandSeparator />
                <CommandGroup>
                  <CommandItem onSelect={createNewIsa}>
                    Create New Config
                  </CommandItem>
                  <CommandItem onSelect={doImport}>Import Config</CommandItem>
                </CommandGroup>
              </Command>
            </PopoverContent>
          </Popover>
          <Button onClick={persistIsaChanges} disabled={!hasUnsavedChanges}>
            Save Changes
          </Button>
          <Button onClick={doExport} disabled={hasUnsavedChanges}>
            Export
          </Button>
        </div>
        <MemoryInfo />
      </div>
      <div className='flex justify-center pb-8'>
        <IsaSettingsForm form={form} disabled={blockEditing} />
      </div>
      <Dialog open={saveModalOpen} onOpenChange={setSaveModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Save Changes?</DialogTitle>
            <DialogDescription>
              You have unsaved changes. Do you want to save them?
            </DialogDescription>
          </DialogHeader>
          <div className='flex gap-4'>
            <Button
              onClick={() => {
                saveChanges();
                setSaveModalOpen(false);
              }}
            >
              Save changes
            </Button>
            <Button
              onClick={() => {
                setSaveModalOpen(false);
                switchIsa(switchingName);
                setSwitchingName('');
              }}
            >
              Discard changes
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

/**
 * Component to inform about memory locations
 */
function MemoryInfo() {
  const activeIsa = useAppSelector(selectActiveConfig);
  const mem = activeIsa.memoryLocations;
  const names = mem.map((m) => m.name);

  const memoryLink = (
    <Link href='/memory' className='link'>
      memory
    </Link>
  );

  return (
    <div className='flex gap-2 pt-4'>
      {mem.length === 0 ? (
        <span>No {memoryLink} locations defined.</span>
      ) : (
        <span>
          {mem.length} {memoryLink} {pluralize('location', mem.length)} defined:
        </span>
      )}
      {names.map((name) => {
        return <DividedBadge key={name}>{name}</DividedBadge>;
      })}
    </div>
  );
}
