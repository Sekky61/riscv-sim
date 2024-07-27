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
import { useEffect } from 'react';
import React from 'react';
import { useForm } from 'react-hook-form';

import {
  type CpuConfig,
  defaultCpuConfig,
  isaFormSchema,
} from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { selectActiveConfig, updateIsa } from '@/lib/redux/isaSlice';
import { saveAsJsonFile } from '@/lib/utils';

import { Button } from '@/components/base/ui/button';
import { toast } from 'sonner';
import { ActiveIsaSelector } from './ActiveIsaSelector';
import { IsaSettingsForm } from './IsaSettingsForm';
import { MemoryInfo } from './MemoryInfo';

// TODO: delete configuration
// TODO: prevent from leaving the page with unsaved changes
export default function Page() {
  // Redux
  const dispatch = useAppDispatch();
  const activeIsa = useAppSelector(selectActiveConfig);

  // If the active ISA is the default, we cannot edit it
  const blockEditing = activeIsa.cpuConfig.name === defaultCpuConfig.name;

  // Lifted state of form
  const form = useForm<CpuConfig>({
    resolver: zodResolver(isaFormSchema),
    defaultValues: defaultCpuConfig,
    mode: 'onChange',
  });

  const hasUnsavedChanges = form.formState.isDirty;
  const hasErrors = !form.formState.isValid;

  // When the active ISA changes, set the form values
  useEffect(() => {
    form.reset(activeIsa.cpuConfig);
  }, [activeIsa, form]);

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

  const doExport = () => {
    saveAsJsonFile(activeIsa.cpuConfig, 'cpuConfig.json');
  };

  return (
    <div>
      <h1>Architecture Settings</h1>
      <div className='border-b mb-4 pb-4 pt-6'>
        <div className='flex justify-center items-center gap-4 flex-wrap'>
          <span className='font-bold'>Active configuration</span>
          <ActiveIsaSelector
            saveChanges={saveChanges}
            hasUnsavedChanges={hasUnsavedChanges}
          />
          <Button
            onClick={persistIsaChanges}
            disabled={!hasUnsavedChanges || hasErrors}
          >
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
    </div>
  );
}
