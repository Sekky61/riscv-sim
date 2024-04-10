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
 *   <li>Instead of array of strings `names` as defined in MemoryLocation, a String field `name` is used.</li>
 *   <li>Instead of array of SpanType `dataTypes`, a String field `dataType` can be used.
 *   This results in the dataTypes being a list with the single SpanType describing the type of all elements.
 *   As a note, the `dataTypes` or `dataType` fields must be present, but not both at the same time.</li>
 *   <li>The individual data points can be a string or a number. The numbers are converted to strings, later they are
 *   parsed to the specified data type using Java's built-in `decode`, `parseUnsignedInt`, `floatToIntBits` and similar.</li>
 *   <li>The data is expressed as a discriminated union of three alternatives. See them below.</li>
 *  </ul>
 * <p>
 *   Now for the alternatives of defining the data: the `.data` field is an object with a field `kind`.
 *   Depending on the value of `kind`, the object has other fields.
 *  <ul>
 *   <li>`kind` is `constant` and the object has the fields `constant` and `size`. This is the same
 *   as using the `data` field with the constant repeated `size` times.</li>
 *   <li>`kind` is `random` and the object has additional fields `min`, `max` and `size`.
 *   The range is inclusive. Both must be integers. The same seed is used for generating the numbers, which results
 *   in the same sequence of numbers for the same min, max and size.</li>
 *   <li>`kind` is `csv`: the last alternative way of defining the data is to use a csv file defined in the `path` field.
 *   The file is read and the data from each cell is used as the data of the memory location.
 *   The number of elements in the data is the same as the number of cells in the CSV file.
 *   The delimiter is a comma. The path must be absolute or relative to the working directory of the application.
 *   Note that this is different from the CSV usage on the simulator web app.</li>
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
    
    // Load up the data object
    JsonNode dataNode = node.get("data");
    String   kind     = dataNode.get("kind").asText();
    
    // First detect if there is a `data` field
    if (kind.equals("data"))
    {
      JsonNode dataNodes = dataNode.get("data");
      for (JsonNode dataElementNode : dataNodes)
      {
        data.add(dataElementNode.asText());
      }
    }
    else if (kind.equals("constant"))
    {
      String constant = dataNode.get("constant").asText();
      int    size     = dataNode.get("size").asInt();
      for (int i = 0; i < size; i++)
      {
        data.add(constant);
      }
    }
    else if (kind.equals("random"))
    {
      int min  = dataNode.get("min").asInt();
      int max  = dataNode.get("max").asInt();
      int size = dataNode.get("size").asInt();
      for (int i = 0; i < size; i++)
      {
        long randomValue = randomSource.nextInt(max - min + 1) + min;
        data.add(String.valueOf(randomValue));
      }
    }
    else if (kind.equals("csv"))
    {
      String csvPath = dataNode.get("path").asText();
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
    else
    {
      throw new RuntimeException("Unknown kind of data");
    }
    
    // questionable, but I wouldn't allow it
    if (data.isEmpty())
    {
      System.err.println("Empty data in memory location");
    }
    
    return new MemoryLocation(names, alignment, dataTypes, data);
  }
}

