/* eslint-disable @typescript-eslint/ban-ts-comment */
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

import type { RootState } from '@/lib/redux/store';

import type { ModalProps, ModalType } from '@/components/modals/ModalRoot';

/**
 * Type that link a name of a modal to its props.
 */
export type Modal<T extends ModalType = ModalType> = {
  modalType: T;
  modalProps: ModalProps<T>;
};

type ModalState = {
  // If multiple modals are needed, this should be an array (stack)
  modalType: ModalType | null;
  // Record<string, never> means empty object
  modalProps: ModalProps<ModalType> | Record<string, never>;
};

// Define the initial state using that type
const initialState: ModalState = {
  modalType: null,
  modalProps: {},
};

export const shortcutsSlice = createSlice({
  name: 'modal',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    openModal: (state, action: PayloadAction<Modal>) => {
      state.modalType = action.payload.modalType;
      state.modalProps = action.payload.modalProps;
    },
    closeModal: (state) => {
      state.modalType = null;
      state.modalProps = {};
    },
  },
});

export const { openModal, closeModal } = shortcutsSlice.actions;

// Selectors
export const selectModalType = (state: RootState) => state.modals.modalType;
export const selectModalProps = (state: RootState) => state.modals.modalProps;

export default shortcutsSlice.reducer;
