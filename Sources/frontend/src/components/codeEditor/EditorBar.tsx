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

import { openFile, saveToFile } from '@/lib/redux/compilerSlice';
import { useAppDispatch } from '@/lib/redux/hooks';

import { Button } from '@/components/base/ui/button';
import React from 'react';

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

type EditorBarProps = {
  mode: 'c' | 'asm';
  checkSlot: React.ReactNode;
  entryPointSlot?: React.ReactNode;
};

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
    dispatch(saveToFile());
  };

  return (
    <div className='p-0.5 pl-3 text-sm flex flex-wrap items-center gap-1 bg-[#f5f5f5] sticky top-0 z-10'>
      <div className='py-1 px-0.5 font-bold'>{editorName}</div>
      {checkSlot}
      <label>
        <Button
          variant='ghost'
          onClick={handleLoadFile}
          className='button-interactions px-2 rounded py-0.5 my-0.5 h-6'
        >
          Load
        </Button>
      </label>
      <Button
        variant='ghost'
        onClick={handleSaveFile}
        className='button-interactions px-2 rounded py-0.5 my-0.5 h-6'
      >
        Save
      </Button>
      {entryPointSlot}
    </div>
  );
}
