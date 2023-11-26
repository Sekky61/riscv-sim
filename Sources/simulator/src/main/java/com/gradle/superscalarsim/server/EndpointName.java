/**
 * @file EndpointName.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Enumerates all endpoints
 * @date 09 Nov      2023 9:00 (created)
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

package com.gradle.superscalarsim.server;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enumerates all endpoints
 */
public enum EndpointName
{
  compile("compile"), parseAsm("parseAsm"), checkConfig("checkConfig"), simulate("simulate"), schema("schema");
  
  private final String pathName;
  
  EndpointName(String pathName)
  {
    this.pathName = pathName;
  }
  
  /**
   * @return The path of the endpoint
   */
  public String getPath()
  {
    return "/" + pathName;
  }
  
  /**
   * @return The name of the endpoint
   */
  @JsonProperty("endpoint")
  public String getName()
  {
    return pathName;
  }
}

