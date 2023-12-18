/**
 * @file CheckConfigHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Handler for the /checkConfig endpoint
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

package com.gradle.superscalarsim.server.checkConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.cpu.CpuConfigValidator;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Handler for the /checkConfig endpoint
 * Parses config and tells if it's valid
 */
public class CheckConfigHandler implements IRequestResolver<CheckConfigRequest, CheckConfigResponse>, IRequestDeserializer<CheckConfigRequest>
{
  
  public CheckConfigResponse resolve(CheckConfigRequest request)
  {
    
    CheckConfigResponse response;
    if (request == null || request.config == null)
    {
      response = new CheckConfigResponse(false, new ArrayList<>(
              List.of(new CpuConfigValidator.Error("Invalid request fields", "root"))));
    }
    else
    {
      // Validate
      SimulationConfig.ValidationResult res = request.config.validate();
      response = new CheckConfigResponse(res.valid, res.messages);
    }
    return response;
  }
  
  @Override
  public CheckConfigRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, CheckConfigRequest.class);
  }
}
