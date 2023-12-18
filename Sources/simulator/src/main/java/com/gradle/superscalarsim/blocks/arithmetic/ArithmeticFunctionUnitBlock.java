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
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeArithmeticInterpreter;
import com.gradle.superscalarsim.code.Expression;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class ArithmeticFunctionUnitBlock
 * @brief Specific function unit class for executing arithmetic instructions
 */
public class ArithmeticFunctionUnitBlock extends AbstractFunctionUnitBlock
{
  /// Array of all supported operators by this FU
  private final List<String> allowedOperators;
  /// Interpreter for interpreting executing instructions
  @JsonIdentityReference(alwaysAsId = true)
  private CodeArithmeticInterpreter arithmeticInterpreter;
  /// Class containing all registers, that simulator uses
  @JsonIdentityReference(alwaysAsId = true)
  private UnifiedRegisterFileBlock registerFileBlock;
  
  public ArithmeticFunctionUnitBlock()
  {
    // Empty
    this.allowedOperators = new ArrayList<>();
  }
  
  /**
   * @param name               Name of the function unit
   * @param delay              Delay for function unit
   * @param issueWindowBlock   Issue window block for comparing instruction and data types
   * @param allowedOperators   Array of all supported operators by this FU
   * @param reorderBufferBlock Class containing simulated Reorder Buffer
   *
   * @brief Constructor
   */
  public ArithmeticFunctionUnitBlock(FunctionalUnitDescription description,
                                     AbstractIssueWindowBlock issueWindowBlock,
                                     List<String> allowedOperators,
                                     ReorderBufferBlock reorderBufferBlock)
  {
    super(description, issueWindowBlock, reorderBufferBlock);
    this.allowedOperators = allowedOperators;
  }// end of Constructor
  //----------------------------------------------------------------------
  
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
    
    // The state may have changed during the handling of the instruction
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  
  /**
   * Assumes that there is an instruction in the function unit
   *
   * @brief Handle the instruction inside FU
   */
  private void handleInstruction()
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
    
    // Instruction computed
    // Write result to the destination register
    InputCodeArgument   destinationArgument = simCodeModel.getArgumentByName("rd");
    Expression.Variable result              = arithmeticInterpreter.interpretInstruction(this.simCodeModel);
    RegisterModel       reg                 = registerFileBlock.getRegister(destinationArgument.getValue());
    // TODO redundant?
    reg.setValueContainer(result.value);
    reg.setReadiness(RegisterReadinessEnum.kExecuted);
    
    this.reorderBufferBlock.getRobItem(this.simCodeModel.getIntegerId()).reorderFlags.setBusy(false);
    this.simCodeModel = null;
  }
  //----------------------------------------------------------------------
  
  /**
   * @return List of allowed operators
   * @brief Get all allowed operators by this FU
   */
  public List<String> getAllowedOperators()
  {
    return allowedOperators;
  }// end of getAllowedOperators
  //----------------------------------------------------------------------
  
  
}
