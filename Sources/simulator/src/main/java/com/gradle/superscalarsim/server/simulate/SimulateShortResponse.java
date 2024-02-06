/**
 * @file SimulateShortResponse.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Short response for the /simulate endpoint and CLI
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

import com.gradle.superscalarsim.cpu.DebugLog;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.cpu.StopReason;

import java.util.Map;

/**
 * Short response for the /simulate endpoint and CLI.
 * Contains only the statistics, register values and debug log.
 */
public class SimulateShortResponse
{
  /**
   * Log messages from the simulation
   */
  public DebugLog debugLog;
  
  /**
   * Statistics of the simulation
   */
  
  public SimulationStatistics statistics;
  
  /**
   * Reason for stopping the simulation. Either not stopped yet, or the simulation ended.
   */
  public StopReason stopReason;
  
  /**
   * Architectural register values at the end of the simulation. Keys are register names, values are register values (bit values).
   * The aliased registers are included twice, once for each name.
   */
  public Map<String, Long> registerValues;
  
  /**
   * Constructor
   */
  public SimulateShortResponse(DebugLog debugLog,
                               SimulationStatistics statistics,
                               StopReason stopReason,
                               Map<String, Long> registers)
  {
    this.debugLog       = debugLog;
    this.statistics     = statistics;
    this.stopReason     = stopReason;
    this.registerValues = registers;
  }
}
