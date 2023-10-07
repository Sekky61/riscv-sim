/**
 * @file GetStateResponse.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Response for the /getState endpoint
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

package com.gradle.superscalarsim.server.getState;

import com.gradle.superscalarsim.code.ParseError;
import com.gradle.superscalarsim.cpu.CpuState;

import java.util.List;

public class GetStateResponse
{
  /**
   * @brief List of errors concerning configuration validation
   */
  public List<String> configErrors;
  /**
   * @brief List of errors concerning code validation
   */
  public List<ParseError> codeErrors;
  /**
   * @brief State of the CPU
   * Returned even if there are errors in the code
   */
  public CpuState state;
  
  GetStateResponse(CpuState state)
  {
    this.state        = state;
    this.configErrors = null;
    this.codeErrors   = null;
  }
  
  GetStateResponse(CpuState state, List<String> configErrors, List<ParseError> codeErrors)
  {
    this.state        = state;
    this.configErrors = configErrors;
    this.codeErrors   = codeErrors;
  }
}
