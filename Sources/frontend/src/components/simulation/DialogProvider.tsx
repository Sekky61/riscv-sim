/**
 * @file    DialogProvider.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Client side provider for dialogs
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

'use client';

import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { Dialog, DialogTrigger } from '@radix-ui/react-dialog';
import { MoreVertical } from 'lucide-react';

type DialogProviderType = {
  seeMoreMessage: string;
  detailDialog: React.ReactNode;
};

/**
 * The existance of this component is purely technical (Next.js clent/server components).
 */
export function DialogProvider({
  seeMoreMessage,
  detailDialog,
}: DialogProviderType) {
  return (
    <Dialog>
      <DialogTrigger>
        <Tooltip>
          <TooltipTrigger asChild aria-label={seeMoreMessage}>
            <div className='iconHighlight h-8 w-8 p-1 rounded-full text-primary'>
              <MoreVertical strokeWidth={1.5} />
            </div>
          </TooltipTrigger>
          <TooltipContent side='bottom' className='p-2'>
            {seeMoreMessage}
          </TooltipContent>
        </Tooltip>
      </DialogTrigger>
      {detailDialog}
    </Dialog>
  );
}
