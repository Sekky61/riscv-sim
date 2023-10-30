/**
 * @file RegisterTypeEnum.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains enumeration definition for register types
 * @date 15 October 2023 15:00 (created) \n
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
package com.gradle.superscalarsim.enums;

/**
 * Different from DataTypeEnum - this is used for register file types
 * (FX/FP functional units), data type is used for interpreting the content of the register.
 *
 * @brief Enumeration definition for different register types used in simulation.
 */
public enum RegisterTypeEnum
{
  /**
   * 32b integer register
   */
  kInt,
  
  /**
   * 32b floating point register
   */
  kFloat,
}
