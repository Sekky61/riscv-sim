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
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.blocks.loadstore.LoadBufferBlock;
import com.gradle.superscalarsim.blocks.loadstore.StoreBufferBlock;
import com.gradle.superscalarsim.cpu.DebugLog;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.cpu.StopReason;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.instruction.DebugInfo;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.memory.LoadBufferItem;
import com.gradle.superscalarsim.models.memory.StoreBufferItem;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @class ReorderBufferBlock
 * @brief Class contains simulated implementation of Reorder buffer.
 * @details The BTB entry is updated, regardless of the prediction result.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class ReorderBufferBlock implements AbstractBlock
{
  
  /**
   * Queue of scheduled instruction in backend
   */
  @JsonIdentityReference(alwaysAsId = true)
  public ArrayDeque<SimCodeModel> reorderQueue;
  
  /**
   * Numerical limit, how many instruction can be committed in a single tick
   */
  public int commitLimit;
  
  /**
   * @brief Flag to be set if the simulation should halt
   */
  public StopReason stopReason;
  /**
   * Debug log
   */
  @JsonIdentityReference(alwaysAsId = true)
  DebugLog debugLog;
  /**
   * Reorder buffer size limit.
   */
  private int bufferSize;
  /**
   * @brief the jump target address triggering the halt
   */
  private long haltTarget;
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
  private SimulationStatistics simulationStatistics;
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
  /**
   * Issues instructions to individual issue windows
   */
  private IssueWindowSuperBlock issueWindowSuperBlock;
  
  public ReorderBufferBlock()
  {
  }
  
  /**
   * @param bufferSize             Reorder buffer size limit
   * @param commitLimit            Numerical limit, how many instruction can be committed in a single tick
   * @param renameMapTableBlock    Class holding mappings from architectural registers to speculative
   * @param decodeAndDispatchBlock Class, which simulates instruction decode and renames registers
   * @param storeBufferBlock       Class that tracks all in-flight store instructions
   * @param loadBufferBlock        Class that tracks all in-flight load instructions
   * @param gShareUnit             GShare unit for getting correct prediction counters
   * @param branchTargetBuffer     Buffer holding information about branch instructions targets
   * @param instructionFetchBlock  Class that fetches code from CodeParser
   * @param statisticsCounter      Class for statistics gathering
   *
   * @brief Constructor
   */
  public ReorderBufferBlock(int bufferSize,
                            int commitLimit,
                            RenameMapTableBlock renameMapTableBlock,
                            DecodeAndDispatchBlock decodeAndDispatchBlock,
                            StoreBufferBlock storeBufferBlock,
                            LoadBufferBlock loadBufferBlock,
                            IssueWindowSuperBlock issueWindowSuperBlock,
                            GShareUnit gShareUnit,
                            BranchTargetBuffer branchTargetBuffer,
                            InstructionFetchBlock instructionFetchBlock,
                            SimulationStatistics statisticsCounter,
                            long haltTarget,
                            DebugLog debugLog)
  {
    this.renameMapTableBlock    = renameMapTableBlock;
    this.decodeAndDispatchBlock = decodeAndDispatchBlock;
    this.storeBufferBlock       = storeBufferBlock;
    this.loadBufferBlock        = loadBufferBlock;
    this.issueWindowSuperBlock  = issueWindowSuperBlock;
    
    this.gShareUnit            = gShareUnit;
    this.branchTargetBuffer    = branchTargetBuffer;
    this.instructionFetchBlock = instructionFetchBlock;
    
    this.simulationStatistics = statisticsCounter;
    
    this.reorderQueue = new ArrayDeque<>();
    
    this.commitLimit = commitLimit;
    this.bufferSize  = bufferSize;
    this.stopReason  = StopReason.kNotStopped;
    this.haltTarget  = haltTarget;
    this.debugLog    = debugLog;
  }// end of Constructor
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
  public void simulate(int cycle)
  {
    // Go through queue and commit all instructions you can
    // until you reach un-committable instruction, or you reach limit
    int commitCount = 0;
    while (commitCount < this.commitLimit && !this.reorderQueue.isEmpty() && this.stopReason == StopReason.kNotStopped)
    {
      SimCodeModel robItem = this.reorderQueue.peek();
      
      if (!robItem.isReadyToBeCommitted())
      {
        break;
      }
      
      if (robItem.getException() != null)
      {
        // TODO do we want to commit this? maybe
        stopReason = StopReason.kException;
      }
      
      commitCount++;
      commitInstruction(robItem, cycle);
      removeInstruction(robItem);
      
      if (robItem.getBranchTarget() == haltTarget)
      {
        stopReason = StopReason.kCallStackHalt;
      }
      
      // Remove item from the front of the queue
      this.reorderQueue.remove();
    }
    
    // Check all instructions if after commit some can be removed, remove them in other units
    flushInvalidInstructions(cycle);
    
    // Pull new instructions from decoder
    pullNewDecodedInstructions();
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * Writes into architectural register, updates statistics
   *
   * @param codeModel Code model of committable instruction
   * @param cycle     Current cycle
   *
   * @brief Process instruction that is ready to be committed
   */
  private void commitInstruction(SimCodeModel codeModel, int cycle)
  {
    codeModel.setCommitId(cycle);
    simulationStatistics.reportCommittedInstruction(codeModel);
    if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
    {
      boolean branchActuallyTaken = codeModel.isBranchLogicResult();
      int     pc                  = codeModel.getSavedPc();
      
      // Update global history register
      // TODO: before or after feedback to predictor?
      if (codeModel.isConditionalBranch())
      {
        gShareUnit.getGlobalHistoryRegister().shiftValue(branchActuallyTaken);
      }
      
      // Feedback to predictor
      // TODO look into gshareunit
      this.gShareUnit.getPredictor(pc).sendFeedback(branchActuallyTaken);
      this.branchTargetBuffer.setEntry(pc, codeModel, codeModel.getBranchTarget());
      
      int nextPc = pc + 4;
      if (branchActuallyTaken)
      {
        nextPc = codeModel.getBranchTarget();
        // Update branch target
      }
      
      int     nextPredictedPc = pc + 4;
      boolean predictionMade  = codeModel.isBranchPredicted() && codeModel.getBranchPredictionTarget() != -1;
      if (predictionMade)
      {
        // A successful positive prediction took place back in fetch
        nextPredictedPc = codeModel.getBranchPredictionTarget();
      }
      
      boolean correctAddressPrediction = nextPc == nextPredictedPc; // pc+4 is predicted if not taken
      if (correctAddressPrediction || codeModel.isBranchComputedInDecode())
      {
        // The instruction stream is correct at this point
        // Update committable status of subsequent instructions
        validateInstructions();
      }
      else
      {
        // The next instruction is wrong. Flush the queue.
        Optional<SimCodeModel> robItem = this.reorderQueue.stream().skip(1).findFirst();
        if (robItem.isPresent())
        {
          invalidateInstructions(robItem.get());
        }
        
        // Feedback to predictor
        // Update target in BTB (branch target, regardless of if it was taken or not and if target was predicted correctly)
        this.branchTargetBuffer.setEntry(pc, codeModel, codeModel.getBranchTarget());
        this.instructionFetchBlock.setPc(nextPc);
        this.decodeAndDispatchBlock.flush();
        this.instructionFetchBlock.flush();
        // Note the flush in statistics
        simulationStatistics.incrementRobFlushes();
      }
    }
    else if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kLoadstore)
    {
      if (codeModel.isStore())
      {
        assert storeBufferBlock.getStoreQueueFirst() == codeModel; // store should be at the front of the queue
        // Store checks later loads that are already executed
        StoreBufferItem storeBufferItem = storeBufferBlock.getStoreBufferItem(codeModel.getIntegerId());
        LoadBufferItem badLoad = loadBufferBlock.findConflictingLoad(storeBufferItem.getAddress(),
                                                                     codeModel.getIntegerId());
        if (badLoad != null)
        {
          // Bad load found, invalidate it
          SimCodeModel model = badLoad.getSimCodeModel();
          invalidateInstructions(model);
          // Repeat the bad load
          this.instructionFetchBlock.setPc(model.getSavedPc());
          // Note the flush in statistics
          simulationStatistics.incrementRobFlushes();
        }
        // Release store buffer entry
        storeBufferBlock.releaseStoreFirst();
      }
      else
      {
        // Release load buffer entry
        // todo assert it?
        if (loadBufferBlock.getQueueSize() > 0 && loadBufferBlock.getLoadQueueFirst() == codeModel)
        {
          loadBufferBlock.releaseLoadFirst();
        }
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
    
    // Arch registers are now updated, print debug info
    DebugInfo debugInfo = codeModel.getDebugInfo();
    if (debugInfo != null)
    {
      debugLog.add(debugInfo, cycle);
    }
  }// end of processCommittableInstruction
  
  /**
   * Sets finished flag, reduces references to speculative registers
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
    
    simCodeModel.setFinished(true);
  }
  //----------------------------------------------------------------------
  
  /**
   * It does not stop at the first valid instruction, but flushes all invalid instructions.
   * Assumes that every instruction after the first invalid instruction is invalid.
   *
   * @brief Removes all invalid (ready to removed) instructions from ROB
   */
  public void flushInvalidInstructions(int cycle)
  {
    // Iterate the queue from the end, remove until first valid instruction
    Iterator<SimCodeModel> it = this.reorderQueue.descendingIterator();
    while (it.hasNext())
    {
      SimCodeModel robItem = it.next();
      if (!robItem.shouldBeRemoved())
      {
        break;
      }
      
      // Notify all that instruction is invalid
      simulationStatistics.incrementFailedInstructions();
      robItem.setCommitId(cycle); // todo: is this correct?
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
    for (SimCodeModel codeModel : this.decodeAndDispatchBlock.getCodeBuffer())
    {
      boolean robFull            = this.bufferSize < (this.reorderQueue.size() + 1);
      boolean instructionHasRoom = !robFull && loadBufferBlock.hasSpace() && storeBufferBlock.hasSpace();
      if (!instructionHasRoom)
      {
        // No more space, stop
        this.decodeAndDispatchBlock.setStallFlag(true);
        break;
      }
      
      SimCodeModel last        = this.reorderQueue.peekLast();
      boolean      speculative = last != null && (last.isSpeculative() || last.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch);
      
      codeModel.setSpeculative(speculative);
      this.reorderQueue.add(codeModel);
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
    
    // Remove pulledCount instructions from decode
    this.decodeAndDispatchBlock.removePulledInstructions(pulledCount);
  }// end of pullNewDecodedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Mark all instructions from top of the queue to the first branch instruction as non-speculative
   */
  private void validateInstructions()
  {
    boolean skipFirst = true;
    for (SimCodeModel item : this.reorderQueue)
    {
      if (skipFirst)
      {
        // Skip because the first instruction is the branch that caused the speculation
        skipFirst = false;
        continue;
      }
      item.setSpeculative(false);
      
      if (item.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
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
    for (SimCodeModel robItem : this.reorderQueue)
    {
      flush = flush || robItem == firstInvalidInstruction;
      if (flush)
      {
        robItem.setSpeculative(false);
        robItem.setValid(false);
        robItem.setHasFailed(true);
      }
    }
    
    // TODO: move to fetch and decode block
    this.decodeAndDispatchBlock.getCodeBuffer().forEach(
            simCodeModel -> simCodeModel.getArguments().stream().filter(argument -> argument.getName().startsWith("r"))
                    .forEach(argument ->
                             {
                               if (renameMapTableBlock.reduceReference(argument.getValue()))
                               {
                                 renameMapTableBlock.freeMapping(argument.getValue());
                               }
                             }));
    // clear what you can
    this.decodeAndDispatchBlock.flush();
    this.instructionFetchBlock.flush();
  }// end of invalidateInstructions
  //----------------------------------------------------------------------
  
  public SimCodeModel getRobItem(int simCodeId)
  {
    return this.reorderQueue.stream().filter(robItem -> robItem.getIntegerId() == simCodeId).findFirst().orElse(null);
  }// end of getFlagsMap
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
  public Stream<SimCodeModel> getReorderQueue()
  {
    return this.reorderQueue.stream();
  }// end of getReorderQueue
  
  /**
   *
   */
  public void simulate_issue(int tick)
  {
    // Issue instruction without a IssueWindowId
    for (SimCodeModel codeModel : this.reorderQueue)
    {
      if (codeModel.issueWindowId == -1)
      {
        issueWindowSuperBlock.selectCorrectIssueWindow(codeModel.getInstructionFunctionModel(), codeModel);
      }
    }
  }
}
