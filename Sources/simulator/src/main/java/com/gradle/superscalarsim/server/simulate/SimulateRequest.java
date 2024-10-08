/**
 * @file SimulateRequest.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Parameters for the /simulate endpoint request
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.cpu.SimulationConfig;

import java.util.Optional;

/**
 * Parameters for the /simulate endpoint request
 */
public class SimulateRequest
{
  /**
   * The requested tick to get the state of.
   * Tick 0 is the initial state of the simulation.
   * If not specified, the state of the last tick is returned (the end of the simulation).
   */
  @JsonProperty(required = true)
  Optional<Integer> tick;
  /**
   * The configuration to use for the simulation
   * Used for getting the initial state in case of a backwards simulation
   */
  @JsonProperty(required = true)
  SimulationConfig config;
  
  public SimulateRequest()
  {
  }
  
  public SimulateRequest(SimulationConfig config, Optional<Integer> tick)
  {
    this.config = config;
    this.tick   = tick;
  }
}
