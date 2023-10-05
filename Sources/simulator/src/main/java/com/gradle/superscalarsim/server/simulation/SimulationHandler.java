/**
 * @file SimulationHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Handler for /simulation requests
 * @date 26 Sep      2023 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gradle.superscalarsim.server.simulation;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.server.IRequestResolver;

/**
 * Handler for /simulation requests
 * Gets a CpuState and a number of steps to run and returns the resulting CpuState
 * - For backwards simulation, TODO
 * - For Getting initial state from a configuration, see
 * {@link com.gradle.superscalarsim.server.getState.GetStateHandler}
 */
public class SimulationHandler implements IRequestResolver<SimulationRequest, SimulationResponse>
{
  
  @Override
  public SimulationResponse resolve(SimulationRequest request)
  {
    
    SimulationResponse response;
    if (request == null || request.state == null)
    {
      // Send error
      response = new SimulationResponse(null, 0);
    }
    else
    {
      // Run simulation
      response = runSimulation(request);
    }
    return response;
  }
  
  private SimulationResponse runSimulation(SimulationRequest request)
  {
    Cpu cpu         = new Cpu(request.state);
    int actualSteps = cpu.run(request.steps);
    return new SimulationResponse(cpu.cpuState, actualSteps);
  }
}
