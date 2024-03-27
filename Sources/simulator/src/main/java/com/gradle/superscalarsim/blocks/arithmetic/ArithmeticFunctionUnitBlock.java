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

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.IssueWindowBlock;
import com.gradle.superscalarsim.code.CodeArithmeticInterpreter;
import com.gradle.superscalarsim.code.Expression;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;

import java.util.List;

/**
 * @class ArithmeticFunctionUnitBlock
 * @brief Specific function unit class for executing arithmetic instructions
 */
public class ArithmeticFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /**
   * Interpreter for interpreting executing instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private CodeArithmeticInterpreter arithmeticInterpreter;
  
  public ArithmeticFunctionUnitBlock()
  {
    // Empty
  }
  
  /**
   * @param description      Description of the function unit
   * @param issueWindowBlock Issue window block for comparing instruction and data types
   * @param allowedOperators Array of all supported operators by this FU
   * @param statistics       Statistics for reporting FU usage
   *
   * @brief Constructor
   */
  public ArithmeticFunctionUnitBlock(FunctionalUnitDescription description,
                                     IssueWindowBlock issueWindowBlock,
                                     List<String> allowedOperators,
                                     SimulationStatistics statistics)
  {
    super(description, issueWindowBlock, statistics);
  }// end of Constructor
  
  /**
   * @param arithmeticInterpreter Arithmetic interpreter object
   *
   * @brief Injects Arithmetic interpreter to the FU
   */
  public void addArithmeticInterpreter(CodeArithmeticInterpreter arithmeticInterpreter)
  {
    this.arithmeticInterpreter = arithmeticInterpreter;
  }// end of addArithmeticInterpreter
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
    
    // The state may have changed during the handling of the instruction
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Finish the execution of the instruction. Set destination register and finish the instruction
   */
  protected void finishExecution()
  {
    // Instruction computed
    // Write result to the destination register
    InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
    assert destinationArgument != null;
    Result<Expression.Variable> result = arithmeticInterpreter.interpretInstruction(this.simCodeModel);
    
    if (result.isException())
    {
      // Mark exception
      this.simCodeModel.setException(result.exception());
    }
    else
    {
      RegisterModel reg = destinationArgument.getRegisterValue();
      reg.setValue(result.value().value.getBits(), result.value().value.getCurrentType());
      reg.setReadiness(RegisterReadinessEnum.kExecuted);
    }
    
    this.simCodeModel.setBusy(false);
    this.simCodeModel = null;
    this.setDelay(0);
    this.zeroTheCounter();
  }
  //----------------------------------------------------------------------
  
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
  }
  
  /**
   * @brief Action that should take place when an instruction starts executing.
   * Calculate the delay, start memory transaction.
   */
  @Override
  protected void handleStartExecution(int cycle)
  {
    this.simCodeModel.setFunctionUnitId(this.functionUnitId);
  }
  
  /**
   * @param simCodeModel Instruction to be executed
   *
   * @return True if the function unit can execute the instruction, false otherwise. Does not check if the function unit is busy.
   */
  @Override
  public boolean canExecuteInstruction(SimCodeModel simCodeModel)
  {
    // Check if the instruction is arithmetic
    InstructionFunctionModel model = simCodeModel.instructionFunctionModel();
    if (model.getInstructionType() != InstructionTypeEnum.kIntArithmetic && model.getInstructionType() != InstructionTypeEnum.kFloatArithmetic)
    {
      return false;
    }
    
    FunctionalUnitDescription.CapabilityName capabilityName = FunctionalUnitDescription.classifyExpression(
            model.getInterpretableAs());
    
    // Compare capability
    for (FunctionalUnitDescription.Capability cap : getDescription().operations)
    {
      if (cap.name == capabilityName)
      {
        return true;
      }
    }
    // Not found
    return false;
  }
}
