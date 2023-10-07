/**
 * @file ReorderBufferBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class with Reorder buffer logic
 * @date 3  February   2021 16:00 (created) \n
 * 27 April      2021 20:00 (revised)
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
package com.gradle.superscalarsim.blocks.base;

import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.StatisticsCounter;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.blocks.loadstore.LoadBufferBlock;
import com.gradle.superscalarsim.blocks.loadstore.StoreBufferBlock;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @class ReorderBufferBlock
 * @brief Class contains simulated implementation of Reorder buffer
 */
public class ReorderBufferBlock implements AbstractBlock
{
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;
  /// Class holding mappings from architectural registers to speculative
  private RenameMapTableBlock renameMapTableBlock;
  /// Class, which simulates instruction decode and renames registers
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  /// Class for statistics gathering
  private StatisticsCounter statisticsCounter;
  
  
  /// GShare unit for getting correct prediction counters
  private GShareUnit gShareUnit;
  /// Buffer holding information about branch instructions targets
  private BranchTargetBuffer branchTargetBuffer;
  /// Class that fetches code from CodeParser
  private InstructionFetchBlock instructionFetchBlock;
  
  /// Buffer that tracks all in-flight load instructions
  private LoadBufferBlock loadBufferBlock;
  /// Buffer that tracks all in-flight store instructions
  private StoreBufferBlock storeBufferBlock;
  
  /// The state
  private ReorderBufferState state;
  
  public ReorderBufferBlock()
  {
  
  }
  
