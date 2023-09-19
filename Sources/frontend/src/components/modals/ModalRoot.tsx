/**
 * @file    ModalRoot.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   [TODO]
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
