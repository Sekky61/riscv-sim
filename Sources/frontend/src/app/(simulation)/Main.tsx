/**
 * @file Main.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The main page of the application with the simulation
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

import CanvasWindow from "@/components/CanvasWindow";
import { SimGrid } from "@/components/simulation/SimGrid";
import Timeline from "@/components/simulation/Timeline";
import { AutoPlay } from "./AutoPlay";
import { useAppSelector } from "@/lib/redux/hooks";
import { selectErrorMessage, selectStateOk } from "@/lib/redux/cpustateSlice";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/base/ui/card";
import { SidePanel } from "@/components/simulation/SidePanel";

/**
 * Show either the simulation or an error message.
 */
export function Main() {

  const stateOk = useAppSelector(selectStateOk);
  const errorMessage = useAppSelector(selectErrorMessage);

  if (!stateOk) {
    return (
      <div className='flex justify-center items-center h-full'>
        <Card className='max-w-lg error-container'>
          <CardHeader>
            <CardTitle>Error</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-lg" >
            {errorMessage}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // All is OK, show the simulation
  return (
    <>
      <CanvasWindow>
        <SimGrid />
      </CanvasWindow>
      <div className='absolute top-3 left-1/2'>
        <Timeline />
      </div>
      <div className='absolute top-3 right-[250px]'>
        <AutoPlay />
      </div>
      <SidePanel />
    </>
  );
}
