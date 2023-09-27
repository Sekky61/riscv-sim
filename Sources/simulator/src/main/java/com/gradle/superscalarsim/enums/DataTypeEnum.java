/**
 * @file    DataTypeEnum.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @brief File contains enumeration definition for data types
 *
 * @date  27 October  2020 15:00 (created) \n
 *        21 November 2020 19:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.enums;

/**
 * @brief Enumeration definition for different data types used in simulation
 */
public enum DataTypeEnum
{
  kInt,         ///< 32bit Integer value
  kLong,        ///< 64bit Integer value
  kFloat,       ///< Float value
  kDouble,      ///< Double value
  kSpeculative  ///< Speculative value
}
