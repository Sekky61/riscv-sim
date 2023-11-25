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
import com.gradle.superscalarsim.blocks.base.DecodeAndDispatchBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.StoreBufferItem;

import java.util.*;

/**
 * @class StoreBufferBlock
 * @brief Class that holds all in-flight store instructions
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class StoreBufferBlock implements AbstractBlock
{
  /**
   * Queue with all uncommitted store instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final Queue<SimCodeModel> storeQueue;
  
  /**
   * Map with additional infos for specific store instructions
   */
  private final Map<Integer, StoreBufferItem> storeMap;
  
  /**
   * List holding all allocated memory access units
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<MemoryAccessUnit> memoryAccessUnitList;
  
  /**
   * Interpreter for processing load store instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  
  /**
   * Class, which simulates instruction decode and renames registers
   */
  @JsonIdentityReference(alwaysAsId = true)
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  
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
   * Store Buffer size
   */
  private int bufferSize;
  
  /**
   * ID counter matching the one in ROB
   */
  private int commitId;
  
  public StoreBufferBlock()
  {
    this.bufferSize = 64;
    this.commitId   = 0;
    
    this.storeQueue = new PriorityQueue<>();
    this.storeMap   = new LinkedHashMap<>();
    
    this.memoryAccessUnitList = new ArrayList<>();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] blockScheduleTask      - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] loadStoreInterpreter   - Interpreter for processing load store instructions
   * @param [in] decodeAndDispatchBlock - Class, which simulates instruction decode and renames registers
   * @param [in] registerFileBlock      - Class containing all registers, that simulator uses
   * @param [in] initLoader             - Initial loader of interpretable instructions and register files
   * @param [in] reorderBufferBlock     - Class contains simulated implementation of Reorder buffer
   *
   * @brief Constructor
   */
  public StoreBufferBlock(CodeLoadStoreInterpreter loadStoreInterpreter,
                          DecodeAndDispatchBlock decodeAndDispatchBlock,
                          UnifiedRegisterFileBlock registerFileBlock,
                          ReorderBufferBlock reorderBufferBlock)
  {
    this.loadStoreInterpreter   = loadStoreInterpreter;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.registerFileBlock      = registerFileBlock;
    this.reorderBufferBlock     = reorderBufferBlock;
    this.bufferSize             = 64;
    this.commitId               = 0;
    
    this.storeQueue = new PriorityQueue<>();
    this.storeMap   = new LinkedHashMap<>();
    
    this.memoryAccessUnitList = new ArrayList<>();
    
    this.reorderBufferBlock.setStoreBufferBlock(this);
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] memoryAccessUnit - Memory access unit to be added
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
   * @param [in] loadStoreFunctionUnitList - list of new memory access units
   *
   * @brief Adds list of memory access units to the Store buffer block
   */
  public void setAllMemoryAccessUnits(List<MemoryAccessUnit> loadStoreFunctionUnitList)
  {
    for (int i = 0; i < loadStoreFunctionUnitList.size(); i++)
    {
      loadStoreFunctionUnitList.get(i).setFunctionUnitId(i + 1);
    }
    this.memoryAccessUnitList.clear();
    this.memoryAccessUnitList.addAll(loadStoreFunctionUnitList);
    this.setFunctionUnitCountInUnits();
  }// end of setAllMemoryAccessUnits
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulates store buffer
   */
  @Override
  public void simulate()
  {
    pullStoreInstructionsFromDecode();
    removeInvalidInstructions();
    updateMapValues();
    selectStoreForDataAccess();
    this.commitId = this.commitId + 1;
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the store buffer
   */
  @Override
  public void reset()
  {
    this.commitId = 0;
    this.storeQueue.clear();
    this.storeMap.clear();
    this.loadStoreInterpreter.resetMemory();
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Pulls all store instructions from decode into the buffer
   */
  private void pullStoreInstructionsFromDecode()
  {
    decodeAndDispatchBlock.getAfterRenameCodeList().forEach(codeModel ->
                                                            {
                                                              if (codeModel.isStore())
                                                              {
                                                                boolean isPresent = this.reorderBufferBlock.getRobItem(
                                                                        codeModel.getIntegerId()) != null;
                                                                if (isBufferFull(1) || !isPresent)
                                                                {
                                                                  return;
                                                                }
                                                                this.storeQueue.add(codeModel);
                                                                InputCodeArgument argument = codeModel.getArgumentByName(
                                                                        "rs2");
                                                                this.storeMap.put(codeModel.getIntegerId(),
                                                                                  new StoreBufferItem(
                                                                                          Objects.requireNonNull(
                                                                                                  argument).getValue(),
                                                                                          codeModel.getIntegerId()));
                                                              }
                                                            });
  }// end of pullStoreInstructionsFromDecode
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes all invalid store instructions from buffer
   */
  private void removeInvalidInstructions()
  {
    List<SimCodeModel> removedInstructions = new ArrayList<>();
    for (SimCodeModel simCodeModel : this.storeQueue)
    {
      if (simCodeModel.hasFailed())
      {
        removedInstructions.add(simCodeModel);
        if (this.storeMap.get(simCodeModel.getIntegerId()).isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(simCodeModel));
        }
        this.storeMap.remove(simCodeModel.getIntegerId());
      }
    }
    this.storeQueue.removeAll(removedInstructions);
  }// end of removeInvalidInstructions
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Checks if store source registers are ready and if yes then marks them
   */
  private void updateMapValues()
  {
    this.storeMap.forEach((string, item) ->
                          {
                            RegisterReadinessEnum state = registerFileBlock.getRegister(item.getSourceRegister())
                                    .getReadiness();
                            item.setSourceReady(
                                    state == RegisterReadinessEnum.kExecuted || state == RegisterReadinessEnum.kAssigned);
                          });
  }// end of updateMapValues
  //----------------------------------------------------------------------
  
  /**
   * @brief Selects store instructions for MA block
   */
  private void selectStoreForDataAccess()
  {
    SimCodeModel codeModel = null;
    for (SimCodeModel simCodeModel : this.storeQueue)
    {
      StoreBufferItem item = this.storeMap.get(simCodeModel.getIntegerId());
      
      //If there is store without address computed stop - there could be WaW hazard
      if (item.getAddress() == -1)
      {
        break;
      }
      
      assert !simCodeModel.hasFailed();
      
      boolean isSpeculative    = reorderBufferBlock.getRobItem(
              simCodeModel.getIntegerId()).reorderFlags.isSpeculative();
      boolean isAvailableForMA = item.getAddress() != -1 && !item.isAccessingMemory() && item.getAccessingMemoryId() == -1 && item.isSourceReady() && !isSpeculative;
      if (isAvailableForMA)
      {
        for (SimCodeModel previousStore : this.storeQueue)
        {
          //Check if we haven't reached current statement
          if (simCodeModel.getIntegerId() == previousStore.getIntegerId())
          {
            break;
          }
          else if ((this.storeMap.get(previousStore.getIntegerId()).getAddress() & ~3L) == (item.getAddress() & ~3L))
          {
            //If there is WaW hazard - stop
            isAvailableForMA = false;
            break;
          }
        }
      }
      if (isAvailableForMA)
      {
        if (codeModel == null)
        {
          codeModel = simCodeModel;
        }
        else if (codeModel.compareTo(simCodeModel) > 0)
        {
          codeModel = simCodeModel;
        }
      }
    }
    
    if (codeModel == null)
    {
      return;
    }
    
    for (MemoryAccessUnit memoryAccessUnit : this.memoryAccessUnitList)
    {
      if (memoryAccessUnit.isFunctionUnitEmpty())
      {
        memoryAccessUnit.resetCounter();
        memoryAccessUnit.setSimCodeModel(codeModel);
        this.storeMap.get(codeModel.getIntegerId()).setAccessingMemory(true);
        this.storeMap.get(codeModel.getIntegerId()).setAccessingMemoryId(this.commitId);
        // todo: return here??
      }
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] possibleAddition - Number of instructions to be possibly added
   *
   * @return True if buffer would be full, false otherwise
   * @brief Checks if buffer would be full if specified number of instructions were to be added
   */
  public boolean isBufferFull(int possibleAddition)
  {
    return this.bufferSize < (this.storeQueue.size() + possibleAddition);
  }// end of isBufferFull
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id identifying specific storeMap entry
   * @param [in] address     - Store instruction address
   *
   * @brief Set Store address
   */
  public void setAddress(int codeModelId, long address)
  {
    this.storeMap.get(codeModelId).setAddress(address);
  }// end of setAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] bufferSize - New store buffer size
   *
   * @brief Set store buffer limit size
   */
  public void setBufferSize(int bufferSize)
  {
    this.bufferSize = bufferSize;
  }// end of setBufferSize
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id identifying specific loadMap entry
   *
   * @brief Set flag marking if the instruction is in the MA block
   */
  public void setMemoryAccessFinished(int codeModelId)
  {
    this.storeMap.get(codeModelId).setAccessingMemory(false);
  }// end of setMemoryAccessFinished
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return First instruction in queue
   * @brief Get first instruction in queue
   */
  public SimCodeModel getStoreQueueFirst()
  {
    return storeQueue.peek();
  }// end of getStoreQueueFirst
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Release store instruction on top of the queue (instruction has been committed)
   */
  public void releaseStoreFirst()
  {
    if (!this.storeQueue.isEmpty())
    {
      SimCodeModel codeModel = storeQueue.poll();
      this.storeMap.remove(codeModel.getIntegerId());
    }
    else
    {
      throw new RuntimeException("Release store when store queue is empty");
    }
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
   * @return Store map
   * @brief Get whole store map
   */
  public Map<Integer, StoreBufferItem> getStoreMap()
  {
    return storeMap;
  }// end of getStoreMap
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return List of store map values
   * @brief Get all store map values as list
   */
  List<StoreBufferItem> getStoreMapAsList()
  {
    return new ArrayList<>(this.storeMap.values());
  }// end of updateMapValues
  //-------------------------------------------------------------------------------------------
}
