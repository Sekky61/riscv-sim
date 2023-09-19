/**
 * @file    ConfirmModal.tsx
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

export type ConfirmModalProps = {
  title: string;
  message: string;
  yesText?: string;
  noText?: string;
  onYes: () => void;
  onNo?: () => void;
};

const ConfirmModal = ({
  title,
  message,
  yesText,
  noText,
  onYes,
  onNo,
}: ConfirmModalProps) => {
  return (
    <div className='m-4'>
      <h1 className='text-xl'>{title}</h1>
      <p className='mb-4'>{message}</p>
      <div>
        <button onClick={onYes} className='button mr-4'>
          {yesText || 'Confirm'}
        </button>
        <button onClick={onNo} className='button'>
          {noText || 'Cancel'}
        </button>
      </div>
    </div>
  );
};

export default ConfirmModal;
