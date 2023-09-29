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

import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.DecodeAndDispatchBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.StoreBufferItem;

import java.util.*;

/**
 * @class StoreBufferBlock
 * @brief Class that holds all in-flight store instructions
 */
public class StoreBufferBlock implements AbstractBlock
{
  /// Queue with all uncommitted store instructions
  private final Queue<SimCodeModel> storeQueue;
  /// Map with additional infos for specific store instructions
  private final Map<Integer, StoreBufferItem> storeMap;
  /// Stack to save released additional instruction info
  private final Stack<StoreBufferItem> flagsStack;
  /// Stack to save released instructions
  private final Stack<SimCodeModel> releaseStack;
  /// Counter, which is used to calculate if buffer can hold instructions pulled into ROB
  public int possibleNewEntries;
  /// List holding all allocated memory access units
  private final List<MemoryAccessUnit> memoryAccessUnitList;
  /// Interpreter for processing load store instructions
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  /// Class, which simulates instruction decode and renames registers
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  /// Initial loader of interpretable instructions and register files
  private InitLoader initLoader;
  /// Class contains simulated implementation of Reorder buffer
  private ReorderBufferBlock reorderBufferBlock;
  /// Store Buffer size
  private int bufferSize;
  /// Id counter matching the one in ROB
  private int commitId;
  
