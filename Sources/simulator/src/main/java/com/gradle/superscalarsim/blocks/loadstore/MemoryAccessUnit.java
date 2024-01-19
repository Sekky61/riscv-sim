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

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.IssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.code.MemoryModel;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.memory.MemoryAccess;
import com.gradle.superscalarsim.models.register.RegisterModel;

/**
 * @class MemoryAccessUnit
 * @brief Specific function unit class for memory access required by load instructions
 */
public class MemoryAccessUnit extends AbstractFunctionUnitBlock
{
  /**
   * Memory. Used for load/store operations
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final MemoryModel memoryModel;
  /**
   * Load buffer with all load instruction entries
   */
  @JsonIdentityReference(alwaysAsId = true)
  private LoadBufferBlock loadBufferBlock;
  /**
   * Store buffer with all Store instruction entries
   */
  @JsonIdentityReference(alwaysAsId = true)
  private StoreBufferBlock storeBufferBlock;
  /**
   * Interpreter for processing load store instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  
  /**
   * The binary state of the MA unit.
   * First delay is for MAU, second delay is for reply from memory.
   */
  private boolean firstDelayPassed = false;
  
  /**
   * Data of the load operation to be able to store it later
   */
  private long savedResult;
  
  /**
   * settings for first delay
   */
  private int baseDelay;
  
