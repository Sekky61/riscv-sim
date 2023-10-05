/**
 * @file DecodeAndDispatchBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for decoding and renaming fetched code
 * @date 3  February  2021 16:00 (created) \n
 * 27 April     2021 20:00 (revised)
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
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @class DecodeAndDispatchBlock
 * @brief Class, which simulates instruction decode and renames registers
 */
public class DecodeAndDispatchBlock implements AbstractBlock
{
  /// List holding code before renaming for difference highlight
  private final List<SimCodeModel> beforeRenameCodeList;
  /// List holding code with renamed registers ready for dispatch
  private final List<SimCodeModel> afterRenameCodeList;
  /// Stack of ids, which indicates points in time when the decode block was stalled (used in backward simulation)
  private final Stack<Integer> stallIdStack;
  /// Stack of the amounts of instructions that were pulled from instruction fetch when the decode block was stalling
  private final Stack<Integer> stalledPullCountStack;
  /// Instruction Fetch Block holding fetched instructions
  private final InstructionFetchBlock instructionFetchBlock;
  private final SimCodeModelAllocator simCodeModelAllocator;
  /// Class holding all mappings from architectural to speculative
  private final RenameMapTableBlock renameMapTableBlock;
  /// Bit register marking history of predictions
  private final GlobalHistoryRegister globalHistoryRegister;
  /// Buffer holding information about branch instructions targets
  private final BranchTargetBuffer branchTargetBuffer;
  /// Parser holding parsed instructions
  private final CodeParser codeParser;
  /// InitLoader class holding information about instruction and registers
  private final InitLoader initLoader;
  /// Counter giving out ids for instructions in order to correctly simulate backwards
  private int idCounter;
  /// Boolean flag indicating if the decode block should be flushed
  private boolean flush;
  /// Boolean flag indicating that one of the buffers is full and should simulate according to that state
  private boolean stallFlag;
  /**
   * @brief Number of instructions that is pulled by the ROB
   * Relevant only when the decode block is stalled.
   */
  private int stalledPullCount;
  
