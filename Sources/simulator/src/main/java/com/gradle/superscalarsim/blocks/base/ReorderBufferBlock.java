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
 * 26 Oct      2023 10:00 (revised)
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.StatisticsCounter;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.blocks.loadstore.LoadBufferBlock;
import com.gradle.superscalarsim.blocks.loadstore.StoreBufferBlock;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.*;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @class ReorderBufferBlock
 * @brief Class contains simulated implementation of Reorder buffer
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class ReorderBufferBlock implements AbstractBlock
{
  
  /**
   * Queue of scheduled instruction in backend
   */
  public ArrayDeque<ReorderBufferItem> reorderQueue;
  
  /**
   * Numerical limit, how many instruction can be committed in a single tick
   */
  public int commitLimit;
  
  /**
   * ID (tick( counter for marking when an instruction was committed/ready
   */
  public int commitId;
  
  /**
   * Flag to mark newly added instructions as speculative.
   * This flag is set after encountering branch instruction.
   */
  public boolean speculativePulls;
  
  /**
   * Reorder buffer size limit.
   */
  public int bufferSize;
  
  /**
   * Class holding mappings from architectural registers to speculative
   */
  @JsonIdentityReference(alwaysAsId = true)
  private RenameMapTableBlock renameMapTableBlock;
  
  /**
   * Class, which simulates instruction decode and renames registers
   */
  @JsonIdentityReference(alwaysAsId = true)
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  
  /**
   * Class for statistics gathering
   */
  @JsonIdentityReference(alwaysAsId = true)
  private StatisticsCounter statisticsCounter;
  
  /**
   * GShare unit for getting correct prediction counters
   */
  @JsonIdentityReference(alwaysAsId = true)
  private GShareUnit gShareUnit;
  
  /**
   * Buffer holding information about branch instructions targets
   */
  @JsonIdentityReference(alwaysAsId = true)
  private BranchTargetBuffer branchTargetBuffer;
  
  /**
   * Class that fetches code from CodeParser
   */
  @JsonIdentityReference(alwaysAsId = true)
  private InstructionFetchBlock instructionFetchBlock;
  
  /**
   * Buffer that tracks all in-flight load instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private LoadBufferBlock loadBufferBlock;
  
  /**
   * Buffer that tracks all in-flight store instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private StoreBufferBlock storeBufferBlock;
  
  public ReorderBufferBlock()
  {
  }
  
  /**
   * @param blockScheduleTask      Task class, where blocks are periodically triggered by the GlobalTimer
   * @param registerFileBlock      Class containing all registers, that simulator uses
   * @param renameMapTableBlock    Class holding mappings from architectural registers to speculative
   * @param decodeAndDispatchBlock Class, which simulates instruction decode and renames registers
   * @param gShareUnit             GShare unit for getting correct prediction counters
   * @param branchTargetBuffer     Buffer holding information about branch instructions targets
   * @param instructionFetchBlock  Class that fetches code from CodeParser
   * @param statisticsCounter      Class for statistics gathering
   *
   * @brief Constructor
   */
  public ReorderBufferBlock(RenameMapTableBlock renameMapTableBlock,
                            DecodeAndDispatchBlock decodeAndDispatchBlock,
                            GShareUnit gShareUnit,
                            BranchTargetBuffer branchTargetBuffer,
                            InstructionFetchBlock instructionFetchBlock,
                            StatisticsCounter statisticsCounter)
  {
    this.renameMapTableBlock    = renameMapTableBlock;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    
    this.gShareUnit            = gShareUnit;
    this.branchTargetBuffer    = branchTargetBuffer;
    this.instructionFetchBlock = instructionFetchBlock;
    
    this.statisticsCounter = statisticsCounter;
    
    this.reorderQueue = new ArrayDeque<>();
    
    this.commitId         = 0;
    this.speculativePulls = false;
    
    this.commitLimit = 4;
    this.bufferSize  = 256;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param storeBufferBlock A Load Buffer block object
   *
   * @brief Sets Load Buffer block object
   */
  public void setLoadBufferBlock(LoadBufferBlock loadBufferBlock)
  {
    this.loadBufferBlock = loadBufferBlock;
  }// end of setLoadBufferBlock
  //----------------------------------------------------------------------
  
  /**
   * @param storeBufferBlock A Store Buffer block object
   *
   * @brief Sets Store Buffer block object
   */
  public void setStoreBufferBlock(StoreBufferBlock storeBufferBlock)
  {
    this.storeBufferBlock = storeBufferBlock;
  }// end of setStoreBufferBlock
  //----------------------------------------------------------------------
  
  /**
   * Actions in a cycle:
   * - Commit all instructions that are ready
   * - Flush badly speculated instructions
   * - Pull new instructions from decoder
   *
   * @brief Simulates committing of instructions
   */
  @Override
  public void simulate()
  {
    // Go through queue and commit all instructions you can
    // until you reach un-committable instruction, or you reach limit
    int commitCount = 0;
    while (commitCount < this.commitLimit && !this.reorderQueue.isEmpty())
    {
      ReorderBufferItem robItem = this.reorderQueue.peek();
      
      if (!robItem.reorderFlags.isReadyToBeCommitted())
      {
        break;
      }
      
      commitCount++;
      processCommittableInstruction(robItem.simCodeModel);
      removeInstruction(robItem);
      // Remove item from the front of the queue
      this.reorderQueue.remove();
    }
    
    // Check all instructions if after commit some can be removed, remove them in other units
    flushInvalidInstructions();
    
    // Pull new instructions from decoder, unless you are flushing
    if (!this.decodeAndDispatchBlock.shouldFlush())
    {
      pullNewDecodedInstructions();
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the Reorder Buffer lists/stacks/variables and calls reset on branch blocks
   */
  @Override
  public void reset()
  {
    this.reorderQueue.clear();
    
    this.commitId         = 0;
    this.speculativePulls = false;
    
    gShareUnit.getGlobalHistoryRegister().reset();
    gShareUnit.getPatternHistoryTable().reset();
    branchTargetBuffer.reset();
  }// end of reset
  
  /**
   * Writes into architectural register, updates statistics
   *
   * @param codeModel Code model of committable instruction
   *
   * @brief Process instruction that is ready to be committed
   */
  private void processCommittableInstruction(SimCodeModel codeModel)
  {
    codeModel.setCommitId(this.commitId);
    statisticsCounter.incrementCommittedInstructions();
    if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
    {
      boolean branchActuallyTaken = codeModel.isBranchLogicResult();
      int     pc                  = codeModel.getSavedPc();
      
      statisticsCounter.incrementAllBranches();
      if (branchActuallyTaken)
      {
        statisticsCounter.incrementTakenBranches();
      }
      
      if (codeModel.isBranchPredicted() == branchActuallyTaken)
      {
        // Correct prediction
        statisticsCounter.incrementCorrectlyPredictedBranches();
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getIntegerId()).upTheProbability();
        this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(codeModel.getIntegerId());
        // Update committable status of subsequent instructions
        validateInstructions();
      }
      else
      {
        // Wrong prediction - feedback to predictor
        int resultPc = pc + codeModel.getBranchTargetOffset();
        // TODO: Why down? Shouldn't the feedback be in the opposite direction of the wrong prediction?
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getIntegerId()).downTheProbability();
        this.branchTargetBuffer.setEntry(pc, codeModel, resultPc, -1, this.commitId);
        
        // Get the second instruction in the queue and invalidate it
        
        Optional<ReorderBufferItem> robItem = this.reorderQueue.stream().skip(1).findFirst();
        if (robItem.isPresent())
        {
          invalidateInstructions(robItem.get().simCodeModel);
        }
        
        GlobalHistoryRegister activeRegister = gShareUnit.getGlobalHistoryRegister();
        // This also removes the value from the history stack
        activeRegister.setHistoryValueAsCurrent(codeModel.getIntegerId());
        activeRegister.shiftValue(false);
        
        this.instructionFetchBlock.setPc(resultPc);
      }
      
      // If we go to the end of the queue, we did not find a branch instruction.
      // This means that we are not speculating at this point.
      if (!reorderQueue.getLast().reorderFlags.isSpeculative())
      {
        this.speculativePulls = false;
      }
    }
    else if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kLoadstore)
    {
      // Release load/store buffer entry
      if (loadBufferBlock.getQueueSize() > 0 && loadBufferBlock.getLoadQueueFirst() == codeModel)
      {
        loadBufferBlock.releaseLoadFirst();
      }
      else if (storeBufferBlock.getQueueSize() > 0 && storeBufferBlock.getStoreQueueFirst() == codeModel)
      {
        storeBufferBlock.releaseStoreFirst();
      }
    }
    
    // Store registers to arch. register file
    List<InstructionFunctionModel.Argument> arguments = codeModel.getInstructionFunctionModel().getArguments();
    for (InstructionFunctionModel.Argument argument : arguments)
    {
      if (!argument.writeBack())
      {
        continue;
      }
      InputCodeArgument codeArgument = codeModel.getArgumentByName(argument.name());
      String            tempRegName  = codeArgument.getValue();
      if (codeArgument == null)
      {
        throw new IllegalArgumentException("Argument " + argument.name() + " not found in code model");
      }
      renameMapTableBlock.directCopyMapping(tempRegName);
    }
  }// end of processCommittableInstruction
  
  /**
   * Sets finished flag, reduces references to speculative registers
   *
   * @brief Handles removal of instruction from the system
   */
  private void removeInstruction(ReorderBufferItem queueItem)
  {
    SimCodeModel simCodeModel = queueItem.simCodeModel;
    
    // Reduce references to speculative registers
    for (InputCodeArgument argument : simCodeModel.getArguments())
    {
      if (!argument.getName().startsWith("r"))
      {
        continue;
      }
      if (renameMapTableBlock.reduceReference(argument.getValue()))
      {
        renameMapTableBlock.freeMapping(argument.getValue());
      }
    }
    
    simCodeModel.setFinished(true);
  }
  //----------------------------------------------------------------------
  
  /**
   * It does not stop at the first valid instruction, but flushes all invalid instructions.
   * Assumes that every instruction after the first invalid instruction is invalid.
   *
   * @brief Removes all invalid (ready to removed) instructions from ROB
   */
  public void flushInvalidInstructions()
  {
    // Iterate the queue from the end, remove until first valid instruction
    Iterator<ReorderBufferItem> it = this.reorderQueue.descendingIterator();
    while (it.hasNext())
    {
      ReorderBufferItem robItem = it.next();
      if (!robItem.reorderFlags.isReadyToBeRemoved())
      {
        break;
      }
      
      // Notify all that instruction is invalid
      statisticsCounter.incrementFailedInstructions();
      SimCodeModel currentInstruction = robItem.simCodeModel;
      currentInstruction.setCommitId(this.commitId); // todo: is this correct?
      if (currentInstruction.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(currentInstruction.getIntegerId());
      }
      removeInstruction(robItem);
      this.reorderQueue.removeLast();
    }
  }// end of flushInvalidInstructions
  //----------------------------------------------------------------------
  
  /**
   * Stalls decode block if there is no room for new instructions.
   * It takes until it can't take anymore.
   *
   * @brief Takes decoded instructions from decoder and orders them
   */
  private void pullNewDecodedInstructions()
  {
    int pulledCount = 0;
    int loadCount   = 0;
    int storeCount  = 0;
    for (SimCodeModel codeModel : this.decodeAndDispatchBlock.getAfterRenameCodeList())
    {
      if (codeModel.isLoad())
      {
        loadCount++;
      }
      else if (codeModel.isStore())
      {
        storeCount++;
      }
      
      boolean robFull   = this.bufferSize < (this.reorderQueue.size() + 1);
      boolean loadFull  = this.loadBufferBlock.isBufferFull(loadCount);
      boolean storeFull = this.storeBufferBlock.isBufferFull(storeCount);
      
      boolean instructionHasRoom = !robFull && !loadFull && !storeFull;
      if (!instructionHasRoom)
      {
        // No more space, stop
        this.decodeAndDispatchBlock.setStallFlag(true);
        this.decodeAndDispatchBlock.setStalledPullCount(pulledCount);
        return;
      }
      
      ReorderBufferItem reorderBufferItem = new ReorderBufferItem(codeModel, new ReorderFlags(this.speculativePulls));
      this.reorderQueue.add(reorderBufferItem);
      this.speculativePulls = this.speculativePulls || codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch;
      if (codeModel.isLoad())
      {
        this.loadBufferBlock.addLoadToBuffer(codeModel);
      }
      else if (codeModel.isStore())
      {
        this.storeBufferBlock.addStoreToBuffer(codeModel);
      }
      pulledCount++;
    }
  }// end of pullNewDecodedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Mark all instructions from top of the queue to the first branch instruction as non-speculative
   */
  private void validateInstructions()
  {
    boolean skipFirst = true;
    for (ReorderBufferItem item : this.reorderQueue)
    {
      if (skipFirst)
      {
        // Skip because the first instruction is the branch that caused the speculation
        skipFirst = false;
        continue;
      }
      item.reorderFlags.setSpeculative(false);
      
      if (item.simCodeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        return;
      }
    }
  }// end of validateInstructions
  //----------------------------------------------------------------------
  
  /**
   * Mark firstInvalidInstruction and all subsequent instructions as invalid.
   *
   * @param firstInvalidInstruction First instruction that should be invalidated
   *
   * @brief Sets instruction flags to notify ROB to flush itself
   */
  public void invalidateInstructions(SimCodeModel firstInvalidInstruction)
  {
    boolean flush = false;
    for (ReorderBufferItem robItem : this.reorderQueue)
    {
      flush = flush || robItem.simCodeModel == firstInvalidInstruction;
      if (flush)
      {
        robItem.reorderFlags.setSpeculative(false);
        robItem.reorderFlags.setValid(false);
        robItem.simCodeModel.setHasFailed(true);
      }
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
    
    this.instructionFetchBlock.clearFetchedCode();
    
    boolean keptAnyInstruction  = !this.reorderQueue.isEmpty();
    boolean lastKeptSpeculative = keptAnyInstruction && this.reorderQueue.getLast().reorderFlags.isSpeculative();
    this.speculativePulls = keptAnyInstruction && lastKeptSpeculative;
  }// end of invalidateInstructions
  //----------------------------------------------------------------------
  
  public ReorderBufferItem getRobItem(int simCodeId)
  {
    return this.reorderQueue.stream().filter(robItem -> robItem.simCodeModel.getIntegerId() == simCodeId).findFirst()
            .orElse(null);
  }// end of getFlagsMap
  //----------------------------------------------------------------------
  
  public void bumpCommitID()
  {
    this.commitId = this.commitId + 1;
  }
  //----------------------------------------------------------------------
  
  /**
   * @return Current reorder queue
   * @brief Get current Reorder queue
   */
  public int getReorderQueueSize()
  {
    return this.reorderQueue.size();
  }// end of getReorderQueue
  //----------------------------------------------------------------------
  
  /**
   * @return Current reorder queue
   * @brief Get current Reorder queue
   */
  public Stream<ReorderBufferItem> getReorderQueue()
  {
    return this.reorderQueue.stream();
  }// end of getReorderQueue
}
