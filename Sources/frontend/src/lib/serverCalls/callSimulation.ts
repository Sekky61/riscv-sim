/**
 * @file    callSimulation.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Call compiler API implementation
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

import { CpuState } from '@/lib/types/cpuApi';

export interface SimulatorResponse {
  '@type': 'com.gradle.superscalarsim.server.simulation.SimulationResponse';
  executedSteps: number;
  state: CpuState;
}

export async function callSimulationImpl(tick: number, cfg: object) {
  // fetch from :8000/compile
  // payload:
  // {
  //   "@type": "com.gradle.superscalarsim.server.simulation.SimulationRequest",
  //   "tick": number,
  //   "config": CpuConfig
  // }

  const serverUrl =
    process.env.NEXT_PUBLIC_SIMSERVER_URL || 'http://localhost:8000';

  const response = await fetch(`${serverUrl}/simulation`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      '@type': 'com.gradle.superscalarsim.server.simulation.SimulationRequest',
      tick,
      config: cfg,
    }),
  });
  const json: SimulatorResponse = await response.json();
  return json;
}
