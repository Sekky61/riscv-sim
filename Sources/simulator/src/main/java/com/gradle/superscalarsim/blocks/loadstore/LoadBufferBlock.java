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

import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.code.CodeLoadStoreInterpreter;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;

import java.util.*;

/**
 * @class LoadBufferBlock
 * @brief Class that holds all in-flight load instructions
 */
public class LoadBufferBlock implements AbstractBlock
{
  /// Queue with all uncommitted load instructions
  private final Queue<SimCodeModel> loadQueue;
  /**
   * Map with additional infos for specific load instructions
   * The actual buffer of the load buffer
   */
  private final Map<Integer, LoadBufferItem> loadMap;
  /// Stack to save released additional instruction info
  private final Stack<LoadBufferItem> flagsStack;
  /// Stack to save released instructions
  private final Stack<SimCodeModel> releaseStack;
  /// Counter, which is used to calculate if buffer can hold instructions pulled into ROB
  public int possibleNewEntries;
  /// List holding all allocated memory access units
  private List<MemoryAccessUnit> memoryAccessUnitList;
  /// Interpreter for processing load store instructions
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  /// Block keeping all in-flight store instructions
  private StoreBufferBlock storeBufferBlock;
  /// Class, which simulates instruction decode and renames registers
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /// Initial loader of interpretable instructions and register files
  private InitLoader initLoader;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  /// Class contains simulated implementation of Reorder buffer
  private ReorderBufferBlock reorderBufferBlock;
  /// Class that fetches code from CodeParser
  private InstructionFetchBlock instructionFetchBlock;
  /// Load Buffer size
  private int bufferSize;
  /// Id counter matching the one in ROB
  private int commitId;
  
  public LoadBufferBlock()
  {
    loadQueue    = new PriorityQueue<>();
    loadMap      = new LinkedHashMap<>();
    releaseStack = new Stack<>();
    flagsStack   = new Stack<>();
  }
  
