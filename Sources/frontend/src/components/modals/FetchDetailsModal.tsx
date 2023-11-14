/**
 * @file    FetchDetailsModal.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Modal for displaying details about the Fetch block
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

import { selectFetch } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/base/ui/card';
import InstructionTable from '@/components/simulation/InstructionTable';

export const FetchDetailsModal = () => {
  const fetch = useAppSelector(selectFetch);

  if (!fetch) throw new Error('Fetch unit not found');

  return (
    <>
      <CardHeader>
        <CardTitle>Fetch Block</CardTitle>
        <CardDescription>Detailed view</CardDescription>
      </CardHeader>
      <CardContent>
        <table>
          <thead>
            <tr>
              <th>PC</th>
              <th>Number of ways</th>
              <th>Stall</th>
              <th>Branch follow limit</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{fetch.pc}</td>
              <td>{fetch.numberOfWays}</td>
              <td>{fetch.stallFlag ? 'Stalled' : 'Not stalled'}</td>
              <td>{fetch.branchFollowLimit}</td>
            </tr>
          </tbody>
        </table>
        <h2 className='text-xl mt-4 mb-2'>Buffer</h2>
        <InstructionTable instructions={fetch.fetchedCode} />
      </CardContent>
    </>
  );
};
