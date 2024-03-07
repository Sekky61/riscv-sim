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
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.memory.LoadBufferItem;
import com.gradle.superscalarsim.models.memory.StoreBufferItem;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.*;

/**
 * @class LoadBufferBlock
 * @brief Class that holds all in-flight load instructions.
 * @details After computing the address, it is compared with older store instructions (stores without address are ignored).
 * In case of a match, the load value is bypassed from the store.
 * If there is no match, a load from memory is performed.
 * The correctness of a loads is checked at retirement of every _store_ instruction.
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
   * Load Buffer size
   */
  private int bufferSize;
  
  public LoadBufferBlock()
  {
    loadQueue = new ArrayDeque<>();
  }
  
  /**
   * @param bufferSize       Size of the load buffer
   * @param storeBufferBlock Block keeping all in-flight store instructions
   *
   * @brief Constructor
   */
  public LoadBufferBlock(int bufferSize, StoreBufferBlock storeBufferBlock)
  {
    this.bufferSize       = bufferSize;
    this.storeBufferBlock = storeBufferBlock;
    
    this.loadQueue            = new ArrayDeque<>();
    this.memoryAccessUnitList = new ArrayList<>();
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
   * @brief Simulates Load buffer
   */
  @Override
  public void simulate(int cycle)
  {
    removeInvalidInstructions();
    selectLoadForDataAccess(cycle);
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes all invalid load instructions from buffer. Instructions become invalid when they are flushed from ROB.
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
        it.remove();
      }
    }
  }// end of removeInvalidInstructions
  
  /**
   * Tries to find work for MAU block.
   * Tries to bypass load instructions by matching store instructions.
   *
   * @brief Selects load instructions for MA block
   */
  private void selectLoadForDataAccess(int cycle)
  {
    LoadBufferItem workForMa = null;
    for (LoadBufferItem item : this.loadQueue)
    {
      boolean dataShouldBeLoaded = item.getAddress() != -1 && !item.isAccessingMemory() && !item.isDestinationReady();
      if (!dataShouldBeLoaded)
      {
        continue;
      }
      
      StoreBufferItem resultStoreItem = storeBufferBlock.findMatchingStore(item);
      boolean         foundStore      = resultStoreItem != null;
      if (foundStore)
      {
        // Store with the same address and a loaded value found! Forward the value.
        forwardLoad(item, resultStoreItem, cycle);
        // But keep looking for work for MA (no break)
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
      if (!memoryAccessUnit.isFunctionUnitEmpty())
      {
        continue;
      }
      memoryAccessUnit.startExecuting(workForMa.getSimCodeModel());
      workForMa.setAccessingMemory(true);
      workForMa.setAccessingMemoryId(cycle);
      return;
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param loadItem  Load instruction to be bypassed
   * @param storeItem Store instruction to be bypassed
   *
   * @brief Forwards value from store to the load
   */
  private void forwardLoad(LoadBufferItem loadItem, StoreBufferItem storeItem, int cycle)
  {
    assert loadItem != null;
    assert storeItem != null;
    
    // TODO: storeItem direct reference to a register
    RegisterModel         sourceReg        = storeItem.getSourceRegister();
    RegisterReadinessEnum resultState      = sourceReg.getReadiness();
    boolean               storeSourceReady = resultState == RegisterReadinessEnum.kExecuted || resultState == RegisterReadinessEnum.kAssigned;
    assert storeSourceReady;
    
    // Write to the load dest. register
    RegisterModel destinationReg = loadItem.getDestinationRegister();
    // TODO: polish this, better API
    destinationReg.copyFrom(sourceReg);
    destinationReg.setReadiness(RegisterReadinessEnum.kAssigned);
    loadItem.setDestinationReady(true);
    loadItem.setHasBypassed(true);
    loadItem.setMemoryAccessId(cycle);
    // The load is done, ready for commit
    loadItem.getSimCodeModel().setBusy(false);
  }// end of processLoadInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param address Address of store instruction being committed.
   * @param cycle   The cycle of store instruction being committed.
   *
   * @brief Checks if there is a badly speculated load instruction in the load buffer.
   * If there are multiple, the oldest one is chosen.
   */
  public LoadBufferItem findConflictingLoad(long address, int cycle)
  {
    // The queue is ordered, so we search from the oldest to the newest
    for (LoadBufferItem bufferItem : this.loadQueue)
    {
      boolean addressesMatch = bufferItem.getAddress() == address;
      boolean isAfterStore   = bufferItem.getSimCodeModel().getIntegerId() > cycle;
      // Do not mark bypassed loads as conflicting
      boolean bypassed = bufferItem.hasBypassed();
      // TODO: what if the load is not yet in MA/executed?
      if (addressesMatch && isAfterStore && !bypassed)
      {
        return bufferItem;
      }
    }
    return null;
  }// end of checkIfProcessedHasConflict
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
    if (!hasSpace())
    {
      throw new RuntimeException("Trying to add load instruction to full load buffer");
    }
    
    // TODO check if load already in buffer
    
    // Create entry in the Load Buffer
    this.loadQueue.add(new LoadBufferItem(simCodeModel));
  }
  
  /**
   * @return True if buffer has space for a single item, false otherwise
   */
  public boolean hasSpace()
  {
    return this.bufferSize > this.loadQueue.size();
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
