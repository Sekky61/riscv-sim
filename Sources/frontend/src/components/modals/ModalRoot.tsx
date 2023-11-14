/**
 * @file    ModalRoot.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Component for rendering modals
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

import { RefObject, useEffect } from 'react';
import ReactModal from 'react-modal';

import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import {
  closeModal,
  selectModalProps,
  selectModalType,
} from '@/lib/redux/modalSlice';

import { Button } from '@/components/base/ui/button';
import { Card, CardFooter } from '@/components/base/ui/card';
import { FetchDetailsModal } from '@/components/modals/FetchDetailsModal';
import { RobDetailsModal } from '@/components/modals/RobDetailsModal';
import { SaveIsaChangesModal } from '@/components/modals/SaveIsaChangesModal';
import { SimCodeDetailModal } from '@/components/modals/SimCodeDetailModal';

/**
 * Modals to be rendered. They should define their size, padding from the edge of the modal and the content.
 * A modal should return a fragment with CardHeader and CardContent components.
 */
const MODAL_COMPONENTS = {
  CONFIRM_ISA_CHANGES_MODAL: SaveIsaChangesModal,
  ROB_DETAILS_MODAL: RobDetailsModal,
  FETCH_DETAILS_MODAL: FetchDetailsModal,
  SIMCODE_DETAILS_MODAL: SimCodeDetailModal,
};

export type ModalType = keyof typeof MODAL_COMPONENTS;

export type ModalProps<M extends ModalType = ModalType> = React.ComponentProps<
  (typeof MODAL_COMPONENTS)[M]
>;

/**
 * modal root component.
 * Renders the modal based on the modalType in the redux store (modalSlice).
 * Modal can be closed by clicking on the close button, by pressing ESC or by clicking outside of the modal.
 *
 * @param appRef reference to the app root element
 */
const ModalRoot = ({ appRef }: { appRef: RefObject<HTMLElement> }) => {
  const dispatch = useAppDispatch();
  const modalType = useAppSelector(selectModalType);
  const modalPropsTemp = useAppSelector(selectModalProps);

  // We are sure that modalProps exist
  const modalProps = modalPropsTemp as ModalProps;
  const isOpen = modalType !== null;

  const SpecificModal = isOpen ? MODAL_COMPONENTS[modalType] : 'div';

  // TODO: The type does not express, that the props are correct
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  /* @ts-ignore */
  const renderedModal = <SpecificModal {...modalProps} />;

  const closeModalRequest = () => {
    dispatch(closeModal());
  };

  // Give react-modal a reference to the app root.
  // This is needed so that react-modal can add aria-hidden to the app root when the modal is open (accessibility)
  useEffect(() => {
    if (!appRef.current) {
      throw new Error(`App ref is not set`);
    }
    ReactModal.setAppElement(appRef.current);
  }, [appRef]);

  return (
    <ReactModal
      isOpen={isOpen}
      className='min-w-[300px]'
      overlayClassName='fixed inset-0 bg-gray-500/40 flex justify-center items-center'
      bodyOpenClassName='react-modal-open'
      onRequestClose={closeModalRequest}
    >
      <Card>
        {renderedModal}
        <CardFooter>
          <Button onClick={closeModalRequest}>Close</Button>
        </CardFooter>
      </Card>
    </ReactModal>
  );
};

export default ModalRoot;
