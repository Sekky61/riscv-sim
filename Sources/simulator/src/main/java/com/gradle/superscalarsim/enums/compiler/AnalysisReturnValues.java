/**
 * @file AnalysisReturnValues.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@stud.fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains enumeration of return values of Compiler for c-subset code
 * @date 12 February  2023 20:20 (created)
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
 * @brief Enumeration values for return value of analysis
 */
public enum AnalysisReturnValues
{
  kSuccess, ///< Parsing finished Successful
  kMultipleCommandsOnTheSameLine, ///< There were multiple commands on the same line
  kFunctionDefinitionNotGlobal, ///< Function definition wasn't global
  kCommandSplitOverMultipleLines, ///< Command was split over multiple lines
  kMisplacedClosingBracket, ///< There was misplaced closing bracket
  kMisplacedOpeningBracket, ///< There was misplaced opening bracket
  kMisplacedElse, ///< There was misplaced else statement
  kMisplacedReturn, ///< There was misplaced else statement
  kMisplacedDelimiter, ///< There was misplaced delimiter
  kWrongType, ///< There was unsupported type in function or variable definition
  kMisplacedOperator, ///< There was operator where it wasn't allowed
  kCompilerBug, ///< There was a bug inside the compiler
  kMainNotDefined, ///< Eof without main defined
  kUnexpectedEof, ///< Eof outside global scope
  kExpectingSpecial, ///< Analyzer was expecting special value, but constant or variable was passed
  kSizeOfArrayMustBePosInt, ///< The size definition of array has to be positive integer
  kVariableRedefinition, ///< Variable was defined two times in the same scope
  kFunctionRedefinition, ///< Function was defined two times
  kUndefined, ///< Function of variable was not defined
  kIllegalValue, ///< Function of variable was not defined
  kUndefinedOperation, ///< Required operation is not defined by target architecture
  kNotEnoughRegisters, ///< There was not enough registers to generate the operation
  kMisplacedKeyword, ///< There was keyword on wrong place
}
