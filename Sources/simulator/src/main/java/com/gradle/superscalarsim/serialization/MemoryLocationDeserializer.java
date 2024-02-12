/**
 * @file MemoryLocationDeserializer.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Deserializer for MemoryLocation
 * @date 12 February      2024 12:00 (created)
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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @brief Deserializer for MemoryLocation.
 * @details MemoryLocation can have different JSON forms.
 * The first one is the actual shape of the {@link MemoryLocation} class.
 * The simulator expects an array of these objects.
 * Next comes the list of deviating fields:
 * <ul>
 *   <li>Instead of array of strings `names` as defined in MemoryLocation, a String field `name` is used.
 *   This disallows manually created memory locations to have multiple names.</li>
 *   <li>Instead of array of SpanType `dataTypes`, a String field `dataType` can be used.
 *   This results in the dataTypes being a list with the single SpanType describing the type of all elements.
 *   As a note, the `dataTypes` or `dataType` fields must be present, but not both at the same time.</li>
 *   <li>The individual data points can be a string or a number. The numbers are converted to strings, later they are
 *   parsed to the specified data type using Java's built-in `decode`, `parseUnsignedInt`, `floatToIntBits` and similar.></li>
 *   <li>The first alternative way of defining the data is to use the fields `constant` and `size`. This is the same
 *   as using the `data` field with the constant repeated `size` times.</li>
 *   <li>The second alternative way of defining the data is to use the fields `random` and `size`. This is the same
 *   as using the `data` field with the random value repeated `size` times.
 *   The random value is an object with the fields `min` and `max`, which are the minimum and maximum values of the
 *   random number. Both are inclusive. Both must be integers. The same seed is used for generating the numbers, which results
 *   in the same sequence of numbers for the same min, max and size.</li>
 *   <li>The last alternative way of defining the data is to use the field `csv` (no `size` field). The file is read and
 *   the data from each cell is used as the data of the memory location. The number of elements in the data is the same as
 *   the number of cells in the CSV file. The delimiter is a comma. The path must be absolute or relative to the working
 *   directory of the application. Note that this is different from the CSV usage on the simulator web app.</li>
 *  </ul>
 * <p>
 *   Find examples in the Sources/simulator/src/test/resources/memoryLocations directory.
 * <p>
 *  TODO: random min and max can be only decimal?
 *  TODO: test if random numbers are generated correctly
 */
public class MemoryLocationDeserializer extends StdDeserializer<MemoryLocation>
{
  /**
   * Random source for generating random numbers with a fixed seed.
   * This means that the same sequence of random numbers is generated for the same min, max and size,
   * but the sequence is different if the deserialization is called in different order.
   * Such case can happen if you change the order of the memory locations in the JSON file.
   */
  private final Random randomSource;
  
  /**
   * No-args constructor. Taken from Jackson documentation.
   */
  public MemoryLocationDeserializer()
  {
    this(null);
  }
  
  /**
   * Taken from Jackson documentation.
   */
  public MemoryLocationDeserializer(Class<?> vc)
  {
    super(vc);
    this.randomSource = new Random(1337);
  }
  
  /**
   * Deserialize JSON content into the MemoryLocation instance.
   * Alternative JSON representations are described in the class documentation.
   * Returned instance is to be constructed by method itself.
   *
   * @param p    Parsed used for reading JSON content
   * @param ctxt Context that can be used to access information about
   *             this deserialization activity.
   *
   * @return Deserialized value
   */
  @Override
  public MemoryLocation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
  {
    JsonNode node = p.getCodec().readTree(p);
    
    // First detect if there is a `name` or `names` field
    List<String> names = new ArrayList<>();
    if (node.has("name"))
    {
      names.add(node.get("name").asText());
    }
    else
    {
      throw new RuntimeException("No name found in the JSON file");
    }
    
    // Then detect if there is a `dataType` or `dataTypes` field
    List<MemoryLocation.SpanType> dataTypes = new ArrayList<>();
    if (node.has("dataType"))
    {
      DataTypeEnum dataType = DataTypeEnum.valueOf(node.get("dataType").asText());
      dataTypes.add(new MemoryLocation.SpanType(0, dataType));
    }
    else
    {
      JsonNode dataTypesNodes = node.get("dataTypes");
      for (JsonNode dataTypeNode : dataTypesNodes)
      {
        DataTypeEnum dataType = DataTypeEnum.valueOf(dataTypeNode.get("dataType").asText());
        dataTypes.add(new MemoryLocation.SpanType(dataTypeNode.get("startOffset").asInt(), dataType));
      }
      if (dataTypes.isEmpty())
      {
        throw new RuntimeException("No data types found in the JSON file");
      }
    }
    
    // Read alignment
    int alignment = node.get("alignment").asInt();
    
    // Data part
    List<String> data = new ArrayList<>();
    
    // First detect if there is a `data` field
    if (node.has("data"))
    {
      JsonNode dataNodes = node.get("data");
      for (JsonNode dataNode : dataNodes)
      {
        data.add(dataNode.asText());
      }
    }
    else if (node.has("constant"))
    {
      String constant = node.get("constant").asText();
      int    size     = node.get("size").asInt();
      for (int i = 0; i < size; i++)
      {
        data.add(constant);
      }
    }
    else if (node.has("random"))
    {
      int min  = node.get("random").get("min").asInt();
      int max  = node.get("random").get("max").asInt();
      int size = node.get("size").asInt();
      for (int i = 0; i < size; i++)
      {
        long randomValue = randomSource.nextInt(max - min + 1) + min;
        data.add(String.valueOf(randomValue));
      }
    }
    else
    {
      // Then detect if there is a `csv` field
      if (node.has("csv"))
      {
        String csvPath = node.get("csv").asText();
        // The working directory is the root of the project
        // If path exists, read the file and fill the data
        Path   path = Paths.get(csvPath);
        Reader reader;
        if (Files.exists(path))
        {
          reader = Files.newBufferedReader(path);
        }
        else
        {
          // This is a small complication for tests
          InputStream is = MemoryLocationDeserializer.class.getResourceAsStream(csvPath);
          reader = new BufferedReader(new InputStreamReader(is));
        }
        
        {
          try (CSVReader csvReader = new CSVReader(reader))
          {
            List<String[]> strings = csvReader.readAll();
            for (String[] line : strings)
            {
              data.addAll(Arrays.asList(line));
            }
          }
          catch (CsvException e)
          {
            throw new RuntimeException("Error while reading CSV file", e);
          }
        }
      }
    }
    
    // questionable, but I wouldn't allow it
    if (data.isEmpty())
    {
      throw new RuntimeException("No data found in the JSON file");
    }
    
    return new MemoryLocation(names, alignment, dataTypes, data);
  }
}

