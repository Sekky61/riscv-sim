/**
 * @file InputCodeModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container for processed line of code
 * @date 10 November  2020 17:45 (created) \n
 * 10 March     2021 18:00 (revised)
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @class InputCodeModel
 * @brief Represents a processed line of code
 * Should be used only for reading (referencing), not for writing
 */
public class InputCodeModel implements IInputCodeModel, Identifiable
{
  /**
   * ID - the index of the instruction in the code
   */
  private final int codeId;
  /**
   * Name of the parsed instruction, matches name from InstructionFunctionLoader
   * Example: "addi"
   */
  private final String instructionName;
  /**
   * Arguments of the instruction
   * The order of arguments is the same as in the original code line and tests depend on this order.
   * Labels use `labelName` as argument to store the name of the label.
   */
  private final List<InputCodeArgument> arguments;
  /**
   * Type of the instruction
   */
  private final InstructionTypeEnum instructionTypeEnum;
  
  /**
   * @brief Instruction function model
   * Contains information about the instruction
   */
  @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
  @JsonIdentityReference(alwaysAsId = true)
  private final InstructionFunctionModel instructionFunctionModel;
  
  public InputCodeModel(InstructionFunctionModel instructionFunctionModel,
                        final List<InputCodeArgument> arguments,
                        int codeId)
  {
    // TODO: delete fields that are duplicate of instructionFunctionModel
    this.instructionFunctionModel = instructionFunctionModel;
    this.codeId                   = codeId;
    this.instructionName          = instructionFunctionModel.getName();
    this.arguments                = arguments == null ? new ArrayList<>() : arguments;
    this.instructionTypeEnum      = instructionFunctionModel.getInstructionType();
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param [in] instructionName     - Name of the parsed instruction
   * @param [in] codeLine            - Unparsed line of code
   * @param [in] arguments           - Arguments of the instruction
   * @param [in] instructionTypeEnum - Type of the instruction
   * @param [in] resultDataType      - Data type of the output
   * @param [in] id                  - ID of the instruction (index in the code)
   *
   * @brief Constructor
   */
  public InputCodeModel(InstructionFunctionModel instructionFunctionModel,
                        final String instructionName,
                        final List<InputCodeArgument> arguments,
                        final InstructionTypeEnum instructionTypeEnum,
                        int codeId)
  {
    this.instructionFunctionModel = instructionFunctionModel;
    this.codeId                   = codeId;
    this.instructionName          = instructionName;
    this.arguments                = arguments == null ? new ArrayList<>() : arguments;
    this.instructionTypeEnum      = instructionTypeEnum;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    StringBuilder genericLine = new StringBuilder(getInstructionName());
    for (int i = 0; i < getArguments().size(); i++)
    {
      genericLine.append(" ").append(getArguments().get(i).getValue());
    }
    return genericLine.toString();
  }
  //------------------------------------------------------
  
  /**
   * @return Name of instruction
   * @brief Get name of instruction
   */
  @Override
  public String getInstructionName()
  {
    return instructionName;
  }// end of getInstructionName
  //------------------------------------------------------
  
  /**
   * @return Instruction arguments
   * @brief Get instruction arguments
   */
  @Override
  public List<InputCodeArgument> getArguments()
  {
    return arguments;
  }// end of getArguments
  
  /**
   * @param name Name of the argument
   *
   * @return An argument by its name
   */
  @Override
  public InputCodeArgument getArgumentByName(String name)
  {
    return arguments.stream().filter(argument -> argument.getName().equals(name)).findFirst().orElse(null);
  }// end of getArgumentByName
  //------------------------------------------------------
  
  /**
   * @return Enum value of instruction type
   * @brief Get instruction type
   */
  @Override
  public InstructionTypeEnum getInstructionTypeEnum()
  {
    return instructionTypeEnum;
  }// end of getInstructionTypeEnum
  
  /**
   * @return Enum value of output data type
   * @brief Get output data type
   */
  @Override
  public DataTypeEnum getDataType()
  {
    return instructionFunctionModel.getDataType();
  }// end of getResultDataType
  //------------------------------------------------------
  
  /**
   * @return ID of the instruction (index in the code)
   */
  @Override
  public int getCodeId()
  {
    return codeId;
  }
  
  @Override
  public InstructionFunctionModel getInstructionFunctionModel()
  {
    return instructionFunctionModel;
  }
  
  /**
   * @return Unique identifier of the object
   * @brief Get the identifier
   */
  @Override
  public String getId()
  {
    return String.valueOf(codeId);
  }
}
