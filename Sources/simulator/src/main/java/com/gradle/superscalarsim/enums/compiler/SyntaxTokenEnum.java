/**
 * @file    SyntaxTokenEnum.java
 *
 * @author  Jakub Horky \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xhorky28@stud.fit.vutbr.cz
 *
 * @brief File contains Enumartion for type of token in syntax tree
 *
 * @date  09 February  2023 18:00 (created)
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
 * @brief Enumeration values for type of the syntax token
 */
public enum SyntaxTokenEnum
{
    kCode, ///< Root of the syntax tree, parsing program
    kFunction, ///< Node is function definition
    kMain, ///< Node is Main function definition
    kDefinition, ///< Node is definition of variable
    kFor, ///< Node is definition of For cycle
    kWhile, ///< Node is definition of While cycle
    kIf, ///< Node is definition of If-then-else statement
    kIfThen, ///< Node is definition of If-then-else statement
    kIfElse, ///< Node is else of If-then-else statement
    kStatementBase, ///< Node is general statement - closer definition will be done later
    kAssignment, ///< Node is assignment statement
    kReturn, ///< Node is return statement
    kVar, ///< Node is Variable
    kArray, ///< Node is Array
    kFunctionCall, ///< Node is Function call
    kConstant, ///< Node is constant value
    kOperation, ///< Node is Operation
    kSyntaxLeaf, ///< Node is leaf from syntax analyzer
    kBracket, ///< Node is an bracket
    kInternalCode, ///< Node contains list of generated internal code
    kTargetCode, ///< Node contains list of generated internal code
}
