/**
 * @file MemoryLocation.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Describes one memory location (constant, array)
 * @date 26 November      2023 14:00 (created)
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


package com.gradle.superscalarsim.cpu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.serialization.MemoryLocationDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of parsing directives in assembly code and JSON definitions from CLI or API.
 * Used in the phase of building the memory, not in the simulation itself.
 * <p>
 * The JSONs can include extra fields, they are ignored during deserialization.
 * One named memory location may have multiple data types. A C struct is an example:
 * <code>
 * Array:
 * .byte   1
 * .word   42
 * </code>.
 * This would result in two data chunks: byte and int.
 * <p>
 * In some cases, the value can be a label, for compatibility with GCC.
 *
 * @brief Describes one named memory location (constant, array, struct).
 * See {@link MemoryLocationDeserializer} for alternative JSON representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = MemoryLocationDeserializer.class)
public class MemoryLocation
{
  /**
   * Names of the memory locations. The first name is the primary one.
   * Derived from the label in the assembly code.
   * When multiple labels point to the same memory location, they are all stored here.
   */
  public List<String> names;
  
  /**
   * Alignment of the memory location in bytes.
   * Warning: This clashes with the .align directive in the assembly code, which is in log2.
   */
  public int alignment;
  /**
   * Either a dataType for the whole memory location or a list of data types for each data chunk.
   * Must be sorted in ascending order of the start offset.
   * Example: [{0, int}] means that the whole memory location is an int.
   * Example: [{0, int}, {7, byte}] means that the first 7 items (data[0-6], not bytes) are int, the rest are bytes.
   */
  public List<SpanType> dataTypes;
  /**
   * Memory items. They lie next to each other in memory.
   * The data type of each item is defined in the dataTypes list.
   * Example: ["1", "2", "3.7"].
   */
  public List<String> data;
  
  /**
   * @brief Constructor with initial value. Useful for testing purposes.
   */
  public MemoryLocation(String name, int alignment, DataTypeEnum dataType, List<String> values)
  {
    assert alignment > 0;
    this.names = new ArrayList<>();
    this.names.add(name);
    
    this.alignment = alignment;
    this.data      = values;
    this.dataTypes = new ArrayList<>();
    this.dataTypes.add(new SpanType(0, dataType));
  }
  
  /**
   * @param names     Names of the memory location
   * @param alignment Alignment of the memory location
   * @param dataTypes Data types of the memory location
   * @param data      Data of the memory location
   *
   * @brief Constructor for custom deserialization.
   */
  public MemoryLocation(List<String> names, int alignment, List<SpanType> dataTypes, List<String> data)
  {
    this.names     = names;
    this.alignment = alignment;
    this.data      = data;
    this.dataTypes = dataTypes;
  }
  
  /**
   * @brief Constructor. Used when building the memory location from the assembly code.
   */
  public MemoryLocation()
  {
    this.alignment = 1;
    this.data      = new ArrayList<>();
    this.dataTypes = new ArrayList<>();
  }
  
  public List<SpanType> getDataTypes()
  {
    return dataTypes;
  }
  
  /**
   * Add a value to the latest data chunk
   */
  public void addValue(String value)
  {
    data.add(value);
  }
  
  /**
   * Add a new alias
   */
  public void addName(String alias)
  {
    if (names == null)
    {
      names = new ArrayList<>();
    }
    names.add(alias);
  }
  
  /**
   * @brief String representation of the memory location.
   */
  @Override
  public String toString()
  {
    StringBuilder sb   = new StringBuilder();
    String        name = getName();
    sb.append(name == null ? "unnamed" : name);
    sb.append(" [");
    for (SpanType spanType : dataTypes)
    {
      sb.append(spanType.dataType).append(" ");
    }
    sb.append(";");
    int length = data.size();
    sb.append(length).append("]");
    return sb.toString();
  }
  
  public String getName()
  {
    if (names == null || names.isEmpty())
    {
      return null;
    }
    return names.get(0);
  }
  
  /**
   * @return Bytes of the memory location
   */
  public byte[] getBytes()
  {
    byte[]       bytes                 = new byte[getByteSize()];
    DataTypeEnum currentDataType       = null;
    int          currentDataTypesIndex = 0;
    int          nextDataTypeIndex     = 0;
    int          currentByteIndex      = 0;
    for (int currentDataIndex = 0; currentDataIndex < data.size(); currentDataIndex++)
    {
      if (currentDataIndex == nextDataTypeIndex)
      {
        currentDataType = dataTypes.get(currentDataTypesIndex).dataType;
        currentDataTypesIndex++;
        nextDataTypeIndex = currentDataTypesIndex < dataTypes.size() ? dataTypes.get(
                currentDataTypesIndex).startOffset : data.size();
      }
      byte[] b = currentDataType.getBytes(data.get(currentDataIndex));
      // copy
      System.arraycopy(b, 0, bytes, currentByteIndex, b.length);
      currentByteIndex += b.length;
    }
    return bytes;
  }
  
  /**
   * @return Size of the whole memory location in bytes
   */
  public int getByteSize()
  {
    int      size         = 0;
    SpanType lastSpanType = dataTypes.get(0);
    for (int i = 1; i < dataTypes.size(); i++)
    {
      SpanType spanType = dataTypes.get(i);
      int      items    = spanType.startOffset - lastSpanType.startOffset;
      size += items * lastSpanType.dataType.getSize();
      lastSpanType = spanType;
    }
    size += (data.size() - lastSpanType.startOffset) * lastSpanType.dataType.getSize();
    return size;
  }
  
  /**
   * @brief Mark a new data type at the current size of data.
   */
  public void addDataChunk(DataTypeEnum dataType)
  {
    int startOffset = data.size();
    dataTypes.add(new SpanType(startOffset, dataType));
  }
  
  /**
   * @param startOffset Offset into the list of values
   * @param dataType    Type of the data
   *
   * @brief Record for the start of a new data type in the list of values.
   */
  public record SpanType(int startOffset, DataTypeEnum dataType)
  {
  }
}
