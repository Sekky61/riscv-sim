/**
 * @file InstructionFetchBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of Instruction Fetch stage
 * @date 1  February  2021 16:00 (created) \n
 * 28 April     2021 11:00 (revised)
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
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @class InstructionFetchBlock
 * @brief Class that fetches code from CodeParser
 */
public class InstructionFetchBlock implements AbstractBlock
{
  /// Allocator for allocating new SimCodeModels
  private final SimCodeModelAllocator simCodeModelAllocator;
  /// GShare unit for getting correct prediction counters
  private final GShareUnit gShareUnit;
  /// Buffer holding information about branch instructions targets
  private final BranchTargetBuffer branchTargetBuffer;
  /**
   * Stack of previous PCs for backward simulation
   * TODO: Can be deleted?
   */
  private final Stack<Integer> previousPcStack;
  /**
   * Limit on the number of times the unit can follow a branch in a single cycle
   */
  private final int branchFollowLimit;
  /// Class containing parsed code
  public InstructionMemoryBlock instructionMemoryBlock;
  /// List for storing fetched code
  private List<SimCodeModel> fetchedCode;
  /// Marks the maximum number of instructions fetched in one tick
  private int numberOfWays;
  /// PC counter
  private int pcCounter;
  /**
   * Flag indicating that decode block was stalled and IF should behave accordingly
   */
  private boolean stallFlag;
  /// Number indicating how many instructions were pulled from IF by the decode block
  private int stallFetchCount;
  /// Next PC set by last fetch to be able to check if the PC changed during simulation
  private int lastPC;
  /// ID of the cycle, starting from 0
  private int cycleId;
  
