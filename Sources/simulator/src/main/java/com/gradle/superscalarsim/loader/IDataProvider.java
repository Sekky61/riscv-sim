/**
 * @file IDataProvider.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains interface for static data provider
 * @date 08 February  2024 20:00 (created)
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

package com.gradle.superscalarsim.loader;

import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterFile;

import java.util.Map;

/**
 * @brief Interface for static data provider is available for cpu to get instructions
 * and register definitions of RISC-V architecture.
 */
public interface IDataProvider
{
  /**
   * @return Register file
   */
  RegisterFile getRegisterFile();
  
  /**
   * @return Description of all instructions in the ISA
   */
  Map<String, InstructionFunctionModel> getInstructionFunctionModels();
  
  /**
   * @param instructionName Name of the instruction (e.g. "addi")
   *
   * @return Instruction function model
   */
  InstructionFunctionModel getInstructionFunctionModel(String instructionName);
}
