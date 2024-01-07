/**
 * @file    MainMemoryDetailsModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Modal for displaying the main memory details
 *
 * @date    06 January 2023, 20:00 (created)
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

import { selectMemoryBytes, selectProgram } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import { HexDump } from '@/components/simulation/MainMemory';

export const MainMemoryDetailsModal = () => {
  const program = useAppSelector(selectProgram);
  const memory = useAppSelector(selectMemoryBytes) ?? new Uint8Array(0);

  if (!program) throw new Error('Program not found');

  const labelTable = [];
  for (const label of Object.values(program.labels)) {
    labelTable.push(
      <tr key={label.name}>
        <td>{label.name}</td>
        <td>{label.address}</td>
      </tr>,
    );
  }

  return (
    <>
      <CardHeader>
        <CardTitle>Main Memory</CardTitle>
        <CardDescription>Detailed view</CardDescription>
      </CardHeader>
      <CardContent>
        <table className='mb-4'>
          <thead>
            <tr>
              <th>Label</th>
              <th>Address</th>
            </tr>
          </thead>
          <tbody>{labelTable}</tbody>
        </table>
        <div>
          Memory Inspector - shows the memory up to the highest touched address
        </div>
        <div className='max-h-64 overflow-y-scroll'>
          <HexDump
            memory={memory}
            labels={program.labels}
            bytesInRow={16}
            showAscii
          />
        </div>
      </CardContent>
    </>
  );
};