  /**
   * @param [in] instructionFetchBlock - Block fetching N instructions each clock event
   * @param [in] blockScheduleTask     - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] renameMapTableBlock   - Class holding mappings from architectural registers to speculative
   * @param [in] globalHistoryRegister - Bit register holding history of predictions
   * @param [in] branchTargetBuffer    - Buffer holding information about branch instructions targets
   * @param [in] codeParser            - Parser holding parsed instructions
   * @param [in] initLoader            - InitLoader class holding information about instruction and registers
   *
   * @brief Constructor
   */
  public DecodeAndDispatchBlock(SimCodeModelAllocator simCodeModelAllocator,
                                InstructionFetchBlock instructionFetchBlock,
                                RenameMapTableBlock renameMapTableBlock,
                                GlobalHistoryRegister globalHistoryRegister,
                                BranchTargetBuffer branchTargetBuffer,
                                CodeParser codeParser,
                                InitLoader initLoader)
  {
    this.simCodeModelAllocator = simCodeModelAllocator;
    this.instructionFetchBlock = instructionFetchBlock;
    this.renameMapTableBlock   = renameMapTableBlock;
    
    this.beforeRenameCodeList  = new ArrayList<>();
    this.afterRenameCodeList   = new ArrayList<>();
    this.stallIdStack          = new Stack<>();
    this.stalledPullCountStack = new Stack<>();
    this.idCounter             = -2;
    this.stallFlag             = false;
    this.stalledPullCount      = 0;
    
    this.globalHistoryRegister = globalHistoryRegister;
    this.branchTargetBuffer    = branchTargetBuffer;
    this.codeParser            = codeParser;
    this.initLoader            = initLoader;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return List of instructions before renaming
   * @brief Gets list of instructions before renaming
   */
  public List<SimCodeModel> getBeforeRenameCodeList()
  {
    return beforeRenameCodeList;
  }// end of getBeforeRenameCodeList
  //----------------------------------------------------------------------
  
  /**
   * @return List of renamed instructions
   * @brief Gets list of instructions with renamed registers
   */
  public List<SimCodeModel> getAfterRenameCodeList()
  {
    return afterRenameCodeList;
  }// end of getAfterRenameCodeList
  //----------------------------------------------------------------------
  
  
  /**
   * @return Boolean value of the flush flag
   * @brief Gets the boolean value ot the flush flag
   */
  public boolean shouldFlush()
  {
    return flush;
  }// end of shouldFlush
  //----------------------------------------------------------------------
  
  /**
   * @return Boolean value of the stall flag
   * @brief Gets the boolean value ot the stall flag
   */
  public boolean shouldStall()
  {
    return this.stallFlag;
  }// end of shouldStall
  //----------------------------------------------------------------------
  
  /**
   * @return Number of instruction, that were pulled by the reorder buffer
   * @brief Get number of instruction, that were pulled by the reorder buffer
   */
  public int getStalledPullCount()
  {
    return stalledPullCount;
  }// end of getStalledPullCount
  //----------------------------------------------------------------------
  
  /**
   * @param [in] stalledPullCount - Number of the pulled instructions
   *
   * @brief Set the number of instructions, that were pulled by the Reorder buffer
   */
  public void setStalledPullCount(int stalledPullCount)
  {
    this.stalledPullCount = stalledPullCount;
  }// end of setStalledPullCount
  //----------------------------------------------------------------------
  
  /**
   * @param [in] flush - New value of the flush flag
   *
   * @brief Sets the flush flag
   */
  public void setFlush(boolean flush)
  {
    this.flush = flush;
  }// end of setFlush
  //----------------------------------------------------------------------
  
  /**
   * @param [in] shouldStall - New boolean value of the stall flag
   *
   * @brief Sets the stall flag
   */
  public void setStallFlag(boolean shouldStall)
  {
    this.stallFlag = shouldStall;
  }// end of setStallFlag
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the decode block
   */
  @Override
  public void reset()
  {
    this.beforeRenameCodeList.clear();
    this.afterRenameCodeList.clear();
    this.renameMapTableBlock.clear();
    this.stallIdStack.clear();
    this.stalledPullCountStack.clear();
    this.idCounter        = -2;
    this.stallFlag        = false;
    this.stalledPullCount = 0;
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates decoding and renaming of instructions before dispatching
   */
  @Override
  public void simulate()
  {
    upStepId();
    int decodeId = getCurrentStepId();
    this.beforeRenameCodeList.clear();
    if (stallFlag)
    {
      // Decode is stalled.
      // Stall fetch block
      this.instructionFetchBlock.setStallFlag(true);
      this.stallIdStack.push(decodeId);
      
      // Remove instructions loaded by ROB
      List<SimCodeModel> removeModel = new ArrayList<>();
      for (int i = 0; i < this.afterRenameCodeList.size(); i++)
      {
        SimCodeModel codeModel = this.afterRenameCodeList.get(i);
        if (i < stalledPullCount)
        {
          removeModel.add(codeModel);
        }
        else
        {
          codeModel.setInstructionBulkNumber(decodeId);
        }
      }
      this.afterRenameCodeList.removeAll(removeModel);
      
      // Take `fetchCount` instructions from fetch block, delete them from fetch block
      List<SimCodeModel> removeInputModel = new ArrayList<>();
      int fetchCount = Math.min((int) this.instructionFetchBlock.getFetchedCode()
                                                                .stream()
                                                                .filter(
                                                                    code -> !code.getInstructionName().equals("nop"))
                                                                .count(),
                                this.instructionFetchBlock.getNumberOfWays() - this.afterRenameCodeList.size());
      this.stalledPullCountStack.push(fetchCount);
      this.instructionFetchBlock.setStallFetchCount(fetchCount);
      for (int i = 0; i < fetchCount; i++)
      {
        SimCodeModel codeModel = this.instructionFetchBlock.getFetchedCode().get(i);
        this.beforeRenameCodeList.add(codeModel);
        removeInputModel.add(codeModel);
      }
      this.instructionFetchBlock.getFetchedCode().removeAll(removeInputModel);
    }
    else
    {
      // ROB took all instructions
      this.afterRenameCodeList.clear();
      // Copy fetched code to before rename list
      this.beforeRenameCodeList.addAll(this.instructionFetchBlock.getFetchedCode());
    }
    
    // Filter out nops and labels
    this.beforeRenameCodeList.removeIf(code -> code.getInstructionName().equals("nop"));
    this.beforeRenameCodeList.removeIf(code -> code.getInstructionName().equals("label"));
    
    checkForBranchInstructions();
    for (SimCodeModel simCodeModel : this.beforeRenameCodeList)
    {
      renameSourceRegisters(simCodeModel);
      // Not sure this does anything
      renameDestinationRegister(simCodeModel);
      this.afterRenameCodeList.add(simCodeModel);
    }
    
    this.stallFlag        = false;
    this.stalledPullCount = 0;
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Simulates backwards (unmaps speculative registers)
   */
  @Override
  public void simulateBackwards()
  {
    int removeCount = this.afterRenameCodeList.size();
    if (!this.stallIdStack.empty() && getCurrentStepId() == this.stallIdStack.peek())
    {
      this.stallIdStack.pop();
      removeCount = this.stalledPullCountStack.pop();
    }
    lowerStepId();
    int                removeLowerBound = this.afterRenameCodeList.size() - removeCount;
    List<SimCodeModel> removeList       = new ArrayList<>();
    for (int i = this.afterRenameCodeList.size() - 1; i >= removeLowerBound; i--)
    {
      SimCodeModel codeModel = this.afterRenameCodeList.get(i);
      removeList.add(codeModel);
      codeModel.getArguments().forEach(argument ->
                                       {
                                         if (!codeModel.getCodeLine().contains(
                                             argument.getValue()) && this.renameMapTableBlock.reduceReference(
                                             argument.getValue()))
                                         {
                                           this.renameMapTableBlock.freeMapping(argument.getValue());
                                         }
                                       });
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        int savedPc = codeModel.getSavedPc();
        branchTargetBuffer.resetEntry(savedPc, codeModel.getId(), -1);
        globalHistoryRegister.revertFromHistory(codeModel.getId());
      }
    }
    this.afterRenameCodeList.removeAll(removeList);
    for (SimCodeModel simCodeModel : this.afterRenameCodeList)
    {
      simCodeModel.setInstructionBulkNumber(getCurrentStepId());
    }
  }// end of simulateBackwards
  //----------------------------------------------------------------------
  
  /**
   * @brief Increment ID for next decode step for backward simulation
   */
  private void upStepId()
  {
    idCounter = idCounter + 1;
  }// end of upStepId
  //----------------------------------------------------------------------
  
  /**
   * @return Id of current step
   * @brief Gets id for current block step
   */
  public int getCurrentStepId()
  {
    return idCounter;
  }// end of getCurrentStepId
  //----------------------------------------------------------------------
  
  /**
   * @brief Decrement ID for next decode step for backward simulation
   */
  private void lowerStepId()
  {
    idCounter = idCounter <= -2 ? -2 : idCounter - 1;
  }// end of lowerStepId
  //----------------------------------------------------------------------
  
  /**
   * @param [in,out] decodeCodeModel - CodeModel with registers to be renamed
   *
   * @brief Renames registers in instruction (1st part of Tomasulo algorithm)
   */
  private void renameDestinationRegister(SimCodeModel simCodeModel)
  {
    // Find rd, rename it
    InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
    if (destinationArgument == null)
    {
      return;
    }
    
    // Rename
    destinationArgument.setValue(renameMapTableBlock.mapRegister(destinationArgument.getValue(), simCodeModel.getId()));
  }// end of renameDestinationRegister
  //----------------------------------------------------------------------
  
  /**
   * @param [in,out] decodeCodeModel - CodeModel with registers to be renamed
   *
   * @brief Checks map table if source registers were renamed in past and renames them in instruction (RAW conflict)
   */
  private void renameSourceRegisters(SimCodeModel simCodeModel)
  {
    simCodeModel.getArguments().forEach(argument ->
                                        {
                                          String oldArgumentValue = argument.getValue();
                                          boolean shouldRename = !argument.getName().equals("rd") && !argument.getName()
                                                                                                              .equals(
                                                                                                                  "imm");
                                          if (shouldRename)
                                          {
                                            String rename = renameMapTableBlock.getMappingForRegister(oldArgumentValue);
                                            argument.setValue(rename);
                                            renameMapTableBlock.increaseReference(rename);
                                          }
                                        });
  }// end of renameSourceRegisters
  //----------------------------------------------------------------------
  
  /**
   * @brief Checks if fetched code (beforeRenameCodeList) holds any branch instructions and processes them
   */
  private void checkForBranchInstructions()
  {
    int modelId = idCounter * this.instructionFetchBlock.getNumberOfWays();
    for (int i = 0; i < this.beforeRenameCodeList.size(); i++)
    {
      SimCodeModel codeModel = this.beforeRenameCodeList.get(i);
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        processBranchInstruction(codeModel, i, modelId);
      }
      modelId++;
    }
  }// end of checkForBranchInstructions
  //----------------------------------------------------------------------
  
  /**
   * @param [in,out] codeModel - Branch code model with arguments
   * @param [in]     position  - Position of instruction in a fetched block
   * @param [in]     modelId   - future Id that this instruction will receive
   *
   * @brief Processes branch instructions in decode block
   */
  private void processBranchInstruction(final SimCodeModel codeModel, int position, int modelId)
  {
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    
    int     instructionPosition = codeModel.getSavedPc();
    boolean unconditional       = instruction.isUnconditionalJump();
    boolean prediction          = codeModel.isBranchPredicted();
    // -1 means that entry was not predicted
    // TODO: this line is sus
    int predTarget = this.branchTargetBuffer.getEntryTarget(instructionPosition);
    int realTarget = calculateRealBranchAddress(codeModel);
    
    boolean globalHistoryBit = prediction && predTarget != 0;
    boolean jumpBad          = unconditional && realTarget != predTarget;
    boolean branchBad        = !unconditional && prediction && realTarget != predTarget;
    if (jumpBad || branchBad)
    {
      // Branch badly predicted
      // Fix entry in BTB, set correct PC
      codeModel.setBranchPredicted(true);
      this.branchTargetBuffer.setEntry(instructionPosition, codeModel, realTarget, modelId, -1);
      this.instructionFetchBlock.setPcCounter(realTarget);
      // Drop instructions after branch
      removeInstructionsFromIndex(position + 1);
      globalHistoryBit = true;
    }
    this.globalHistoryRegister.shiftSpeculativeValue(modelId, globalHistoryBit);
  }// end of processBranchInstruction
  //----------------------------------------------------------------------
  
  /**
   * @param [in] index - index of first instruction to remove
   *
   * @brief Remove all instructions from beforeRenameCodeList from specified index until the end
   */
  private void removeInstructionsFromIndex(int index)
  {
    while (index < this.beforeRenameCodeList.size())
    {
      this.beforeRenameCodeList.remove(index);
    }
  }// end of removeInstructionsFromIndex
  //----------------------------------------------------------------------
  
  /**
   * @param [in] codeModel - branch code model
   *
   * @return Branch jump target, regardless of prediction
   * @brief Calculates branch instruction target if possible (label or offset)
   */
  private int calculateRealBranchAddress(final SimCodeModel codeModel)
  {
    // TODO: jalr instruction bases offset on register value, can we handle that?
    // Or, should it fall through and be caught later as bad prediction?
    InputCodeArgument immediateArgument = codeModel.getArgumentByName("imm");
    if (immediateArgument == null)
    {
      throw new RuntimeException("Branch instruction does not have immediate argument");
    }
    // If there is label, get its position
    int labelOffset = codeParser.getLabelPosition(immediateArgument.getValue());
    if (labelOffset == -1)
    {
      // No label found -- this must be a relative jump (imm is relative to current pc)
      return codeModel.getSavedPc() + Integer.parseInt(immediateArgument.getValue());
    }
    // Jump after label
    return labelOffset + 1;
  }// end of calculateRealBranchAddress
  //----------------------------------------------------------------------
}
