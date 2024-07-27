/**
 * @file SimulateResponse.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Response for the /simulate endpoint
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

import com.gradle.superscalarsim.cpu.CpuState;
import com.gradle.superscalarsim.cpu.StopReason;

/**
 * Response for the /simulate endpoint
 */
public class SimulateResponse
{
  /**
   * Delta of the executed steps
   */
  public int executedSteps;
  /**
   * State of the CPU at the requested tick, or at the end of the simulation, whichever comes first
   */
  public CpuState state;
  
  /**
   * Reason for stopping the simulation. Either not stopped yet, or the simulation ended.
   */
  public StopReason stopReason;
  
  SimulateResponse(CpuState state, int executed_steps, StopReason stopReason)
  {
    this.executedSteps = executed_steps;
    this.state         = state;
    this.stopReason    = stopReason;
  }
  
  /**
   * Convert the response to a short response
   */
  public SimulateShortResponse toShortResponse()
  {
    return new SimulateShortResponse(state.debugLog, state.statistics, stopReason,
                                     state.unifiedRegisterFileBlock.getArchitecturalRegisterValues());
  }
}
