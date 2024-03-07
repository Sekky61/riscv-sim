/**
 * @file LoadStoreFunctionUnit.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Load/Store Function unit
 * @date 14 March   2021 12:00 (created) \n
 * 14 May     2021 10:30 (revised)
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
package com.gradle.superscalarsim.blocks.loadstore;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.IssueWindowBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.util.Result;

/**
 * @class LoadStoreFunctionUnit
 * @brief Function unit class for executing load/store instructions (computing address).
 * @details When an instruction gets to this stage, the data to load/store must already be in the register (otherwise issue wouldn't have happened).
 * So this function unit only computes the address and puts the address into the load/store buffer.
 * The store in store buffer also needs to know the value to store, but it is read from the register field.
 */
public class LoadStoreFunctionUnit extends AbstractFunctionUnitBlock
{
  
  /**
   * Load buffer with all load instruction entries
   */
  @JsonIdentityReference(alwaysAsId = true)
  private LoadBufferBlock loadBufferBlock;
  
  /**
   * Store buffer with all store instruction entries
   */
  @JsonIdentityReference(alwaysAsId = true)
  private StoreBufferBlock storeBufferBlock;
  
  /**
   * Interpreter for processing load store instructions
   */
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  
  public LoadStoreFunctionUnit()
  {
  
  }
  
  /**
   * @brief Finishes execution of the instruction
   */
  @Override
  protected void finishExecution()
  {
    // Execute
    Result<Long> addressRes = loadStoreInterpreter.interpretAddress(simCodeModel);
    
    if (addressRes.isException())
    {
      // Handle exception, make sure the ROB will clear it, and that MAU will not execute
      this.simCodeModel.setException(addressRes.exception());
      this.simCodeModel.setBusy(false);
      // Do not set the address
    }
    else
    {
      long address = addressRes.value();
      if (simCodeModel.isStore())
      {
        storeBufferBlock.setAddress(simCodeModel.getIntegerId(), address);
      }
      else
      {
        // A load
        loadBufferBlock.setAddress(simCodeModel.getIntegerId(), address);
      }
    }
    this.simCodeModel = null;
  }
  
  /**
   * @param description          Description of the function unit
   * @param issueWindowBlock     Issue window block for comparing instruction and data types
   * @param loadBufferBlock      Load buffer with all load instruction entries
   * @param storeBufferBlock     Store buffer with all store instruction entries
   * @param loadStoreInterpreter Interpreter for processing load store instructions
   * @param statistics           Statistics for reporting FU usage
   *
   * @brief Constructor
   */
  public LoadStoreFunctionUnit(FunctionalUnitDescription description,
                               IssueWindowBlock issueWindowBlock,
                               LoadBufferBlock loadBufferBlock,
                               StoreBufferBlock storeBufferBlock,
                               CodeLoadStoreInterpreter loadStoreInterpreter,
                               SimulationStatistics statistics)
  {
    super(description, issueWindowBlock, statistics);
    this.loadBufferBlock      = loadBufferBlock;
    this.storeBufferBlock     = storeBufferBlock;
    this.loadStoreInterpreter = loadStoreInterpreter;
  }// end of Constructor
  
  /**
   * @param simCodeModel Instruction to be executed
   *
   * @return True if the function unit can execute the instruction, false otherwise.
   */
  @Override
  public boolean canExecuteInstruction(SimCodeModel simCodeModel)
  {
    return simCodeModel.getInstructionFunctionModel().getInstructionType() == InstructionTypeEnum.kLoadstore;
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
      handleInstruction();
    }
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  
  /**
   * Assumes there is an active instruction in the function unit.
   *
   * @brief Handles instruction in the function unit (Computes address).
   */
  private void handleInstruction()
  {
    incrementBusyCycles();
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
    
    finishExecution();
  }
  //----------------------------------------------------------------------
}
