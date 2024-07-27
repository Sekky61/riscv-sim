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
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

/**
 * @class IssueWindowSuperBlock
 * @brief Class containing logic for dispatching instructions from decode stage (ROB) to Issue windows
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class IssueWindowSuperBlock implements AbstractBlock
{
  /**
   * The Fixed point issue window.
   */
  private final IssueWindowBlock aluIssueWindowBlock;
  /**
   * The Floating point issue window.
   */
  private final IssueWindowBlock fpIssueWindowBlock;
  /**
   * The Branch issue window.
   */
  private final IssueWindowBlock branchIssueWindowBlock;
  /**
   * The Load/Store issue window.
   */
  private final IssueWindowBlock loadStoreIssueWindowBlock;
  /**
   * Class, which simulates instruction decode and renames registers.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final ReorderBufferBlock reorderBufferBlock;
  
  /**
   * @param reorderBufferBlock        ROB
   * @param aluIssueWindowBlock       ALU issue window
   * @param fpIssueWindowBlock        FP issue window
   * @param branchIssueWindowBlock    Branch issue window
   * @param loadStoreIssueWindowBlock Load/Store issue window
   *
   * @brief Constructor
   */
  public IssueWindowSuperBlock(ReorderBufferBlock reorderBufferBlock,
                               IssueWindowBlock aluIssueWindowBlock,
                               IssueWindowBlock fpIssueWindowBlock,
                               IssueWindowBlock branchIssueWindowBlock,
                               IssueWindowBlock loadStoreIssueWindowBlock)
  {
    this.reorderBufferBlock        = reorderBufferBlock;
    this.aluIssueWindowBlock       = aluIssueWindowBlock;
    this.fpIssueWindowBlock        = fpIssueWindowBlock;
    this.branchIssueWindowBlock    = branchIssueWindowBlock;
    this.loadStoreIssueWindowBlock = loadStoreIssueWindowBlock;
    
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates dispatching instructions to Issue windows
   */
  @Override
  public void simulate(int cycle)
  {
    // Issue instruction without a IssueWindowId
    this.reorderBufferBlock    // formatter trick
            .getReorderQueue() // ROB
            .filter(codeModel -> codeModel.issueWindowId == -1) // Only instructions not in an issue window
            .forEach(simCodeModel -> selectCorrectIssueWindow(simCodeModel, cycle));
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Instruction to be dispatched
   * @param cycle     Current cycle
   *
   * @brief Selects issue window based on instruction type and dispatches the instruction
   */
  public void selectCorrectIssueWindow(SimCodeModel codeModel, int cycle)
  {
    IssueWindowBlock selectedIssue = switch (codeModel.instructionFunctionModel().instructionType())
    {
      case kIntArithmetic -> aluIssueWindowBlock;
      case kFloatArithmetic -> fpIssueWindowBlock;
      case kLoadstore -> loadStoreIssueWindowBlock;
      case kJumpbranch -> branchIssueWindowBlock;
    };
    selectedIssue.dispatchInstruction(codeModel, cycle);
  }// end of selectCorrectIssueWindow
  //----------------------------------------------------------------------
}
