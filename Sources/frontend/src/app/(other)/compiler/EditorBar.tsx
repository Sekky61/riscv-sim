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

'use client';

import { openFile, saveCodeToFile } from '@/lib/redux/compilerSlice';
import { useAppDispatch } from '@/lib/redux/hooks';

import { Button } from '@/components/base/ui/button';
import { loadFile } from '@/lib/utils';
import { Download, Upload } from 'lucide-react';
import React from 'react';

type EditorBarProps = {
  mode: 'c' | 'asm';
  checkSlot: React.ReactNode;
  entryPointSlot?: React.ReactNode;
};

/**
 * Top bar of both code editors.
 * Save and Load buttons allow to save and load code from file.
 * The assembler files can be used in the simulator CLI.
 *
 * Slots are used to add additional buttons.
 */
export default function EditorBar({
  mode,
  checkSlot,
  entryPointSlot,
}: EditorBarProps) {
  const dispatch = useAppDispatch();
  const editorName = mode === 'c' ? 'C Code' : 'ASM Code';

  // Load file and set it as C/ASM code
  const handleLoadFile = () => {
    loadFile((contents) => {
      dispatch(openFile({ code: contents, type: mode }));
    });
  };

  const handleSaveFile = () => {
    dispatch(saveCodeToFile(mode));
  };

  return (
    <div className='p-0.5 pl-3 text-sm flex flex-wrap items-center gap-1 secondary-container sticky top-0 z-10'>
      <div className='py-1 px-0.5 font-bold'>{editorName}</div>
      <label>
        <Button
          variant='secondary'
          onClick={handleLoadFile}
          className='button-interactions px-2 rounded py-0.5 my-0.5 h-6'
        >
          <Upload size={16} strokeWidth={1.75} className='mr-1' />
          Load
        </Button>
      </label>
      <Button
        variant='secondary'
        onClick={handleSaveFile}
        className='button-interactions px-2 rounded py-0.5 my-0.5 h-6'
      >
        <Download size={16} strokeWidth={1.75} className='mr-1' />
        Save
      </Button>
      {checkSlot}
      {entryPointSlot}
    </div>
  );
}
