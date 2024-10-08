/**
 * @file RegisterReadinessEnum.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains enumeration values for register states
 * @date 3 February   2020 16:00 (created) \n
 * 16 February  2020 16:00 (revised)
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
 * @brief Enumeration values for register states
 */
public enum RegisterReadinessEnum
{
  kFree,      ///< Register is free to be allocated
  kAllocated, ///< Register is allocated and ready for assignment
  kExecuted,  ///< Register holds executed value
  kAssigned   ///< Register has non-speculative value, ISA registers are always assigned
}
