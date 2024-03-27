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
package com.gradle.superscalarsim.models.instruction;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.Identifiable;

import java.util.List;

/**
 * @param codeId                   ID - the index of the instruction in the code.
 *                                 codeId*4 = PC of the instruction.
 * @param arguments                Arguments of the instruction
 *                                 The order of arguments is the same as in the original code line and tests depend on this order.
 *                                 Labels use `labelName` as argument to store the name of the label.
 * @param instructionFunctionModel Description of the instruction opcode
 * @param debugInfo                Debug info attached to the instruction
 *
 * @class InputCodeModel
 * @brief Represents a processed line of code
 * Should be used only for reading (referencing), not for writing
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "codeId")
public record InputCodeModel(
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
        // Needed here, not on the record
        @JsonIdentityReference(alwaysAsId = true) InstructionFunctionModel instructionFunctionModel,
        List<InputCodeArgument> arguments, int codeId, DebugInfo debugInfo) implements Identifiable, IInputCodeModel
{
  /**
   * @return The debug info attached to the instruction. Null if none present.
   */
  @Override
  public DebugInfo debugInfo()
  {
    return debugInfo;
  }
  //------------------------------------------------------
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    StringBuilder genericLine = new StringBuilder(instructionFunctionModel.name());
    for (int i = 0; i < arguments().size(); i++)
    {
      genericLine.append(" ").append(arguments().get(i).getValue());
    }
    return genericLine.toString();
  }
  //------------------------------------------------------
  
  /**
   * @return Name of the instruction (e.g. addi)
   */
  @Override
  public String getInstructionName()
  {
    return instructionFunctionModel.name();
  }
  
  /**
   * @return Instruction arguments
   * @brief Get instruction arguments
   */
  @Override
  public List<InputCodeArgument> arguments()
  {
    return arguments;
  }// end of getArguments
  
  /**
   * @param name Name of the argument
   *
   * @return An argument by its name
   */
  public InputCodeArgument getArgumentByName(String name)
  {
    return arguments.stream().filter(argument -> argument.getName().equals(name)).findFirst().orElse(null);
  }// end of getArgumentByName
  //------------------------------------------------------
  
  /**
   * @return Enum value of instruction type
   * @brief Get instruction type
   */
  public InstructionTypeEnum getInstructionTypeEnum()
  {
    return instructionFunctionModel.instructionType();
  }// end of getInstructionTypeEnum
  
  /**
   * @return ID of the instruction (index in the code)
   */
  @Override
  public int codeId()
  {
    return codeId;
  }
  
  @Override
  public InstructionFunctionModel instructionFunctionModel()
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
  
  /**
   * @return True if the instruction is a conditional branch
   */
  public boolean isConditionalBranch()
  {
    return instructionFunctionModel.isConditionalJump();
  }
  
  /**
   * @return the PC of the instruction
   */
  public int getPc()
  {
    return codeId * 4;
  }
}
