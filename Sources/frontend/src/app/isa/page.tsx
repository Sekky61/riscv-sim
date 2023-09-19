'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import clsx from 'clsx';
import { ChevronDown } from 'lucide-react';
import { useEffect, useState } from 'react';
import React from 'react';
import { useForm } from 'react-hook-form';
import { notify } from 'reapop';

import {
  isaFormDefaultValues,
  isaNamed,
  IsaNamedConfig,
  isArithmeticUnitConfig,
} from '@/lib/forms/Isa';
import { IsaConfig } from '@/lib/forms/Isa';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  createIsa,
  IsaSaveChecked,
  newActiveIsa,
  selectActiveIsa,
  selectActiveIsaName,
  selectIsas,
  selectValidatedIsas,
  updateIsa,
} from '@/lib/redux/isaSlice';
import { openModal } from '@/lib/redux/modalSlice';

import IsaSettingsForm from '@/components/form/IsaSettingsForm';
import { SaveIsaChangesModalProps } from '@/components/modals/SaveIsaChangesModal';

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
  }, [activeIsa, form, isas]);

  const generateIsaName = () => {
    // Iterate over the names of the saved ISAs
    // Find the highest number and add 1
    // If there are no saved ISAs, return "Isa 1"
    // Isa [number] regex
    const regex = /Isa (\d+)/;
    let biggestNum = 0;
    for (let i = 0; i < isas.length; i++) {
      const match = isas[i].name.match(regex);
      if (match) {
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
        title: `Updates have been saved.`,
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

  const configBoxClicked: React.MouseEventHandler<HTMLDivElement> = (e) => {
    // Ignore clicks from the input
    if (!(e.target instanceof HTMLInputElement)) {
      setSavesOpen(!savesOpen);
    }
  };

  return (
    <div>
      <h1 className='mb-8 text-2xl'>ISA Configuration</h1>
      <div className='mb-2'>Pick a configuration</div>
      <div className='mb-8 flex gap-4'>
        <div
          onClick={configBoxClicked}
          className={
            'relative flex w-80 items-center justify-between gap-4 border p-3 hover:cursor-pointer hover:bg-gray-100 active:bg-gray-200 ' +
            (savesOpen ? 'rounded-t-md' : 'rounded-md')
          }
        >
          <input
            className='form-input'
            {...form.register('name')}
            disabled={blockEditing}
          />
          <ChevronDown
            className={
              'pointer-events-none h-6 w-6 ' +
              (savesOpen ? 'rotate-180 transform' : '')
            }
          />
          <div
            className={
              'absolute left-0 top-full z-10 w-full rounded-b-md border border-t-0 bg-white ' +
              (savesOpen ? '' : 'hidden')
            }
          >
            <IsaLocalStorageItems onIsaSavePicked={onChangeSelected} />
            <button
              onClick={createNewIsa}
              className='button w-full border-t p-1'
            >
              Create new ISA
            </button>
          </div>
        </div>
        <button
          onClick={persistIsaChanges}
          disabled={!hasUnsavedChanges}
          className='button'
        >
          Save Changes
        </button>
      </div>
      <div
        className={
          blockEditing
            ? 'pointer-events-none opacity-60 hover:cursor-not-allowed'
            : ''
        }
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

export function IsaLocalStorageItems({ onIsaSavePicked }: IsaItemsProps) {
  const validatedIsas = useAppSelector(selectValidatedIsas);
  return (
    <>
      {validatedIsas.map((isa) => (
        <IsaItem key={isa.name} isa={isa} onIsaSavePicked={onIsaSavePicked} />
      ))}
    </>
  );
}

export type IsaItemProps = IsaItemsProps & { isa: IsaSaveChecked };

function IsaItem({ isa, onIsaSavePicked }: IsaItemProps) {
  const [hover, setHover] = useState(false);
  const activeIsaName = useAppSelector(selectActiveIsaName);
  const isActiveIsa = activeIsaName === isa.name;
  const classes = clsx(
    'flex p-2 relative',
    isa.valid &&
      !isActiveIsa &&
      'hover:bg-gray-100 active:bg-gray-300 hover:cursor-pointer',
    isActiveIsa && 'bg-gray-200',
  );
  return (
    <div
      onClick={() => onIsaSavePicked(isa.name)}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      className={classes}
    >
      {isa.name}
      {hover && <IsaSettingsDisplay isa={isa} />}
    </div>
  );
}

interface IsaSettingsDisplayProps {
  isa: IsaConfig;
}

export function IsaSettingsDisplay({ isa }: IsaSettingsDisplayProps) {
  return (
    <div className='tooltiptext neutral-bg ml-2 rounded border p-4'>
      <h3 className='my-1'>Fetch</h3>
      <ul className='list-disc ml-4'>
        <li>
          <b>Fetch width:</b> {isa.fetchWidth}
        </li>
        <li>
          <b>Commit width:</b> {isa.commitWidth}
        </li>
      </ul>
      <h3 className='my-1'>Buffers</h3>
      <ul className='list-disc ml-4'>
        <li>
          <b>ROB size:</b> {isa.robSize}
        </li>
        <li>
          <b>LB size:</b> {isa.lbSize}
        </li>
        <li>
          <b>SB size:</b> {isa.sbSize}
        </li>
      </ul>
      <h3 className='my-1'>Functional Units ({isa.fUnits.length})</h3>
      <ul className='list-decimal ml-4'>
        {isa.fUnits.map((unit) => {
          let nOfOps = null;
          if (isArithmeticUnitConfig(unit)) {
            nOfOps = ` - (${unit.operations.length}) operations`;
          }
          return (
            <li key={unit.id}>
              <b>{unit.fuType}</b> - Latency: {unit.latency} {nOfOps}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
