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

import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;

/**
 * @class ArithmeticFunctionUnitBlock
 * @brief Specific function unit class for executing load/store instructions
 */
public class LoadStoreFunctionUnit extends AbstractFunctionUnitBlock
{
  /// Load buffer with all load instruction entries
  private LoadBufferBlock loadBufferBlock;
  /// Store buffer with all store instruction entries
  private StoreBufferBlock storeBufferBlock;
  /// Interpreter for processing load store instructions
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  
  public LoadStoreFunctionUnit()
  {
  
  }
  
  /**
   * @param [in] blockScheduleTask  - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] reorderBufferBlock - Class containing simulated Reorder Buffer
   * @param [in] delay              - Delay for function unit
   * @param [in] issueWindowBlock   - Issue window block for comparing instruction and data types
   * @param [in] loadBufferBlock    - Load buffer with all load instruction entries
   * @param [in] storeBufferBlock   - Store buffer with all store instruction entries
   *
   * @brief Constructor
   */
  public LoadStoreFunctionUnit(ReorderBufferBlock reorderBufferBlock,
                               int delay,
                               AbstractIssueWindowBlock issueWindowBlock,
                               LoadBufferBlock loadBufferBlock,
                               StoreBufferBlock storeBufferBlock,
                               CodeLoadStoreInterpreter loadStoreInterpreter)
  {
    super(reorderBufferBlock, delay, issueWindowBlock);
    this.loadBufferBlock      = loadBufferBlock;
    this.storeBufferBlock     = storeBufferBlock;
    this.loadStoreInterpreter = loadStoreInterpreter;
  }// end of Constructor
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
      long address = loadStoreInterpreter.interpretAddress(simCodeModel);
      if (storeBufferBlock.getStoreMap().containsKey(simCodeModel.getIntegerId()))
      {
        storeBufferBlock.setAddress(simCodeModel.getIntegerId(), address);
      }
      else
      {
        loadBufferBlock.setAddress(simCodeModel.getIntegerId(), address);
      }
      this.simCodeModel = null;
    }
    
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  //----------------------------------------------------------------------
}
