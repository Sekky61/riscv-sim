import { useHotkeys } from 'react-hotkeys-hook';

import { enterEditorMode, selectEditorMode } from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

export const COMPILE_SHORTCUT = 'ctrl+enter';

const CompilerShortcuts = () => {
  const dispatch = useAppDispatch();
  const editorMode = useAppSelector(selectEditorMode);

  // TODO: 'ctrl+s' to save the code

  // TODO: shortcuts do not work when the user is typing in the editor
  useHotkeys(
    'ctrl+left',
    () => {
      if (editorMode === 'asm') {
        dispatch(enterEditorMode('c'));
      }
    },
    undefined,
    [editorMode, dispatch, enterEditorMode],
  );

  useHotkeys(
    'ctrl+right',
    () => {
      if (editorMode === 'c') {
        dispatch(enterEditorMode('asm'));
      }
    },
    undefined,
    [editorMode, dispatch, enterEditorMode],
  );

  return null;
};

export default CompilerShortcuts;
