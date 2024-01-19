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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.List;

/**
 * @class IssueWindowSuperBlock
 * @brief Class containing logic for dispatching instructions from decode stage to Issue windows
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class IssueWindowSuperBlock implements AbstractBlock
{
  /**
   * Class, which simulates instruction decode and renames registers.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  
  /**
   * List of all issue windows.
   */
  private List<IssueWindowBlock> issueWindowBlockList;
  
  public IssueWindowSuperBlock()
  {
  }
  
  /**
   * @param blockScheduleTask      Task class, where blocks are periodically triggered by the GlobalTimer
   * @param decodeAndDispatchBlock Class, which simulates instruction decode and renames registers
   * @param loader                 Initial loader of interpretable instructions and register files
   *
   * @brief Constructor
   */
  public IssueWindowSuperBlock(DecodeAndDispatchBlock decodeAndDispatchBlock, List<IssueWindowBlock> issueWindowBlocks)
  {
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.issueWindowBlockList   = issueWindowBlocks;
    
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates dispatching instructions to Issue windows
   */
  @Override
  public void simulate(int cycle)
  {
    // TODO: move to decode block
    // TODO: places like this, where simcodemodels are deleted, leave behind history of GlobalHistoryRegister
    if (decodeAndDispatchBlock.shouldFlush())
    {
      this.decodeAndDispatchBlock.getCodeBuffer().forEach(codeModel -> codeModel.setFinished(true));
      this.decodeAndDispatchBlock.getCodeBuffer().clear();
      this.decodeAndDispatchBlock.setFlush(false);
    }
    else
    {
      int pullCount = !decodeAndDispatchBlock.shouldStall() ? this.decodeAndDispatchBlock.getCodeBuffer()
              .size() : this.decodeAndDispatchBlock.getStalledPullCount();
      
      for (int i = 0; i < pullCount; i++)
      {
        SimCodeModel             codeModel = this.decodeAndDispatchBlock.getCodeBuffer().get(i);
        InstructionFunctionModel model     = codeModel.getInstructionFunctionModel();
        selectCorrectIssueWindow(model, codeModel);
      }
    }
  }// end of simulate
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
   * @param instruction Instruction model on which Issue window is chosen
   * @param codeModel   Model representing instruction with set arguments
   *
   * @brief Selects issue window based on instruction type and instruction data type and dispatches the instruction
   * TODO: is it guaranteed that a window will be found?
   */
  private void selectCorrectIssueWindow(InstructionFunctionModel instruction, SimCodeModel codeModel)
  {
    for (IssueWindowBlock issueWindow : this.issueWindowBlockList)
    {
      if (issueWindow.canHold(instruction))
      {
        issueWindow.dispatchInstruction(codeModel);
        return;
      }
    }
  }// end of selectCorrectIssueWindow
  //----------------------------------------------------------------------
}
