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
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeBranchInterpreter;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.OptionalInt;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class BranchFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /// Interpreter for interpreting executing instructions
  @JsonIdentityReference(alwaysAsId = true)
  private CodeBranchInterpreter branchInterpreter;
  /// Class containing all registers, that simulator uses
  @JsonIdentityReference(alwaysAsId = true)
  private UnifiedRegisterFileBlock registerFileBlock;
  
  public BranchFunctionUnitBlock()
  {
  
  }
  
  /**
   * @param name               Name of the function unit
   * @param issueWindowBlock   Issue window block for comparing instruction and data types
   * @param delay              Delay for function unit
   * @param reorderBufferBlock Class containing simulated Reorder Buffer
   *
   * @brief Constructor
   */
  public BranchFunctionUnitBlock(FunctionalUnitDescription description,
                                 AbstractIssueWindowBlock issueWindowBlock,
                                 ReorderBufferBlock reorderBufferBlock)
  {
    super(description, issueWindowBlock, reorderBufferBlock);
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param branchInterpreter Branch interpreter object
   *
   * @brief Injects Branch interpreter to the FU
   */
  public void addBranchInterpreter(CodeBranchInterpreter branchInterpreter)
  {
    this.branchInterpreter = branchInterpreter;
  }// end of addBranchInterpreter
  //----------------------------------------------------------------------
  
  /**
   * @param registerFileBlock UnifiedRegisterFileBlock object with all registers
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
    if (!isFunctionUnitEmpty())
    {
      handleInstruction();
    }
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  
  /**
   * @brief Processes instruction
   */
  public void handleInstruction()
  {
    if (this.simCodeModel.hasFailed())
    {
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      this.simCodeModel = null;
      this.zeroTheCounter();
      return;
    }
    
    if (hasTimerStartedThisTick())
    {
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
    }
    
    tickCounter();
    if (!hasDelayPassed())
    {
      return;
    }
    
    // Execute
    int         instructionPosition = this.simCodeModel.getSavedPc();
    OptionalInt jumpOffset          = branchInterpreter.interpretInstruction(this.simCodeModel, instructionPosition);
    boolean     jumpTaken           = jumpOffset.isPresent();
    // If the branch was taken or not
    this.simCodeModel.setBranchLogicResult(jumpTaken);
    // Used to fix BTB and PC in misprediction
    if (jumpTaken)
    {
      this.simCodeModel.setBranchTargetOffset(jumpOffset.getAsInt());
    }
    InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
    if (destinationArgument != null)
    {
      // Write the result to the register
      RegisterModel reg                     = registerFileBlock.getRegister(destinationArgument.getValue());
      int           nextInstructionPosition = instructionPosition + 4;
      reg.setValue(nextInstructionPosition);
      reg.setReadiness(RegisterReadinessEnum.kExecuted);
    }
    
    this.reorderBufferBlock.getRobItem(this.simCodeModel.getIntegerId()).reorderFlags.setBusy(false);
    this.simCodeModel = null;
  }
  
  //----------------------------------------------------------------------
}