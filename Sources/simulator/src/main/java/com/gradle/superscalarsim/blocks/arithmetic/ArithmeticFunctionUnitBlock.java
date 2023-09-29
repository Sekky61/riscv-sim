/**
 * @file ArithmeticFunctionUnitBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Arithmetic Function Unit
 * @date 9  February 2021 16:00 (created) \n
 * 14 May      2021 10:30 (revised)
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
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeArithmeticInterpreter;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.RegisterModel;
import com.gradle.superscalarsim.models.SimCodeModel;

/**
 * @class ArithmeticFunctionUnitBlock
 * @brief Specific function unit class for executing arithmetic instructions
 */
public class ArithmeticFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /// Array of all supported operators by this FU
  private final String[] allowedOperators;
  /// Interpreter for interpreting executing instructions
  private CodeArithmeticInterpreter arithmeticInterpreter;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  
  public ArithmeticFunctionUnitBlock()
  {
    // Empty
    this.allowedOperators = new String[0];
  }
  
  /**
   * @param [in] blockScheduleTask  - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] reorderBufferBlock - Class containing simulated Reorder Buffer
   * @param [in] delay              - Delay for function unit
   * @param [in] allowedOperators   - Array of all supported operators by this FU
   *
   * @brief Constructor
   */
  public ArithmeticFunctionUnitBlock(ReorderBufferBlock reorderBufferBlock,
                                     int delay,
                                     AbstractIssueWindowBlock issueWindowBlock,
                                     String[] allowedOperators)
  {
    super(reorderBufferBlock, delay, issueWindowBlock);
    this.allowedOperators = allowedOperators;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] arithmeticInterpreter - Arithmetic interpreter object
   *
   * @brief Injects Arithmetic interpreter to the FU
   */
  public void addArithmeticInterpreter(CodeArithmeticInterpreter arithmeticInterpreter)
  {
    this.arithmeticInterpreter = arithmeticInterpreter;
  }// end of addArithmeticInterpreter
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
      // Write result to the destination register
      if (hasTimerStarted())
      {
        this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      }
      InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
      double            result              = arithmeticInterpreter.interpretInstruction(this.simCodeModel);
      RegisterModel     reg                 = registerFileBlock.getRegister(destinationArgument.getValue());
      reg.setValue(result);
      reg.setReadiness(RegisterReadinessEnum.kExecuted);
      
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
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId -= this.functionUnitCount;
      for (SimCodeModel codeModel : this.reorderBufferBlock.getReorderQueue())
      {
        if (codeModel.getFunctionUnitId() == this.functionUnitId && issueWindowBlock.isCorrectDataType(
            codeModel.getResultDataType()) && issueWindowBlock.isCorrectInstructionType(
            codeModel.getInstructionTypeEnum()))
        {
          this.resetReverseCounter();
          this.simCodeModel = codeModel;
          if (!this.failedInstructions.isEmpty() && this.simCodeModel == this.failedInstructions.peek())
          {
            this.failedInstructions.pop();
            this.popHistoryCounter();
          }
          InputCodeArgument arg = simCodeModel.getArgumentByName("rd");
          if (arg != null)
          {
            registerFileBlock.getRegister(arg.getValue()).setReadiness(RegisterReadinessEnum.kAllocated);
          }
          reorderBufferBlock.getFlagsMap().get(codeModel.getId()).setBusy(true);
          return;
        }
      }
      if (!this.failedInstructions.isEmpty() && this.failedInstructions.peek()
                                                                       .getFunctionUnitId() == this.functionUnitId)
      {
        this.simCodeModel = this.failedInstructions.pop();
        this.popHistoryCounter();
      }
    }
  }// end of simulateBackwards
  //----------------------------------------------------------------------
  
  /**
   * @return Array of allowed operators
   * @brief Get all allowed operators by this FU
   */
  public String[] getAllowedOperators()
  {
    return allowedOperators;
  }// end of getAllowedOperators
  //----------------------------------------------------------------------
}
