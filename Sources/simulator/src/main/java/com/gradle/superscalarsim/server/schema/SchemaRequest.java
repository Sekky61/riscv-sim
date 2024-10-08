/**
 * @file SchemaRequest.java
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

package com.gradle.superscalarsim.server.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.server.EndpointName;

/**
 * Parameters for the /schema endpoint request
 */
public class SchemaRequest
{
  /**
   * Name of the endpoint to get the schema for.
   * Example: "simulate" for the "/simulate" endpoint
   */
  @JsonProperty(required = true)
  EndpointName endpoint;
  
  /**
   * Request to get the schema for. Either "request" or "response".
   */
  @JsonProperty(required = true)
  RequestResponse requestResponse;
  
  public enum RequestResponse
  {
    request, response
  }
  
  public SchemaRequest()
  {
  
  }
  
  public SchemaRequest(EndpointName endpoint, RequestResponse requestResponse)
  {
    this.endpoint        = endpoint;
    this.requestResponse = requestResponse;
  }
}
