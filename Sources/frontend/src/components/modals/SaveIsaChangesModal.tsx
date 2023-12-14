/**
 * @file    SaveIsaChangesModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Modal for warning about unsaved changes in ISA configuration
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

import { notify } from 'reapop';

import { CpuConfig } from '@/lib/forms/Isa';
import { useAppDispatch } from '@/lib/redux/hooks';
import { updateIsa } from '@/lib/redux/isaSlice';
import { closeModal } from '@/lib/redux/modalSlice';

import ConfirmModal from '@/components/modals/ConfirmModal';

export type SaveIsaChangesModalProps = {
  isa: CpuConfig;
  oldName: string;
};

export const SaveIsaChangesModal = ({
  isa,
  oldName,
}: SaveIsaChangesModalProps) => {
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
            title: 'Updates have been saved.',
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
