/**
 * @file SimulationHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Handler for /simulate requests
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

package com.gradle.superscalarsim.server.simulate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;

import java.io.IOException;
import java.io.InputStream;

/**
 * @brief Handler for /simulate requests
 * Gets a Configuration and a tick and returns the state of the cpu at that tick
 * - For better performance, a state can be provided
 * - For backwards simulation, pass a tick lower than the current tick
 * - For getting initial state from a configuration, run with tick 0
 */
public class SimulateHandler implements IRequestResolver<SimulateRequest, SimulateResponse>, IRequestDeserializer<SimulateRequest>
{
  @Override
  public SimulateResponse resolve(SimulateRequest request)
  {
    SimulateResponse response;
    if (request == null || request.config == null || request.tick < 0)
    {
      // Send error
      // TODO: Add proper error handling
      throw new IllegalArgumentException("Invalid request fields");
    }
    else
    {
      // Check configuration, it may be used
      CpuConfiguration.ValidationResult errors = request.config.validate();
      if (errors.valid)
      {
        // Run simulation
        response = runSimulation(request);
      }
      else
      {
        // TODO: Add proper error handling
        for (String error : errors.messages)
        {
          System.err.println(error);
        }
        throw new IllegalArgumentException("Invalid request values");
      }
    }
    return response;
  }
  
  /**
   * @param request Request with the configuration and tick, optionally with the state
   *
   * @return Response with the state and the number of steps simulated
   * @brief Run the simulation
   */
  private SimulateResponse runSimulation(SimulateRequest request)
  {
    // If state is not provided, simulate from the beginning
    Cpu cpu        = new Cpu(request.config);
    int tickBefore = cpu.cpuState.tick;
    cpu.simulateState(request.tick);
    int actualSteps = cpu.cpuState.tick - tickBefore;
    return new SimulateResponse(cpu.cpuState, actualSteps);
  }
  
  @Override
  public SimulateRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, SimulateRequest.class);
  }
}
