/**
 * @file SerializerFactory.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Factory for JSON serializer
 * @todo delete
 * @date 08 Nov      2023 20:00 (created)
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

package com.gradle.superscalarsim.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Serialization
{
  public static ObjectMapper getSerializer()
  {
    return createObjectMapper();
  }
  
  public static ObjectMapper getDeserializer()
  {
    return createObjectMapper();
  }
  
  private static ObjectMapper createObjectMapper()
  {
    ObjectMapper objectMapper = new ObjectMapper();
    // Configure that all fields are serialized, but getters and setters are not
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                                       .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                       .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                       .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                       .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    objectMapper.registerModule(new CustomSerializerModule());
    return objectMapper;
  }
}
