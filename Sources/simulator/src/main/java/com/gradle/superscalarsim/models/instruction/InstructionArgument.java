/**
 * @file InstructionArgument.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Instruction argument (name, value, type)
 * @date 27 March  2024 22:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2020  Jan Vavra
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
package com.gradle.superscalarsim.models.instruction;

import com.gradle.superscalarsim.enums.DataTypeEnum;

/**
 * @param name         Name of the argument (example: "rd")
 * @param type         Data type of the argument (example: "kInt")
 * @param defaultValue Default value of the argument (example: "0" or null)
 * @param writeBack    True if the argument should be written back to register file on commit
 * @param isOffset     True if the argument is an offset. By default, false. Used by offset instructions.
 * @param silent       True if the argument is a data dependency, but is not allowed to be used in ASM code
 *
 * @brief Name convention: "r" for register, "i" for immediate.
 */
public record InstructionArgument(String name, DataTypeEnum type, String defaultValue, boolean writeBack,
                                  boolean isOffset, boolean silent)
{
  public InstructionArgument(String name, DataTypeEnum type, String defaultValue)
  {
    this(name, type, defaultValue, false, false, false);
  }
  
  public boolean isOffset()
  {
    return isOffset;
  }
  
  public String name()
  {
    return name;
  }
  
  public DataTypeEnum type()
  {
    return type;
  }
  
  public String defaultValue()
  {
    return defaultValue;
  }
  
  public boolean writeBack()
  {
    return writeBack;
  }
  
  public boolean silent()
  {
    return silent;
  }
  
  @Override
  public String toString()
  {
    return name + ":" + type + (defaultValue != null ? ":" + defaultValue : "");
  }
  
  /**
   * @return True if the argument is a register
   */
  public boolean isRegister()
  {
    return name.startsWith("r");
  }
  
  /**
   * @return True if the argument is an immediate
   */
  public boolean isImmediate()
  {
    return name.startsWith("i");
  }
}
