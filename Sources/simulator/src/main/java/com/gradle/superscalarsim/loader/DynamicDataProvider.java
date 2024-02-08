/**
 * @file DynamicDataProvider.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Version of the data provider which loads data from code.
 * @date 08 Feb      2024 20:00 (created)
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
 * @brief Loads necessary objects for simulation
 * @details Class which provides register files and instruction set from code.
 * This should be used for testing purposes.
 */
public class DynamicDataProvider implements IDataProvider
{
  /**
   * Holds register file with all registers and aliases.
   */
  private RegisterFile registerFile;
  /**
   * Holds loaded ISA for interpreting values and action by simulation code
   */
  private Map<String, InstructionFunctionModel> instructionFunctionModels;
  
  
  /**
   * @brief Constructor, but does not load registers from files
   */
  public DynamicDataProvider(RegisterFile registerFile, Map<String, InstructionFunctionModel> instructionFunctionModels)
  {
    this.registerFile              = registerFile;
    this.instructionFunctionModels = instructionFunctionModels;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return Register file
   */
  @Override
  public RegisterFile getRegisterFile()
  {
    return registerFile;
  }// end of getRegisterFile
  //------------------------------------------------------
  
  public InstructionFunctionModel getInstructionFunctionModel(String instructionName)
  {
    return getInstructionFunctionModels().get(instructionName);
  }
  
  /**
   * @return loaded instruction set
   */
  @Override
  public Map<String, InstructionFunctionModel> getInstructionFunctionModels()
  {
    return instructionFunctionModels;
  }// end of getInstructionFunctionModelList
  //------------------------------------------------------
  
}
