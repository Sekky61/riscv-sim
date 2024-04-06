/**
 * @file Symbol.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Class for a symbol (label) in the code
 * @date 27 Nov  2023 17:00 (created)
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


package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;

/**
 * @brief Represents a symbol in the code.
 * Used for jumps, data pointers.
 */
public class Symbol
{
  /**
   * Name of the symbol
   */
  public String name;
  
  /**
   * Value of the symbol. An instruction address, or a data address.
   * This same object is also referenced by {@link com.gradle.superscalarsim.models.instruction.InputCodeArgument} in the instruction.
   * So changing this value will change the value in the instruction.
   * Can be null if the symbol is not yet resolved.
   */
  private RegisterDataContainer value;
  
  /**
   * Type of the symbol
   */
  public SymbolType type;
  
  /**
   * @param name  Name of the symbol
   * @param value Value of the symbol. Can be null.
   * @param type  Type of the symbol
   *
   * @brief Constructor
   */
  public Symbol(String name, RegisterDataContainer value, SymbolType type)
  {
    this.name  = name;
    this.value = value;
    this.type  = type;
  }
  
  public RegisterDataContainer getValue()
  {
    return value;
  }
  
  public void setValue(RegisterDataContainer value)
  {
    this.value = value;
  }
  
  public long getAddress()
  {
    return (long) value.getValue(DataTypeEnum.kLong);
  }
  
  /**
   * String representation of the label.
   */
  @Override
  public String toString()
  {
    return "name='" + name + ": " + value.toString();
  }
  
  /**
   * Type of the symbol
   */
  public enum SymbolType
  {
    /**
     * Symbol is a label
     */
    LABEL,
    /**
     * Symbol is a data pointer
     */
    DATA
  }
}
