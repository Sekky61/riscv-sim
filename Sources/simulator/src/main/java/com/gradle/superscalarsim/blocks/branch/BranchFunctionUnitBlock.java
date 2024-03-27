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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.IssueWindowBlock;
import com.gradle.superscalarsim.code.CodeBranchInterpreter;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class BranchFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /**
   * Interpreter for interpreting executing instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final CodeBranchInterpreter branchInterpreter;
  
  /**
   * @param description       Description of the function unit
   * @param issueWindowBlock  Issue window block for comparing instruction and data types
   * @param branchInterpreter Interpreter for interpreting executing instructions
   * @param statistics        Simulation statistics
   *
   * @brief Constructor
   */
  public BranchFunctionUnitBlock(FunctionalUnitDescription description,
                                 IssueWindowBlock issueWindowBlock,
                                 CodeBranchInterpreter branchInterpreter,
                                 SimulationStatistics statistics)
  {
    super(description, issueWindowBlock, statistics);
    this.branchInterpreter = branchInterpreter;
  }// end of Constructor
  
  /**
   * @brief Finishes execution of the instruction
   */
  @Override
  protected void finishExecution()
  {
    // Execute
    Result<CodeBranchInterpreter.BranchResult> jumpTargetRes = branchInterpreter.interpretInstruction(
            this.simCodeModel);
    // I don't think jump target uses division
    assert !jumpTargetRes.isException();
    CodeBranchInterpreter.BranchResult jump       = jumpTargetRes.value();
    int                                jumpTarget = jump.target();
    boolean                            jumpTaken  = jump.jumpTaken();
    // If the branch was taken or not
    this.simCodeModel.setBranchLogicResult(jumpTaken);
    // Used to fix BTB and PC in misprediction
    this.simCodeModel.setBranchTarget(jumpTarget);
    InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
    if (destinationArgument != null)
    {
      // Write the result to the register
      RegisterModel reg                     = destinationArgument.getRegisterValue();
      int           nextInstructionPosition = this.simCodeModel.getSavedPc() + 4;
      reg.setValue(nextInstructionPosition);
      reg.setReadiness(RegisterReadinessEnum.kExecuted);
    }
    
    this.simCodeModel.setBusy(false);
    this.simCodeModel = null;
    zeroTheCounter();
    setDelay(0);
  }
  
  /**
   * @brief Action that should take place when an instruction failed.
   * Remove the instruction, reset counter, cancel memory transaction.
   */
  @Override
  protected void handleFailedInstruction()
  {
    this.simCodeModel.setFunctionUnitId(this.functionUnitId);
    this.simCodeModel = null;
    this.zeroTheCounter();
    this.setDelay(0);
  }
  
  /**
   * @param cycle
   *
   * @brief Action that should take place when an instruction starts executing.
   * Calculate the delay, start memory transaction.
   */
  @Override
  protected void handleStartExecution(int cycle)
  {
    this.simCodeModel.setFunctionUnitId(this.functionUnitId);
    this.setDelay(this.delay);
  }
  
  /**
   * @param simCodeModel Instruction to be executed
   *
   * @return True if the function unit can execute the instruction, false otherwise.
   */
  @Override
  public boolean canExecuteInstruction(SimCodeModel simCodeModel)
  {
    return simCodeModel.instructionFunctionModel().getInstructionType() == InstructionTypeEnum.kJumpbranch;
  }
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates execution of an instruction
   */
  @Override
  public void simulate(int cycle)
  {
    if (!isFunctionUnitEmpty())
    {
      handleInstruction(cycle);
    }
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  
  /**
   * @brief Processes instruction
   */
  public void handleInstruction(int cycle)
  {
    if (this.simCodeModel.hasFailed())
    {
      handleFailedInstruction();
      return;
    }
    
    if (hasTimerStartedThisTick())
    {
      handleStartExecution(cycle);
    }
    //
    //    tickCounter();
    //    if (!hasDelayPassed())
    //    {
    //      incrementBusyCycles();
    //      return;
    //    }
    //
    //    finishExecution();
  }
  
  //----------------------------------------------------------------------
}