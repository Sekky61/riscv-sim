/**
 * @file LoadBufferBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Load buffer
 * @date 12 March   2021 16:00 (created) \n
 * 28 April   2021 16:45 (revised)
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.base.InstructionFetchBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.memory.LoadBufferItem;
import com.gradle.superscalarsim.models.memory.StoreBufferItem;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.*;

/**
 * @class LoadBufferBlock
 * @brief Class that holds all in-flight load instructions
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class LoadBufferBlock implements AbstractBlock
{
  /**
   * Queue with all uncommitted load instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final ArrayDeque<LoadBufferItem> loadQueue;
  
  /**
   * List holding all allocated memory access units
   */
  @JsonIdentityReference(alwaysAsId = true)
  private List<MemoryAccessUnit> memoryAccessUnitList;
  
  /**
   * Block keeping all in-flight store instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private StoreBufferBlock storeBufferBlock;
  
  /**
   * Class containing all registers, that simulator uses
   */
  @JsonIdentityReference(alwaysAsId = true)
  private UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * Class contains simulated implementation of Reorder buffer
   */
  @JsonIdentityReference(alwaysAsId = true)
  private ReorderBufferBlock reorderBufferBlock;
  
  /**
   * Class that fetches code from CodeParser
   */
  @JsonIdentityReference(alwaysAsId = true)
  private InstructionFetchBlock instructionFetchBlock;
  
  /**
   * Load Buffer size
   */
  private int bufferSize;
  
  public LoadBufferBlock()
  {
    loadQueue = new ArrayDeque<>();
  }
  
  /**
   * @param storeBufferBlock       Block keeping all in-flight store instructions
   * @param decodeAndDispatchBlock Class, which simulates instruction decode and renames registers
   * @param registerFileBlock      Class containing all registers, that simulator uses
   * @param reorderBufferBlock     Class contains simulated implementation of Reorder buffer
   * @param instructionFetchBlock  Class that fetches code from CodeParser
   *
   * @brief Constructor
   */
  public LoadBufferBlock(StoreBufferBlock storeBufferBlock,
                         UnifiedRegisterFileBlock registerFileBlock,
                         ReorderBufferBlock reorderBufferBlock,
                         InstructionFetchBlock instructionFetchBlock)
  {
    this.storeBufferBlock      = storeBufferBlock;
    this.registerFileBlock     = registerFileBlock;
    this.reorderBufferBlock    = reorderBufferBlock;
    this.instructionFetchBlock = instructionFetchBlock;
    this.bufferSize            = 64;
    
    this.loadQueue            = new ArrayDeque<>();
    this.memoryAccessUnitList = new ArrayList<>();
    
    this.reorderBufferBlock.setLoadBufferBlock(this);
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param memoryAccessUnit Memory access unit to be added
   *
   * @brief Add memory access block to the load buffer
   */
  public void addMemoryAccessUnit(MemoryAccessUnit memoryAccessUnit)
  {
    memoryAccessUnit.setFunctionUnitId(this.memoryAccessUnitList.size());
    this.memoryAccessUnitList.add(memoryAccessUnit);
    setFunctionUnitCountInUnits();
  }// end of addMemoryAccessUnit
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Set number of MAs to MA for correct id creation
   */
  private void setFunctionUnitCountInUnits()
  {
    for (MemoryAccessUnit memoryAccessUnit : this.memoryAccessUnitList)
    {
      memoryAccessUnit.setFunctionUnitCount(this.memoryAccessUnitList.size());
    }
  }// end of setFunctionUnitCountInUnits
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param bufferSize New load buffer size
   *
   * @brief Set load buffer limit size
   */
  public void setBufferSize(int bufferSize)
  {
    this.bufferSize = bufferSize;
  }// end of setBufferSize
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulates Load buffer
   */
  @Override
  public void simulate(int cycle)
  {
    checkIfProcessedHasConflict(cycle);
    removeInvalidInstructions();
    selectLoadForDataAccess(cycle);
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the load buffer
   */
  @Override
  public void reset()
  {
    this.loadQueue.clear();
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * Can cause a flush of the ROB
   *
   * @brief Checks if the instruction has no conflicts with all previously executed store instructions.
   */
  private void checkIfProcessedHasConflict(int cycle)
  {
    for (LoadBufferItem bufferItem : this.loadQueue)
    {
      if (!bufferItem.isDestinationReady() && !bufferItem.isAccessingMemory())
      {
        continue;
      }
      int     beforeMaId   = bufferItem.getMemoryAccessId();
      boolean beforeBypass = bufferItem.hasBypassed();
      
      if (findMatchingStore(bufferItem) == null)
      {
        // No conflict
        continue;
      }
      
      // Conflict detected
      // Remove the load and flush ROB
      SimCodeModel simCodeModel = bufferItem.getSimCodeModel();
      reorderBufferBlock.invalidateInstructions(simCodeModel);
      reorderBufferBlock.flushInvalidInstructions(cycle);
      bufferItem.setMemoryFailedId(cycle);
      bufferItem.setMemoryAccessId(beforeMaId);
      bufferItem.setHasBypassed(beforeBypass);
      // Fix PC
      instructionFetchBlock.setPc(simCodeModel.getSavedPc());
    }
  }// end of checkIfProcessedHasConflict
  
  /**
   * @brief Removes all invalid load instructions from buffer
   */
  private void removeInvalidInstructions()
  {
    // Iterate the queue from the end, remove until first valid instruction
    Iterator<LoadBufferItem> it = this.loadQueue.descendingIterator();
    while (it.hasNext())
    {
      LoadBufferItem loadItem  = it.next();
      SimCodeModel   codeModel = loadItem.getSimCodeModel();
      
      // Previous call of `checkIfProcessedHasConflict` might have cause a ROB flush
      // ROB in turn invalidated instructions
      if (codeModel.hasFailed())
      {
        // Remove
        if (loadItem.isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(codeModel));
        }
        this.loadQueue.removeLast();
      }
    }
  }// end of removeInvalidInstructions
  //-------------------------------------------------------------------------------------------
  
  /**
   * Tries to find work for MA block. Tries to bypass load instructions by matching store instructions
   *
   * @brief Selects load instructions for MA block
   */
  private void selectLoadForDataAccess(int cycle)
  {
    LoadBufferItem workForMa = null;
    for (LoadBufferItem item : this.loadQueue)
    {
      boolean isAvailableForMA = item.getAddress() != -1 && !item.isAccessingMemory() && !item.isDestinationReady();
      if (!isAvailableForMA)
      {
        continue;
      }
      
      StoreBufferItem resultStoreItem = findMatchingStore(item);
      boolean         foundStore      = resultStoreItem != null;
      if (foundStore)
      {
        // Bypass, if possible. But keep looking for work for MA
        // If the bypass is not successful, it will be successful later when the store is executed
        tryBypassLoad(item, resultStoreItem, cycle);
      }
      else
      {
        // Item found - conflict free
        workForMa = item;
        break;
      }
    }
    
    if (workForMa == null)
    {
      return;
    }
    
    // TODO: Does this mean load block can only dispatch one load in a tick? What about more MAs?
    for (MemoryAccessUnit memoryAccessUnit : this.memoryAccessUnitList)
    {
      if (memoryAccessUnit.isFunctionUnitEmpty())
      {
        memoryAccessUnit.resetCounter();
        memoryAccessUnit.setSimCodeModel(workForMa.getSimCodeModel());
        workForMa.setAccessingMemory(true);
        workForMa.setAccessingMemoryId(cycle);
        return;
      }
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief finds matching store instruction in store buffer for given load instruction
   */
  private StoreBufferItem findMatchingStore(LoadBufferItem loadItem)
  {
    assert loadItem != null;
    long         loadAddress  = loadItem.getAddress();
    SimCodeModel simCodeModel = loadItem.getSimCodeModel();
    for (StoreBufferItem storeItem : this.storeBufferBlock.getStoreMapAsList())
    {
      if (loadAddress == storeItem.getAddress() && simCodeModel.getIntegerId() > storeItem.getSourceResultId())
      {
        return storeItem;
      }
    }
    return null;
  }// end of findMatchingStore
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param loadItem  Load instruction to be bypassed
   * @param storeItem Store instruction to be bypassed
   *
   * @return True if successfully bypassed, false otherwise
   * @brief Tries to bypass load instruction by store instruction
   */
  private boolean tryBypassLoad(LoadBufferItem loadItem, StoreBufferItem storeItem, int cycle)
  {
    assert loadItem != null;
    assert storeItem != null;
    
    RegisterModel         sourceReg        = registerFileBlock.getRegister(storeItem.getSourceRegister());
    RegisterReadinessEnum resultState      = sourceReg.getReadiness();
    boolean               storeSourceReady = resultState == RegisterReadinessEnum.kExecuted || resultState == RegisterReadinessEnum.kAssigned;
    if (!storeSourceReady)
    {
      // Cannot speculatively load, the value is not computed yet
      return false;
    }
    
    // We found a matching store with computed value
    // Bypass memory access by speculation
    RegisterModel destinationReg = registerFileBlock.getRegister(loadItem.getDestinationRegister());
    destinationReg.setValue(sourceReg.getValue());
    destinationReg.setReadiness(RegisterReadinessEnum.kAssigned);
    loadItem.setDestinationReady(true);
    loadItem.setHasBypassed(true);
    loadItem.setMemoryAccessId(cycle);
    reorderBufferBlock.getRobItem(loadItem.getSimCodeModel().getIntegerId()).reorderFlags.setBusy(false);
    return true;
  }// end of processLoadInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * Adds item to the load buffer
   *
   * @param simCodeModel Instruction to be added
   */
  public void addLoadToBuffer(SimCodeModel simCodeModel)
  {
    if (!simCodeModel.isLoad())
    {
      throw new RuntimeException("Trying to add non-load instruction to load buffer");
    }
    
    if (isBufferFull(1))
    {
      throw new RuntimeException("Trying to add load instruction to full load buffer");
    }
    
    // TODO check if load already in buffer
    
    // Create entry in the Load Buffer
    InputCodeArgument argument = simCodeModel.getArgumentByName("rd");
    this.loadQueue.add(new LoadBufferItem(simCodeModel, Objects.requireNonNull(argument).getValue()));
  }
  
  /**
   * @param possibleAddition Number of instructions to be possibly added
   *
   * @return True if buffer would be full, false otherwise
   * @brief Checks if buffer would be full if specified number of instructions were to be added
   */
  public boolean isBufferFull(int possibleAddition)
  {
    return this.bufferSize < (this.loadQueue.size() + possibleAddition);
  }// end of isBufferFull
  
  /**
   * @param codeModelId ID identifying specific loadMap entry
   * @param address     Load instruction address
   *
   * @brief Set load address
   */
  public void setAddress(int codeModelId, long address)
  {
    Objects.requireNonNull(getLoadBufferItem(codeModelId)).setAddress(address);
  }// end of setAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModelId ID of the load instruction
   *
   * @return Load buffer entry
   * @brief Finds the corresponding load buffer entry for given load instruction
   */
  public LoadBufferItem getLoadBufferItem(int codeModelId)
  {
    for (LoadBufferItem loadItem : this.loadQueue)
    {
      if (loadItem.getSimCodeModel().getIntegerId() == codeModelId)
      {
        return loadItem;
      }
    }
    return null;
  }// end of findLoadBufferItem
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModelId ID identifying specific loadMap entry
   *
   * @brief Set flag if the destination register of the load instruction is ready to be loaded into
   */
  public void setDestinationAvailable(int codeModelId)
  {
    Objects.requireNonNull(getLoadBufferItem(codeModelId)).setDestinationReady(true);
  }// end of setCodeRegisterAvailable
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModelId ID identifying specific loadMap entry
   *
   * @brief Set flag marking if the instruction is in the MA block
   */
  public void setMemoryAccessFinished(int codeModelId)
  {
    Objects.requireNonNull(getLoadBufferItem(codeModelId)).setAccessingMemory(false);
  }// end of setMemoryAccessFinished
  //-------------------------------------------------------------------------------------------
  
  /**
   * Expects that the load buffer is not empty
   *
   * @return First instruction in queue
   * @brief Get first instruction in queue
   */
  public SimCodeModel getLoadQueueFirst()
  {
    assert !loadQueue.isEmpty();
    return loadQueue.peek().getSimCodeModel();
  }// end of getLoadQueueFirst
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Release load instruction on top of the queue (lowest ID)
   */
  public void releaseLoadFirst()
  {
    assert !loadQueue.isEmpty();
    loadQueue.poll();
  }// end of releaseLoadFirst
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Queue size
   * @brief Get load queue size
   */
  public int getQueueSize()
  {
    return this.loadQueue.size();
  }// end of getQueueSize
  //-------------------------------------------------------------------------------------------
}
