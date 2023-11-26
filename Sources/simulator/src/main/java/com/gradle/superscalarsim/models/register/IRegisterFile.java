/**
 * @file IRegisterFile.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains interface for register file
 * @date 19 Oct      2023 21:00 (revised)
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
package com.gradle.superscalarsim.models.register;

import com.gradle.superscalarsim.enums.RegisterTypeEnum;

/**
 * Interaction with a register file.
 *
 * @brief Interface for register file
 */
public interface IRegisterFile
{
  /**
   * @return Type of register file
   */
  RegisterTypeEnum getDataType();
  
  /**
   * @param registerName Name of register
   *
   * @return Register with given name or null if not found
   */
  RegisterModel getRegister(String registerName);
  
  /**
   * @return Number of registers in register file
   */
  int getRegisterCount();
}
