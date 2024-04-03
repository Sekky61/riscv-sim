/**
 * @file Label.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Class for label in the code
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
 * @brief Represents a label in the code.
 * Used for jumps, data pointers.
 */
public class Label
{
  /**
   * Name of the label
   */
  public String name;
  
  /**
   * Position of the label in the code (in bytes).
   * This same object is also referenced by {@link com.gradle.superscalarsim.models.instruction.InputCodeArgument} in the instruction.
   * So changing this value will change the value in the instruction.
   */
  private RegisterDataContainer address;
  
  public Label(String name, int address)
  {
    this.name    = name;
    this.address = RegisterDataContainer.fromValue(address);
  }
  
  public RegisterDataContainer getAddressContainer()
  {
    return address;
  }
  
  public long getAddress()
  {
    return (long) address.getValue(DataTypeEnum.kLong);
  }
  
  /**
   * String representation of the label.
   */
  @Override
  public String toString()
  {
    return "name='" + name + ": " + address.toString();
  }
}
