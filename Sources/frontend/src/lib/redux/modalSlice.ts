/**
 * @file    modalSlice.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Redux state for modals
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

/* eslint-disable @typescript-eslint/ban-ts-comment */
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

import type { RootState } from '@/lib/redux/store';

import type { ModalProps, ModalType } from '@/components/modals/ModalRoot';

/**
 * Type that link a name of a modal to its props.
 */
export type Modal<T extends ModalType> = {
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
    openModal: <T extends ModalType>(
      state: ModalState,
      action: PayloadAction<Modal<T>>,
    ) => {
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
