/**
 * @file    TokenOperationEnum.java
 *
 * @author  Jakub Horky \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xhorky28@stud.fit.vutbr.cz
 *
 * @brief File contains type of operations
 *
 * @date  12 February  2023 17:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Jakub Horky
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

package com.gradle.superscalarsim.enums.compiler;

/**
 * @brief Enumeration values for values the operations inside syntax tree
 */
public enum TokenOperationEnum
{
    kAddition, ///< Value is addition operation
    kSubtraction, ///< Value is subtraction operation
    kMultiplication, ///< Value is multiplication operation
    kDivision, ///< Value is division operation
    kModulo, ///< Value is module operation
    kEqual, ///< Value is equal operation
    kNotEqual, ///< Value is not equal operation
    kLessThan, ///< Value is == operation
    kLessOrEqualThan, ///< Value is <= operation
    kMoreThen, ///< Value is > operation
    kMoreOrEqualThan, ///< Value is >= operation
    kAssignment ///< Value is Assignment operation
}