  /**
   * @param description          Description of the function unit
   * @param reorderBufferBlock   Class containing simulated Reorder Buffer
   * @param issueWindowBlock     Issue window block for comparing instruction and data types
   * @param loadBufferBlock      Buffer keeping all in-flight load instructions
   * @param storeBufferBlock     Buffer keeping all in-flight store instructions
   * @param loadStoreInterpreter Interpreter processing load/store instructions
   * @param statistics           Statistics for reporting FU usage
   *
   * @brief Constructor
   */
  public MemoryAccessUnit(FunctionalUnitDescription description,
                          ReorderBufferBlock reorderBufferBlock,
                          IssueWindowBlock issueWindowBlock,
                          LoadBufferBlock loadBufferBlock,
                          StoreBufferBlock storeBufferBlock,
                          MemoryModel memoryModel,
                          CodeLoadStoreInterpreter loadStoreInterpreter,
                          SimulationStatistics statistics)
  {
    super(description, issueWindowBlock, reorderBufferBlock, statistics);
    this.loadBufferBlock      = loadBufferBlock;
    this.storeBufferBlock     = storeBufferBlock;
    this.loadStoreInterpreter = loadStoreInterpreter;
    this.baseDelay            = description.latency;
    this.memoryModel          = memoryModel;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates memory access
   */
  @Override
  public void simulate(int cycle)
  {
    if (isFunctionUnitEmpty())
    {
      this.functionUnitId += this.functionUnitCount;
    }
    else
    {
      handleInstruction(cycle);
    }
  }// end of simulate
  
  private void handleInstruction(int cycle)
  {
    incrementBusyCycles();
    if (this.simCodeModel.hasFailed())
    {
      // Instruction has failed, remove it from MAU
      this.simCodeModel.setFunctionUnitId(this.functionUnitId);
      this.simCodeModel = null;
      this.zeroTheCounter();
      
      this.setDelay(baseDelay);
      this.firstDelayPassed = false;
      return;
    }
    
    if (hasTimerStartedThisTick())
    {
      // First tick of work, leave your ID in store and load buffers
      if (simCodeModel.isLoad())
      {
        this.loadBufferBlock.getLoadBufferItem(simCodeModel.getIntegerId()).setMemoryAccessId(this.functionUnitId);
      }
      else if (simCodeModel.isStore())
      {
        this.storeBufferBlock.getStoreBufferItem(simCodeModel.getIntegerId()).setMemoryAccessId(this.functionUnitId);
      }
      else
      {
        throw new RuntimeException("Instruction is not load or store");
      }
    }
    
    // hasDelayPassed increments counter, checks if work (waiting) is done
    tickCounter();
    if (!hasDelayPassed())
    {
      return;
    }
    
    // Delay passed, execute
    boolean allowAccessFinish = true;
    if (!firstDelayPassed)
    {
      // First delay is over, start memory access
      firstDelayPassed = true;
      
      // This contacts memoryModel, pulls data and delay
      MemoryAccess access        = loadStoreInterpreter.interpretInstruction(this.simCodeModel);
      long         address       = access.getAddress();
      int          numberOfBytes = access.getSize();
      int          id            = this.simCodeModel.getIntegerId();
      
      Pair<Integer, Long> result;
      if (access.isStore())
      {
        int delay = processStoreOperation(access);
        result = new Pair<>(delay, access.getData());
      }
      else
      {
        result = processLoadOperation(numberOfBytes * 8, address, access.isSigned(), id, cycle);
      }
      
      int delay = result.getFirst();
      savedResult = result.getSecond();
      
      // Set delay for memory response
      this.setDelay(delay);
      this.resetCounter();
      if (delay != 0)
      {
        // Memory returned with delay for access, do not allow to finish in the same execution
        allowAccessFinish = false;
      }
    }
    
    if (firstDelayPassed && allowAccessFinish)
    {
      // Wait for memory is over, instruction is finished
      firstDelayPassed = false;
      this.setDelay(baseDelay);
      int simCodeId = this.simCodeModel.getIntegerId();
      this.reorderBufferBlock.getRobItem(simCodeId).reorderFlags.setBusy(false);
      if (this.simCodeModel.isLoad())
      {
        InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
        RegisterModel     destRegister        = destinationArgument.getRegisterValue();
        destRegister.setValue(savedResult, simCodeModel.getInstructionFunctionModel().getArgumentByName("rd").type());
        destRegister.setReadiness(RegisterReadinessEnum.kExecuted);
        this.loadBufferBlock.setDestinationAvailable(simCodeId);
        this.loadBufferBlock.setMemoryAccessFinished(simCodeId);
      }
      else if (this.simCodeModel.isStore())
      {
        this.storeBufferBlock.setMemoryAccessFinished(simCodeId);
      }
      
      this.simCodeModel = null;
    }
  }
  //----------------------------------------------------------------------
  
  /**
   * @param sizeBits     How many bits to store
   * @param address      Address to store to
   * @param valueBits    Value to store (lower sizeBits bits are used)
   * @param id           ID of a store instruction, that is being executed
   * @param currentCycle Current cycle
   *
   * @return Delay of this access
   */
  private int processStoreOperation(MemoryAccess memoryAccess)
  {
    return memoryModel.store(memoryAccess);
  }// end of processStoreOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param sizeBits     Size of the loaded value (8, 16, 32)
   * @param address      Address to read from
   * @param isSigned     True in case of signed value, false otherwise
   * @param id           ID of a load instruction, that is being executed
   * @param currentCycle Current cycle
   *
   * @return Pair of delay of this access and data (64 bits, suitable for assignment to register)
   */
  private Pair<Integer, Long> processLoadOperation(int sizeBits,
                                                   long address,
                                                   boolean isSigned,
                                                   int id,
                                                   int currentCycle)
  {
    int                 numberOfBytes = sizeBits / 8;
    Pair<Integer, Long> loadedData    = memoryModel.load(address, numberOfBytes, id, currentCycle);
    
    // Apply mask to zero out or sign extend the value
    long bits      = loadedData.getSecond();
    long validMask = (1L << sizeBits) - 1;
    if (sizeBits >= 64)
    {
      validMask = -1;
    }
    bits = bits & validMask;
    if (isSigned && ((1L << (sizeBits - 1)) & bits) != 0)
    {
      // Fill with sign bit
      long signMask = ~validMask;
      bits = bits | signMask;
    }
    
    return new Pair<>(loadedData.getFirst(), bits);
  }// end of processLoadOperation
  //-------------------------------------------------------------------------------------------
  
  @Override
  public void reset()
  {
    super.reset();
    firstDelayPassed = false;
    this.setDelay(baseDelay);
  }
  
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
  
  /**
   * @param simCodeModel Possible executing model
   *
   * @brief Removes instruction from MA block if there is one
   */
  public void tryRemoveCodeModel(SimCodeModel simCodeModel)
  {
    if (this.simCodeModel == null || this.simCodeModel != simCodeModel)
    {
      // todo Maybe the 'this.simCodeModel != simCodeModel' is wrong, because buffers try to remove a lot of times
      throw new RuntimeException("Trying to remove wrong code model from MAU");
    }
    
    tickCounter();
    this.simCodeModel.setFunctionUnitId(this.functionUnitId - 1);
    this.simCodeModel = null;
    this.zeroTheCounter();
    
    this.setDelay(baseDelay);
    this.firstDelayPassed = false;
    
  }// end of tryRemoveCodeModel
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets base delay for this unit - first delay without response from cache
   */
  public void setBaseDelay(int baseDelay)
  {
    this.baseDelay = baseDelay;
  }
}
