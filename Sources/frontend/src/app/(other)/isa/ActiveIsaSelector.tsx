/**
 * @file    ActiveIsaSelector.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The ISA selector component
 *
 * @date    20 March 2024, 8:00 (created)
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

// The use client actually means a 'boundary' between the client and the server
// A function cannot pass this boundary
//'use client';

import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/base/ui/popover';
import { defaultCpuConfig } from '@/constant/defaults';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  createIsa,
  newActiveIsa,
  selectActiveConfig,
  selectIsas,
} from '@/lib/redux/isaSlice';
import { useState } from 'react';
import { toast } from 'sonner';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { cn, loadFile } from '@/lib/utils';
import type { CpuConfig } from '@/lib/forms/Isa';
import { Button } from '@/components/base/ui/button';
import { Check, ChevronsUpDown } from 'lucide-react';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandSeparator,
} from '@/components/base/ui/command';
import { isIsaConfig } from '@/lib/forms/validators';

type ActiveIsaSelectorProps = {
  hasUnsavedChanges: boolean;
  saveChanges: () => void;
};

export function ActiveIsaSelector({
  hasUnsavedChanges,
  saveChanges,
}: ActiveIsaSelectorProps) {
  // Redux
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);
  const isas = useAppSelector(selectIsas);
  const [savesOpen, setSavesOpen] = useState(false);
  const [saveModalOpen, setSaveModalOpen] = useState(false);
  const [switchingName, setSwitchingName] = useState('');

  // Create a new ISA with default values and generated name
  const createNewIsa = () => {
    // Generate a name
    const nam = generateIsaName();
    dispatch(createIsa({ ...defaultCpuConfig, name: nam }));
    setSavesOpen(false);
    toast.success(`Created new ISA ${nam}.`);
  };

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
        const num = Number.parseInt(match[1]);
        if (num > biggestNum) {
          biggestNum = num;
        }
      }
    }
    return `Isa ${biggestNum + 1}`;
  };

  const doImport = () => {
    loadFile((json_string) => {
      // TODO: resolve issue with extra fields on the form
      let newConfig: unknown;
      try {
        newConfig = JSON.parse(json_string);
      } catch (e) {
        toast.error('Invalid JSON');
        return;
      }
      if (!isIsaConfig(newConfig)) {
        toast.error('Invalid ISA configuration');
        return;
      }
      // Fill a name if not present
      if (!newConfig.name) {
        newConfig.name = generateIsaName();
      }
      dispatch(createIsa(newConfig));
      setSavesOpen(false);
      toast.success(`Imported ISA: ${newConfig.name}`);
    });
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

  return (
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
            <CommandItem onSelect={createNewIsa}>Create New Config</CommandItem>
            <CommandItem onSelect={doImport}>Import Config</CommandItem>
          </CommandGroup>
        </Command>
      </PopoverContent>
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
    </Popover>
  );
}
