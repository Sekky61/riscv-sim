/**
 * @file ErrorMessage.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Bubble to be displayed when the configuration is not correct
 *
 * @date     16 March 2024, 22:00 (created)
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
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import {
  selectErrorMessage,
  selectSimulationStatus,
  selectStateOk,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import { saveAsJsonFile } from '@/lib/utils';
import { Button } from '@/components/base/ui/button';
import Link from 'next/link';

export function ErrorMessage() {
  const stateOk = useAppSelector(selectStateOk);
  const errorMessage = useAppSelector(selectErrorMessage);
  const simulationStatus = useAppSelector(selectSimulationStatus);

  const isSymbolMissingError = errorMessage?.includes('is not defined');

  // Prevents a flash of the error message when the simulation is loading
  if (stateOk || simulationStatus === 'loading') {
    return null;
  }

  return (
    <div className='flex justify-center items-center h-full'>
      <Card className='max-w-lg error-container'>
        <CardHeader>
          <CardTitle>Error</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='text-lg'>{errorMessage}</div>
          <div className='flex gap-2 flex-wrap mt-4'>
            {isSymbolMissingError && (
              <Link href='/memory'>
                <Button>Go to Memory page and define it</Button>
              </Link>
            )}
            <Button
              variant='destructive'
              onClick={() => {
                // dump localStorage to file
                saveAsJsonFile(
                  localStorage,
                  `riscv-simulator-local-storage-${Date.now()}.json`,
                );
              }}
            >
              {' '}
              Dump localStorage to file{' '}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
