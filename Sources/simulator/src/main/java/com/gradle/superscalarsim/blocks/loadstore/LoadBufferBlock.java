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
import com.gradle.superscalarsim.blocks.base.DecodeAndDispatchBlock;
import com.gradle.superscalarsim.blocks.base.InstructionFetchBlock;
import com.gradle.superscalarsim.blocks.base.ReorderBufferBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.*;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.*;

/**
 * @class LoadBufferBlock
 * @brief Class that holds all in-flight load instructions
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class LoadBufferBlock implements AbstractBlock
{
  /// Queue with all uncommitted load instructions
  @JsonIdentityReference(alwaysAsId = true)
  private final Queue<SimCodeModel> loadQueue;
  /**
   * Map with additional info for specific load instructions
   * The actual buffer of the load buffer
   */
  private final Map<Integer, LoadBufferItem> loadMap;
  /// Counter, which is used to calculate if buffer can hold instructions pulled into ROB
  public int possibleNewEntries;
  /// List holding all allocated memory access units
  @JsonIdentityReference(alwaysAsId = true)
  private List<MemoryAccessUnit> memoryAccessUnitList;
  /// Block keeping all in-flight store instructions
  @JsonIdentityReference(alwaysAsId = true)
  private StoreBufferBlock storeBufferBlock;
  /// Class, which simulates instruction decode and renames registers
  @JsonIdentityReference(alwaysAsId = true)
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /// Class containing all registers, that simulator uses
  @JsonIdentityReference(alwaysAsId = true)
  private UnifiedRegisterFileBlock registerFileBlock;
  /// Class contains simulated implementation of Reorder buffer
  @JsonIdentityReference(alwaysAsId = true)
  private ReorderBufferBlock reorderBufferBlock;
  /// Class that fetches code from CodeParser
  @JsonIdentityReference(alwaysAsId = true)
  private InstructionFetchBlock instructionFetchBlock;
  /// Load Buffer size
  private int bufferSize;
  /// ID counter matching the one in ROB
  private int commitId;
  
  public LoadBufferBlock()
  {
    loadQueue = new PriorityQueue<>();
    loadMap   = new LinkedHashMap<>();
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
                         DecodeAndDispatchBlock decodeAndDispatchBlock,
                         UnifiedRegisterFileBlock registerFileBlock,
                         ReorderBufferBlock reorderBufferBlock,
                         InstructionFetchBlock instructionFetchBlock)
  {
    this.storeBufferBlock       = storeBufferBlock;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.registerFileBlock      = registerFileBlock;
    this.reorderBufferBlock     = reorderBufferBlock;
    this.instructionFetchBlock  = instructionFetchBlock;
    this.bufferSize             = 64;
    this.commitId               = 0;
    this.possibleNewEntries     = 0;
    
    this.loadQueue = new PriorityQueue<>();
    this.loadMap   = new LinkedHashMap<>();
    
    this.memoryAccessUnitList = new ArrayList<>();
    
    this.reorderBufferBlock.setLoadBufferBlock(this);
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] memoryAccessUnit - Memory access unit to be added
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
   * @return Load buffer limit size
   * @brief Get the load buffer limit size
   */
  public int getBufferSize()
  {
    return bufferSize;
  }// end of getBufferSize
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] bufferSize - New load buffer size
   *
   * @brief Set load buffer limit size
   */
  public void setBufferSize(int bufferSize)
  {
    this.bufferSize = bufferSize;
  }// end of setBufferSize
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Store buffer queue
   * @brief Get whole store buffer queue
   */
  public Queue<SimCodeModel> getLoadQueue()
  {
    return this.loadQueue;
  }// end of getLoadQueue
  //-------------------------------------------------------------------------------------------
  
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
   * @brief Increment possible entries that were confirmed to the ROB
   */
  public void incrementPossibleNewEntries()
  {
    this.possibleNewEntries++;
  }// end of incrementPossibleNewEntries
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulates Load buffer
   */
  @Override
  public void simulate()
  {
    this.possibleNewEntries = 0;
    pullLoadInstructionsFromDecode();
    checkIfProcessedHasConflict();
    removeInvalidInstructions();
    selectLoadForDataAccess();
    this.commitId = this.commitId + 1;
  }// end of simulate
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the load buffer
   */
  @Override
  public void reset()
  {
    this.commitId = 0;
    this.loadQueue.clear();
    this.loadMap.clear();
    this.possibleNewEntries = 0;
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Pulls all load instructions from decode into the buffer.
   * Creates entry for them in the load buffer.
   */
  private void pullLoadInstructionsFromDecode()
  {
    decodeAndDispatchBlock.getAfterRenameCodeList().forEach(codeModel ->
                                                            {
                                                              if (!isInstructionLoad(codeModel))
                                                              {
                                                                return;
                                                              }
                                                              boolean containsKey = this.reorderBufferBlock.getRobItem(
                                                                      codeModel.getIntegerId()) != null;
                                                              if (isBufferFull(1) || !containsKey)
                                                              {
                                                                return;
                                                              }
                                                              this.loadQueue.add(codeModel);
                                                              // Create entry in the Load Buffer
                                                              InputCodeArgument argument = codeModel.getArgumentByName(
                                                                      "rd");
                                                              this.loadMap.put(codeModel.getIntegerId(),
                                                                               new LoadBufferItem(
                                                                                       Objects.requireNonNull(argument)
                                                                                               .getValue()));
                                                              
                                                            });
  }// end of pullLoadInstructionsFromDecode
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Checks if the instruction has no conflicts with all previously executed store instructions
   */
  private void checkIfProcessedHasConflict()
  {
    
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      LoadBufferItem bufferItem        = loadMap.get(simCodeModel.getIntegerId());
      boolean        isDestReady       = bufferItem.isDestinationReady();
      boolean        isAccessingMemory = bufferItem.isAccessingMemory();
      if (!isDestReady && !isAccessingMemory)
      {
        continue;
      }
      int     beforeMaId   = bufferItem.getMemoryAccessId();
      boolean beforeBypass = bufferItem.hasBypassed();
      if (processLoadInstruction(simCodeModel) != null)
      {
        continue;
      }
      
      // Remove the load and anything after it from ROB
      reorderBufferBlock.invalidateInstructions(simCodeModel);
      reorderBufferBlock.flushInvalidInstructions();
      bufferItem.setMemoryFailedId(this.commitId);
      bufferItem.setMemoryAccessId(beforeMaId);
      bufferItem.setHasBypassed(beforeBypass);
      // Fix PC
      instructionFetchBlock.setPc(simCodeModel.getSavedPc());
      simCodeModel.setSavedPc(instructionFetchBlock.getPc());
    }
  }// end of checkIfProcessedHasConflict
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes all invalid load instructions from buffer
   */
  private void removeInvalidInstructions()
  {
    List<SimCodeModel> removedInstructions = new ArrayList<>();
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      if (simCodeModel.hasFailed())
      {
        removedInstructions.add(simCodeModel);
        if (this.loadMap.get(simCodeModel.getIntegerId()).isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(simCodeModel));
        }
        this.loadMap.remove(simCodeModel.getIntegerId());
      }
    }
    this.loadQueue.removeAll(removedInstructions);
  }// end of removeInvalidInstructions
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Selects load instructions for MA block
   */
  private void selectLoadForDataAccess()
  {
    SimCodeModel codeModel = null;
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      LoadBufferItem item             = this.loadMap.get(simCodeModel.getIntegerId());
      boolean        isAvailableForMA = item.getAddress() != -1 && !item.isAccessingMemory() && !item.isDestinationReady();
      
      if (isAvailableForMA)
      {
        SimCodeModel possibleCodeModel = processLoadInstruction(simCodeModel);
        if (codeModel == null)
        {
          codeModel = possibleCodeModel;
        }
        else if (possibleCodeModel != null && codeModel.compareTo(possibleCodeModel) > 0)
        {
          codeModel = possibleCodeModel;
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
        this.loadMap.get(codeModel.getIntegerId()).setAccessingMemory(true);
        this.loadMap.get(codeModel.getIntegerId()).setAccessingMemoryId(this.commitId);
      }
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModel - Code model to be checked
   *
   * @return True if the model is load instruction, false otherwise
   * @brief Checks if specified code model is load instruction
   */
  public boolean isInstructionLoad(SimCodeModel codeModel)
  {
    if (codeModel.getInstructionTypeEnum() != InstructionTypeEnum.kLoadstore)
    {
      return false;
    }
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    return instruction != null && instruction.getInterpretableAs().startsWith("load");
  }// end of isInstructionLoad
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] possibleAddition - Number of instructions to be possibly added
   *
   * @return True if buffer would be full, false otherwise
   * @brief Checks if buffer would be full if specified number of instructions were to be added
   */
  public boolean isBufferFull(int possibleAddition)
  {
    return this.bufferSize < (this.loadQueue.size() + possibleAddition + this.possibleNewEntries);
  }// end of isBufferFull
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] simCodeModel - load instruction to be matched
   *
   * @return Null if successfully matched, input if failed
   * @brief Check if load instruction can be matched with previously executed store instruction
   */
  private SimCodeModel processLoadInstruction(SimCodeModel simCodeModel)
  {
    ReorderBufferItem robItem = this.reorderBufferBlock.getRobItem(simCodeModel.getIntegerId());
    if (robItem == null)
    {
      //If current instruction has been flushed from reorder buffer stop computing it
      return simCodeModel;
    }
    LoadBufferItem  loadItem        = this.loadMap.get(simCodeModel.getIntegerId());
    StoreBufferItem resultStoreItem = null;
    for (StoreBufferItem storeItem : this.storeBufferBlock.getStoreMapAsList())
    {
      if (loadItem.getAddress() == storeItem.getAddress() && simCodeModel.getIntegerId() > storeItem.getSourceResultId())
      {
        boolean isNewItemBetter = resultStoreItem == null || (storeItem.getSourceResultId() < resultStoreItem.getSourceResultId() && storeItem.getAddress() == loadItem.getAddress());
        resultStoreItem = isNewItemBetter ? storeItem : resultStoreItem;
      }
    }
    
    if (resultStoreItem == null)
    {
      return simCodeModel;
    }
    RegisterModel         sourceReg   = registerFileBlock.getRegister(resultStoreItem.getSourceRegister());
    RegisterReadinessEnum resultState = sourceReg.getReadiness();
    
    boolean storeSourceReady = resultState == RegisterReadinessEnum.kExecuted || resultState == RegisterReadinessEnum.kAssigned;
    if (!storeSourceReady)
    {
      // Cannot speculatively load, the value is not computed yet
      return null;
    }
    
    // Bypass memory access by speculation
    RegisterModel destinationReg = registerFileBlock.getRegister(loadItem.getDestinationRegister());
    destinationReg.setValue(sourceReg.getValue());
    destinationReg.setReadiness(RegisterReadinessEnum.kAssigned);
    loadMap.get(simCodeModel.getIntegerId()).setDestinationReady(true);
    loadMap.get(simCodeModel.getIntegerId()).setHasBypassed(true);
    loadMap.get(simCodeModel.getIntegerId()).setMemoryAccessId(this.commitId);
    reorderBufferBlock.getRobItem(simCodeModel.getIntegerId()).reorderFlags.setBusy(false);
    return null;
    
  }// end of processLoadInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id identifying specific loadMap entry
   * @param [in] address     - Load instruction address
   *
   * @brief Set load address
   */
  public void setAddress(int codeModelId, long address)
  {
    this.loadMap.get(codeModelId).setAddress(address);
  }// end of setAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id identifying specific loadMap entry
   *
   * @brief Set flag if the destination register of the load instruction is ready to be loaded into
   */
  public void setDestinationAvailable(int codeModelId)
  {
    this.loadMap.get(codeModelId).setDestinationReady(true);
  }// end of setCodeRegisterAvailable
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id identifying specific loadMap entry
   *
   * @brief Set flag marking if the instruction is in the MA block
   */
  public void setMemoryAccessFinished(int codeModelId)
  {
    this.loadMap.get(codeModelId).setAccessingMemory(false);
  }// end of setMemoryAccessFinished
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return First instruction in queue
   * @brief Get first instruction in queue
   */
  public SimCodeModel getLoadQueueFirst()
  {
    return loadQueue.peek();
  }// end of getLoadQueueFirst
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Release load instruction on top of the queue and commits it
   */
  public void releaseLoadFirst()
  {
    if (!this.loadQueue.isEmpty())
    {
      SimCodeModel codeModel = loadQueue.poll();
      
      this.loadMap.remove(codeModel.getIntegerId());
    }
    else
    {
      throw new RuntimeException("Release store when load queue is empty");
    }
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
  
  /**
   * @return Load map
   * @brief Get whole load map
   */
  public Map<Integer, LoadBufferItem> getLoadMap()
  {
    return this.loadMap;
  }// end of getLoadMap
  //----------------------------------------------------------------------
  
  /**
   * @brief Notifies all listeners that number of MA units has changed
   */
  public StoreBufferBlock getStoreBufferBlock()
  {
    return storeBufferBlock;
  }// end of getStoreBufferBlock
  //-------------------------------------------------------------------------------------------
}