  public StoreBufferBlock()
  {
    this.bufferSize = 64;
    this.commitId   = 0;
    
    this.storeQueue   = new PriorityQueue<>();
    this.storeMap     = new LinkedHashMap<>();
    this.releaseStack = new Stack<>();
    this.flagsStack   = new Stack<>();
    
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
                          InitLoader initLoader,
                          ReorderBufferBlock reorderBufferBlock)
  {
    this.loadStoreInterpreter   = loadStoreInterpreter;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.registerFileBlock      = registerFileBlock;
    this.initLoader             = initLoader;
    this.reorderBufferBlock     = reorderBufferBlock;
    this.bufferSize             = 64;
    this.commitId               = 0;
    
    this.storeQueue   = new PriorityQueue<>();
    this.storeMap     = new LinkedHashMap<>();
    this.releaseStack = new Stack<>();
    this.flagsStack   = new Stack<>();
    
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
   * @brief Resets the all the lists/stacks/variables in the store buffer
   */
  @Override
  public void reset()
  {
    this.commitId = 0;
    this.storeQueue.clear();
    this.storeMap.clear();
    this.releaseStack.clear();
    this.flagsStack.clear();
    this.loadStoreInterpreter.resetMemory();
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] possibleAddition - Number of instructions to be possibly added
   *
   * @return True if buffer would be full, false otherwise
   * @brief Checks if buffer would be full if specified number of instructions were to be added
   */
  public boolean isBufferFull(int possibleAddition)
  {
    return this.bufferSize < (this.storeQueue.size() + possibleAddition + this.possibleNewEntries);
  }// end of isBufferFull
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Increment possible entries that were confirmed to the ROB
   */
  public void incrementPossibleNewEntries()
  {
    this.possibleNewEntries++;
  }// end of incrementPossibleNewEntries
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulates store buffer
   */
  @Override
  public void simulate()
  {
    this.possibleNewEntries = 0;
    pullStoreInstructionsFromDecode();
    removeInvalidInstructions();
    updateMapValues();
    selectStoreForDataAccess();
    this.commitId = this.commitId + 1;
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulates backwards the store buffer
   */
  @Override
  public void simulateBackwards()
  {
    this.possibleNewEntries = 0;
    this.commitId           = this.commitId == 0 ? 0 : this.commitId - 1;
    while (!this.releaseStack.isEmpty() && this.releaseStack.peek().getCommitId() == this.commitId)
    {
      SimCodeModel codeModel = this.releaseStack.pop();
      this.storeQueue.add(codeModel);
      this.storeMap.put(codeModel.getId(), this.flagsStack.pop());
    }
    removeInstructionsInDecodeBlock();
    updateMapValues();
    pullFromMA();
  }// end of simulateBackwards
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
   * @return Store buffer queue
   * @brief Get whole Store buffer queue
   */
  public Queue<SimCodeModel> getStoreQueue()
  {
    return storeQueue;
  }// end of getStoreQueue
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
  //----------------------------------------------------------------------
  
  /**
   * @return Store buffer limit size
   * @brief Get Store buffer limit size
   */
  public int getBufferSize()
  {
    return bufferSize;
  }// end of getBufferSize
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
   * @brief Release store instruction on top of the queue and commits it
   */
  public void releaseStoreFirst()
  {
    if (!this.storeQueue.isEmpty())
    {
      SimCodeModel codeModel = storeQueue.poll();
      this.releaseStack.add(codeModel);
      this.flagsStack.add(this.storeMap.get(codeModel.getId()));
      
      this.storeMap.remove(codeModel.getId());
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
  //-------------------------------------------------------------------------------------------
  
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
   * @brief Pull all viable load instructions from MA
   */
  private void pullFromMA()
  {
    for (AbstractFunctionUnitBlock memoryAccessUnit : this.memoryAccessUnitList)
    {
      if (!memoryAccessUnit.isFunctionUnitEmpty() && memoryAccessUnit.hasReversedDelayPassed() && this.storeMap.containsKey(
          memoryAccessUnit.getSimCodeModel().getId()) && this.storeMap.get(memoryAccessUnit.getSimCodeModel().getId())
                                                                      .getAccessingMemoryId() == this.commitId)
      {
        SimCodeModel codeModel = memoryAccessUnit.getSimCodeModel();
        memoryAccessUnit.setSimCodeModel(null);
        StoreBufferItem item = this.storeMap.get(codeModel.getId());
        item.setMemoryAccessId(-1);
        item.setAccessingMemoryId(-1);
        item.setAccessingMemory(false);
      }
    }
  }// end of pullFromMA
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Pulls all store instructions from decode into the buffer
   */
  private void pullStoreInstructionsFromDecode()
  {
    decodeAndDispatchBlock.getAfterRenameCodeList().forEach(codeModel ->
                                                            {
                                                              if (isInstructionStore(codeModel))
                                                              {
                                                                if (isBufferFull(
                                                                    1) || !this.reorderBufferBlock.getFlagsMap()
                                                                                                  .containsKey(
                                                                                                      codeModel.getId()))
                                                                {
                                                                  return;
                                                                }
                                                                this.storeQueue.add(codeModel);
                                                                InputCodeArgument argument =
                                                                    codeModel.getArgumentByName(
                                                                    "rs2");
                                                                this.storeMap.put(codeModel.getId(),
                                                                                  new StoreBufferItem(
                                                                                      Objects.requireNonNull(argument)
                                                                                             .getValue(),
                                                                                      codeModel.getId()));
                                                              }
                                                            });
  }// end of pullStoreInstructionsFromDecode
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
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Selects store instructions for MA block
   */
  private void selectStoreForDataAccess()
  {
    SimCodeModel codeModel = null;
    for (SimCodeModel simCodeModel : this.storeQueue)
    {
      StoreBufferItem item = this.storeMap.get(simCodeModel.getId());
      
      //If there is store without address computed stop - there could be WaW hazard
      if (item.getAddress() == -1)
      {
        break;
      }
      
      boolean isAvailableForMA =
          item.getAddress() != -1 && !item.isAccessingMemory() && item.getAccessingMemoryId() == -1 && item.isSourceReady() && !reorderBufferBlock.getFlagsMap()
                                                                                                                                                                         .get(
                                                                                                                                                                             simCodeModel.getId())
                                                                                                                                                                         .isSpeculative();
      if (isAvailableForMA)
      {
        for (SimCodeModel previousStore : this.storeQueue)
        {
          //Check if we haven't reached current statement
          if (simCodeModel.getId() == previousStore.getId())
          {
            break;
          }
          else if ((this.storeMap.get(previousStore.getId()).getAddress() & ~3L) == (item.getAddress() & ~3L))
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
        this.storeMap.get(codeModel.getId()).setAccessingMemory(true);
        this.storeMap.get(codeModel.getId()).setAccessingMemoryId(this.commitId);
      }
    }
  }// end of selectLoadForDataAccess
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
        this.releaseStack.add(simCodeModel);
        this.flagsStack.add(this.storeMap.get(simCodeModel.getId()));
        if (this.storeMap.get(simCodeModel.getId()).isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(simCodeModel));
        }
        this.storeMap.remove(simCodeModel.getId());
      }
    }
    this.storeQueue.removeAll(removedInstructions);
  }// end of removeInvalidInstructions
  
  /**
   * @return List of store map values
   * @brief Get all store map values as list
   */
  List<StoreBufferItem> getStoreMapAsList()
  {
    return new ArrayList<>(this.storeMap.values());
  }// end of updateMapValues
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes instructions, which this.possibleNewEntries = 0;are currently in decode block
   */
  private void removeInstructionsInDecodeBlock()
  {
    for (SimCodeModel instruction : this.decodeAndDispatchBlock.getAfterRenameCodeList())
    {
      this.storeQueue.remove(instruction);
      this.storeMap.remove(instruction.getId());
    }
  }// end of removeInstructionsInDecodeBlock
  //----------------------------------------------------------------------
  
  /**
   * @return List of memory access unit blocks
   * @brief Get list of memory access units
   */
  public List<MemoryAccessUnit> getMemoryAccessBlockList()
  {
    return this.memoryAccessUnitList;
  }// end of getMemoryAccessBlockList
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModel - Code model to be checked
   *
   * @return True if the model is store instruction, false otherwise
   * @brief Checks if specified code model is store instruction
   */
  public boolean isInstructionStore(SimCodeModel codeModel)
  {
    if (codeModel.getInstructionTypeEnum() != InstructionTypeEnum.kLoadstore)
    {
      return false;
    }
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    
    return instruction != null && instruction.getInterpretableAs().startsWith("store");
    
  }// end of isInstructionStore
  //----------------------------------------------------------------------
}
