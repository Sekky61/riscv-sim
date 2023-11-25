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
  public List<Byte> value;
  
  /**
   * Data type of the memory location
   */
  public DataTypeEnum dataType;
  
  /**
   * @brief Constructor
   */
  public MemoryLocation(String name, int alignment, List<Byte> value, DataTypeEnum dataType)
  {
    this.name      = name;
    this.value     = value;
    this.dataType  = dataType;
    this.alignment = alignment;
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
      return name + " " + dataType + " " + value.get(0);
    }
  }
  
  /**
   * @return Size of the memory location in bytes
   */
  public int getSize()
  {
    return value.size();
  }
  
}
