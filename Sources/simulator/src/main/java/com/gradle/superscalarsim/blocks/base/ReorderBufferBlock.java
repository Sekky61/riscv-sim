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
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.List;
import java.util.stream.Stream;

/**
 * @class ReorderBufferBlock
 * @brief Class contains simulated implementation of Reorder buffer
 */
public class ReorderBufferBlock implements AbstractBlock
{
  /**
   * Class containing all registers, that simulator uses
   */
  private UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * Class holding mappings from architectural registers to speculative
   */
  private RenameMapTableBlock renameMapTableBlock;
  
  /**
   * Class, which simulates instruction decode and renames registers
   */
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  
  /**
   * Class for statistics gathering
   */
  private StatisticsCounter statisticsCounter;
  
  
  /**
   * GShare unit for getting correct prediction counters
   */
  private GShareUnit gShareUnit;
  
  /**
   * Buffer holding information about branch instructions targets
   */
  private BranchTargetBuffer branchTargetBuffer;
  
  /**
   * Class that fetches code from CodeParser
   */
  private InstructionFetchBlock instructionFetchBlock;
  
  
  /**
   * Buffer that tracks all in-flight load instructions
   */
  private LoadBufferBlock loadBufferBlock;
  
  /**
   * Buffer that tracks all in-flight store instructions
   */
  private StoreBufferBlock storeBufferBlock;
  
  /**
   * The state. This is separated from the block to make it easily serializable (cyclic references).
   */
  private ReorderBufferState state;
  
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
    // First check if any instruction is ready for committing and set their register to assigned
    for (ReorderBufferItem robItem : this.state.reorderQueue)
    {
      SimCodeModel currentInstruction = robItem.simCodeModel;
      assert !currentInstruction.getInstructionName().equals("nop");
      if (!robItem.reorderFlags.isReadyToBeCommitted())
      {
        continue;
      }
      currentInstruction.setReadyId(this.state.commitId);
      
      // Mark registers of write back arguments as assigned
      for (InstructionFunctionModel.Argument argument : currentInstruction.getInstructionFunctionModel().getArguments())
      {
        if (!argument.writeBack())
        {
          continue;
        }
        String        regName = currentInstruction.getArgumentByName(argument.name()).getValue();
        RegisterModel reg     = registerFileBlock.getRegister(regName);
        assert reg.getReadiness() == RegisterReadinessEnum.kExecuted;
        reg.setReadiness(RegisterReadinessEnum.kAssigned);
      }
    }
    
