import { notify } from 'reapop';

import { IsaNamedConfig } from '@/lib/forms/Isa';
import { useAppDispatch } from '@/lib/redux/hooks';
import { updateIsa } from '@/lib/redux/isaSlice';
import { closeModal } from '@/lib/redux/modalSlice';

import ConfirmModal from '@/components/modals/ConfirmModal';

export type SaveIsaChangesModalProps = {
  isa: IsaNamedConfig;
  oldName: string;
};

const SaveIsaChangesModal = ({ isa, oldName }: SaveIsaChangesModalProps) => {
  const dispatch = useAppDispatch();
  return (
    <ConfirmModal
      title='Save Changes?'
      message='You have unsaved changes. Do you want to save them?'
      yesText='Save changes'
      noText='Discard changes'
      onYes={() => {
        // TODO: should updateIsa and notify be in a thunk?
        dispatch(closeModal());
        dispatch(updateIsa({ isa, oldName }));
        dispatch(
          notify({
            title: `Updates have been saved.`,
            status: 'success',
          }),
        );
      }}
      onNo={() => {
        dispatch(closeModal());
      }}
    />
  );
};

export default SaveIsaChangesModal;
