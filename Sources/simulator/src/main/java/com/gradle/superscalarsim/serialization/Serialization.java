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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

/**
 * Factory for JSON serializer, deserializer and schema generator.
 */
public class Serialization
{
  /**
   * @return ObjectMapper for serialization
   */
  public static ObjectMapper getSerializer()
  {
    return createObjectMapper();
  }
  
  /**
   * Internal method for creating the ObjectMapper
   */
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
  
  /**
   * @return ObjectMapper for deserialization
   */
  public static ObjectMapper getDeserializer()
  {
    return createObjectMapper();
  }
  
  /**
   * @param cls The class to generate JSON schema for
   *
   * @return JSON schema for the given class
   */
  public static JsonNode getSchema(Class<?> cls)
  {
    ObjectMapper        objectMapper = createObjectMapper();
    JsonSchemaGenerator schemaGen    = new JsonSchemaGenerator(objectMapper);
    return schemaGen.generateJsonSchema(cls);
  }
}
