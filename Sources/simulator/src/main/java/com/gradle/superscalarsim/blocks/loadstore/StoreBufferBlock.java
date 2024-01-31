/**
 * @file StoreBufferBlock.java
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
 * 09 April   2023 21:00 (revised)
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
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.memory.LoadBufferItem;
import com.gradle.superscalarsim.models.memory.StoreBufferItem;

import java.util.*;

/**
 * @class StoreBufferBlock
 * @brief Class that holds all in-flight store instructions
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class StoreBufferBlock implements AbstractBlock
{
  /**
   * Queue with all uncommitted store instructions and additional information.
   * The stores are in order.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final ArrayDeque<StoreBufferItem> storeQueue;
  
  /**
   * List holding all allocated memory access units
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<MemoryAccessUnit> memoryAccessUnitList;
  
  /**
   * Class containing all registers, that simulator uses
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * Store Buffer size
   */
  private int bufferSize;
  
  /**
   * @param bufferSize        Interpreter for processing load store instructions
   * @param registerFileBlock Class containing all registers, that simulator uses
   *
   * @brief Constructor
   */
  public StoreBufferBlock(int bufferSize, UnifiedRegisterFileBlock registerFileBlock)
  {
    this.registerFileBlock = registerFileBlock;
    this.bufferSize        = bufferSize;
    
    this.storeQueue           = new ArrayDeque<>();
    this.memoryAccessUnitList = new ArrayList<>();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param memoryAccessUnit Memory access unit to be added
   *
   * @brief Add memory access block to the store buffer
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
   * @brief Simulates store buffer
   */
  @Override
  public void simulate(int cycle)
  {
    removeInvalidInstructions();
    updateMapValues();
    selectStoreForDataAccess(cycle);
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the store buffer
   */
  @Override
  public void reset()
  {
    this.storeQueue.clear();
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes all invalid store instructions from buffer
   */
  private void removeInvalidInstructions()
  {
    Iterator<StoreBufferItem> it = this.storeQueue.descendingIterator();
    while (it.hasNext())
    {
      StoreBufferItem storeItem    = it.next();
      SimCodeModel    simCodeModel = storeItem.getSimCodeModel();
      if (simCodeModel.hasFailed())
      {
        if (storeItem.isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(simCodeModel));
        }
        it.remove();
      }
    }
  }// end of removeInvalidInstructions
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Checks if store source registers are ready and if yes then marks them
   * TODO move to method on the StoreBufferItem
   */
  private void updateMapValues()
  {
    for (StoreBufferItem item : this.storeQueue)
    {
      RegisterReadinessEnum state = registerFileBlock.getRegister(item.getSourceRegister()).getReadiness();
      item.setSourceReady(state == RegisterReadinessEnum.kExecuted || state == RegisterReadinessEnum.kAssigned);
    }
  }// end of updateMapValues
  //----------------------------------------------------------------------
  
  /**
   * @brief Selects store instructions for MA block.
   * The selected store is a non-speculative store with address.
   * In case of multiple non-speculative stores to the same address, the older is written, the newer is written later.
   * TODO: Could the old store be simply removed?
   */
  private void selectStoreForDataAccess(int cycle)
  {
    StoreBufferItem storeItem = null;
    for (StoreBufferItem item : this.storeQueue)
    {
      // If there is store without address computed stop - there could be WaW hazard
      if (item.getAddress() == -1)
      {
        break;
      }
      
      SimCodeModel simCodeModel = item.getSimCodeModel();
      assert !simCodeModel.hasFailed();
      
      boolean isSpeculative    = simCodeModel.isSpeculative();
      boolean isAvailableForMA = !isSpeculative && item.getAddress() != -1 && !item.isAccessingMemory() && item.getAccessingMemoryId() == -1 && item.isSourceReady();
      if (!isAvailableForMA)
      {
        continue;
      }
      
      boolean hazardFound = false;
      for (StoreBufferItem previousStore : this.storeQueue)
      {
        // Check if we haven't reached current statement
        if (simCodeModel.getIntegerId() == previousStore.getSimCodeModel().getIntegerId())
        {
          break;
        }
        
        // I suppose that stores can be at most 4 bytes, TODO check
        if ((previousStore.getAddress() & ~3L) == (item.getAddress() & ~3L))
        {
          // If there is WaW hazard - stop
          hazardFound = true;
          break;
        }
      }
      if (!hazardFound)
      {
        storeItem = item;
        break;
      }
    }
    
    if (storeItem == null)
    {
      return;
    }
    
    for (MemoryAccessUnit memoryAccessUnit : this.memoryAccessUnitList)
    {
      if (memoryAccessUnit.isFunctionUnitEmpty())
      {
        memoryAccessUnit.resetCounter();
        memoryAccessUnit.setSimCodeModel(storeItem.getSimCodeModel());
        storeItem.setAccessingMemory(true);
        storeItem.setAccessingMemoryId(cycle);
        // todo: return here??
        return;
      }
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return True if buffer has space for a single item, false otherwise
   */
  public boolean hasSpace()
  {
    return this.bufferSize > this.storeQueue.size();
  }// end of isBufferFull
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModelId ID identifying specific storeMap entry
   * @param address     Store instruction address
   *
   * @brief Set Store address
   */
  public void setAddress(int codeModelId, long address)
  {
    getStoreBufferItem(codeModelId).setAddress(address);
  }// end of setAddress
  //-------------------------------------------------------------------------------------------
  
  public StoreBufferItem getStoreBufferItem(int id)
  {
    for (StoreBufferItem item : this.storeQueue)
    {
      if (item.getSimCodeModel().getIntegerId() == id)
      {
        return item;
      }
    }
    return null;
  }// end of getStoreBufferItem
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Finds a matching store instruction in store buffer for given load instruction.
   * The store must be preceding the load instruction in program order.
   * It also has to be the most recent one if multiple stores to the same address exist.
   * It also has to have the value computed.
   */
  public StoreBufferItem findMatchingStore(LoadBufferItem loadItem)
  {
    // TODO: can be more optimal - the stores are sorted by program order
    assert loadItem != null;
    long            loadAddress     = loadItem.getAddress();
    SimCodeModel    simCodeModel    = loadItem.getSimCodeModel();
    StoreBufferItem bestCandidate   = null;
    int             bestCandidateId = -1;
    for (StoreBufferItem storeItem : this.storeQueue)
    {
      int     candidateId             = storeItem.getSourceResultId();
      boolean moreRecentButStillOlder = bestCandidateId < candidateId && simCodeModel.getIntegerId() > candidateId;
      boolean isSourceReady           = storeItem.isSourceReady();
      if (loadAddress == storeItem.getAddress() && moreRecentButStillOlder && isSourceReady)
      {
        bestCandidateId = candidateId;
        bestCandidate   = storeItem;
      }
    }
    return bestCandidate;
  }// end of findMatchingStore
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param codeModelId ID identifying specific loadMap entry
   *
   * @brief Set flag marking if the instruction is in the MA block
   */
  public void setMemoryAccessFinished(int codeModelId)
  {
    getStoreBufferItem(codeModelId).setAccessingMemory(false);
  }// end of setMemoryAccessFinished
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return First instruction in queue
   * @brief Get first instruction in queue
   */
  public SimCodeModel getStoreQueueFirst()
  {
    assert !this.storeQueue.isEmpty();
    return storeQueue.peek().getSimCodeModel();
  }// end of getStoreQueueFirst
  
  /**
   * @brief Release store instruction on top of the queue (instruction has been committed)
   */
  public void releaseStoreFirst()
  {
    if (this.storeQueue.isEmpty())
    {
      throw new RuntimeException("Release store when store queue is empty");
    }
    storeQueue.poll();
  }// end of releaseStoreFirst
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Queue size
   * @brief Get store queue size
   */
  public int getQueueSize()
  {
    return this.storeQueue.size();
  }// end of getQueueSize
  
  /**
   * @param codeModel The store instruction to be added to the buffer
   *
   * @brief Add an instruction to the store buffer
   */
  public void addStoreToBuffer(SimCodeModel codeModel)
  {
    InputCodeArgument argument = codeModel.getArgumentByName("rs2");
    this.storeQueue.add(
            new StoreBufferItem(codeModel, Objects.requireNonNull(argument).getValue(), codeModel.getIntegerId()));
  }// end of addStoreToBuffer
  //-------------------------------------------------------------------------------------------
}
