/**
 * @file StopReason.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Reason for simulation end
 * @date 24 jan      2024 13:00 (created)
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

package com.gradle.superscalarsim.cpu;

public enum StopReason
{
  /**
   * Simulation is running.
   */
  kNotStopped,
  /**
   * Simulation stopped because of an exception in the code (not a Java exception).
   */
  kException,
  /**
   * Simulation stopped because the PC ran past the end of the code.
   */
  kEndOfCode,
  /**
   * Simulation stopped because the entry function returned.
   */
  kCallStackHalt,
  /**
   * Simulation stopped because the maximum number of cycles was reached (Protection against infinite loops).
   */
  kMaxCycles,
  /**
   * Simulation stopped because the maximum time was reached (Protection against infinite loops).
   */
  kTimeOut,
  /**
   * Simulation did not even start, because of a bad configuration.
   */
  kBadConfig
}
