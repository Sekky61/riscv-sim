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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.Identifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * @class InstructionFunctionModel
 * @brief Definition of instruction from instruction set
 * @details Class contains definition of instruction, which is used to interpret and show real value in I-cache and to
 * interpret functionality of that instruction. Set (list) of instruction gives instruction set.
 * Json file, which is used to create object of this class, follows same name structure for each variable.
 * ID is the name of the instruction.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class InstructionFunctionModel implements Identifiable
{
  /**
   * Name of the instruction
   */
  private final String name;
  
  /**
   * Type of the instruction (arithmetic, load/store, branch)
   */
  private final InstructionTypeEnum instructionType;
  
  /**
   * Definition of instruction arguments for parsing and validation.
   */
  private final List<InstructionArgument> arguments;
  
  /**
   * @brief Codified interpretation of instruction
   */
  private final String interpretableAs;
  
  public InstructionFunctionModel()
  {
    this.name            = "";
    this.instructionType = InstructionTypeEnum.kIntArithmetic;
    this.arguments       = new ArrayList<>();
    this.interpretableAs = "";
  }
  
  /**
   * @param name            Instruction name (has to unique)
   * @param instructionType Type of the instruction
   * @param arguments       List of arguments of the instruction
   * @param interpretableAs String of code, which tells how to interpret instruction
   *
   * @brief Constructor
   */
  public InstructionFunctionModel(String name,
                                  InstructionTypeEnum instructionType,
                                  List<InstructionArgument> arguments,
                                  String interpretableAs)
  {
    this.name            = name;
    this.instructionType = instructionType;
    this.arguments       = arguments;
    this.interpretableAs = interpretableAs;
  }// end of Constructor
  
  /**
   * @return True if the instruction is a NOP
   */
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
   * @brief Get type of instruction
   */
  public InstructionTypeEnum getInstructionType()
  {
    return instructionType;
  }// end of getInstructionType
  //------------------------------------------------------
  
  /**
   * @return Instruction arguments
   * @brief Get instruction arguments, used for parsing and validation
   */
  public List<InstructionArgument> getArguments()
  {
    return arguments;
  }// end of getArguments
  //------------------------------------------------------
  
  /**
   * @return Argument with given name
   */
  public InstructionArgument getArgumentByName(String name)
  {
    return arguments.stream().filter(argument -> argument.name().equals(name)).findFirst().orElse(null);
  }
  
  public boolean hasDefaultArguments()
  {
    return arguments.stream().anyMatch(argument -> argument.defaultValue() != null);
  }
  
  /**
   * @return String of java code
   * @brief Get string of code for interpreting an instruction. Interpretation depends on instruction type.
   */
  public String getInterpretableAs()
  {
    return interpretableAs;
  }// end of getInterpretableAs
  
  /**
   * @return True if instruction is an unconditional jump
   */
  public boolean isUnconditionalJump()
  {
    return interpretableAs.endsWith("true");
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
