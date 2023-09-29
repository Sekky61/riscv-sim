/**
 * @file AluIssueWindowBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for ALU Issue window
 * @date 9  February   2021 16:00 (created) \n
 * 27 April      2021 20:00 (revised)
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
package com.gradle.superscalarsim.blocks.arithmetic;

import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.PrecedingTable;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @class AluIssueWindowBlock
 * @brief Specific Issue window class for all ALU FUs (processing int or long instructions)
 */
public class AluIssueWindowBlock extends AbstractIssueWindowBlock
{
  /// List for all function units associated with this window
  private List<ArithmeticFunctionUnitBlock> functionUnitBlockList;
  /// Preceding table with all allowed instructions
  private PrecedingTable precedingTable;
  
  public AluIssueWindowBlock()
  {
  
  }
  
  /**
   * @param [in] blockScheduleTask - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] loader            - Initial loader of interpretable instructions and register files
   * @param [in] registerFileBlock - Class containing all registers, that simulator uses
   * @param [in] precedingTable    - Preceding table with all allowed instructions
   *
   * @brief Constructor
   */
  public AluIssueWindowBlock(InitLoader loader,
                             UnifiedRegisterFileBlock registerFileBlock,
                             PrecedingTable precedingTable)
  {
    super(loader, registerFileBlock);
    this.functionUnitBlockList = new ArrayList<>();
    this.precedingTable        = precedingTable;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instruction - instruction to be issued
   *
   * @return Suitable function unit
   * @brief Selects suitable function unit for certain instruction
   */
  @Override
  public AbstractFunctionUnitBlock selectSufficientFunctionUnit(InstructionFunctionModel instruction)
  {
    for (ArithmeticFunctionUnitBlock functionBlock : this.functionUnitBlockList)
    {
      if (functionBlock.isFunctionUnitEmpty() && isInstructionSupported(functionBlock.getAllowedOperators(),
                                                                        instruction.getInterpretableAs()))
      {
        return functionBlock;
      }
    }
    return null;
  }// end of selectSufficientFunctionUnit
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates backwards (moves instructions from FUs)
   */
  @Override
  public void simulateBackwards()
  {
    this.windowId = this.windowId - 1;
    for (AbstractFunctionUnitBlock functionUnitBlock : this.functionUnitBlockList)
    {
      if (!functionUnitBlock.isFunctionUnitEmpty() && functionUnitBlock.hasReversedDelayPassed() && functionUnitBlock.getSimCodeModel()
                                                                                                                     .getIssueWindowId() == this.windowId)
      {
        SimCodeModel codeModel = functionUnitBlock.getSimCodeModel();
        functionUnitBlock.setSimCodeModel(null);
        codeModel.setFunctionUnitId(0);
        this.getIssuedInstructions().add(codeModel);
        createArgumentValidityEntry(codeModel);
      }
    }
    while (!this.failedInstructions.empty() && this.failedInstructions.peek().getIssueWindowId() == this.getWindowId())
    {
      SimCodeModel codeModel = failedInstructions.pop();
      this.getIssuedInstructions().add(codeModel);
      this.argumentValidityMap.put(codeModel.getId(), this.failedValidityMaps.pop());
      codeModel.setIssueWindowId(0);
    }
    this.getIssuedInstructions().sort(SimCodeModel::compareTo);
    updateValidityItems();
  }// end of simulateBackwards
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instructionType - Type of the instruction (branch, arithmetic, eg.)
   *
   * @return True if compatible, false otherwise
   * @brief Checks if provided instruction type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectInstructionType(InstructionTypeEnum instructionType)
  {
    return instructionType == InstructionTypeEnum.kArithmetic;
  }// end of isCorrectInstructionType
  //----------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type (int, float, eg.)
   *
   * @return True if compatible, false otherwise
   * @brief Checks if provided data type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectDataType(DataTypeEnum dataType)
  {
    return dataType == DataTypeEnum.kInt || dataType == DataTypeEnum.kLong;
  }// end of isCorrectDataType
  //----------------------------------------------------------------------
  
  /**
   * @brief Set number of FUs to FU for correct id creation
   */
  @Override
  public void setFunctionUnitCountInUnits()
  {
    for (ArithmeticFunctionUnitBlock functionUnitBlock : this.functionUnitBlockList)
    {
      functionUnitBlock.setFunctionUnitCount(this.functionUnitBlockList.size());
    }
  }// end of setFunctionUnitCountInUnits
  //----------------------------------------------------------------------
  
  /**
   * @param [in] functionUnitBlock - FU to bind with this window
   *
   * @brief Associate function block with this window
   */
  public void setFunctionUnitBlock(ArithmeticFunctionUnitBlock functionUnitBlock)
  {
    functionUnitBlock.setFunctionUnitId(this.functionUnitBlockList.size());
    this.functionUnitBlockList.add(functionUnitBlock);
    this.setFunctionUnitCountInUnits();
  }// end of setFunctionUnitBlock
  //----------------------------------------------------------------------
  
  /**
   * @param [in] allowedInstructions      - Array of allowed instructions by the FU
   * @param [in] interpretableInstruction - String containing all operations used to calculate instruction
   *
   * @return True if FU instruction pool supports instruction, false otherwise
   * @brief Checks if provided instruction can be executed by any function unit
   */
  private boolean isInstructionSupported(String[] allowedInstructions, String interpretableInstruction)
  {
    String[]     allInstructions   = precedingTable.getAllowedInstructions();
    List<String> foundInstructions = new ArrayList<>();
    String       instruction       = String.copyValueOf(interpretableInstruction.toCharArray());
    for (String operation : allInstructions)
    {
      if (instruction.contains(operation))
      {
        foundInstructions.add(operation);
        instruction = instruction.replace(operation, "");
      }
    }
    
    Arrays.stream(allowedInstructions).forEach(foundInstructions::remove);
    
    return foundInstructions.isEmpty();
  }// end of isInstructionSupported
  //----------------------------------------------------------------------
}
