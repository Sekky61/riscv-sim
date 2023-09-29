/**
 * @file BranchFunctionUnitBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Branch Function Unit
 * @date 1  March  2020 16:00 (created) \n
 * 14 May    2021 10:30 (revised)
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
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeBranchInterpreter;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.RegisterModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.OptionalInt;

public class BranchFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /// Interpreter for interpreting executing instructions
  private CodeBranchInterpreter branchInterpreter;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  
  public BranchFunctionUnitBlock()
  {
  
  }
  
  /**
   * @param [in] blockScheduleTask     - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] reorderBufferBlock    - Class containing simulated Reorder Buffer
   * @param [in] delay                 - Delay for function unit
   * @param [in] issueWindowBlock   - Issue window block for comparing instruction and data types
   *
   * @brief Constructor
   */
  public BranchFunctionUnitBlock(ReorderBufferBlock reorderBufferBlock,
                                 AbstractIssueWindowBlock issueWindowBlock,
                                 int delay)
  {
    super(reorderBufferBlock, delay, issueWindowBlock);
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] branchInterpreter - Branch interpreter object
   *
   * @brief Injects Branch interpreter to the FU
   */
  public void addBranchInterpreter(CodeBranchInterpreter branchInterpreter)
  {
    this.branchInterpreter = branchInterpreter;
  }// end of addBranchInterpreter
  //----------------------------------------------------------------------
  
  /**
   * @param [in] registerFileBlock - UnifiedRegisterFileBlock object with all registers
   *
   * @brief Injects UnifiedRegisterFileBlock to the FU
   */
  public void addRegisterFileBlock(UnifiedRegisterFileBlock registerFileBlock)
  {
    this.registerFileBlock = registerFileBlock;
  }// end of addRegisterFileBlock
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates execution of an instruction
   */
  @Override
  public void simulate()
  {
    if (!isFunctionUnitEmpty() && this.simCodeModel.hasFailed())
    {
      hasDelayPassed();
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      this.failedInstructions.push(this.simCodeModel);
      this.simCodeModel = null;
      this.zeroTheCounter();
    }
    
    if (!isFunctionUnitEmpty() && hasTimerStarted())
    {
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
    }
    
    if (!isFunctionUnitEmpty() && hasDelayPassed())
    {
      if (hasTimerStarted())
      {
        this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      }
      int instructionPosition     = this.simCodeModel.getSavedPc();
      int nextInstructionPosition = instructionPosition + 1;
      
      OptionalInt jumpOffset = branchInterpreter.interpretInstruction(this.simCodeModel, instructionPosition);
      boolean     jumpTaken  = jumpOffset.isPresent();
      // If the branch was taken or not
      this.simCodeModel.setBranchLogicResult(jumpTaken);
      // Used to fix BTB and PC in misprediction
      if (jumpTaken)
      {
        this.simCodeModel.setBranchTargetOffset(jumpOffset.getAsInt());
      }
      // Write the result to the register
      InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
      if (destinationArgument != null)
      {
        RegisterModel reg = registerFileBlock.getRegister(destinationArgument.getValue());
        reg.setValue(nextInstructionPosition);
        reg.setReadiness(RegisterReadinessEnum.kExecuted);
      }
      
      this.reorderBufferBlock.getFlagsMap().get(this.simCodeModel.getId()).setBusy(false);
      this.simCodeModel = null;
    }
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates backwards (resets flags and waits until un-execution of instruction)
   */
  @Override
  public void simulateBackwards()
  {
    if (!isFunctionUnitEmpty())
    {
      return;
    }
    
    this.functionUnitId -= this.functionUnitCount;
    for (SimCodeModel codeModel : this.reorderBufferBlock.getReorderQueue())
    {
      if (codeModel.getFunctionUnitId() != this.functionUnitId || !issueWindowBlock.isCorrectDataType(
          codeModel.getResultDataType()) || !issueWindowBlock.isCorrectInstructionType(
          codeModel.getInstructionTypeEnum()))
      {
        // Skip
        continue;
      }
      
      // Put `codeModel` back to this unit
      this.resetReverseCounter();
      reorderBufferBlock.getFlagsMap().get(codeModel.getId()).setBusy(true);
      this.simCodeModel = codeModel;
      if (!this.failedInstructions.isEmpty() && this.simCodeModel == this.failedInstructions.peek())
      {
        this.failedInstructions.pop();
        this.popHistoryCounter();
      }
      // Restore result readiness
      simCodeModel.getArguments().stream().filter(argument -> argument.getName().equals("rd")).findFirst().ifPresent(
          destinationArgument -> registerFileBlock.getRegister(destinationArgument.getValue())
                                                  .setReadiness(RegisterReadinessEnum.kAllocated));
      // Remove target and brcd arguments
      simCodeModel.setBranchLogicResult(false);
      simCodeModel.setBranchTargetOffset(0);
      
      return;
    }
    
    // Not found in ROB, check failed instructions
    if (!this.failedInstructions.isEmpty() && this.failedInstructions.peek().getFunctionUnitId() == this.functionUnitId)
    {
      // Put failed `codeModel` back to this unit
      this.simCodeModel = this.failedInstructions.pop();
      this.popHistoryCounter();
    }
  }// end of simulateBackwards
  //----------------------------------------------------------------------
}
