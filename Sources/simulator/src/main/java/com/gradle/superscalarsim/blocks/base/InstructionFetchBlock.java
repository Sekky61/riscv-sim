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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.factories.SimCodeModelFactory;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class InstructionFetchBlock
 * @brief Class that fetches code from CodeParser
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class InstructionFetchBlock implements AbstractBlock
{
  /**
   * Allocator for SimCodeModels
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final SimCodeModelFactory simCodeModelFactory;
  
  /**
   * GShare unit for getting correct prediction counters
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final GShareUnit gShareUnit;
  
  /**
   * Buffer holding information about branch instructions targets
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final BranchTargetBuffer branchTargetBuffer;
  
  /**
   * Limit on the number of times the unit can follow a branch in a single cycle
   */
  private final int branchFollowLimit;
  
  /**
   * Class containing parsed code
   */
  @JsonIdentityReference(alwaysAsId = true)
  public InstructionMemoryBlock instructionMemoryBlock;
  
  /**
   * List for storing fetched code
   */
  @JsonIdentityReference(alwaysAsId = true)
  private List<SimCodeModel> fetchedCode;
  
  /**
   * Marks the maximum number of instructions fetched in one tick
   */
  private int numberOfWays;
  
  /**
   * Current Program Counter - pointer to the next instruction to be fetched
   */
  private int pc;
  
  /**
   * Flag indicating that decode block was stalled and IF should behave accordingly
   */
  private boolean stallFlag;
  
  /**
   * @param parser             Class containing parsed code
   * @param blockScheduleTask  Task class, where blocks are periodically triggered by the GlobalTimer
   * @param gShareUnit         GShare unit for getting correct prediction counters
   * @param branchTargetBuffer Buffer holding information about branch instructions targets
   *
   * @brief Constructor
   */
  public InstructionFetchBlock(int numberOfWays,
                               int branchFollowLimit,
                               SimCodeModelFactory simCodeModelAllocator,
                               InstructionMemoryBlock parser,
                               GShareUnit gShareUnit,
                               BranchTargetBuffer branchTargetBuffer)
  {
    this.simCodeModelFactory    = simCodeModelAllocator;
    this.instructionMemoryBlock = parser;
    this.gShareUnit             = gShareUnit;
    this.branchTargetBuffer     = branchTargetBuffer;
    
    this.numberOfWays      = numberOfWays;
    this.pc                = 0;
    this.fetchedCode       = new ArrayList<>();
    this.stallFlag         = false;
    this.branchFollowLimit = branchFollowLimit;
  }// end of Constructor
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
   * @brief Simulates fetching instructions. The only point of creating SimCodeModel(s).
   */
  @Override
  public void simulate(int cycle)
  {
    if (stallFlag)
    {
      // Fetch is stalled. Do nothing, resume next cycle
      stallFlag = false;
      return;
    }
    fetchedCode.clear();
    fetchInstructions(cycle);
  }// end of simulate
  //----------------------------------------------------------------------
  
  //----------------------------------------------------------------------
  
  /**
   * Mutates PC.
   * Fetches instructions from the memory.
   * If there is an entry in the BTB, it will follow the branch.
   * Otherwise, it will fetch following instructions.
   *
   * @brief Fetching logic
   */
  private void fetchInstructions(int cycle)
  {
    int followedBranches = 0;
    int encounteredJumps = 0;
    
    for (int i = 0; i < numberOfWays; i++)
    {
      // Unique ID of the instruction
      int simCodeId = cycle * numberOfWays + i;
      SimCodeModel codeModel = this.simCodeModelFactory.createInstance(instructionMemoryBlock.getInstructionAt(pc),
                                                                       simCodeId, cycle);
      
      // This if emulates the in my opinion wrong logic. Removing it will cause the program to fetch
      // instructions until a number of jumps are _followed_
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        encounteredJumps++;
        if (encounteredJumps > branchFollowLimit)
        {
          // Stop loading instructions, fill with nops
          //          codeModel.setBranchPredicted(false, codeModel.getSavedPc() + 4);
          for (int j = i; j < numberOfWays; j++)
          {
            SimCodeModel nopCodeModel = this.simCodeModelFactory.createInstance(instructionMemoryBlock.getNop(),
                                                                                cycle * numberOfWays + j, cycle);
            fetchedCode.add(nopCodeModel);
          }
          break;
        }
        
        int predictorIndex = gShareUnit.getPredictorIndex(pc);
        int predictorState = gShareUnit.getPredictor(pc).getState();
        
        boolean unconditional = this.branchTargetBuffer.isEntryUnconditional(pc);
        boolean prediction    = this.gShareUnit.getPredictor(pc).getCurrentPrediction();
        boolean shouldJump    = prediction || unconditional;
        int     newPc         = this.branchTargetBuffer.getEntryTarget(pc);
        boolean areWeJumping  = shouldJump && newPc >= 0;
        codeModel.registerBranch(predictorIndex, predictorState, areWeJumping, newPc);
        if (areWeJumping && followedBranches < branchFollowLimit)
        {
          this.pc = newPc;
          followedBranches++;
        }
        else
        {
          // No jump, just increment PC
          this.pc += 4;
        }
        // Update global history register
        if (codeModel.isConditionalBranch())
        {
          gShareUnit.getGlobalHistoryRegister().shiftValue(areWeJumping, codeModel.getIntegerId());
        }
      }
      else
      {
        this.pc += 4;
      }
      
      fetchedCode.add(codeModel);
    }
  }// end of fetchInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Clears fetched code buffer
   */
  public void flush()
  {
    this.fetchedCode.clear();
  }
  //----------------------------------------------------------------------
  
  /**
   * @return Number of instructions to pull. Basically filters out nops.
   */
  public int getPullCount()
  {
    int count = 0;
    for (SimCodeModel simCode : this.getFetchedCode())
    {
      if (!simCode.getInstructionName().equals("nop"))
      {
        count++;
      }
    }
    return count;
  }
  
  /**
   * Get fetched instructions
   *
   * @return Fetched instructions
   */
  public List<SimCodeModel> getFetchedCode()
  {
    return fetchedCode;
  }// end of getFetchedCode
  
  /**
   * Gets current PC counter value
   *
   * @return PC counter value
   */
  public int getPc()
  {
    return pc;
  }// end of getCodeLine
  //----------------------------------------------------------------------
  
  /**
   * @param pc New value of the PC counter
   *
   * @brief Set the PC value (used during branches)
   */
  public void setPc(int pc)
  {
    assert pc % 4 == 0;
    assert pc >= 0;
    this.pc = pc;
  }// end of setPcCounter
}
