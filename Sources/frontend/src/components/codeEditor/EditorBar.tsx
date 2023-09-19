import { ChangeEvent } from 'react';

import { openFile, selectEditorMode } from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

type EditorBarProps = {
  mode: 'c' | 'asm';
};

export default function EditorBar({ mode }: EditorBarProps) {
  const dispatch = useAppDispatch();
  const editorMode = useAppSelector(selectEditorMode);
  const _isActive = editorMode == mode;
  const editorName = mode == 'c' ? 'C code' : 'ASM code';
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

  return (
    <div className='pl-3 text-sm flex gap-1 bg-[#f5f5f5]'>
      <div className='py-1 px-0.5 font-bold'>{editorName}</div>
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
