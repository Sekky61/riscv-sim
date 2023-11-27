/**
 * @file    EditorBar.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Top bar of the code editors
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

import clsx from 'clsx';
import { CheckCircle, Circle, XCircle } from 'lucide-react';

import {
  callParseAsm,
  openFile,
  saveToFile,
  selectAsmDirty,
  selectAsmErrors,
  selectCDirty,
  selectCErrors,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

type EditorBarProps = {
  mode: 'c' | 'asm';
};

/**
 * Show dialog and calls callback with file contents
 */
function loadFile(callback: (contents: string) => void) {
  // Show dialog
  const input = document.createElement('input');
  input.type = 'file';
  input.onchange = () => {
    if (!input.files) {
      console.warn('No file selected');
      return;
    }
    const file = input.files[0];
    if (file === undefined) {
      console.warn('No file selected');
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => {
      const contents = e.target?.result;
      if (typeof contents === 'string') {
        callback(contents);
      }
    };
    reader.readAsText(file);
  };
  input.click();
}

export default function EditorBar({ mode }: EditorBarProps) {
  const dispatch = useAppDispatch();
  const editorName = mode === 'c' ? 'C Code' : 'ASM Code';

  // Load file and set it as C/ASM code
  const handleLoadFile = () => {
    loadFile((contents) => {
      dispatch(openFile({ code: contents, type: mode }));
    });
  };

  const handleSaveFile = () => {
    dispatch(saveToFile());
  };

  const errorDisplay = mode === 'c' ? <CErrorsDisplay /> : <AsmErrorsDisplay />;

  return (
    <div className='pl-3 text-sm flex items-center gap-1 bg-[#f5f5f5] sticky top-0 z-10'>
      <div className='py-1 px-0.5 font-bold'>{editorName}</div>
      {errorDisplay}
      <label>
        <button
          onClick={handleLoadFile}
          className='button-interactions px-2 rounded py-0.5 my-0.5'
        >
          Load
        </button>
      </label>
      <button
        onClick={handleSaveFile}
        className='button-interactions px-2 rounded py-0.5 my-0.5'
      >
        Save
      </button>
    </div>
  );
}

const AsmErrorsDisplay = () => {
  const dispatch = useAppDispatch();
  const errors = useAppSelector(selectAsmErrors);
  const dirty = useAppSelector(selectAsmDirty);
  const hasErrors = errors.length > 0;

  const checkAsm = () => {
    dispatch(callParseAsm());
  };

  const boxStyle = clsx(
    'flex items-center px-2 rounded py-0.5 my-0.5',
    dirty && 'button-interactions',
    hasErrors &&
      !dirty &&
      'bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40',
    !hasErrors &&
      !dirty &&
      'bg-green-500/20 hover:bg-green-500/30 active:bg-green-500/40',
  );

  let iconType: 'circle' | 'tick' | 'x';
  if (dirty) {
    iconType = 'circle';
  } else if (hasErrors) {
    iconType = 'x';
  } else {
    iconType = 'tick';
  }

  return (
    <button className={boxStyle} onClick={checkAsm}>
      <div className='mr-2'>
        <StatusIcon type={iconType} />
      </div>
      Check
    </button>
  );
};

const StatusIcon = ({ type }: { type: 'circle' | 'tick' | 'x' }) => {
  switch (type) {
    case 'circle':
      return <Circle size={16} />;
    case 'tick':
      return <CheckCircle size={16} className='text-green-500' />;
    case 'x':
      return <XCircle size={16} className='text-red-500' />;
  }
};

/**
 * A fixed width element
 */
const CErrorsDisplay = () => {
  const errors = useAppSelector(selectCErrors);
  const dirty = useAppSelector(selectCDirty);
  const hasErrors = errors.length > 0;

  let iconType: 'circle' | 'tick' | 'x';
  if (dirty) {
    iconType = 'circle';
  } else if (hasErrors) {
    iconType = 'x';
  } else {
    iconType = 'tick';
  }

  return (
    <div className='mr-2'>
      <StatusIcon type={iconType} />
    </div>
  );
};
