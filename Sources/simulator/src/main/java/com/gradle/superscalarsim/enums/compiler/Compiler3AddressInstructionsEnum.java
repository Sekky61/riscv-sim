/**
 * @file    Compiler3AddressInstructionsEnum.java
 *
 * @author  Jakub Horky\n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xhorky28@fit.vutbr.cz
 *
 * @brief File contains enumeration definition for 3 address code operations
 *
 * @date  03 March  2023 16:43 (created) \n
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023 Jakub Horky
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
 * @brief Enumeration definition for 3 address code operations
 */
public enum Compiler3AddressInstructionsEnum
{
    kAdd, ///< Addition
    kSub, ///< Subtraction
    kBitAnd, ///< Bitwise AND
    kBitOr, ///< Bitwise OR
    kBitNot, ///< Bitwise Negation
    kBitXor, ///< Bitwise XOR
    kLogAnd, ///< Logical AND
    kLogOr, ///< Logical OR
    kLogNot, ///< Logical Negation
    kArRShift, ///< Arithmetical right shift - maintains sign
    kLogRShift, ///< Logical right shift - Fills top with zero
    kLogLShift, ///< Logical left shift - Fills bottom with zero
    kMul, ///< Multiplication
    kDiv, ///< Division
    kRem, ///< Remainder (modulo)
    kSqrt, ///< Square root
    kLoad, ///< Load, Destination is target reg, Src1 is Addr, Src2 is offset (usually index)
    kStore, ///< Store, Destination is Addr, Src1 is reg, Src2 is offset
    kBranch, ///< Branch
    kJump, ///< Unconditional jump
    kLess, ///< Comparison <
    kLessOrEq, ///< Comparison <=
    kMore, ///< Comparison >
    kMoreOrEq, ///< Comparison >=
    kEqual, ///< Comparison ==
    kNotEqual, ///< Comparison !=
    kLabel, ///< Comparison ==
    kLoadImm, ///< Load immediate value
    kLoadPC, ///< Load program counter
    kBackupRegs, ///< Pseudo instruction to backup all used registers until restored
    kRestoreRegs, ///< Pseudo instruction to restore all registers since backup
    kInstructionDefinition, ///< Pseudo instruction defining the variable
    kMove, ///< Move value from one register to another
}
