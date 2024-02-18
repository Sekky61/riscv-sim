/**
 * @file CompileRequest.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Request for the /compile endpoint
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

package com.gradle.superscalarsim.server.parseAsm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.cpu.SimulationConfig;

/**
 * @brief Request for the /parseAsm endpoint
 */
public class ParseAsmRequest
{
  /**
   * @brief The ASM code to parse
   */
  @JsonProperty(required = true)
  String code;
  
  /**
   * @brief The Cpu configuration.
   * This is not required, but if it is present, it will be used to inform the parser about the defined memory locations.
   * The code from this object is ignored.
   */
  @JsonProperty(required = false)
  SimulationConfig config;
  
  /**
   * @brief Default constructor for deserialization
   */
  public ParseAsmRequest()
  {
  
  }
  
  /**
   * @param code   The ASM code to parse
   * @param config The Cpu configuration
   *
   * @brief Constructor for the request
   */
  public ParseAsmRequest(String code, SimulationConfig config)
  {
    this.code   = code;
    this.config = config;
  }
  
}
