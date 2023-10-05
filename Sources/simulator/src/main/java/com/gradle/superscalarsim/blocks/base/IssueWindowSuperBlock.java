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
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @class IssueWindowSuperBlock
 * @brief Class containing logic for dispatching instructions from decode stage to Issue windows
 */
public class IssueWindowSuperBlock implements AbstractBlock
{
  /// List of failed instructions that were to be pulled from decode
  private final Stack<SimCodeModel> failedInstructions;
  /// Class, which simulates instruction decode and renames registers
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /// List of all issue windows
  private List<AbstractIssueWindowBlock> issueWindowBlockList;
  /// Initial loader of interpretable instructions and register files
  private InitLoader loader;
  
  public IssueWindowSuperBlock()
  {
    failedInstructions = new Stack<>();
  }
  
  /**
   * @param [in] blockScheduleTask      - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] decodeAndDispatchBlock - Class, which simulates instruction decode and renames registers
   * @param [in] loader                 - Initial loader of interpretable instructions and register files
   *
   * @brief Constructor
   */
  public IssueWindowSuperBlock(DecodeAndDispatchBlock decodeAndDispatchBlock, InitLoader loader)
  {
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.issueWindowBlockList   = new ArrayList<>();
    this.loader                 = loader;
    this.failedInstructions     = new Stack<>();
    
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
    this.failedInstructions.clear();
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates dispatching instructions to Issue windows
   */
  @Override
  public void simulate()
  {
    if (decodeAndDispatchBlock.shouldFlush())
    {
      for (int i = 0; i < this.decodeAndDispatchBlock.getAfterRenameCodeList().size(); i++)
      {
        this.failedInstructions.push(this.decodeAndDispatchBlock.getAfterRenameCodeList().get(i));
      }
      this.decodeAndDispatchBlock.getAfterRenameCodeList().clear();
      this.decodeAndDispatchBlock.getBeforeRenameCodeList().clear();
      this.decodeAndDispatchBlock.setFlush(false);
    }
    else
    {
      int pullCount = !decodeAndDispatchBlock.shouldStall() ? this.decodeAndDispatchBlock.getAfterRenameCodeList()
                                                                                         .size() :
          this.decodeAndDispatchBlock.getStalledPullCount();
      
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
   * Simulate backwards (pulls instructions from Issue windows to decode)
   */
  @Override
  public void simulateBackwards()
  {
    int                id                     = this.decodeAndDispatchBlock.getCurrentStepId();
    List<SimCodeModel> returningCodeModelList = new ArrayList<>();
    for (AbstractIssueWindowBlock issueWindowBlock : this.issueWindowBlockList)
    {
      List<SimCodeModel> currentFoundCodeModelList = new ArrayList<>();
      issueWindowBlock.getIssuedInstructions().forEach(decodeCodeModel ->
                                                       {
                                                         if (decodeCodeModel.getInstructionBulkNumber() == id)
                                                         {
                                                           currentFoundCodeModelList.add(decodeCodeModel);
                                                         }
                                                       });
      issueWindowBlock.getIssuedInstructions().removeAll(currentFoundCodeModelList);
      returningCodeModelList.addAll(currentFoundCodeModelList);
    }
    if (returningCodeModelList.isEmpty() && !this.failedInstructions.isEmpty() && this.failedInstructions.peek()
                                                                                                         .getInstructionBulkNumber() == id)
    {
      while (!this.failedInstructions.isEmpty() && this.failedInstructions.peek().getInstructionBulkNumber() == id)
      {
        returningCodeModelList.add(this.failedInstructions.pop());
      }
    }
    this.decodeAndDispatchBlock.getAfterRenameCodeList().addAll(returningCodeModelList);
    this.decodeAndDispatchBlock.getAfterRenameCodeList().sort(SimCodeModel::compareTo);
  }// end of simulateBackwards
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
