/**
 * @file IInputCodeModel.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Interface for a single instruction
 * @date 26 Sep      2023 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
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

package com.gradle.superscalarsim.models.instruction;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;

import java.util.List;

public interface IInputCodeModel
{
  /**
   * @return Name of the instruction (e.g. addi)
   */
  String getInstructionName();
  
  /**
   * @return List of arguments of the instruction
   */
  List<InputCodeArgument> getArguments();
  
  /**
   * @param name Name of the argument
   *
   * @return An argument by its name
   */
  InputCodeArgument getArgumentByName(String name);
  
  /**
   * @return Type of the instruction (e.g. kArithmetic)
   */
  InstructionTypeEnum getInstructionTypeEnum();
  
  /**
   * @return Data type of the instruction (e.g. kInt)
   */
  DataTypeEnum getDataType();
  
  /**
   * TODO: explain indexing (PC+4?)
   *
   * @return ID of the instruction (index in the code)
   */
  int getCodeId();
  
  /**
   * Get description of the instruction
   *
   * @return Instruction function model
   */
  InstructionFunctionModel getInstructionFunctionModel();
}
