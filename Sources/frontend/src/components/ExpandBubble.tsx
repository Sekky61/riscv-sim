/**
 * @file    ExpandBubble.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A bubble that can be displayed, typically on hover
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

import { ReactChildren } from '@/lib/reactTypes';

type ExpandBubbleProps = {
  children: ReactChildren;
};

/**
 * A bubble that expands to the right of the parent element.
 */
const ExpandBubble = ({ children }: ExpandBubbleProps) => {
  return (
    <div className='absolute left-full top-0 z-10'>
      <div className='dropdown-bubble ml-1'>{children}</div>
    </div>
  );
};

export default ExpandBubble;
