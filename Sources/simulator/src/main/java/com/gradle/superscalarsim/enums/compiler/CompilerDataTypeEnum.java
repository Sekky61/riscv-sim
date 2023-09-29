/**
 * @file DataTypeEnum.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@stud.fit.vutbr.cz
 * @brief File contains enumeration definition for compiler data types
 * @date 12 March  2023 20:50 (created) \n
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Jakub Horky
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
package com.gradle.superscalarsim.enums.compiler;

/**
 * @brief Enumeration definition for different data types used in simulation
 */
public enum CompilerDataTypeEnum
{
  kInt, ///< 32bit Integer value
  kUInt, ///< 32bit Unsigned Integer value
  kFloat, ///< Float value
  kDouble, ///< Double value
  kChar, ///< 8-bit integer value
  kUChar, ///< 8-bit unsigned integer value
  kHalf, ///< 16-bit integer value
  kUHalf, ///< 16-bit unsigned integer value
  kVoid, ///< Null value
  kVar, ///< Variable
  kLong, /// 64-bit integer value
  kULong, /// 64-bit unsigned integer value
}
