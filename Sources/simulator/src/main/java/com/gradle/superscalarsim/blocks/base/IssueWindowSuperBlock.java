/**
 * @file IssueWindowSuperBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for dispatching instructions to correct Issuing windows
 * @date 9  February   2021 16:00 (created) \n
 * 28 April      2021 11:30 (revised)
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
package com.gradle.superscalarsim.blocks.base;

import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.arithmetic.AluIssueWindowBlock;
import com.gradle.superscalarsim.blocks.arithmetic.FpIssueWindowBlock;
import com.gradle.superscalarsim.blocks.branch.BranchIssueWindowBlock;
import com.gradle.superscalarsim.blocks.loadstore.LoadStoreIssueWindowBlock;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class IssueWindowSuperBlock
 * @brief Class containing logic for dispatching instructions from decode stage to Issue windows
 */
public class IssueWindowSuperBlock implements AbstractBlock
{
  /**
   * Class, which simulates instruction decode and renames registers.
   */
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /**
   * List of all issue windows.
   */
  private List<AbstractIssueWindowBlock> issueWindowBlockList;
  
  public IssueWindowSuperBlock()
  {
  }
  
  /**
   * @param [in] blockScheduleTask      - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] decodeAndDispatchBlock - Class, which simulates instruction decode and renames registers
   * @param [in] loader                 - Initial loader of interpretable instructions and register files
   *
   * @brief Constructor
   */
  public IssueWindowSuperBlock(DecodeAndDispatchBlock decodeAndDispatchBlock)
  {
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.issueWindowBlockList   = new ArrayList<>();
    
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] aluIssueWindowBlock - Specific Issue window class for all ALU FUs (processing int or long instructions)
   *
   * @brief Injects ALU Issue window to the list
   */
  public void addAluIssueWindow(AluIssueWindowBlock aluIssueWindowBlock)
  {
    this.issueWindowBlockList.add(aluIssueWindowBlock);
  }// end of addAluIssueWindow
  //----------------------------------------------------------------------
  
  /**
   * @param [in] fpIssueWindowBlock - Specific Issue window class for all
   *             Floating point FUs (processing float or double instructions)
   *
   * @brief Injects Floating point Issue window to the list
   */
  public void addFpIssueWindow(FpIssueWindowBlock fpIssueWindowBlock)
  {
    this.issueWindowBlockList.add(fpIssueWindowBlock);
  }// end of addFpIssueWindow
  //----------------------------------------------------------------------
  
  /**
   * @param [in] branchIssueWindowBlock - Specific Issue window class for all
   *             Branch FUs (processing jump and branch instructions)
   *
   * @brief Injects Branch Issue window to the list
   */
  public void addBranchIssueWindow(BranchIssueWindowBlock branchIssueWindowBlock)
  {
    this.issueWindowBlockList.add(branchIssueWindowBlock);
  }// end of addBranchIssueWindow
  //----------------------------------------------------------------------
  
  /**
   * @param [in] loadStoreIssueWindowBlock - Specific Issue window class for all LoadStore FUs
   *
   * @brief Injects LoadStore Issue window to the list
   */
  public void addLoadStoreIssueWindow(LoadStoreIssueWindowBlock loadStoreIssueWindowBlock)
  {
    this.issueWindowBlockList.add(loadStoreIssueWindowBlock);
  }// end of addLoadStoreIssueWindow
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the failed instruction stack
   */
  @Override
  public void reset()
  {
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates dispatching instructions to Issue windows
   */
  @Override
  public void simulate()
  {
    // TODO: move to decode block
    // TODO: places like this, where simcodemodels are deleted, leave behind history of GlobalHistoryRegister
    if (decodeAndDispatchBlock.shouldFlush())
    {
      this.decodeAndDispatchBlock.getAfterRenameCodeList().forEach(codeModel -> codeModel.setFinished(true));
      this.decodeAndDispatchBlock.getAfterRenameCodeList().clear();
      this.decodeAndDispatchBlock.getBeforeRenameCodeList().forEach(codeModel -> codeModel.setFinished(true));
      this.decodeAndDispatchBlock.getBeforeRenameCodeList().clear();
      this.decodeAndDispatchBlock.setFlush(false);
    }
    else
    {
      int pullCount = !decodeAndDispatchBlock.shouldStall() ? this.decodeAndDispatchBlock.getAfterRenameCodeList()
              .size() : this.decodeAndDispatchBlock.getStalledPullCount();
      
      for (int i = 0; i < pullCount; i++)
      {
        SimCodeModel             codeModel = this.decodeAndDispatchBlock.getAfterRenameCodeList().get(i);
        InstructionFunctionModel model     = codeModel.getInstructionFunctionModel();
        selectCorrectIssueWindow(model, codeModel);
      }
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instruction - Instruction model on which Issue window is chosen
   * @param [in] codeModel   - Model representing instruction with set arguments
   *
   * @brief Selects issue window based on instruction type and instruction data type and dispatches the instruction
   */
  private void selectCorrectIssueWindow(InstructionFunctionModel instruction, SimCodeModel codeModel)
  {
    for (AbstractIssueWindowBlock issueWindow : this.issueWindowBlockList)
    {
      if (issueWindow.isCorrectInstructionType(instruction.getInstructionType()) && issueWindow.isCorrectDataType(
              instruction.getOutputDataType()))
      {
        issueWindow.dispatchInstruction(codeModel);
        issueWindow.createArgumentValidityEntry(codeModel);
      }
    }
  }// end of selectCorrectIssueWindow
  //----------------------------------------------------------------------
}
