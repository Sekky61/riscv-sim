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
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.code.MemoryModel;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.memory.MemoryAccess;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @class MemoryAccessUnit
 * @brief Function unit class for memory access required by load instructions
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
   * Delay added to the memory access by this unit.
   */
  private int baseDelay;
  
  /**
   * Current memory transaction, or null if no transaction is in progress.
   * TODO do not serialize (or maybe it does not matter here)
   */
  private MemoryTransaction transaction;
  
  /**
   * @param description          Description of the function unit
   * @param issueWindowBlock     Issue window block for comparing instruction and data types
   * @param loadBufferBlock      Buffer keeping all in-flight load instructions
   * @param storeBufferBlock     Buffer keeping all in-flight store instructions
   * @param memoryModel          Memory. Used for load/store operations
   * @param loadStoreInterpreter Interpreter processing load/store instructions
   * @param statistics           Statistics for reporting FU usage
   *
   * @brief Constructor
   */
  public MemoryAccessUnit(FunctionalUnitDescription description,
                          IssueWindowBlock issueWindowBlock,
                          LoadBufferBlock loadBufferBlock,
                          StoreBufferBlock storeBufferBlock,
                          MemoryModel memoryModel,
                          CodeLoadStoreInterpreter loadStoreInterpreter,
                          SimulationStatistics statistics)
  {
    super(description, issueWindowBlock, statistics);
    this.loadBufferBlock      = loadBufferBlock;
    this.storeBufferBlock     = storeBufferBlock;
    this.loadStoreInterpreter = loadStoreInterpreter;
    this.baseDelay            = description.latency;
    this.memoryModel          = memoryModel;
    transaction               = null;
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
      // todo why
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
      // In case this clock tick is the last one of the instruction, take result from mem/cache
      if (hasDelayPassed())
      {
        memoryModel.finishTransaction(transaction.id());
      }
      
      // Mark the transaction as canceled
      if (transaction != null)
      {
        transaction.setCanceled();
        transaction = null;
      }
      
      zeroTheCounter();
      setDelay(baseDelay);
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
      
      // Start execution
      int delay = startExecution(cycle);
      this.setDelay(delay);
      // todo +baseDelay ? But careful to take the transaction result in time
    }
    
    // hasDelayPassed increments counter, checks if work (waiting) is done
    if (hasDelayPassed())
    {
      assert simCodeModel != null;
      assert transaction != null;
      // Wait for memory is over, instruction is finished
      this.setDelay(baseDelay);
      int simCodeId = simCodeModel.getIntegerId();
      simCodeModel.setBusy(false);
      // Take result
      memoryModel.finishTransaction(transaction.id());
      if (this.simCodeModel.isLoad())
      {
        InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
        RegisterModel     destRegister        = destinationArgument.getRegisterValue();
        long              savedResult         = transaction.dataAsLong();
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
      this.transaction  = null;
      zeroTheCounter();
    }
    
    tickCounter();
  }
  //----------------------------------------------------------------------
  
  /**
   * @return Delay of this access and id of the transaction
   * @brief starts execution of instruction
   */
  private int startExecution(int cycle)
  {
    // This contacts memoryModel, pulls data and delay
    Result<MemoryAccess> accessRes = loadStoreInterpreter.interpretInstruction(this.simCodeModel);
    assert !accessRes.isException();
    MemoryAccess access        = accessRes.value();
    long         address       = access.getAddress();
    int          numberOfBytes = access.getSize();
    
    // Convert to a MemoryTransaction
    byte[] data = null;
    if (access.isStore())
    {
      ByteBuffer byteBuffer = ByteBuffer.allocate(8);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putLong(access.getData());
      // Take only first size bytes
      data = new byte[numberOfBytes];
      System.arraycopy(byteBuffer.array(), 0, data, 0, numberOfBytes);
    }
    
    
    transaction = new MemoryTransaction(-1, functionUnitId, simCodeModel.getCodeId(), cycle, address, data,
                                        numberOfBytes, access.isStore(), access.isSigned());
    // return memory delay
    return memoryModel.execute(transaction);
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
      return;
      //          // todo Maybe the 'this.simCodeModel != simCodeModel' is wrong, because buffers try to remove a lot of times
      //          throw new RuntimeException("Trying to remove wrong code model from MAU");
    }
    
    tickCounter();
    this.simCodeModel.setFunctionUnitId(this.functionUnitId - 1);
    this.simCodeModel = null;
    this.zeroTheCounter();
    
    this.setDelay(baseDelay);
    // todo cancel transaction?
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
