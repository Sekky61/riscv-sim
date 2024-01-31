/**
 * @file    SimConfigNotUpToDateModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Modal for warning about sim config not being up to date in the simulation
 *
 * @date    27 January 2024, 22:00 (created)
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

import { useAppDispatch } from '@/lib/redux/hooks';
import { closeModal } from '@/lib/redux/modalSlice';

import ConfirmModal from '@/components/modals/ConfirmModal';
import { reloadSimulation } from '@/lib/redux/cpustateSlice';
import { pullSimConfig } from '@/lib/redux/simConfigSlice';

export const SimConfigNotUpToDateModal = () => {
  const dispatch = useAppDispatch();
  return (
    <ConfirmModal
      title='Config not up to date'
      message='The code or the configuration of the simulation has changed. Do you want to reload the simulation with the new configuration and code?'
      yesText='Yes, reload simulation'
      noText='No, keep current simulation'
      onYes={() => {
        dispatch(pullSimConfig());
        dispatch(reloadSimulation());
        dispatch(
          notify({
            title: 'Simulation reloaded',
            status: 'success',
          }),
        );
        dispatch(closeModal());
      }}
      onNo={() => {
        dispatch(closeModal());
      }}
    />
  );
};
