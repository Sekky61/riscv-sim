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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * @class DecodeAndDispatchBlock
 * @brief Class, which simulates instruction decode and renames registers
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class DecodeAndDispatchBlock implements AbstractBlock
{
  /**
   * List holding code before renaming for difference highlight
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<SimCodeModel> beforeRenameCodeList;
  /**
   * List holding code with renamed registers ready for dispatch
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<SimCodeModel> afterRenameCodeList;
  /**
   * Instruction Fetch Block holding fetched instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InstructionFetchBlock instructionFetchBlock;
  /**
   * Class holding all mappings from architectural to speculative
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final RenameMapTableBlock renameMapTableBlock;
  /**
   * Bit register marking history of predictions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final GlobalHistoryRegister globalHistoryRegister;
  /**
   * Buffer holding information about branch instructions targets
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final BranchTargetBuffer branchTargetBuffer;
  /**
   * Parser holding parsed instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InstructionMemoryBlock instructionMemoryBlock;
  /**
   * Parser holding parsed instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final SimulationStatistics statistics;
  /**
   * Counter giving out ids for instructions in order to correctly simulate backwards
   */
  private int idCounter;
  /**
   * Boolean flag indicating if the decode block should be flushed
   */
  private boolean flush;
  /**
   * Boolean flag indicating that one of the buffers is full and should simulate according to that state
   */
  private boolean stallFlag;
  /**
   * @brief Number of instructions that is pulled by the ROB
   * Relevant only when the decode block is stalled.
   */
  private int stalledPullCount;
  
  /**
   * Decode buffer size limit
   * TODO: Make configurable
   */
  private int decodeBufferSize;
  
  /**
   * @param instructionFetchBlock Block fetching N instructions each clock event
   * @param renameMapTableBlock   Class holding mappings from architectural registers to speculative
   * @param globalHistoryRegister A bit register holding history of predictions
   * @param branchTargetBuffer    Buffer holding information about branch instructions targets
   * @param codeParser            Parser holding parsed instructions
   * @param decodeBufferSize      Size of the decode buffer
   * @param statistics            Statistics class holding information about the run
   *
   * @brief Constructor
   */
  public DecodeAndDispatchBlock(InstructionFetchBlock instructionFetchBlock,
                                RenameMapTableBlock renameMapTableBlock,
                                GlobalHistoryRegister globalHistoryRegister,
                                BranchTargetBuffer branchTargetBuffer,
                                InstructionMemoryBlock codeParser,
                                int decodeBufferSize,
                                SimulationStatistics statistics)
  {
    this.instructionFetchBlock = instructionFetchBlock;
    this.renameMapTableBlock   = renameMapTableBlock;
    this.statistics            = statistics;
    
    this.beforeRenameCodeList = new ArrayList<>();
    this.afterRenameCodeList  = new ArrayList<>();
    this.idCounter            = -2; // todo
    this.stallFlag            = false;
    this.stalledPullCount     = 0;
    
    this.globalHistoryRegister  = globalHistoryRegister;
    this.branchTargetBuffer     = branchTargetBuffer;
    this.instructionMemoryBlock = codeParser;
    this.decodeBufferSize       = decodeBufferSize;
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
   * @param stalledPullCount Number of the pulled instructions
   *
   * @brief Set the number of instructions, that were pulled by the Reorder buffer
   */
  public void setStalledPullCount(int stalledPullCount)
  {
    this.stalledPullCount = stalledPullCount;
  }// end of setStalledPullCount
  //----------------------------------------------------------------------
  
  /**
   * @param flush New value of the flush flag
   *
   * @brief Sets the flush flag
   */
  public void setFlush(boolean flush)
  {
    this.flush = flush;
  }// end of setFlush
  //----------------------------------------------------------------------
  
  /**
   * @param shouldStall New boolean value of the stall flag
   *
   * @brief Sets the stall flag
   */
  public void setStallFlag(boolean shouldStall)
  {
    this.stallFlag = shouldStall;
  }// end of setStallFlag
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
      
      // Remove instructions loaded by ROB
      List<SimCodeModel> removeModel = new ArrayList<>();
      for (int i = 0; i < this.afterRenameCodeList.size(); i++)
      {
        SimCodeModel codeModel = this.afterRenameCodeList.get(i);
        if (i < stalledPullCount)
        {
          removeModel.add(codeModel);
        }
      }
      this.afterRenameCodeList.removeAll(removeModel);
      
      // Take `fetchCount` instructions from fetch block, delete them from fetch block
      // TODO: is fetchCount correct given that it is configurable?
      List<SimCodeModel> removeInputModel = new ArrayList<>();
      int fetchCount = Math.min((int) this.instructionFetchBlock.getFetchedCode().stream()
                                        .filter(code -> !code.getInstructionName().equals("nop")).count(),
                                this.instructionFetchBlock.getNumberOfWays() - this.afterRenameCodeList.size());
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
    for (int i = this.beforeRenameCodeList.size() - 1; i >= 0; i--)
    {
      SimCodeModel codeModel = this.beforeRenameCodeList.get(i);
      if (codeModel.getInstructionName().equals("nop") || codeModel.getInstructionName().equals("label"))
      {
        codeModel.setFinished(true);
        this.beforeRenameCodeList.remove(i);
      }
    }
    
    checkForBranchInstructions();
    for (SimCodeModel simCodeModel : this.beforeRenameCodeList)
    {
      renameSourceRegisters(simCodeModel);
      // Not sure if this does anything
      renameDestinationRegister(simCodeModel);
      this.afterRenameCodeList.add(simCodeModel);
      statistics.reportDecodedInstruction(simCodeModel);
    }
    
    // Rename ended, report map table to statistics
    statistics.reportAllocatedRegisters(renameMapTableBlock.getAllocatedSpeculativeRegistersCount());
    
    this.stallFlag        = false;
    this.stalledPullCount = 0;
  }// end of simulate
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
    this.idCounter        = -2;
    this.stallFlag        = false;
    this.stalledPullCount = 0;
  }// end of reset
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
   * @param decodeCodeModel CodeModel with registers to be renamed
   *
   * @brief Checks map table if source registers were renamed in past and renames them in instruction (RAW conflict)
   */
  private void renameSourceRegisters(SimCodeModel simCodeModel)
  {
    for (InputCodeArgument argument : simCodeModel.getArguments())
    {
      String  oldArgumentValue = argument.getValue();
      boolean shouldRename     = !argument.getName().equals("rd") && !argument.getName().equals("imm");
      if (shouldRename)
      {
        RegisterModel rename = renameMapTableBlock.getMappingForRegister(oldArgumentValue);
        argument.setRegisterValue(rename);
        if (rename.isSpeculative())
        {
          // Rename the string only if the register is speculative. This is because of register aliases
          argument.setValue(rename.getName());
        }
        renameMapTableBlock.increaseReference(rename.getName());
      }
    }
  }// end of renameSourceRegisters
  //----------------------------------------------------------------------
  
  /**
   * @param decodeCodeModel CodeModel with registers to be renamed
   *
   * @brief Renames registers in instruction (1st part of Tomasulo algorithm)
   */
  private void renameDestinationRegister(SimCodeModel simCodeModel)
  {
    // Rename all arguments that will be written back
    for (InstructionFunctionModel.Argument argument : simCodeModel.getInstructionFunctionModel().getArguments())
    {
      if (argument.writeBack())
      {
        InputCodeArgument destinationArgument = simCodeModel.getArgumentByName(argument.name());
        RegisterModel mappedReg = renameMapTableBlock.mapRegister(destinationArgument.getValue(),
                                                                  simCodeModel.getIntegerId());
        assert mappedReg != null;
        // Get reference
        destinationArgument.setRegisterValue(mappedReg);
        if (mappedReg.isSpeculative())
        {
          // Rename the string only if the register is speculative. This is because of register aliases
          destinationArgument.setValue(mappedReg.getName());
        }
      }
    }
  }// end of renameDestinationRegister
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Branch code model with arguments
   * @param position  Position of instruction in a fetched block
   * @param modelId   future ID that this instruction will receive
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
    int         predTarget    = this.branchTargetBuffer.getEntryTarget(instructionPosition);
    OptionalInt realTargetOpt = calculateRealBranchAddress(codeModel);
    int         realTarget    = realTargetOpt.orElse(-1);
    
    boolean globalHistoryBit = prediction && predTarget != 0;
    boolean shouldJump       = unconditional || prediction;
    boolean jumpBad          = shouldJump && realTarget != predTarget && realTargetOpt.isPresent();
    if (jumpBad)
    {
      // Branch badly predicted
      // Fix entry in BTB, set correct PC
      codeModel.setBranchPredicted(true);
      this.branchTargetBuffer.setEntry(instructionPosition, codeModel, realTarget, modelId, -1);
      this.instructionFetchBlock.setPc(realTarget);
      // Drop instructions after branch
      removeInstructionsFromIndex(position + 1);
      globalHistoryBit = true;
    }
    this.globalHistoryRegister.shiftSpeculativeValue(modelId, globalHistoryBit);
  }// end of processBranchInstruction
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Branch code model
   *
   * @return Branch jump target if possible to calculate in decode stage, empty otherwise
   * @brief Calculates branch instruction target if possible (label or offset)
   */
  private OptionalInt calculateRealBranchAddress(final SimCodeModel codeModel)
  {
    // TODO: jalr instruction bases offset on register value, can we handle that?
    // TODO: also jr, ret instruction
    // Or, should it fall through and be caught later as bad prediction?
    InputCodeArgument immediateArgument = codeModel.getArgumentByName("imm");
    if (immediateArgument == null)
    {
      // Cannot predict, continue on next instruction
      return OptionalInt.empty();
      //      throw new RuntimeException("Branch instruction does not have immediate argument");
    }
    // If there is label, get its position
    int labelOffset = instructionMemoryBlock.getLabelPosition(immediateArgument.getValue());
    if (labelOffset == -1)
    {
      // No label found -- this must be a relative jump (imm is relative to current pc)
      int x = codeModel.getSavedPc() + Integer.parseInt(immediateArgument.getValue());
      return OptionalInt.of(x);
    }
    // Jump after label
    return OptionalInt.of(labelOffset);
  }// end of calculateRealBranchAddress
  //----------------------------------------------------------------------
  
  /**
   * @param index index of first instruction to remove
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
}