  /**
   * @param [in] blockScheduleTask      - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] registerFileBlock      - Class containing all registers, that simulator uses
   * @param [in] renameMapTableBlock    - Class holding mappings from architectural registers to speculative
   * @param [in] decodeAndDispatchBlock - Class, which simulates instruction decode and renames registers
   * @param [in] gShareUnit             - GShare unit for getting correct prediction counters
   * @param [in] branchTargetBuffer     - Buffer holding information about branch instructions targets
   * @param [in] instructionFetchBlock  - Class that fetches code from CodeParser
   * @param [in] statisticsCounter      - Class for statistics gathering
   *
   * @brief Constructor
   */
  public ReorderBufferBlock(UnifiedRegisterFileBlock registerFileBlock,
                            RenameMapTableBlock renameMapTableBlock,
                            DecodeAndDispatchBlock decodeAndDispatchBlock,
                            GShareUnit gShareUnit,
                            BranchTargetBuffer branchTargetBuffer,
                            InstructionFetchBlock instructionFetchBlock,
                            StatisticsCounter statisticsCounter,
                            ReorderBufferState state)
  {
    this.registerFileBlock      = registerFileBlock;
    this.renameMapTableBlock    = renameMapTableBlock;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    
    this.gShareUnit            = gShareUnit;
    this.branchTargetBuffer    = branchTargetBuffer;
    this.instructionFetchBlock = instructionFetchBlock;
    
    this.statisticsCounter = statisticsCounter;
    
    this.state = state;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value representing the limit buffer size
   * @brief Gets a limit of how many instructions can be stored inside the ROB
   */
  public int getBufferSize()
  {
    return this.state.bufferSize;
  }// end of getBufferSize
  //----------------------------------------------------------------------
  
  /**
   * @param [in] bufferSize - new buffer size limit
   *
   * @brief Sets limit on how many instructions can be stored inside the ROB
   */
  public void setBufferSize(int bufferSize)
  {
    this.state.bufferSize = bufferSize;
  }// end of setBufferSize
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value representing the commit limit
   * @brief Gets a limit of how many instructions can be committed in one cycle
   */
  public int getCommitLimit()
  {
    return state.commitLimit;
  }// end of setLoadBufferBlock
  //----------------------------------------------------------------------
  
  /**
   * @param [in] commitLimit - New commit limit
   *
   * @brief Sets limit on how many instructions should be commited in one tick
   */
  public void setCommitLimit(int commitLimit)
  {
    this.state.commitLimit = commitLimit;
  }// end of setCommitLimit
  //----------------------------------------------------------------------
  
  /**
   * @param [in] storeBufferBlock - A Load Buffer block object
   *
   * @brief Sets Load Buffer block object
   */
  public void setLoadBufferBlock(LoadBufferBlock loadBufferBlock)
  {
    this.loadBufferBlock = loadBufferBlock;
  }// end of setLoadBufferBlock
  //----------------------------------------------------------------------
  
  /**
   * @param [in] storeBufferBlock - A Store Buffer block object
   *
   * @brief Sets Store Buffer block object
   */
  public void setStoreBufferBlock(StoreBufferBlock storeBufferBlock)
  {
    this.storeBufferBlock = storeBufferBlock;
  }// end of setStoreBufferBlock
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates committing of instructions
   */
  @Override
  public void simulate()
  {
    boolean hasInstructionBeenProcessed;
    int     commitCount = 0;
    
    // First check if any instruction is ready for committing and set their register to assigned
    for (SimCodeModel currentInstruction : this.state.reorderQueue)
    {
      ReorderFlags currentReorderFlags = this.state.flagsMap.get(currentInstruction.getId());
      if (currentReorderFlags.isReadyToBeCommitted())
      {
        currentInstruction.setReadyId(this.state.commitId);
        InputCodeArgument argument = currentInstruction.getArgumentByName("rd");
        if (argument == null)
        {
          continue;
        }
        RegisterModel reg = registerFileBlock.getRegister(argument.getValue());
        if (reg.getReadiness() == RegisterReadinessEnum.kExecuted)
        {
          reg.setReadiness(RegisterReadinessEnum.kAssigned);
        }
      }
    }// End assign check
    
    // Next, go through queue and commit all instructions you can
    // until you reach un-committable instruction, or you reach limit
    do
    {
      if (this.state.reorderQueue.isEmpty())
      {
        break;
      }
      
      hasInstructionBeenProcessed = false;
      SimCodeModel currentInstruction  = this.state.reorderQueue.peek();
      ReorderFlags currentReorderFlags = this.state.flagsMap.get(currentInstruction.getId());
      
      // Check if instruction should be committed or removed because of failed speculation
      if (currentReorderFlags.isReadyToBeCommitted())
      {
        statisticsCounter.incrementCommittedInstructions();
        hasInstructionBeenProcessed = true;
        commitCount++;
        this.state.reorderQueue.poll();
        this.state.flagsStack.push(this.state.flagsMap.get(currentInstruction.getId()));
        this.state.flagsMap.remove(currentInstruction.getId());
        
        processCommittableInstruction(currentInstruction);
        currentInstruction.setCommitId(this.state.commitId);
        // Save instruction to release stack, for possible later backward simulation
        this.state.releaseStack.push(currentInstruction);
      }
      
    }
    while (hasInstructionBeenProcessed && commitCount < this.state.commitLimit);
    // End commit stage
    
    // Check all instructions if after commit some can be removed, remove them in other units
    flushInvalidInstructions();
    pullNewDecodedInstructions();
    
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates backwards (takes already committed functions back into the ROB)
   */
  @Override
  public void simulateBackwards()
  {
    this.state.commitId = this.state.commitId == 0 ? 0 : this.state.commitId - 1;
    // Put back all instructions that were released in this cycle
    while (!this.state.releaseStack.empty() && this.state.releaseStack.peek().getCommitId() == this.state.commitId)
    {
      resetAssignedRegisters();
      SimCodeModel currentInstruction = this.state.releaseStack.pop();
      this.state.reorderQueue.add(currentInstruction);
      ReorderFlags flags = this.state.flagsStack.pop();
      if (flags.isReadyToBeRemoved())
      {
        this.statisticsCounter.decrementFailedInstructions();
      }
      else if (flags.isReadyToBeCommitted())
      {
        this.statisticsCounter.decrementCommittedInstructions();
      }
      flags.setValid(true);
      this.state.flagsMap.put(currentInstruction.getId(), flags);
      currentInstruction.setHasFailed(false);
      
      if (currentInstruction.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        if (flags.isReadyToBeCommitted())
        {
          this.statisticsCounter.decrementAllBranches();
        }
        resetSpeculativeFlags(currentInstruction.getId());
        int pcValue = currentInstruction.getSavedPc();
        this.gShareUnit.getGlobalHistoryRegister().revertToHistory(currentInstruction.getId());
        if (!currentInstruction.hasFailed())
        {
          this.gShareUnit.getPredictorFromOld(pcValue, currentInstruction.getId()).predictBackwards();
        }
        
        // TODO: maybe
        boolean prediction = currentInstruction.isBranchPredicted();
        boolean result     = currentInstruction.isBranchLogicResult();
        if (prediction == result)
        {
          this.statisticsCounter.decrementCorrectlyPredictedBranches();
        }
        else
        {
          // Misprediction, revert to BTB before the correction
          this.branchTargetBuffer.resetEntry(pcValue, -1, state.commitId);
        }
      }
    }
    
    for (SimCodeModel currentInstruction : this.state.reorderQueue)
    {
      if (currentInstruction.getReadyId() == this.state.commitId)
      {
        currentInstruction.getArguments().stream().filter(argument -> argument.getName().equals("rd")).findFirst()
                .ifPresent(argument -> registerFileBlock.getRegister(argument.getValue())
                        .setReadiness(RegisterReadinessEnum.kExecuted));
      }
    }
    
    removeInstructionsInDecodeBlock();
    this.state.speculativePulls = false;
    this.state.reorderQueue.stream().sorted().reduce((first, second) -> second).ifPresent(codeModel ->
                                                                                          {
                                                                                            ReorderFlags codeModelFlags = state.flagsMap.get(
                                                                                                    codeModel.getId());
                                                                                            this.state.speculativePulls = codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch || codeModelFlags.isSpeculative();
                                                                                          });
  }// end of simulateBackwards
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the Reorder Buffer lists/stacks/variables and calls reset on branch blocks
   */
  @Override
  public void reset()
  {
    this.state.reorderQueue.clear();
    this.state.flagsMap.clear();
    this.state.releaseStack.clear();
    this.state.preCommitModelStack.clear();
    this.state.flagsStack.clear();
    
    this.state.commitId         = 0;
    this.state.speculativePulls = false;
    
    gShareUnit.getGlobalHistoryRegister().reset();
    gShareUnit.getPatternHistoryTable().reset();
    branchTargetBuffer.reset();
  }// end of reset
  
  /**
   * @brief Checks top of the register stack and resets value and mapping if commit ids are equal
   */
  private void resetAssignedRegisters()
  {
    while (!this.state.preCommitModelStack.empty() && this.state.preCommitModelStack.peek()
            .getId() == this.state.commitId)
    {
      PreCommitModel destinationRegister = this.state.preCommitModelStack.pop();
      if (destinationRegister.getSpeculRegister().isEmpty())
      {
        this.registerFileBlock.getRegister(destinationRegister.getArchRegister())
                .setValue(destinationRegister.getValue());
      }
      else
      {
        boolean mappingExists = this.renameMapTableBlock.getRegisterMap()
                .containsKey(destinationRegister.getSpeculRegister());
        this.renameMapTableBlock.mapRegister(destinationRegister.getArchRegister(),
                                             destinationRegister.getSpeculRegister(),
                                             destinationRegister.getRegisterOrder());
        
        if (!mappingExists)
        {
          double value = this.registerFileBlock.getRegister(destinationRegister.getArchRegister()).getValue();
          this.registerFileBlock.getRegister(destinationRegister.getArchRegister())
                  .setValue(destinationRegister.getValue());
          this.registerFileBlock.getRegister(destinationRegister.getSpeculRegister()).setValue(value);
        }
        this.registerFileBlock.getRegister(destinationRegister.getSpeculRegister())
                .setReadiness(destinationRegister.getSpeculState());
      }
    }
  }// end of resetAssignedRegisters
  //----------------------------------------------------------------------
  
  /**
   * @param [in] codeModelId - Id marking flagEntry to omit
   *
   * @brief Set all flag entries to speculative except the specified one
   */
  private void resetSpeculativeFlags(int codeModelId)
  {
    this.state.flagsMap.forEach((integer, reorderFlags) -> reorderFlags.setSpeculative(integer != codeModelId));
  }// end of resetSpeculativeFlags
  //----------------------------------------------------------------------
  
  /**
   * @brief Removes instructions, which are currently in decode block
   */
  private void removeInstructionsInDecodeBlock()
  {
    for (SimCodeModel instruction : this.decodeAndDispatchBlock.getAfterRenameCodeList())
    {
      this.state.reorderQueue.remove(instruction);
      this.getFlagsMap().remove(instruction.getId());
    }
  }// end of removeInstructionsInDecodeBlock
  //----------------------------------------------------------------------
  
  /**
   * @return Map of ROB flags
   * @brief Get the map of flags
   */
  public Map<Integer, ReorderFlags> getFlagsMap()
  {
    return state.flagsMap;
  }// end of getFlagsMap
  //----------------------------------------------------------------------
  
  /**
   * @param [in] codeModel - Code model of committable instruction
   *
   * @brief Process instruction that is ready to be committed
   */
  private void processCommittableInstruction(SimCodeModel codeModel)
  {
    if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
    {
      boolean prediction          = codeModel.isBranchPredicted();
      boolean branchActuallyTaken = codeModel.isBranchLogicResult();
      int     pc                  = codeModel.getSavedPc();
      
      statisticsCounter.incrementAllBranches();
      if (prediction == branchActuallyTaken)
      {
        // Correct prediction
        statisticsCounter.incrementCorrectlyPredictedBranches();
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getId()).upTheProbability();
        this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(codeModel.getId());
        validateInstructions();
      }
      else
      {
        // Wrong prediction - feedback to predictor
        int resultPc = pc + codeModel.getBranchTargetOffset();
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getId()).downTheProbability();
        this.branchTargetBuffer.setEntry(pc, codeModel, resultPc, -1, state.commitId);
        invalidateInstructions(this.state.reorderQueue.peek());
        
        GlobalHistoryRegister activeRegister = gShareUnit.getGlobalHistoryRegister();
        activeRegister.setHistoryValueAsCurrent(codeModel.getId());
        activeRegister.shiftValue(false);
        
        this.instructionFetchBlock.setPcCounter(resultPc);
      }
    }
    else if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kLoadstore)
    {
      if (loadBufferBlock.getQueueSize() > 0 && loadBufferBlock.getLoadQueueFirst() == codeModel)
      {
        loadBufferBlock.releaseLoadFirst();
      }
      else if (storeBufferBlock.getQueueSize() > 0 && storeBufferBlock.getStoreQueueFirst() == codeModel)
      {
        storeBufferBlock.releaseStoreFirst();
      }
    }
    //Store destination register into architectural
    InputCodeArgument destinationArgument = codeModel.getArgumentByName("rd");
    if (destinationArgument != null)
    {
      //Save the value of architectural register in case of backward simulation
      saveRegisterValue(renameMapTableBlock.getMapping(destinationArgument.getValue()), codeModel.getId());
      renameMapTableBlock.directCopyMapping(destinationArgument.getValue());
    }
    
