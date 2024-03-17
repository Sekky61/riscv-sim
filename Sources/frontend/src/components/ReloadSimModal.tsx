/**
 * @file    ReloadSimModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Check if the configuration has changed and ask to reload the simulation
 *
 * @date    30 January 2024, 21:00 (created)
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

import { useState } from 'react';

import { Button } from '@/components/base/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';
import { useReloadSim } from '@/lib/hooks/useReloadSim';

export function ReloadSimModal() {
  const { same, cleanReload } = useReloadSim();
  const [openModal, setOpenModal] = useState(!same);

  return (
    <Dialog open={openModal} onOpenChange={setOpenModal}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>The Configuration Has Changed</DialogTitle>
          <DialogDescription>
            The code, memory, or the CPU configuration has changed. Do you
            wish to <b>apply the changes</b>?
          </DialogDescription>
        </DialogHeader>
        <div className='flex gap-4'>
          <Button
            onClick={() => {
              cleanReload();
              setOpenModal(false);
            }}
          >
            Yes, reload simulation
          </Button>
          <Button
            onClick={() => {
              setOpenModal(false);
            }}
            variant='ghost'
          >
            No, keep the old simulation
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
