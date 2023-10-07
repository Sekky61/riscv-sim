/**
 * @file SimulationRequest.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Parameters for the /simulation endpoint request
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

import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.cpu.CpuState;

/**
 * Parameters for the /simulation endpoint request
 */
public class SimulationRequest
{
  /**
   * The requested tick to get the state of
   */
  int tick;
  /**
   * The configuration to use for the simulation
   * Used for getting the initial state in case of a backwards simulation
   */
  CpuConfiguration config;
  /**
   * The state to start the simulation from. Can be null
   *
   * @details Including this field is simply a performance optimization.
   * If the state is not provided, it will be first calculated from the configuration,
   * then simulated to the desired {@link #tick}
   */
  CpuState state;
}