    // Save the value of temporary registers for backward simulation
    codeModel.getArguments().stream().filter(argument -> argument.getName().startsWith("r")).forEach(argument ->
                                                                                                     {
                                                                                                       saveRegisterValue(
                                                                                                               argument.getValue(),
                                                                                                               codeModel.getId());
                                                                                                       if (renameMapTableBlock.reduceReference(
                                                                                                               argument.getValue()))
                                                                                                       {
                                                                                                         renameMapTableBlock.freeMapping(
                                                                                                                 argument.getValue());
                                                                                                       }
                                                                                                     });
  }// end of processCommittableInstruction
  //----------------------------------------------------------------------
  
  /**
   * @brief Removes all invalid (ready to removed) instructions from ROB
   */
  public void flushInvalidInstructions()
  {
    List<SimCodeModel> instructionForRemoval = new ArrayList<>();
    for (SimCodeModel currentInstruction : this.state.reorderQueue)
    {
      ReorderFlags currentReorderFlags = this.state.flagsMap.get(currentInstruction.getId());
      if (currentReorderFlags.isReadyToBeRemoved())
      {
        statisticsCounter.incrementFailedInstructions();
        currentInstruction.setCommitId(this.state.commitId);
        instructionForRemoval.add(currentInstruction);
        this.state.flagsStack.push(this.state.flagsMap.get(currentInstruction.getId()));
        this.state.flagsMap.remove(currentInstruction.getId());
        currentInstruction.getArguments().stream().filter(argument -> argument.getName().startsWith("r"))
                .forEach(argument ->
                         {
                           saveRegisterValue(argument.getValue(), currentInstruction.getId());
                           if (renameMapTableBlock.reduceReference(argument.getValue()))
                           {
                             renameMapTableBlock.freeMapping(argument.getValue());
                           }
                         });
        this.state.releaseStack.push(currentInstruction);
        
        if (currentInstruction.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
        {
          this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(currentInstruction.getId());
        }
      }
    }
    this.state.reorderQueue.removeAll(instructionForRemoval);
  }// end of flushInvalidInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Takes decoded instructions from decoder and orders them
   */
  private void pullNewDecodedInstructions()
  {
    if (!this.decodeAndDispatchBlock.shouldFlush())
    {
      int pullCount = 0;
      for (SimCodeModel codeModel : this.decodeAndDispatchBlock.getAfterRenameCodeList())
      {
        if (!checkIfInstructionsHaveRoom(codeModel))
        {
          this.decodeAndDispatchBlock.setStallFlag(true);
          this.decodeAndDispatchBlock.setStalledPullCount(pullCount);
          return;
        }
        this.state.reorderQueue.add(codeModel);
        this.state.flagsMap.put(codeModel.getId(), new ReorderFlags(this.state.speculativePulls));
        this.state.speculativePulls = this.state.speculativePulls || codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch;
        pullCount++;
      }
    }
  }// end of pullNewDecodedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Validate all instructions from top of the queue to the first branch instruction
   */
  private void validateInstructions()
  {
    List<SimCodeModel> polledInstructions = new ArrayList<>();
    SimCodeModel       codeModel          = this.state.reorderQueue.poll();
    while (codeModel != null)
    {
      polledInstructions.add(codeModel);
      this.state.flagsMap.get(codeModel.getId()).setSpeculative(false);
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        this.state.reorderQueue.addAll(polledInstructions);
        return;
      }
      codeModel = this.state.reorderQueue.poll();
    }
    this.state.reorderQueue.addAll(polledInstructions);
    this.state.speculativePulls = false;
  }// end of validateInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets instruction flags to notify ROB to flush itself
   */
  public void invalidateInstructions(SimCodeModel firstInvalidInstruction)
  {
    List<SimCodeModel> polledInstructions = new ArrayList<>();
    SimCodeModel       codeModel          = this.state.reorderQueue.peek();
    while (codeModel != null && firstInvalidInstruction != codeModel)
    {
      polledInstructions.add(this.state.reorderQueue.poll());
      codeModel = this.state.reorderQueue.peek();
    }
    
    for (SimCodeModel simCodeModel : this.state.reorderQueue)
    {
      simCodeModel.setHasFailed(true);
      ReorderFlags reorderFlags = this.state.flagsMap.get(simCodeModel.getId());
      reorderFlags.setSpeculative(false);
      reorderFlags.setValid(false);
    }
    // clear what you can
    this.decodeAndDispatchBlock.setFlush(true);
    this.decodeAndDispatchBlock.getAfterRenameCodeList().forEach(
            simCodeModel -> simCodeModel.getArguments().stream().filter(argument -> argument.getName().startsWith("r"))
                    .forEach(argument ->
                             {
                               saveRegisterValue(argument.getValue(), simCodeModel.getId());
                               if (renameMapTableBlock.reduceReference(argument.getValue()))
                               {
                                 renameMapTableBlock.freeMapping(argument.getValue());
                               }
                             }));
    this.instructionFetchBlock.getFetchedCode().clear();
    
    this.state.speculativePulls = !polledInstructions.isEmpty() && this.state.flagsMap.get(
            polledInstructions.get(polledInstructions.size() - 1).getId()).isSpeculative();
    this.state.reorderQueue.addAll(polledInstructions);
  }// end of invalidateInstructions
  //----------------------------------------------------------------------
  
  /**
   * @param [in] register - Speculative register to save
   *
   * @brief Save register value, its mapping and state for possible later backward simulation
   */
  private void saveRegisterValue(final String register, final int orderId)
  {
    if (this.renameMapTableBlock.isSpeculativeRegister(register))
    {
      // Workaround for the same register being freed from invalidation in reorder buffer
      // and invalidation from fetch buffer
      if (!this.renameMapTableBlock.getRegisterMap().containsKey(register))
      {
        return;
      }
      String        registerName  = this.renameMapTableBlock.getRegisterMap().get(register).getArchitecturalRegister();
      RegisterModel registerModel = registerFileBlock.getRegister(registerName);
      this.state.preCommitModelStack.push(
              new PreCommitModel(this.state.commitId, registerName, register, registerModel.getValue(), orderId,
                                 registerModel.getReadiness()));
    }
    else
    {
      double value = this.registerFileBlock.getRegister(register).getValue();
      this.state.preCommitModelStack.push(new PreCommitModel(this.state.commitId, register, "", value, orderId, null));
    }
  }// end of saveRegisterValue
  //----------------------------------------------------------------------
  
  /**
   * @param [in] codeModel - Code model to be added into the buffers
   *
   * @return True if there is a space, false if one of the buffers does not have room
   * @brief Verifies if Reorder/Load/Store buffers have space for newly decoded instructions
   */
  private boolean checkIfInstructionsHaveRoom(SimCodeModel codeModel)
  {
    int allInstructionCount = 1;
    
    if (loadBufferBlock.isInstructionLoad(codeModel))
    {
      this.loadBufferBlock.incrementPossibleNewEntries();
    }
    else if (storeBufferBlock.isInstructionStore(codeModel))
    {
      this.storeBufferBlock.incrementPossibleNewEntries();
    }
    
    return !this.isBufferFull(allInstructionCount) && !this.loadBufferBlock.isBufferFull(
            0) && !this.storeBufferBlock.isBufferFull(0);
  }// end of checkIfInstructionsHaveRoom
  //----------------------------------------------------------------------
  
  /**
   * @param [in] possibleAddition - how many instructions we want to store
   *
   * @return True if buffer will overflow, false otherwise
   * @brief Checks if buffer will overflow if instructions were to be added into ROB
   */
  public boolean isBufferFull(int possibleAddition)
  {
    return this.state.bufferSize < (this.state.reorderQueue.size() + possibleAddition);
  }// end of isBufferFull
  //----------------------------------------------------------------------
  
  public void bumpCommitID()
  {
    this.state.commitId = this.state.commitId + 1;
  }
  //----------------------------------------------------------------------
  
  /**
   * @return Current reorder queue
   * @brief Get current Reorder queue
   */
  public Queue<SimCodeModel> getReorderQueue()
  {
    return state.reorderQueue;
  }// end of getReorderQueue
  //----------------------------------------------------------------------
}
