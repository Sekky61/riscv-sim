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
package com.gradle.superscalarsim.models.instruction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.Identifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * @param name            Instruction name (has to be unique)
 * @param instructionType Type of the instruction (arithmetic, load/store, branch)
 * @param arguments       List of arguments of the instruction
 * @param interpretableAs Codified interpretation of instruction
 *
 * @brief Definition of instruction from instruction set
 * @details Class contains definition of instruction, which is used to interpret and show real value in I-cache and to
 * interpret functionality of that instruction. Set (list) of instruction gives instruction set.
 * Json file, which is used to create object of this class, follows same name structure for each variable.
 * ID is the name of the instruction.
 */
public record InstructionFunctionModel(String name, InstructionTypeEnum instructionType,
                                       List<InstructionArgument> arguments,
                                       String interpretableAs) implements Identifiable
{
  /**
   * @return True if the instruction is a NOP
   */
  @JsonIgnore
  public boolean isNop()
  {
    return name.equals("nop");
  }// end of isNop
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formatting
   */
  @Override
  public String toString()
  {
    return "Instruction: " + name + " " + arguments + " (" + interpretableAs + ")";
  }// end of toString
  //------------------------------------------------------
  
  /**
   * @return Argument with given name, or null if not found
   */
  public InstructionArgument getArgumentByName(String name)
  {
    return arguments.stream().filter(argument -> argument.name().equals(name)).findFirst().orElse(null);
  }
  
  /**
   * @return The type of value this instruction produces
   */
  public DataTypeEnum getOutputType()
  {
    return getArgumentByName("rd").type();
  }
  
  /**
   * @return True if the instruction can have default arguments (arguments with default values).
   */
  public boolean hasDefaultArguments()
  {
    return arguments.stream().anyMatch(argument -> argument.defaultValue() != null);
  }
  
  /**
   * @return True if instruction is a conditional jump. Assumes that the instruction is a jump.
   */
  public boolean isConditionalJump()
  {
    return !interpretableAs.endsWith("true");
  }// end of isUnconditionalJump
  //------------------------------------------------------
  
  /**
   * @return Unique identifier of the object
   * @brief Get the identifier
   */
  @Override
  public String getId()
  {
    return name;
  }// end of getId
  
  /**
   * Used for creating string representation of the instruction with renamed arguments
   * Example of an output: "addi rd, rs1, imm", "lw rd, imm(rs1)", only it is tokenized, so ["addi ", "rd", ",", "rs1", ",", "imm"].
   * Note the space after instruction name.
   *
   * @return List of tokens representing the template of the instruction
   */
  @JsonProperty
  public List<String> getSyntaxTemplate()
  {
    List<String> syntaxTemplate = new ArrayList<>();
    syntaxTemplate.add(name + " ");
    boolean                   isLoadStore = instructionType == InstructionTypeEnum.kLoadstore;
    List<InstructionArgument> args        = getAsmArguments();
    for (int i = 0; i < args.size(); i++)
    {
      boolean wrapInParens = isLoadStore && i == args.size() - 1;
      if (i != 0)
      {
        if (wrapInParens)
        {
          syntaxTemplate.add("(");
        }
        else
        {
          syntaxTemplate.add(",");
        }
      }
      InstructionArgument arg = args.get(i);
      syntaxTemplate.add(arg.name());
    }
    if (isLoadStore)
    {
      syntaxTemplate.add(")");
    }
    return syntaxTemplate;
  }// end of getRenamedCodeLine
  
  /**
   * @return List of arguments, which are not silent
   */
  public List<InstructionArgument> getAsmArguments()
  {
    List<InstructionArgument> asmArguments = new ArrayList<>();
    for (InstructionArgument argument : arguments)
    {
      if (!argument.silent())
      {
        asmArguments.add(argument);
      }
    }
    return asmArguments;
  }
  
}
