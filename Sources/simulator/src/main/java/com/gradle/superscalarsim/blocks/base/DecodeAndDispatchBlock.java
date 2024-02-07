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
import com.gradle.superscalarsim.code.CodeBranchInterpreter;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;

import java.util.ArrayList;
import java.util.Iterator;
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
   * List holding code with renamed registers ready for dispatch
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<SimCodeModel> codeBuffer;
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
   * Interpreter for interpreting branch instructions
   */
  private CodeBranchInterpreter codeBranchInterpreter;
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
                                SimulationStatistics statistics,
                                CodeBranchInterpreter codeBranchInterpreter)
  {
    this.instructionFetchBlock = instructionFetchBlock;
    this.renameMapTableBlock   = renameMapTableBlock;
    this.statistics            = statistics;
    
    this.codeBuffer       = new ArrayList<>();
    this.stallFlag        = false;
    this.stalledPullCount = 0;
    
    this.globalHistoryRegister  = globalHistoryRegister;
    this.branchTargetBuffer     = branchTargetBuffer;
    this.instructionMemoryBlock = codeParser;
    this.decodeBufferSize       = decodeBufferSize;
    this.codeBranchInterpreter  = codeBranchInterpreter;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return List of renamed instructions
   * @brief Gets list of instructions with renamed registers
   */
  public List<SimCodeModel> getCodeBuffer()
  {
    return codeBuffer;
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
  public void simulate(int cycle)
  {
    if (shouldFlush())
    {
      this.codeBuffer.clear();
      setFlush(false);
    }
    // If ROB did not pull all instructions, stall decode block
    
    stallFlag |= !this.codeBuffer.isEmpty();
    
    if (stallFlag)
    {
      // Decode is stalled. Stall fetch block (prevent fetching new instructions until decode can take the old ones)
      this.instructionFetchBlock.setStallFlag(true);
    }
    else
    {
      // Not stalled. ROB took all instructions
      this.codeBuffer.clear();
      // Copy fetched code to before rename list
      this.codeBuffer.addAll(this.instructionFetchBlock.getFetchedCode());
    }
    
    // Filter out nops and labels
    for (int i = this.codeBuffer.size() - 1; i >= 0; i--)
    {
      SimCodeModel codeModel = this.codeBuffer.get(i);
      if (codeModel.getInstructionName().equals("nop") || codeModel.getInstructionName().equals("label"))
      {
        codeModel.setFinished(true);
        this.codeBuffer.remove(i);
      }
    }
    
    // Normal for, because processBranchInstruction can remove instructions
    for (int i = 0; i < this.codeBuffer.size(); i++)
    {
      SimCodeModel simCodeModel = this.codeBuffer.get(i);
      renameSourceRegisters(simCodeModel);
      // Not sure if this does anything
      boolean renameSuccessful = renameDestinationRegister(simCodeModel);
      
      if (!renameSuccessful)
      {
        // discard, stall fetch, retry next cycle
        this.stallFlag = true;
        this.instructionFetchBlock.setStallFlag(true);
        this.codeBuffer.clear();
        return;
      }
      
      statistics.reportDecodedInstruction(simCodeModel);
      
      // Calculate branch after rename, the computation may change registers (CALL instruction)
      // TODO: maybe, a condition should disallow this in Decode
      if (simCodeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        processBranchInstruction(simCodeModel);
      }
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
    this.codeBuffer.clear();
    this.renameMapTableBlock.clear();
    this.stallFlag        = false;
    this.stalledPullCount = 0;
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @brief Checks if fetched code (beforeRenameCodeList) holds any branch instructions and processes them
   */
  private void checkForBranchInstructions()
  {
    for (int i = 0; i < this.codeBuffer.size(); i++)
    {
      SimCodeModel codeModel = this.codeBuffer.get(i);
      if (codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        processBranchInstruction(codeModel);
      }
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
    InstructionFunctionModel functionModel = simCodeModel.getInstructionFunctionModel();
    for (InstructionFunctionModel.Argument argDesc : functionModel.getArguments())
    {
      boolean shouldRename = !argDesc.writeBack() && argDesc.name().startsWith("r");
      if (shouldRename)
      {
        InputCodeArgument argument         = simCodeModel.getArgumentByName(argDesc.name());
        String            oldArgumentValue = argument.getValue();
        RegisterModel     rename           = renameMapTableBlock.getMappingForRegister(oldArgumentValue);
        argument.setRegisterValue(rename);
        if (rename.isSpeculative())
        {
          // Rename the string only if the register is speculative. This is because of register aliases
          argument.setStringValue(rename.getName());
        }
        renameMapTableBlock.increaseReference(rename.getName());
      }
    }
  }// end of renameSourceRegisters
  //----------------------------------------------------------------------
  
  /**
   * @param decodeCodeModel CodeModel with registers to be renamed
   *
   * @return True if the instruction was renamed, false otherwise (no free rename registers)
   * @brief Renames registers in instruction (1st part of Tomasulo algorithm)
   */
  private boolean renameDestinationRegister(SimCodeModel simCodeModel)
  {
    // Rename all arguments that will be written back
    for (InstructionFunctionModel.Argument argument : simCodeModel.getInstructionFunctionModel().getArguments())
    {
      if (argument.writeBack())
      {
        InputCodeArgument destinationArgument = simCodeModel.getArgumentByName(argument.name());
        
        if (!renameMapTableBlock.hasFreeRegisters())
        {
          return false;
        }
        
        RegisterModel mappedReg = renameMapTableBlock.mapRegister(destinationArgument.getValue(),
                                                                  simCodeModel.getIntegerId());
        assert mappedReg != null;
        // Set reference
        destinationArgument.setRegisterValue(mappedReg);
        destinationArgument.setStringValue(mappedReg.getName());
      }
    }
    
    return true;
  }// end of renameDestinationRegister
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Branch code model with arguments
   *
   * @brief Processes branch instructions in decode block. Can delete from the buffer.
   */
  private void processBranchInstruction(final SimCodeModel codeModel)
  {
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    
    int     instructionPosition = codeModel.getSavedPc();
    boolean unconditional       = instruction.isUnconditionalJump();
    boolean prediction          = codeModel.isBranchPredicted();
    // -1 means that entry was not predicted
    // TODO: this line is sus
    int         predTarget    = this.branchTargetBuffer.getEntryTarget(instructionPosition);
    OptionalInt realTargetOpt = calculateRealBranchAddress(codeModel);
    
    boolean globalHistoryBit = prediction && predTarget != 0;
    boolean conditionKnown   = unconditional;
    boolean targetKnown      = realTargetOpt.isPresent();
    boolean badCondition     = !prediction && unconditional;
    boolean badTarget        = targetKnown && predTarget != realTargetOpt.getAsInt();
    boolean jumpBad          = targetKnown && conditionKnown && (badCondition || badTarget);
    if (jumpBad)
    {
      // Branch badly predicted, and we know the right target and condition
      // Fix entry in BTB, set correct PC
      codeModel.setBranchPredicted(true);
      this.branchTargetBuffer.setEntry(instructionPosition, codeModel, realTargetOpt.getAsInt(),
                                       codeModel.getIntegerId(), -1);
      this.instructionFetchBlock.setPc(realTargetOpt.getAsInt());
      // Drop instructions after branch
      removeAfter(codeModel);
      globalHistoryBit = true;
    }
    this.globalHistoryRegister.shiftSpeculativeValue(codeModel.getIntegerId(), globalHistoryBit);
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
    boolean                  canCalculateAddress = true;
    InstructionFunctionModel instruction         = codeModel.getInstructionFunctionModel();
    for (InstructionFunctionModel.Argument argument : instruction.getArguments())
    {
      if (argument.isRegister() && !argument.writeBack())
      {
        canCalculateAddress = false;
        break;
      }
    }
    if (!canCalculateAddress)
    {
      return OptionalInt.empty();
    }
    
    // Arguments are not registers, we can calculate the address
    // TODO - maybe more relaxed check?
    
    Result<OptionalInt> maybeTargetRes = codeBranchInterpreter.interpretInstruction(codeModel);
    if (maybeTargetRes.isException())
    {
      // Couldn't calculate target
      return OptionalInt.empty();
    }
    return maybeTargetRes.value();
  }// end of calculateRealBranchAddress
  //----------------------------------------------------------------------
  
  /**
   * @param index index of first instruction to remove
   *
   * @brief Remove all instructions from beforeRenameCodeList from specified index until the end
   */
  private void removeAfter(SimCodeModel codeModel)
  {
    boolean                found    = false;
    Iterator<SimCodeModel> iterator = this.codeBuffer.iterator();
    while (iterator.hasNext())
    {
      SimCodeModel next = iterator.next();
      if (next.equals(codeModel))
      {
        found = true;
        continue;
      }
      if (found)
      {
        iterator.remove();
      }
    }
  }// end of removeInstructionsFromIndex
  
  /**
   * @param pulledCount Number of instructions that were pulled by the ROB
   *
   * @brief Removes instructions from the head of the buffer
   */
  public void removePulledInstructions(int pulledCount)
  {
    if (pulledCount > 0)
    {
      this.codeBuffer.subList(0, pulledCount).clear();
    }
  }// end of removePulledInstructions
  
  //----------------------------------------------------------------------
}
