/**
 * @file MemoryAccessUnit.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Memory Access Function unit
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
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.Objects;

/**
 * @class MemoryAccessUnit
 * @brief Specific function unit class for memory access required by load instructions
 */
public class MemoryAccessUnit extends AbstractFunctionUnitBlock
{
  /// Load buffer with all load instruction entries
  private LoadBufferBlock loadBufferBlock;
  /// Store buffer with all Store instruction entries
  private StoreBufferBlock storeBufferBlock;
  /// Interpreter for processing load store instructions
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  /// Clock cycle counter
  private int cycleCount;
  
  /// First delay is for MAU, second delay is for reply from memory
  private boolean firstDelayPassed = false;
  /// Data of the load operation to be able to store it later
  private long savedResult;
  /// settings for first delay
  private int baseDelay;
  
  public MemoryAccessUnit()
  {
  }
  
  /**
   * @param reorderBufferBlock   Class containing simulated Reorder Buffer
   * @param delay                Delay for function unit
   * @param issueWindowBlock     Issue window block for comparing instruction and data types
   * @param loadBufferBlock      Buffer keeping all in-flight load instructions
   * @param storeBufferBlock     Buffer keeping all in-flight store instructions
   * @param loadStoreInterpreter Interpreter processing load/store instructions
   * @param registerFileBlock    Class containing all registers, that simulator uses
   *
   * @brief Constructor
   */
  public MemoryAccessUnit(ReorderBufferBlock reorderBufferBlock,
                          int delay,
                          AbstractIssueWindowBlock issueWindowBlock,
                          LoadBufferBlock loadBufferBlock,
                          StoreBufferBlock storeBufferBlock,
                          CodeLoadStoreInterpreter loadStoreInterpreter,
                          UnifiedRegisterFileBlock registerFileBlock)
  {
    super(reorderBufferBlock, delay, issueWindowBlock);
    this.loadBufferBlock      = loadBufferBlock;
    this.storeBufferBlock     = storeBufferBlock;
    this.loadStoreInterpreter = loadStoreInterpreter;
    this.registerFileBlock    = registerFileBlock;
    this.baseDelay            = delay;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates memory access
   */
  @Override
  public void simulate()
  {
    cycleCount++;
    if (!isFunctionUnitEmpty() && this.simCodeModel.hasFailed())
    {
      hasDelayPassed();
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      this.simCodeModel = null;
      this.zeroTheCounter();
      
      this.setDelay(baseDelay);
      this.firstDelayPassed = false;
    }
    
    if (!isFunctionUnitEmpty() && hasTimerStarted())
    {
      if (this.loadBufferBlock.getLoadMap().containsKey(this.simCodeModel.getId()))
      {
        this.loadBufferBlock.getLoadMap().get(this.simCodeModel.getId()).setMemoryAccessId(this.functionUnitId);
      }
      else if (this.storeBufferBlock.getStoreMap().containsKey(this.simCodeModel.getId()))
      {
        this.storeBufferBlock.getStoreMap().get(this.simCodeModel.getId()).setMemoryAccessId(this.functionUnitId);
      }
    }
    
    if (!isFunctionUnitEmpty() && hasDelayPassed())
    {
      //If memory returns 0 delay for access, allow finish in the same execution
      boolean allowAccessFinish = true;
      if (!firstDelayPassed)
      {
        firstDelayPassed = true;
        
        Pair<Integer, Long> result = loadStoreInterpreter.interpretInstruction(this.simCodeModel, cycleCount);
        savedResult = result.getSecond();
        
        //Set delay for memory response
        this.setDelay(result.getFirst());
        this.resetCounter();
        if (result.getFirst() != 0)
        {
          allowAccessFinish = false;
        }
      }
      
      if (firstDelayPassed && allowAccessFinish)
      {
        firstDelayPassed = false;
        this.setDelay(baseDelay);
        this.reorderBufferBlock.getFlagsMap().get(this.simCodeModel.getId()).setBusy(false);
        if (this.loadBufferBlock.getLoadMap().containsKey(this.simCodeModel.getId()))
        {
          InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
          RegisterModel destRegister = registerFileBlock.getRegister(
                  Objects.requireNonNull(destinationArgument).getValue());
          destRegister.setBits(savedResult);
          destRegister.setReadiness(RegisterReadinessEnum.kExecuted);
          this.loadBufferBlock.setDestinationAvailable(simCodeModel.getId());
          this.loadBufferBlock.setMemoryAccessFinished(simCodeModel.getId());
        }
        else if (this.storeBufferBlock.getStoreMap().containsKey(this.simCodeModel.getId()))
        {
          this.storeBufferBlock.setMemoryAccessFinished(simCodeModel.getId());
        }
        
        this.simCodeModel = null;
      }
    }
    
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  @Override
  public void reset()
  {
    super.reset();
    firstDelayPassed = false;
    cycleCount       = 0;
    this.setDelay(baseDelay);
  }
  
  /**
   * @return True if counter is at 0 or less, false otherwise
   * @brief Checks if counter reached 0
   */
  @Override
  public boolean hasReversedDelayPassed()
  {
    return getCounter() <= 0;
  }// end of hasReversedDelayPassed
  
  /**
   * @param [in] simCodeModel - Possible executing model
   *
   * @brief Removes instruction from MA block if there is one
   */
  public void tryRemoveCodeModel(SimCodeModel simCodeModel)
  {
    if (this.simCodeModel != null && this.simCodeModel == simCodeModel)
    {
      hasDelayPassed();
      this.simCodeModel.setFunctionUnitId(this.functionUnitId - 1);
      this.simCodeModel = null;
      this.zeroTheCounter();
      
      this.setDelay(baseDelay);
      this.firstDelayPassed = false;
    }
  }// end of tryRemoveCodeModel
  //----------------------------------------------------------------------
  
  /**
   * @brief Gets base delay for this unit - first delay without response from cache
   */
  public int getBaseDelay()
  {
    return baseDelay;
  }
  
  /**
   * @brief Sets base delay for this unit - first delay without response from cache
   */
  public void setBaseDelay(int baseDelay)
  {
    this.baseDelay = baseDelay;
  }
  
}
