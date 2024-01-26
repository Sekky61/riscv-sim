/**
 * @file    StatusIcon.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Status icon component
 *
 * @date    26 January 2024, 14:00 (created)
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

import { CheckCircle, Circle, XCircle } from 'lucide-react';

export const StatusIcon = ({ type }: { type: 'circle' | 'tick' | 'x' }) => {
  switch (type) {
    case 'circle':
      return <Circle size={16} />;
    case 'tick':
      return <CheckCircle size={16} className='text-green-500' />;
    case 'x':
      return <XCircle size={16} className='text-red-500' />;
  }
};