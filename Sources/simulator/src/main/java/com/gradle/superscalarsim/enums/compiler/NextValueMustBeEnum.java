/**
 * @file NextValueMustBeEnum.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@stud.fit.vutbr.cz
 * @brief File contains enumeration of allowed operations in next value of Analysis of user made c-subset code
 * @date 17 February  2023 15:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023  Jakub Horky
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
 * @brief Enumeration values for type of the next token in syntax analysis
 */
public enum NextValueMustBeEnum
{
  kAny, ///< Any token - semantic analyzer will figure out if it is valid
  kEqual, ///< Next token has to be =,
  kEqualOrValueOrBracket, ///< Next token can be anything, but if it is = do something
  kOperationOrBracketOrDelimiter, ///< Next token has to be operation, or specified End token
  kValueOrBracket, ///< Next token has to be value or bracket,
  kTypeOrValueOrBracket, ///<  Next vale must be type identification, value or bracket
  kValue, ///< Next token has to be Value (variable or constant)
  kValueOrDelimiter, ///< Next token can be value or delimiter
  kBracket, ///< Next value must be bracket
  kDelimiter, ///< Next value must be delimiter
  kEqualOrDelimiter, ///< Next value must be equal or delimiter
  kOperatorOrValueOrBracket ///< Next value must be & or | or Value or opening bracket
}
