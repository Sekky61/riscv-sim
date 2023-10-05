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
import { CheckCircle, XCircle } from 'lucide-react';
import { ChangeEvent } from 'react';

import {
  callParseAsm,
  openFile,
  selectAsmErrors,
  selectCErrors,
  selectDirty,
  selectEditorMode,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

type EditorBarProps = {
  mode: 'c' | 'asm';
};

export default function EditorBar({ mode }: EditorBarProps) {
  const dispatch = useAppDispatch();
  const editorMode = useAppSelector(selectEditorMode);
  const _isActive = editorMode == mode;
  const editorName = mode == 'c' ? 'C Code' : 'ASM Code';
  const inputId = 'file-input-' + mode;

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      // Read the file and set C code
      const file = e.target.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        const contents = e.target?.result;
        if (typeof contents === 'string') {
          dispatch(openFile({ code: contents, type: mode }));
        }
      };
      reader.readAsText(file);
    }
  };

  const errorDisplay = mode == 'c' ? <CErrorsDisplay /> : <AsmErrorsDisplay />;

  return (
    <div className='pl-3 text-sm flex items-center gap-1 bg-[#f5f5f5]'>
      <div className='py-1 px-0.5 font-bold'>{editorName}</div>
      {errorDisplay}
      <label htmlFor={inputId}>
        <div className='button-interactions px-2 rounded py-0.5 my-0.5'>
          Load
        </div>
        <input
          type='file'
          id={inputId}
          className='hidden'
          onChange={handleFileChange}
        />
      </label>
      <div className='button-interactions px-2 rounded py-0.5 my-0.5'>Save</div>
    </div>
  );
}

const AsmErrorsDisplay = () => {
  const dispatch = useAppDispatch();
  const errors = useAppSelector(selectAsmErrors);
  const dirty = useAppSelector(selectDirty);
  const hasErrors = errors.length > 0;

  const checkAsm = () => {
    dispatch(callParseAsm());
  };

  const boxStyle = clsx(
    'flex items-center button-interactions px-2 rounded py-0.5 my-0.5',
    hasErrors && !dirty && 'bg-red-500/20',
    !hasErrors && !dirty && 'bg-green-500/20',
  );

  return (
    <div className={boxStyle}>
      <div className='mr-2'>
        {hasErrors ? (
          <XCircle
            size={16}
            className={dirty ? 'text-gray-600' : 'text-red-500'}
          />
        ) : (
          <CheckCircle
            size={16}
            className={dirty ? 'text-gray-600' : 'text-green-500'}
          />
        )}
      </div>
      <button onClick={checkAsm}>Check</button>
    </div>
  );
};

/**
 * A fixed width element
 */
const CErrorsDisplay = () => {
  const errors = useAppSelector(selectCErrors);
  const dirty = useAppSelector(selectDirty);
  const hasErrors = errors.length > 0;

  return (
    <div className='mr-2'>
      {hasErrors ? (
        <XCircle
          size={16}
          className={dirty ? 'text-gray-600' : 'text-red-500'}
        />
      ) : (
        <CheckCircle
          size={16}
          className={dirty ? 'text-gray-600' : 'text-green-500'}
        />
      )}
    </div>
  );
};
