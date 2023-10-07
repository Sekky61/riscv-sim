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
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.InputCodeModel;
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
  /// List for storing fetched code
  private final List<SimCodeModel> fetchedCode;
  /// GShare unit for getting correct prediction counters
  private final GShareUnit gShareUnit;
  /// Buffer holding information about branch instructions targets
  private final BranchTargetBuffer branchTargetBuffer;
  /**
   * Stack of previous PCs for backward simulation
   * TODO: Can be deleted?
   */
  private final Stack<Integer> previousPcStack;
  /// List of PC counter values indicating, which instructions were fetched
  private final List<Integer> fetchVector;
  /// Class containing parsed code
  public CodeParser parser;
  /// Marks the maximum number of instructions fetched in one tick
  private int numberOfWays;
  /// PC counter
  private int pcCounter;
  /// Flag indicating that decode block was stalled and IF should behave accordingly
  private boolean stallFlag;
  /// Number indicating how many instructions were pulled from IF by the decode block
  private int stallFetchCount;
  /// Next PC set by last fetch to be able to check if the PC changed during simulation
  private int lastPC;
  /// ID of the cycle, starting from 0
  private int cycleId;
  
  /**
   * @param [in] parser             - Class containing parsed code
   * @param [in] blockScheduleTask  - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] gShareUnit         - GShare unit for getting correct prediction counters
   * @param [in] branchTargetBuffer - Buffer holding information about branch instructions targets
   *
   * @brief Constructor
   */
  public InstructionFetchBlock(SimCodeModelAllocator simCodeModelAllocator,
                               CodeParser parser,
                               GShareUnit gShareUnit,
                               BranchTargetBuffer branchTargetBuffer)
  {
    this.simCodeModelAllocator = simCodeModelAllocator;
    this.parser                = parser;
    this.gShareUnit            = gShareUnit;
    this.branchTargetBuffer    = branchTargetBuffer;
    
    this.numberOfWays    = 3;
    this.pcCounter       = 0;
    this.fetchedCode     = new ArrayList<>();
    this.previousPcStack = new Stack<>();
    this.fetchVector     = new ArrayList<>();
    this.stallFlag       = false;
    this.stallFetchCount = 0;
    this.lastPC          = 0;
    this.cycleId         = -1;
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
   * @param [in] numberOfWays - New number of fetched instructions
   *
   * @brief Set number of fetched instructions per tick
   */
  public void setNumberOfWays(int numberOfWays)
  {
    this.numberOfWays = numberOfWays;
  }// end of setNumberOfWays
  //----------------------------------------------------------------------
  
  /**
   * @return List of fetched PC counters
   * @brief Get list of fetched PC counters in current step
   */
  public List<Integer> getFetchVector()
  {
    return fetchVector;
  }// end of getFetchVector
  //----------------------------------------------------------------------
  
  public boolean getStallFlag()
  {
    return this.stallFlag;
  }
  //----------------------------------------------------------------------
  
  /**
   * @param [in] stallFlag - New boolean value of the stall flag
   *
   * @brief Set the stall flag, that switches the IF simulation to stall mode
   */
  public void setStallFlag(boolean stallFlag)
  {
    this.stallFlag = stallFlag;
  }// end of setStallFlag
  
  public int getStallFetchCount()
  {
    return this.stallFetchCount;
  }
  
  /**
   * @param [in] stallFetchCount - The amount of instructions that were pulled (and removed) by the decode block
   *
   * @brief Set the amount of instructions that were pulled by the decode block
   */
  public void setStallFetchCount(int stallFetchCount)
  {
    this.stallFetchCount = stallFetchCount;
  }// end of setStallFetchCount
  
  public int getLastPC()
  {
    return this.lastPC;
  }
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates fetching instructions
   */
  @Override
  public void simulate()
  {
    // The only point of creating simcodemodel. Its id is unique.
    // For serialization purposes, all simcodemodels need to be located in one place.
    // Other classes handling them will get only an id to access them.
    this.cycleId++;
    if (stallFlag)
    {
      // Fetch is stalled.
      int newPcCounter = this.pcCounter;
      //Check if the PC counter was changed outside of fetch unit, if not restart last fetch
      if (this.pcCounter == this.lastPC)
      {
        newPcCounter = this.previousPcStack.empty() ? 0 : this.previousPcStack.peek();
        for (int i = 0; i < stallFetchCount; i++)
        {
          boolean branchPredicted = isBranchingPredicted(newPcCounter);
          if (branchPredicted)
          {
            newPcCounter = this.branchTargetBuffer.getEntryTarget(newPcCounter);
          }
          else
          {
            // No jump, just increment PC
            newPcCounter++;
          }
        }
      }
      this.pcCounter = newPcCounter;
      
      // All fetched instructions are about to be flushed, so they are marked as finished
      this.fetchedCode.forEach(codeModel -> codeModel.setFinished(true));
    }
    int fetchLimitLow  = Math.min(pcCounter, parser.getParsedCode().size());
    int fetchLimitHigh = Math.min(pcCounter + numberOfWays, parser.getParsedCode().size());
    this.previousPcStack.push(this.pcCounter);
    int loadedInstructions = fetchInstructions(fetchLimitLow, fetchLimitHigh);
    this.pcCounter       = this.pcCounter + loadedInstructions;
    this.lastPC          = this.pcCounter;
    this.stallFlag       = false;
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
    this.fetchVector.clear();
    this.stallFlag       = false;
    this.pcCounter       = 0;
    this.stallFetchCount = 0;
    this.lastPC          = 0;
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel      Potential branch code model
   * @param programCounter Instruction position in program
   *
   * @return Either position of current instruction or target position of branch instruction
   * @brief Checks if instruction is branch instructions and returns correct position
   */
  private int checkForBranching(SimCodeModel codeModel, int programCounter)
  {
    int result = programCounter;
    if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
    {
      codeModel.setSavedPc(programCounter);
      boolean branchPredicted = isBranchingPredicted(programCounter);
      if (branchPredicted)
      {
        result = this.branchTargetBuffer.getEntryTarget(programCounter);
      }
      codeModel.setBranchPredicted(branchPredicted);
    }
    return result;
  }// end of checkForBranching
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
   * @param [in] fetchLimitLow  - index of instruction with lowest number
   * @param [in] fetchLimitHigh - index of last instruction (exclusive range)
   *
   * @return Number of loaded instructions. Also populates fetchVector and fetchedCode
   * @brief Fetching logic
   */
  private int fetchInstructions(final int fetchLimitLow, final int fetchLimitHigh)
  {
    fetchedCode.clear();
    fetchVector.clear();
    
    int branchCount        = 0;
    int loadedInstructions = 0;
    int nopDifference      = 0;
    int probableFetchHigh  = fetchLimitHigh;
    
    int fetchCounter = 0;
    
    if (fetchLimitLow != fetchLimitHigh)
    {
      for (int i = fetchLimitLow; i < probableFetchHigh; i++)
      {
        //Check if we are not fetching past end of program
        if (i >= parser.getParsedCode().size())
        {
          break;
        }
        // Unique ID of the instruction
        // Labels and NOPs are not counted - original design generated IDs in decode, where
        // it was already filtered
        int simCodeId = this.cycleId * numberOfWays + fetchCounter;
        fetchCounter++;
        SimCodeModel codeModel = this.simCodeModelAllocator.createSimCodeModel(parser.getParsedCode().get(i), simCodeId,
                                                                               cycleId);
        checkIfLoadCodeLine(codeModel, i);
        boolean isJumpBranch = codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch;
        // TODO: Why like this? Why not end fetch after first branch?
        // Why does fetch know the instruction type?
        if (branchCount > 0 && isJumpBranch)
        {
          codeModel.setFinished(true);
          break;
        }
        
        fetchVector.add(i);
        fetchedCode.add(codeModel);
        
        // BTB - branch prediction
        int newPc = checkForBranching(codeModel, i);
        if (newPc != i)
        {
          probableFetchHigh  = fetchLimitHigh - i + newPc - 1;
          i                  = newPc - 1;
          this.pcCounter     = newPc;
          loadedInstructions = -1; // TODO: What?
        }
        
        loadedInstructions++;
        nopDifference++;
        if (isJumpBranch)
        {
          branchCount += 1;
        }
      }
    }
    int fetchDifference = numberOfWays - nopDifference;
    for (int i = 0; i < fetchDifference; i++)
    {
      fetchVector.add(fetchLimitLow + nopDifference + i);
      SimCodeModel nop = simCodeModelAllocator.createSimCodeModel(new InputCodeModel(null, "nop", null, null, null, 0),
                                                                  0, cycleId);
      fetchedCode.add(nop);
    }
    return loadedInstructions;
  }// end of fetchInstructions
  //----------------------------------------------------------------------
  
  /**
   * @param [in, out] codeModel      - Model to be checked
   * @param [in] programCounter - Current position of program counter
   *             Adds "pc" argument to codeModel
   *
   * @brief Checks if provided codeLine is load/store and if yes, fills it with additional PC argument
   */
  private void checkIfLoadCodeLine(SimCodeModel codeModel, int programCounter)
  {
    if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kLoadstore)
    {
      codeModel.setSavedPc(programCounter);
    }
  }// end of checkIfLoadCodeLine
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
