/**
 * @file BranchIssueWindowBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Branch Issue window
 * @date 1  March   2021 16:00 (created) \n
 * 28 April   2021 11:50 (revised)
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
package com.gradle.superscalarsim.blocks.branch;

import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class BranchIssueWindowBlock
 * @brief Specific Issue window class for all Branch FUs (processing jump and branch instructions)
 */
public class BranchIssueWindowBlock extends AbstractIssueWindowBlock
{
  
  /// List for all function units associated with this window
  private List<BranchFunctionUnitBlock> functionUnitBlockList;
  
  public BranchIssueWindowBlock()
  {
  }
  
  /**
   * @param [in] blockScheduleTask - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] loader            - Initial loader of interpretable instructions and register files
   * @param [in] registerFileBlock - Class containing all registers, that simulator uses
   * @brief Constructor
   */
  public BranchIssueWindowBlock(InitLoader loader, UnifiedRegisterFileBlock registerFileBlock)
  {
    super(loader, registerFileBlock);
    this.functionUnitBlockList = new ArrayList<>();
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instruction - instruction to be issued
   * @return Suitable function unit
   * @brief Selects suitable function unit for certain instruction
   */
  @Override
  public AbstractFunctionUnitBlock selectSufficientFunctionUnit(InstructionFunctionModel instruction)
  {
    for (BranchFunctionUnitBlock functionBlock : this.functionUnitBlockList)
    {
      if (functionBlock.isFunctionUnitEmpty())
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
   * @return True if compatible, false otherwise
   * @brief Checks if provided instruction type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectInstructionType(InstructionTypeEnum instructionType)
  {
    return instructionType == InstructionTypeEnum.kJumpbranch;
  }// end of isCorrectInstructionType
  //----------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type (int, float, eg.)
   * @return True if compatible, false otherwise
   * @brief Checks if provided data type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectDataType(DataTypeEnum dataType)
  {
    return true;
  }// end of isCorrectDataType
  //----------------------------------------------------------------------
  
  /**
   * @brief Set number of FUs to FU for correct id creation
   */
  @Override
  public void setFunctionUnitCountInUnits()
  {
    for (BranchFunctionUnitBlock functionUnitBlock : this.functionUnitBlockList)
    {
      functionUnitBlock.setFunctionUnitCount(this.functionUnitBlockList.size());
    }
  }// end of setFunctionUnitCountInUnits
  //----------------------------------------------------------------------
  
  
  /**
   * @param [in] functionUnitBlock - FU to bind with this window
   * @brief Associate function block with this window
   */
  public void setFunctionUnitBlock(BranchFunctionUnitBlock functionUnitBlock)
  {
    functionUnitBlock.setFunctionUnitId(this.functionUnitBlockList.size());
    this.functionUnitBlockList.add(functionUnitBlock);
    this.setFunctionUnitCountInUnits();
  }// end of setFunctionUnitBlock
  //----------------------------------------------------------------------
  
  /**
   * Gets list of all function units associated with this window
   *
   * @return List of function units
   */
  public List<BranchFunctionUnitBlock> getFunctionUnitBlockList()
  {
    return functionUnitBlockList;
  }// end of getFunctionUnitBlockList
  //----------------------------------------------------------------------
}
