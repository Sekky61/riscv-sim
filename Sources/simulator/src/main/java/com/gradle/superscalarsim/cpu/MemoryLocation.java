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
 * Result of parsing directives in assembly code
 *
 * @brief Describes one memory location (constant, array)
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
   * Value of the memory location
   */
  public List<Byte> bytes;
  
  /**
   * Data type of the memory location
   */
  public DataTypeEnum dataType;

  /**
   * @brief Constructor for deserialization
   */
  public MemoryLocation() {
  }
  
  /**
   * @brief Constructor
   */
  public MemoryLocation(String name, int alignment, List<Byte> bytes, DataTypeEnum dataType)
  {
    this.name      = name;
    this.dataType  = dataType;
    this.alignment = alignment;
    this.bytes     = bytes;
  }
  
  /**
   * @brief Constructor for FX values
   */
  public static MemoryLocation createFx(String name, int alignment, List<Long> fxValues, DataTypeEnum dataType)
  {
    // Convert to bytes
    List<Byte> bytes = new ArrayList<>();
    for (Long fxValue : fxValues)
    {
      byte[] b = dataType.getBytes(fxValue.toString());
      for (byte b1 : b)
      {
        bytes.add(b1);
      }
    }
    return new MemoryLocation(name, alignment, bytes, dataType);
  }
  
  /**
   * @brief Constructor for FP values
   */
  public static MemoryLocation createFp(String name, int alignment, List<Double> fpValues, DataTypeEnum dataType)
  {
    // Convert to bytes
    List<Byte> bytes = new ArrayList<>();
    for (Double fpValue : fpValues)
    {
      byte[] b = dataType.getBytes(fpValue.toString());
      for (byte b1 : b)
      {
        bytes.add(b1);
      }
    }
    return new MemoryLocation(name, alignment, bytes, dataType);
  }
  
  /**
   * @brief String representation of the memory location
   */
  @Override
  public String toString()
  {
    
    boolean isArray = getSize() > dataType.getSize();
    if (isArray)
    {
      return name + " " + dataType + "[" + getSize() / dataType.getSize() + "]";
    }
    else
    {
      return name + " " + dataType + " " + bytes.get(0);
    }
  }
  
  /**
   * @return Size of the memory location in bytes
   */
  public int getSize()
  {
    return bytes.size();
  }
  
}
