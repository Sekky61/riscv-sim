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

import com.gradle.superscalarsim.enums.DataTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of parsing directives in assembly code.
 * Can have multiple sequences of values of different data types.
 *
 * @brief Describes one named memory location (constant, array, struct).
 */
public class MemoryLocation
{
  /**
   * Name of the memory location
   * Derived from the label in the assembly code
   */
  public String name;
  
  /**
   * Alignment of the memory location in bytes
   */
  public int alignment;
  
  /**
   * Chunks of data.
   * They lie next to each other in memory.
   * One named memory location may have multiple data types. A C struct is an example.
   * <p>
   * <code>
   * Array:
   * .byte   1
   * .word   42
   * </code>
   * </p>
   * This would result in two data chunks: byte and int.
   */
  List<DataChunk> dataChunks;
  
  /**
   * @brief Constructor for deserialization
   */
  public MemoryLocation()
  {
  }
  
  /**
   * @brief Constructor with initial value. For testing purposes.
   */
  public MemoryLocation(String name, int alignment, DataTypeEnum dataType, List<String> values)
  {
    this(name, alignment);
    this.dataChunks.add(new DataChunk(dataType));
    this.dataChunks.get(0).values = values;
  }
  
  /**
   * @brief Constructor
   */
  public MemoryLocation(String name, int alignment)
  {
    this.name       = name;
    this.alignment  = alignment;
    this.dataChunks = new ArrayList<>();
  }
  
  public List<DataChunk> getDataChunks()
  {
    return dataChunks;
  }
  
  /**
   * Add a value to the latest data chunk
   */
  public void addValue(String value)
  {
    dataChunks.get(dataChunks.size() - 1).values.add(value);
  }
  
  /**
   * @brief String representation of the memory location
   */
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(name + " ");
    for (DataChunk dataChunk : dataChunks)
    {
      result.append(dataChunk.toString()).append(" ");
    }
    return result.toString();
  }
  
  /**
   * @return Bytes of the memory location
   */
  public List<Byte> getBytes()
  {
    List<Byte> bytes = new ArrayList<>();
    for (DataChunk dataChunk : dataChunks)
    {
      bytes.addAll(dataChunk.getBytes());
    }
    return bytes;
  }
  
  /**
   * @return Size of the whole memory location in bytes
   */
  public int getByteSize()
  {
    int size = 0;
    for (DataChunk dataChunk : dataChunks)
    {
      size += dataChunk.getByteSize();
    }
    return size;
  }
  
  /**
   * @brief Add a new data chunk
   */
  public void addDataChunk(DataTypeEnum dataType)
  {
    dataChunks.add(new DataChunk(dataType));
  }
  
  /**
   *
   */
  public static class DataChunk
  {
    /**
     * Data type of the memory location
     */
    public DataTypeEnum dataType;
    
    /**
     * Value of the memory location.
     * This is the semantic value of a memory location. If the data type is int, each of
     * these values will occupy 4 bytes.
     */
    public List<String> values;
    
    /**
     * @brief Constructor for deserialization
     */
    public DataChunk()
    {
    }
    
    /**
     * @brief Constructor
     */
    public DataChunk(DataTypeEnum dataType)
    {
      this.dataType = dataType;
      this.values   = new ArrayList<>();
    }
    
    /**
     * @brief String representation of the memory chunk
     */
    @Override
    public String toString()
    {
      boolean isArray = getByteSize() > dataType.getSize();
      if (isArray)
      {
        return dataType + "[" + getByteSize() / dataType.getSize() + "]";
      }
      else
      {
        return dataType + " " + getBytes().get(0);
      }
    }
    
    /**
     * @return Size of the memory location in bytes
     */
    public int getByteSize()
    {
      return getBytes().size();
    }
    
    /**
     * Interpret the values according to the data type
     *
     * @return List of bytes
     */
    public List<Byte> getBytes()
    {
      List<Byte> bytes = new ArrayList<>();
      for (Object o : values)
      {
        byte[] b = dataType.getBytes(o.toString());
        for (byte value : b)
        {
          bytes.add(value);
        }
      }
      return bytes;
    }
  }
}
