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

import java.util.ArrayList;
import java.util.List;

/**
 * @class InstructionFunctionModel
 * @brief Definition of instruction from instruction set
 * @details Class contains definition of instruction, which is used to interpret and show real value in I-cache and to
 * interpret functionality of that instruction. Set (list) of instruction gives instruction set.
 * Json file, which is used to create object of this class, follows same name structure for each variable.
 */
public class InstructionFunctionModel
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
  private final List<Argument> arguments;
  
  /**
   * @brief Codified interpretation of instruction
   */
  private final String interpretableAs;
  
  /**
   * @brief Explicitly stated instruction data type. Most likely null and should be inferred from arguments.
   */
  private DataTypeEnum dataType;
  
  public InstructionFunctionModel()
  {
    this.name            = "";
    this.instructionType = InstructionTypeEnum.kArithmetic;
    this.arguments       = new ArrayList<>();
    this.interpretableAs = "";
    this.dataType        = null;
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
                                  List<Argument> arguments,
                                  String interpretableAs)
  {
    this.name            = name;
    this.instructionType = instructionType;
    this.arguments       = arguments;
    this.interpretableAs = interpretableAs;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formating
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
   * @brief Get type of an instruction
   */
  public InstructionTypeEnum getInstructionType()
  {
    return instructionType;
  }// end of getInstructionType
  //------------------------------------------------------
  
  /**
   * Instruction data type is either explicitly stated or inferred from arguments
   *
   * @return Data type of the instruction
   */
  public DataTypeEnum getDataType()
  {
    if (this.dataType != null)
    {
      return this.dataType;
    }
    if (arguments.isEmpty())
    {
      return null;
    }
    else
    {
      return arguments.get(0).type;
    }
  }
  
  /**
   * @return Instruction arguments
   * @brief Get instruction arguments, used for parsing and validation
   */
  public List<Argument> getArguments()
  {
    return arguments;
  }// end of getArguments
  //------------------------------------------------------
  
  /**
   * @return List of arguments, which are not silent
   */
  public List<Argument> getAsmArguments()
  {
    List<Argument> asmArguments = new ArrayList<>();
    for (Argument argument : arguments)
    {
      if (!argument.silent)
      {
        asmArguments.add(argument);
      }
    }
    return asmArguments;
  }
  
  /**
   * @return Argument with given name
   */
  public Argument getArgumentByName(String name)
  {
    return arguments.stream().filter(argument -> argument.name.equals(name)).findFirst().orElse(null);
  }
  
  public boolean hasDefaultArguments()
  {
    return arguments.stream().anyMatch(argument -> argument.defaultValue != null);
  }
  
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
   * @return True if instruction is an unconditional jump
   */
  public boolean isUnconditionalJump()
  {
    return interpretableAs.endsWith("true");
  }// end of isUnconditionalJump
  
  /**
   * @param name         Name of the argument (example: "rd")
   * @param type         Data type of the argument (example: "kInt")
   * @param defaultValue Default value of the argument (example: "0" or null)
   * @param writeBack    True if the argument should be written back to register file on commit
   *
   * @brief Could be a record, but is not because of serialization issues
   */
  public static class Argument
  {
    private final String name;
    private final DataTypeEnum type;
    private final String defaultValue;
    private final boolean writeBack;
    
    /**
     * @brief If true, count this argument as data dependency, but is not allowed to be used in ASM code.
     */
    private final boolean silent;
    
    public Argument(String name, DataTypeEnum type, String defaultValue)
    {
      this.name         = name;
      this.type         = type;
      this.defaultValue = defaultValue;
      this.writeBack    = false;
      this.silent       = false;
    }
    
    public Argument(String name, DataTypeEnum type, String defaultValue, boolean writeBack)
    {
      this.name         = name;
      this.type         = type;
      this.defaultValue = defaultValue;
      this.writeBack    = writeBack;
      this.silent       = false;
    }
    
    public String name()
    {
      return name;
    }
    
    public DataTypeEnum type()
    {
      return type;
    }
    
    public String defaultValue()
    {
      return defaultValue;
    }
    
    public boolean writeBack()
    {
      return writeBack;
    }
    
    public boolean silent()
    {
      return silent;
    }
    
    @Override
    public String toString()
    {
      return name + ":" + type + (defaultValue != null ? ":" + defaultValue : "");
    }
  }
}
