// Source: https://stackoverflow.com/questions/35623656/how-can-i-display-a-modal-dialog-in-redux-that-performs-asynchronous-actions/35641680#35641680

import ReactModal from 'react-modal';

import { useAppSelector } from '@/lib/redux/hooks';
import { selectModalProps, selectModalType } from '@/lib/redux/modalSlice';

import RobDetailsModal from '@/components/modals/RobDetailsModal';
import SaveIsaChangesModal from '@/components/modals/SaveIsaChangesModal';

/**
 * Modals to be rendered. They should define their size, padding from the edge of the modal and the content.
 */
const MODAL_COMPONENTS = {
  CONFIRM_ISA_CHANGES_MODAL: SaveIsaChangesModal,
  ROB_DETAILS_MODAL: RobDetailsModal,
};

export type ModalType = keyof typeof MODAL_COMPONENTS;

export type ModalProps<M extends ModalType = ModalType> = React.ComponentProps<
  (typeof MODAL_COMPONENTS)[M]
>;

const ModalRoot = () => {
  const modalType = useAppSelector(selectModalType);
  const modalPropsTemp = useAppSelector(selectModalProps);

  if (!modalType) {
    return null;
  }

  // We are sure that modalProps exist
  const modalProps = modalPropsTemp as ModalProps;

  const SpecificModal = MODAL_COMPONENTS[modalType];

  // TODO: The type does not express, that the props are correct
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  /* @ts-ignore */
  const renderedModal = <SpecificModal {...modalProps} />;

  return (
    <ReactModal
      isOpen={modalType !== null}
      className='bg-neutral-99 rounded border'
      overlayClassName='fixed inset-0 bg-gray-500/40 flex justify-center items-center'
      bodyOpenClassName='react-modal-open'
    >
      {renderedModal}
    </ReactModal>
  );
};

export default ModalRoot;
