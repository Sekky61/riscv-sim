/**
 * @file InstructionDescriptionHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Response for the /instructionDescription endpoint
 * @date 08 feb      2024 10:00 (created)
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

package com.gradle.superscalarsim.server.instructionDescriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @class InstructionDescriptionHandler
 * @brief Handler class for instruction description requests.
 * Gets the instruction descriptions and returns them in a map.
 */
public class InstructionDescriptionHandler implements IRequestResolver<InstructionDescriptionRequest, InstructionDescriptionResponse>, IRequestDeserializer<InstructionDescriptionRequest>
{
  
  public InstructionDescriptionResponse resolve(InstructionDescriptionRequest request)
  {
    // Load the instruction descriptions
    StaticDataProvider                    loader = new StaticDataProvider();
    Map<String, InstructionFunctionModel> models = loader.getInstructionFunctionModels();
    return new InstructionDescriptionResponse(models);
  }
  
  @Override
  public InstructionDescriptionRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, InstructionDescriptionRequest.class);
  }
}
