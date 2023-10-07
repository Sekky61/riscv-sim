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
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.RegisterModel;
import com.gradle.superscalarsim.models.ReorderFlags;
import com.gradle.superscalarsim.models.SimCodeModel;

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
      assert !currentInstruction.getInstructionName().equals("nop");
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
        // Delete instruction from ROB
        this.state.reorderQueue.poll();
        this.state.flagsMap.remove(currentInstruction.getId());
        
        processCommittableInstruction(currentInstruction);
        currentInstruction.setCommitId(this.state.commitId);
        // Instruction can also be removed from allocator
        currentInstruction.setFinished(true);
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
   * @brief Resets the Reorder Buffer lists/stacks/variables and calls reset on branch blocks
   */
  @Override
  public void reset()
  {
    this.state.reorderQueue.clear();
    this.state.flagsMap.clear();
    
    this.state.commitId         = 0;
    this.state.speculativePulls = false;
    
    gShareUnit.getGlobalHistoryRegister().reset();
    gShareUnit.getPatternHistoryTable().reset();
    branchTargetBuffer.reset();
  }// end of reset
  
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
      renameMapTableBlock.directCopyMapping(destinationArgument.getValue());
    }
    
    // Save the value of temporary registers for backward simulation
    codeModel.getArguments().stream().filter(argument -> argument.getName().startsWith("r")).forEach(argument ->
                                                                                                     {
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
        currentInstruction.setFinished(true);
        this.state.flagsMap.remove(currentInstruction.getId());
        currentInstruction.getArguments().stream().filter(argument -> argument.getName().startsWith("r"))
                .forEach(argument ->
                         {
                           if (renameMapTableBlock.reduceReference(argument.getValue()))
                           {
                             renameMapTableBlock.freeMapping(argument.getValue());
                           }
                         });
        
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
    // TODO: move to fetch and decode block
    this.decodeAndDispatchBlock.getAfterRenameCodeList().forEach(
            simCodeModel -> simCodeModel.getArguments().stream().filter(argument -> argument.getName().startsWith("r"))
                    .forEach(argument ->
                             {
                               if (renameMapTableBlock.reduceReference(argument.getValue()))
                               {
                                 renameMapTableBlock.freeMapping(argument.getValue());
                               }
                             }));
    
    for (SimCodeModel simCode : this.instructionFetchBlock.getFetchedCode())
    {
      simCode.setFinished(true);
    }
    this.instructionFetchBlock.getFetchedCode().clear();
    
    this.state.speculativePulls = !polledInstructions.isEmpty() && this.state.flagsMap.get(
            polledInstructions.get(polledInstructions.size() - 1).getId()).isSpeculative();
    this.state.reorderQueue.addAll(polledInstructions);
  }// end of invalidateInstructions
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
  
  /**
   * @return Map of ROB flags
   * @brief Get the map of flags
   */
  public Map<Integer, ReorderFlags> getFlagsMap()
  {
    return state.flagsMap;
  }// end of getFlagsMap
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