  /**
   * @param parser             Class containing parsed code
   * @param blockScheduleTask  Task class, where blocks are periodically triggered by the GlobalTimer
   * @param gShareUnit         GShare unit for getting correct prediction counters
   * @param branchTargetBuffer Buffer holding information about branch instructions targets
   *
   * @brief Constructor
   */
  public InstructionFetchBlock(SimCodeModelAllocator simCodeModelAllocator,
                               InstructionMemoryBlock parser,
                               GShareUnit gShareUnit,
                               BranchTargetBuffer branchTargetBuffer)
  {
    this.simCodeModelAllocator  = simCodeModelAllocator;
    this.instructionMemoryBlock = parser;
    this.gShareUnit             = gShareUnit;
    this.branchTargetBuffer     = branchTargetBuffer;
    
    this.numberOfWays      = 3;
    this.pcCounter         = 0;
    this.fetchedCode       = new ArrayList<>();
    this.previousPcStack   = new Stack<>();
    this.stallFlag         = false;
    this.stallFetchCount   = 0;
    this.lastPC            = 0;
    this.cycleId           = -1;
    this.branchFollowLimit = 1;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return Number of ways
   * @brief Gets number of ways
   */
  public int getNumberOfWays()
  {
    return numberOfWays;
  }// end of getNumberOfWays
  //----------------------------------------------------------------------
  
  /**
   * @param numberOfWays New number of fetched instructions
   *
   * @brief Set number of fetched instructions per tick
   */
  public void setNumberOfWays(int numberOfWays)
  {
    this.numberOfWays = numberOfWays;
  }// end of setNumberOfWays
  //----------------------------------------------------------------------
  
  /**
   * @param stallFlag New boolean value of the stall flag
   *
   * @brief Set the stall flag, that switches the IF simulation to stall mode
   */
  public void setStallFlag(boolean stallFlag)
  {
    this.stallFlag = stallFlag;
  }// end of setStallFlag
  
  /**
   * @param stallFetchCount The amount of instructions that were pulled (and removed) by the decode block
   *
   * @brief Set the amount of instructions that were pulled by the decode block
   */
  public void setStallFetchCount(int stallFetchCount)
  {
    this.stallFetchCount = stallFetchCount;
  }// end of setStallFetchCount
  
  /**
   * @brief Simulates fetching instructions
   */
  @Override
  public void simulate()
  {
    // The only point of creating SimCodeModel(s).
    this.cycleId++;
    if (stallFlag)
    {
      // Fetch is stalled. Do nothing, resume next cycle
      this.stallFlag = false;
      return;
    }
    this.previousPcStack.push(this.pcCounter);
    this.fetchedCode     = fetchInstructions();
    this.lastPC          = this.pcCounter;
    this.stallFetchCount = 0;
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the instruction fetch block
   */
  @Override
  public void reset()
  {
    this.fetchedCode.clear();
    this.previousPcStack.clear();
    this.stallFlag       = false;
    this.pcCounter       = 0;
    this.stallFetchCount = 0;
    this.lastPC          = 0;
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * Mutates PC
   *
   * @return Fetched instructions
   * @brief Fetching logic
   */
  private List<SimCodeModel> fetchInstructions()
  {
    List<SimCodeModel> fetchedCode      = new ArrayList<>();
    int                followedBranches = 0;
    int                encounteredJumps = 0;
    
    for (int i = 0; i < numberOfWays; i++)
    {
      // Unique ID of the instruction
      int simCodeId = this.cycleId * numberOfWays + i;
      SimCodeModel codeModel = this.simCodeModelAllocator.createSimCodeModel(
              instructionMemoryBlock.getInstructionAt(pcCounter), simCodeId, cycleId);
      
      codeModel.setSavedPc(pcCounter);
      
      // This if emulates the in my opinion wrong logic. Removing it will cause the program to fetch
      // instructions until a number of jumps are _followed_
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        encounteredJumps++;
        if (encounteredJumps > 1)
        {
          // Stop loading instructions, fill with nops
          codeModel.setBranchPredicted(false);
          for (int j = i; j < numberOfWays; j++)
          {
            SimCodeModel nopCodeModel = this.simCodeModelAllocator.createSimCodeModel(instructionMemoryBlock.getNop(),
                                                                                      simCodeId, cycleId);
            nopCodeModel.setSavedPc(pcCounter);
            fetchedCode.add(nopCodeModel);
          }
          break;
        }
      }
      
      // TODO: If we cannot follow anymore, do we still fetch instructions, or end early?
      // And does it matter if the branch is taken?
      boolean branchPredicted = isBranchingPredicted(pcCounter);
      if (branchPredicted && followedBranches < branchFollowLimit)
      {
        // Follow that branch
        codeModel.setBranchPredicted(true);
        int newPc = this.branchTargetBuffer.getEntryTarget(pcCounter);
        assert newPc >= 0;
        this.pcCounter = newPc;
        followedBranches++;
      }
      else
      {
        // No jump, just increment PC
        this.pcCounter += 4;
      }
      fetchedCode.add(codeModel);
    }
    return fetchedCode;
  }// end of fetchInstructions
  //----------------------------------------------------------------------
  
  /**
   * @return True if branch was predicted
   */
  private boolean isBranchingPredicted(int pc)
  {
    int     target        = this.branchTargetBuffer.getEntryTarget(pc);
    boolean prediction    = this.gShareUnit.getPredictor(pc).getCurrentPrediction();
    boolean unconditional = this.branchTargetBuffer.isEntryUnconditional(pc);
    return target != -1 && (prediction || unconditional);
  }
  //----------------------------------------------------------------------
  
  /**
   * Get fetched instructions
   *
   * @return Fetched instructions
   */
  public List<SimCodeModel> getFetchedCode()
  {
    return fetchedCode;
  }// end of getFetchedCode
  //----------------------------------------------------------------------
  
  /**
   * Gets current PC counter value
   *
   * @return PC counter value
   */
  public int getPcCounter()
  {
    return pcCounter;
  }// end of getCodeLine
  //----------------------------------------------------------------------
  
  /**
   * @param [in] pcCounter - New value of the PC counter
   *
   * @brief Set the PC counter value (used during branches)
   */
  public void setPcCounter(int pcCounter)
  {
    this.pcCounter = pcCounter;
  }// end of setPcCounter
}
