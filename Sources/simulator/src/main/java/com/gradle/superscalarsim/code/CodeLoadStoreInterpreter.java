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

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.SimCodeModel;
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
public class CodeLoadStoreInterpreter
{
  /**
   * Memory. Used for load/store operations
   */
  private final MemoryModel memoryModel;
  
  /**
   * Register file. Used for calculating addresses and getting values for store operations.
   */
  private final UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * @param [in] initLoader - Initial loader of interpretable instructions and register files
   * @param [in] memoryModel - Class simulating memory
   *
   * @brief Constructor
   */
  public CodeLoadStoreInterpreter(final MemoryModel memoryModel, final UnifiedRegisterFileBlock registerFileBlock)
  {
    this.memoryModel       = memoryModel;
    this.registerFileBlock = registerFileBlock;
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
   * @return Returns the bits to be assigned to register.
   * @brief Interprets load/store instruction from codeModel, returns loaded/stored data.
   */
  public Pair<Integer, Long> interpretInstruction(final SimCodeModel codeModel, int currentCycle)
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
    
    String loadStore   = interpretableAsParams[0];
    String sizeBitsStr = interpretableAsParams[1];
    int    sizeBits    = Integer.parseInt(sizeBitsStr);
    
    long address = interpretAddress(codeModel);
    
    switch (loadStore)
    {
      case "load" ->
      {
        boolean isSigned = instruction.getArgumentByName("rd").type().isSigned();
        return processLoadOperation(sizeBits, address, isSigned, codeModel.getId(), currentCycle);
      }
      case "store" ->
      {
        String        storeRegisterName = interpretableAsParams[3];
        String        regName           = codeModel.getArgumentByName(storeRegisterName).getValue();
        RegisterModel reg               = registerFileBlock.getRegister(regName);
        if (reg == null)
        {
          throw new IllegalStateException("Register " + regName + " not found");
        }
        long valueBits = (long) reg.getValue(DataTypeEnum.kLong);
        int  delay     = processStoreOperation(sizeBits, address, valueBits, codeModel.getId(), currentCycle);
        return new Pair<>(delay, valueBits);
      }
      default -> throw new IllegalStateException("Unexpected value: " + interpretableAsParams[0]);
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
    String addressExpr = interpretableAsParams[2];
    
    List<String>              varNames  = Expression.getVariableNames(addressExpr);
    List<Expression.Variable> variables = codeModel.getVariables(varNames, registerFileBlock);
    
    Expression.Variable addressResult = Expression.interpret(addressExpr, variables);
    if (addressResult == null)
    {
      throw new IllegalStateException("Address result is null");
    }
    int address = (int) addressResult.value.getValue(DataTypeEnum.kInt);
    
    return address;
  }// end of interpretAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param sizeBits     Size of the loaded value (8, 16, 32)
   * @param address      Address to read from
   * @param isSigned     True in case of signed value, false otherwise
   * @param id           ID of a load instruction, that is being executed
   * @param currentCycle Current cycle
   *
   * @return Pair of delay of this access and data (64 bits, suitable for assignment to register)
   */
  private Pair<Integer, Long> processLoadOperation(int sizeBits,
                                                   long address,
                                                   boolean isSigned,
                                                   int id,
                                                   int currentCycle)
  {
    int                 numberOfBytes = sizeBits / 8;
    Pair<Integer, Long> loadedData    = memoryModel.load(address, numberOfBytes, id, currentCycle);
    
    // Apply mask to zero out or sign extend the value
    long bits      = loadedData.getSecond();
    long validMask = (1L << sizeBits) - 1;
    if (sizeBits >= 64)
    {
      validMask = -1;
    }
    bits = bits & validMask;
    if (isSigned && ((1L << (sizeBits - 1)) & bits) != 0)
    {
      // Fill with sign bit
      long signMask = ~validMask;
      bits = bits | signMask;
    }
    
    return new Pair<>(loadedData.getFirst(), bits);
  }// end of processLoadOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param sizeBits     How many bits to store
   * @param address      Address to store to
   * @param valueBits    Value to store (lower sizeBits bits are used)
   * @param id           ID of a store instruction, that is being executed
   * @param currentCycle Current cycle
   *
   * @return Delay of this access
   */
  private int processStoreOperation(int sizeBits, long address, long valueBits, int id, int currentCycle)
  {
    int numberOfBytes = sizeBits / 8;
    return memoryModel.store(address, valueBits, numberOfBytes, id, currentCycle);
  }// end of processStoreOperation
  //-------------------------------------------------------------------------------------------
}
