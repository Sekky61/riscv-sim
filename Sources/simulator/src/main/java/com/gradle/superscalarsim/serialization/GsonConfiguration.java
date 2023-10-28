/**
 * @file GsonConfiguration.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Serialize and deserialize objects to/from JSON
 * @todo delete
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

package com.gradle.superscalarsim.serialization;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gradle.superscalarsim.code.ParseError;

import java.util.HashMap;
import java.util.Map;

import static com.cedarsoftware.util.io.JsonWriter.CUSTOM_WRITER_MAP;
import static com.cedarsoftware.util.io.JsonWriter.TYPE_NAME_MAP;

/**
 * @brief Provider for Gson instance configured with custom (de)serializers
 */
public class GsonConfiguration
{
  
  /**
   * Map from long type names to short ones.
   * Used to shorten the JSON output.
   */
  static Map<String, String> typeMap;
  
  static
  {
    typeMap = new HashMap<>();
    typeMap.put("java.util.ArrayList", "ArrayList");
    typeMap.put("java.util.BitSet", "BitSet");
    typeMap.put("com.gradle.superscalarsim.models.InputCodeModel", "InputCodeModel");
    typeMap.put("com.gradle.superscalarsim.models.register.RegisterModel", "RegisterModel");
    typeMap.put("com.gradle.superscalarsim.models.register.RegisterValueModel", "RegisterValueModel");
    typeMap.put("com.gradle.superscalarsim.models.InputCodeArgument", "InputCodeArgument");
    typeMap.put("com.gradle.superscalarsim.models.InstructionFunctionModel$Argument",
                "InstructionFunctionModelArgument");
    typeMap.put("com.gradle.superscalarsim.models.SimCodeModel", "SimCodeModel");
    typeMap.put("com.gradle.superscalarsim.blocks.branch.OneBitPredictor", "OneBitPredictor");
    typeMap.put("com.gradle.superscalarsim.blocks.branch.TwoBitPredictor", "TwoBitPredictor");
    typeMap.put("com.gradle.superscalarsim.blocks.branch.ZeroBitPredictor", "ZeroBitPredictor");
    typeMap.put("com.gradle.superscalarsim.models.RenameMapModel", "RenameMapModel");
    typeMap.put("com.gradle.superscalarsim.models.ReorderBufferItem", "ReorderBufferItem");
  }
  
  /**
   * @return Instance of options for JsonWriter (JavaIo library)
   */
  public static Map<String, Object> getJsonWriterOptions()
  {
    // https://github.com/jdereg/json-io/blob/master/user-guide.md
    Map<Class, JsonWriter.JsonClassWriterEx> javaIoWriters = new HashMap<>();
    javaIoWriters.put(ParseError.class, new ParseError.CustomParseErrorWriter());
    
    Map<String, Object> javaIoOptions = new HashMap<>();
    javaIoOptions.put(CUSTOM_WRITER_MAP, javaIoWriters);
    javaIoOptions.put(TYPE_NAME_MAP, typeMap);
    
    return javaIoOptions;
  }
  
  public static Map<String, Object> getJsonReaderOptions()
  {
    Map<String, Object> javaIoOptions = new HashMap<>();
    javaIoOptions.put(TYPE_NAME_MAP, typeMap);
    return javaIoOptions;
  }
  
  public static Gson getGson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    return builder.create();
  }
}
