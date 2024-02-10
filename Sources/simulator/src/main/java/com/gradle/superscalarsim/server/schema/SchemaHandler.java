/**
 * @file SchemaHandler.java
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigRequest;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigResponse;
import com.gradle.superscalarsim.server.compile.CompileRequest;
import com.gradle.superscalarsim.server.compile.CompileResponse;
import com.gradle.superscalarsim.server.instructionDescriptions.InstructionDescriptionRequest;
import com.gradle.superscalarsim.server.instructionDescriptions.InstructionDescriptionResponse;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmRequest;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmResponse;
import com.gradle.superscalarsim.server.simulate.SimulateRequest;
import com.gradle.superscalarsim.server.simulate.SimulateResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SchemaHandler implements IRequestResolver<SchemaRequest, JsonNode>, IRequestDeserializer<SchemaRequest>
{
  
  @Override
  public SchemaRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, SchemaRequest.class);
  }
  
  /**
   * @param request Request to find the schema for
   *
   * @return JsonSchema for the request
   * @brief Find the correct schema for the request
   */
  @Override
  public JsonNode resolve(SchemaRequest request)
  {
    boolean isRequest = Objects.equals(request.requestResponse, SchemaRequest.RequestResponse.request);
    
    // Match the request to the correct handler
    Class<?> handler = switch (request.endpoint)
    {
      case simulate -> isRequest ? SimulateRequest.class : SimulateResponse.class;
      case parseAsm -> isRequest ? ParseAsmRequest.class : ParseAsmResponse.class;
      case compile -> isRequest ? CompileRequest.class : CompileResponse.class;
      case schema -> isRequest ? SchemaRequest.class : JsonNode.class;
      case checkConfig -> isRequest ? CheckConfigRequest.class : CheckConfigResponse.class;
      case instructionDescription -> isRequest ? InstructionDescriptionRequest.class : InstructionDescriptionResponse.class;
    };
    
    // Get the schema for the handler
    return Serialization.getSchema(handler);
  }
}