  /**
   * @param [in] blockScheduleTask      - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] loadStoreInterpreter   - Interpreter for processing load store instructions
   * @param [in] storeBufferBlock       - Block keeping all in-flight store instructions
   * @param [in] decodeAndDispatchBlock - Class, which simulates instruction decode and renames registers
   * @param [in] registerFileBlock      - Class containing all registers, that simulator uses
   * @param [in] initLoader             - Initial loader of interpretable instructions and register files
   * @param [in] reorderBufferBlock     - Class contains simulated implementation of Reorder buffer
   * @param [in] instructionFetchBlock  - Class that fetches code from CodeParser
   *
   * @brief Constructor
   */
  public LoadBufferBlock(CodeLoadStoreInterpreter loadStoreInterpreter,
                         StoreBufferBlock storeBufferBlock,
                         DecodeAndDispatchBlock decodeAndDispatchBlock,
                         UnifiedRegisterFileBlock registerFileBlock,
                         InitLoader initLoader,
                         ReorderBufferBlock reorderBufferBlock,
                         InstructionFetchBlock instructionFetchBlock)
  {
    this.loadStoreInterpreter   = loadStoreInterpreter;
    this.storeBufferBlock       = storeBufferBlock;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.registerFileBlock      = registerFileBlock;
    this.initLoader             = initLoader;
    this.reorderBufferBlock     = reorderBufferBlock;
    this.instructionFetchBlock  = instructionFetchBlock;
    this.bufferSize             = 64;
    this.commitId               = 0;
    this.possibleNewEntries     = 0;
    
    this.loadQueue    = new PriorityQueue<>();
    this.loadMap      = new LinkedHashMap<>();
    this.releaseStack = new Stack<>();
    this.flagsStack   = new Stack<>();
    
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
   * @param [in] loadStoreFunctionUnitList - list of new memory access units
   *
   * @brief Adds list of memory access units to the Load buffer block
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
   * @brief Resets the all the lists/stacks/variables in the load buffer
   */
  @Override
  public void reset()
  {
    this.commitId = 0;
    this.loadQueue.clear();
    this.loadMap.clear();
    this.releaseStack.clear();
    this.flagsStack.clear();
    this.possibleNewEntries = 0;
  }// end of reset
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
      this.releaseStack.add(codeModel);
      this.flagsStack.add(this.loadMap.get(codeModel.getId()));
      
      this.loadMap.remove(codeModel.getId());
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
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Checks if any load has failed and if yes, reset PC counter to its correct position
   */
  private void resetPcIfFailure()
  {
    if (this.loadQueue.isEmpty())
    {
      return;
    }
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      LoadBufferItem item = this.loadMap.get(simCodeModel.getId());
      if (simCodeModel.hasFailed() && item.getMemoryFailedId() == simCodeModel.getCommitId())
      {
        item.setMemoryFailedId(-1);
        int savedPc          = simCodeModel.getSavedPc();
        int currentPcCounter = instructionFetchBlock.getPcCounter();
        instructionFetchBlock.setPcCounter(savedPc);
        // TODO: might be unnecessary
        simCodeModel.setSavedPc(currentPcCounter);
      }
    }
  }// end of resetPcIfFailure
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Pull all viable load instructions from MA
   */
  private void pullFromMA()
  {
    for (AbstractFunctionUnitBlock memoryAccessUnit : this.memoryAccessUnitList)
    {
      if (!memoryAccessUnit.isFunctionUnitEmpty() && memoryAccessUnit.hasReversedDelayPassed() && this.loadMap.containsKey(
              memoryAccessUnit.getSimCodeModel().getId()) && this.loadMap.get(
              memoryAccessUnit.getSimCodeModel().getId()).getAccessingMemoryId() == this.commitId)
      {
        SimCodeModel codeModel = memoryAccessUnit.getSimCodeModel();
        memoryAccessUnit.setSimCodeModel(null);
        LoadBufferItem item = this.loadMap.get(codeModel.getId());
        item.setMemoryAccessId(-1);
        item.setAccessingMemoryId(-1);
        item.setAccessingMemory(false);
        
      }
    }
  }// end of pullFromMA
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
                                                              if (isBufferFull(
                                                                      1) || !this.reorderBufferBlock.getFlagsMap()
                                                                      .containsKey(codeModel.getId()))
                                                              {
                                                                return;
                                                              }
                                                              this.loadQueue.add(codeModel);
                                                              // Create entry in the Load Buffer
                                                              InputCodeArgument argument = codeModel.getArgumentByName(
                                                                      "rd");
                                                              this.loadMap.put(codeModel.getId(), new LoadBufferItem(
                                                                      Objects.requireNonNull(argument).getValue()));
                                                              
                                                            });
  }// end of pullLoadInstructionsFromDecode
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Selects load instructions for MA block
   */
  private void selectLoadForDataAccess()
  {
    SimCodeModel codeModel = null;
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      LoadBufferItem item             = this.loadMap.get(simCodeModel.getId());
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
        this.loadMap.get(codeModel.getId()).setAccessingMemory(true);
        this.loadMap.get(codeModel.getId()).setAccessingMemoryId(this.commitId);
      }
    }
  }// end of selectLoadForDataAccess
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] simCodeModel - load instruction to be matched
   *
   * @return Null if successfully matched, input if failed
   * @brief Check if load instruction can be matched with previously executed store instruction
   */
  private SimCodeModel processLoadInstruction(SimCodeModel simCodeModel)
  {
    if (reorderBufferBlock.getFlagsMap().get(simCodeModel.getId()) == null)
    {
      //If current instruction has been flushed from reorder buffer stop computing it
      return simCodeModel;
    }
    LoadBufferItem  loadItem        = this.loadMap.get(simCodeModel.getId());
    StoreBufferItem resultStoreItem = null;
    for (StoreBufferItem storeItem : this.storeBufferBlock.getStoreMapAsList())
    {
      if (loadItem.getAddress() == storeItem.getAddress() && simCodeModel.getId() > storeItem.getSourceResultId())
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
    loadMap.get(simCodeModel.getId()).setDestinationReady(true);
    loadMap.get(simCodeModel.getId()).setHasBypassed(true);
    loadMap.get(simCodeModel.getId()).setMemoryAccessId(this.commitId);
    reorderBufferBlock.getFlagsMap().get(simCodeModel.getId()).setBusy(false);
    return null;
    
  }// end of processLoadInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Checks if the instruction has no conflicts with all previously executed store instructions
   */
  private void checkIfProcessedHasConflict()
  {
    
    for (SimCodeModel simCodeModel : this.loadQueue)
    {
      LoadBufferItem bufferItem        = loadMap.get(simCodeModel.getId());
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
      instructionFetchBlock.setPcCounter(simCodeModel.getSavedPc());
      simCodeModel.setSavedPc(instructionFetchBlock.getPcCounter());
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
        this.releaseStack.add(simCodeModel);
        this.flagsStack.add(this.loadMap.get(simCodeModel.getId()));
        if (this.loadMap.get(simCodeModel.getId()).isAccessingMemory())
        {
          this.memoryAccessUnitList.forEach(ma -> ma.tryRemoveCodeModel(simCodeModel));
        }
        this.loadMap.remove(simCodeModel.getId());
      }
    }
    this.loadQueue.removeAll(removedInstructions);
  }// end of removeInvalidInstructions
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Removes instructions, which are currently in decode block
   */
  private void removeInstructionsInDecodeBlock()
  {
    for (SimCodeModel instruction : this.decodeAndDispatchBlock.getAfterRenameCodeList())
    {
      this.loadQueue.remove(instruction);
      this.loadMap.remove(instruction.getId());
    }
  }// end of removeInstructionsInDecodeBlock
  //----------------------------------------------------------------------
  
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
   * @brief Revert all bypasses that happened at specified id
   */
  private void revertAllBypasses()
  {
    for (SimCodeModel codeModel : this.loadQueue)
    {
      LoadBufferItem item = this.loadMap.get(codeModel.getId());
      if (item.hasBypassed() && item.getMemoryAccessId() == this.commitId)
      {
        item.setMemoryAccessId(-1);
        item.setDestinationReady(false);
        item.setHasBypassed(false);
        registerFileBlock.getRegister(item.getDestinationRegister()).setReadiness(RegisterReadinessEnum.kAllocated);
        if (reorderBufferBlock.getFlagsMap().containsKey(codeModel.getId()))
        {
          reorderBufferBlock.getFlagsMap().get(codeModel.getId()).setBusy(true);
        }
      }
    }
  }// end of revertAllBypasses
  //------------------------------------------------------------------getBufferSize-------------------------
  
  /**
   * @brief Notifies all listeners that number of MA units has changed
   */
  public StoreBufferBlock getStoreBufferBlock()
  {
    return storeBufferBlock;
  }// end of getStoreBufferBlock
  //-------------------------------------------------------------------------------------------
}
