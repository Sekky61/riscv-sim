/**
 * @file CodeLoadStoreInterpreter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains load/store interpreter
 * @date 17 December  2020 10:00 (created) \n
 * 17 May       2021 11:00 (revised)
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
package com.gradle.superscalarsim.code;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.memory.MemoryAccess;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.List;

/**
 * Example of an interpretation of a load/store instruction:
 * "load:16:\\rs1 \\imm +" - load 16 bits from address rs1 + imm
 * For explanation of the syntax, see {@link Expression}
 *
 * @class CodeLoadStoreInterpreter
 * @brief Interprets load/store instruction provided in InputCodeModel class
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class CodeLoadStoreInterpreter
{
  /**
   * Memory. Used for load/store operations
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final MemoryModel memoryModel;
  
  /**
   * @param memoryModel       Memory model
   * @param registerFileBlock Register file block
   * @param labelMap          Label map
   *
   * @brief Constructor
   */
  public CodeLoadStoreInterpreter(final MemoryModel memoryModel)
  {
    this.memoryModel = memoryModel;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the memory
   */
  public void resetMemory()
  {
    this.memoryModel.reset();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModel    code to be interpreted
   * @param currentCycle current cycle
   *
   * @return Returns pair - delay of this access and data
   * @brief Interprets load/store instruction from codeModel, returns loaded/stored data.
   */
  public MemoryAccess interpretInstruction(final SimCodeModel codeModel, int currentCycle)
  {
    final InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    if (instruction == null)
    {
      throw new IllegalStateException("Instruction is null");
    }
    
    String[] interpretableAsParams = instruction.getInterpretableAs().split(":");
    
    if (interpretableAsParams.length != 3 && interpretableAsParams.length != 4)
    {
      throw new IllegalStateException("Unexpected number of parameters: " + interpretableAsParams.length);
    }
    
    String  loadStore = interpretableAsParams[0];
    boolean isStore   = loadStore.equals("store");
    
    String sizeBitsStr = interpretableAsParams[1];
    int    sizeBits    = Integer.parseInt(sizeBitsStr);
    int    sizeBytes   = sizeBits / 8;
    
    long address = interpretAddress(codeModel);
    
    if (isStore)
    {
      String        storeRegisterName = interpretableAsParams[3];
      RegisterModel reg               = codeModel.getArgumentByName(storeRegisterName).getRegisterValue();
      if (reg == null)
      {
        throw new IllegalStateException("Register " + storeRegisterName + " not found");
      }
      long valueBits = (long) reg.getValue(DataTypeEnum.kLong);
      return MemoryAccess.store(address, sizeBytes, valueBits, false);
    }
    else
    {
      boolean isSigned = instruction.getArgumentByName("rd").type().isSigned();
      return MemoryAccess.load(address, sizeBytes, isSigned);
    }
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModel instruction to be interpreted
   *
   * @return Address to be loaded/stored based on the instruction
   */
  public long interpretAddress(final SimCodeModel codeModel)
  {
    final InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    if (instruction == null)
    {
      throw new IllegalStateException("Instruction is null");
    }
    
    String[] interpretableAsParams = instruction.getInterpretableAs().split(":");
    if (interpretableAsParams.length < 3)
    {
      throw new IllegalStateException("Unexpected number of parameters: " + interpretableAsParams.length);
    }
    String                    addressExpr = interpretableAsParams[2];
    List<Expression.Variable> variables   = codeModel.getVariables();
    
    Expression.Variable addressResult = Expression.interpret(addressExpr, variables);
    if (addressResult == null)
    {
      throw new IllegalStateException("Address result is null");
    }
    int address = (int) addressResult.value.getValue(DataTypeEnum.kInt);
    
    if (address < 0)
    {
      throw new IllegalStateException("Address is negative: " + address);
    }
    
    return address;
  }// end of interpretAddress
  //-------------------------------------------------------------------------------------------
}
