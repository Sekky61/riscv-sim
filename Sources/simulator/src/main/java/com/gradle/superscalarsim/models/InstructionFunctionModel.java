/**
 * @file InstructionFunctionModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of instruction from instruction set
 * @date 28 October  2020 13:00 (created) \n
 * 15 May      2021 12:10 (revised)
 * 26 Sep      2023 10:00 (revised)
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
package com.gradle.superscalarsim.models;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;

/**
 * @class InstructionFunctionModel
 * @brief Definition of instruction from instruction set
 * @details Class contains definition of instruction, which is used to interpret and show real value in I-cache and to
 * interpret functionality of that instruction. Set (list) of instruction gives instruction set.
 * Json file, which is used to create object of this class, follows same name structure for each variable.
 */
public class InstructionFunctionModel
{
  /// Name of instruction
  private final String name;
  /// Data type on which instruction operates
  private final DataTypeEnum inputDataType;
  /// Data type of output register/memory
  private final DataTypeEnum outputDataType;
  /// Type of the instruction
  private final InstructionTypeEnum instructionType;
  /// Syntax of instruction for validation
  private final String instructionSyntax;
  /**
   * @brief Codified interpretation of instruction
   */
  private final String interpretableAs;
  
  public InstructionFunctionModel()
  {
    this.name              = "";
    this.inputDataType     = DataTypeEnum.kInt;
    this.outputDataType    = DataTypeEnum.kInt;
    this.instructionType   = InstructionTypeEnum.kArithmetic;
    this.instructionSyntax = "";
    this.interpretableAs   = "";
  }
  
  /**
   * @param [in] name              - Instruction name (has to unique)
   * @param [in] instructionType   - Type of the instruction
   * @param [in] inputDataType     - Data type, which is used in instruction
   * @param [in] outputDataType    - Data type of output register/memory
   * @param [in] instructionSyntax - Instruction syntax for verification of input assembly code
   * @param [in] interpretableAs   - String of java code, which tells how to interpret instruction
   * @param [in] rawItemModelList  - List of objects, containing instruction format from 32 down to 0
   *
   * @brief Constructor
   */
  public InstructionFunctionModel(String name,
                                  InstructionTypeEnum instructionType,
                                  String inputDataType,
                                  String outputDataType,
                                  String instructionSyntax,
                                  String interpretableAs)
  {
    this.name              = name;
    this.inputDataType     = DataTypeEnum.valueOf(inputDataType);
    this.outputDataType    = DataTypeEnum.valueOf(outputDataType);
    this.instructionType   = instructionType;
    this.instructionSyntax = instructionSyntax;
    this.interpretableAs   = interpretableAs;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formating
   */
  @Override
  public String toString()
  {
    return "Instruction: " + name + '\n' + "instruction type " + instructionType + '\n' + "input data type: " + inputDataType + '\n' + "output data type: " + outputDataType + '\n' + "syntax: " + instructionSyntax + '\n' + "interpretable as: " + interpretableAs + '\n';
  }// end of toString
  //------------------------------------------------------
  
  /**
   * @return Instruction name
   * @brief Get name of an instruction
   */
  public String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------
  
  /**
   * @return Instruction type
   * @brief Get type of an instruction
   */
  public InstructionTypeEnum getInstructionType()
  {
    return instructionType;
  }// end of getInstructionType
  //------------------------------------------------------
  
  /**
   * @return Instruction input data type
   * @brief Get input data type of instruction
   */
  public DataTypeEnum getInputDataType()
  {
    return inputDataType;
  }// end of getInputDataType
  //------------------------------------------------------
  
  /**
   * @return Instruction output data type
   * @brief Get output data type of instruction
   */
  public DataTypeEnum getOutputDataType()
  {
    return outputDataType;
  }// end of getOutputDataType
  //------------------------------------------------------
  
  /**
   * @return Instruction syntax
   * @brief Get syntax of an instruction for input assembly code verification
   */
  public String getInstructionSyntax()
  {
    return instructionSyntax;
  }// end of getInstructionSyntax
  //------------------------------------------------------
  
  /**
   * @return String of java code
   * @brief Get string of code for interpreting an instruction. Interpretation depends on instruction type.
   */
  public String getInterpretableAs()
  {
    return interpretableAs;
  }// end of getInterpretableAs
  //------------------------------------------------------
  
  /**
   * @return True if instruction is a unconditional jump
   */
  public boolean isUnconditionalJump()
  {
    return interpretableAs.startsWith("jump");
  }// end of isUnconditionalJump
}