    // Next, go through queue and commit all instructions you can
    // until you reach un-committable instruction, or you reach limit
    int commitCount = 0;
    while (commitCount < this.state.commitLimit && !this.state.reorderQueue.isEmpty())
    {
      ReorderBufferItem robItem = this.state.reorderQueue.peek();
      
      if (!robItem.reorderFlags.isReadyToBeCommitted())
      {
        break;
      }
      
      // Delete instruction from ROB
      this.state.reorderQueue.poll();
      
      // Check if instruction should be committed or removed because of failed speculation
      statisticsCounter.incrementCommittedInstructions();
      commitCount++;
      
      SimCodeModel currentInstruction = robItem.simCodeModel;
      processCommittableInstruction(currentInstruction);
      currentInstruction.setCommitId(this.state.commitId);
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
    this.state.reorderQueue.clear();
    
    this.state.commitId         = 0;
    this.state.speculativePulls = false;
    
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
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getId()).upTheProbability();
        this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(codeModel.getId());
        // Update committable status of subsequent instructions
        validateInstructions();
      }
      else
      {
        // Wrong prediction - feedback to predictor
        int resultPc = pc + codeModel.getBranchTargetOffset();
        // TODO: Why down? Shouldn't the feedback be in the opposite direction of the wrong prediction?
        this.gShareUnit.getPredictorFromOld(pc, codeModel.getId()).downTheProbability();
        this.branchTargetBuffer.setEntry(pc, codeModel, resultPc, -1, state.commitId);
        
        ReorderBufferItem robItem = this.state.reorderQueue.peek();
        if (robItem != null)
        {
          invalidateInstructions(robItem.simCodeModel);
        }
        
        GlobalHistoryRegister activeRegister = gShareUnit.getGlobalHistoryRegister();
        // This also removes the value from the history stack
        activeRegister.setHistoryValueAsCurrent(codeModel.getId());
        activeRegister.shiftValue(false);
        
        this.instructionFetchBlock.setPc(resultPc);
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
    
    // Clear
    removeInstruction(codeModel);
  }// end of processCommittableInstruction
  
  /**
   * It does not stop at the first valid instruction, but flushes all invalid instructions.
   *
   * @brief Removes all invalid (ready to removed) instructions from ROB
   */
  public void flushInvalidInstructions()
  {
    for (ReorderBufferItem robItem : this.state.reorderQueue)
    {
      if (!robItem.reorderFlags.isReadyToBeRemoved())
      {
        continue;
      }
      
      statisticsCounter.incrementFailedInstructions();
      SimCodeModel currentInstruction = robItem.simCodeModel;
      currentInstruction.setCommitId(this.state.commitId);
      if (currentInstruction.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        this.gShareUnit.getGlobalHistoryRegister().removeHistoryValue(currentInstruction.getId());
      }
      
      // Remove
      removeInstruction(currentInstruction);
      this.state.reorderQueue.poll();
    }
  }// end of flushInvalidInstructions
  //----------------------------------------------------------------------
  
  /**
   * Stalls decode block if there is no room for new instructions.
   *
   * @brief Takes decoded instructions from decoder and orders them
   */
  private void pullNewDecodedInstructions()
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
      ReorderBufferItem reorderBufferItem = new ReorderBufferItem(codeModel,
                                                                  new ReorderFlags(this.state.speculativePulls));
      this.state.reorderQueue.add(reorderBufferItem);
      this.state.speculativePulls = this.state.speculativePulls || codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch;
      pullCount++;
    }
  }// end of pullNewDecodedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Mark all instructions from top of the queue to the first branch instruction as non-speculative
   */
  private void validateInstructions()
  {
    for (ReorderBufferItem item : this.state.reorderQueue)
    {
      item.reorderFlags.setSpeculative(false);
      if (item.simCodeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        return;
      }
    }
    // If we go to the end of the queue, we did not find a branch instruction.
    // This means that we are not speculating at this point.
    this.state.speculativePulls = false;
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
    for (ReorderBufferItem robItem : this.state.reorderQueue)
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
    
    boolean keptAnyInstruction  = !this.state.reorderQueue.isEmpty();
    boolean lastKeptSpeculative = keptAnyInstruction && this.state.reorderQueue.getLast().reorderFlags.isSpeculative();
    this.state.speculativePulls = keptAnyInstruction && lastKeptSpeculative;
  }// end of invalidateInstructions
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Code model to be added into the buffers
   *
   * @return True if there is a space, false if one of the buffers does not have room
   * @brief Verifies if Reorder/Load/Store buffers have space for newly decoded instructions
   */
  private boolean checkIfInstructionsHaveRoom(SimCodeModel codeModel)
  {
    if (loadBufferBlock.isInstructionLoad(codeModel))
    {
      this.loadBufferBlock.incrementPossibleNewEntries();
    }
    else if (storeBufferBlock.isInstructionStore(codeModel))
    {
      this.storeBufferBlock.incrementPossibleNewEntries();
    }
    
    boolean robFull   = this.state.bufferSize < (this.state.reorderQueue.size() + 1);
    boolean loadFull  = this.loadBufferBlock.isBufferFull(0);
    boolean storeFull = this.storeBufferBlock.isBufferFull(0);
    
    return !robFull && !loadFull && !storeFull;
  }// end of checkIfInstructionsHaveRoom
  //----------------------------------------------------------------------
  
  /**
   * Sets finished flag, reduces references and frees registers
   *
   * @brief Handles removal of instruction from the system
   */
  private void removeInstruction(SimCodeModel simCodeModel)
  {
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
  }
  //----------------------------------------------------------------------
  
  public ReorderBufferItem getRobItem(int simcodeId)
  {
    return state.reorderQueue.stream().filter(robItem -> robItem.simCodeModel.getId() == simcodeId).findFirst()
            .orElse(null);
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
  public int getReorderQueueSize()
  {
    return state.reorderQueue.size();
  }// end of getReorderQueue
  //----------------------------------------------------------------------
  
  /**
   * @return Current reorder queue
   * @brief Get current Reorder queue
   */
  public Stream<ReorderBufferItem> getReorderQueue()
  {
    return state.reorderQueue.stream();
  }// end of getReorderQueue
}
