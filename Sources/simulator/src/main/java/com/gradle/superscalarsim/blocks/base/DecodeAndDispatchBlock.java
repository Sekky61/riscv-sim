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
import com.gradle.superscalarsim.code.CodeBranchInterpreter;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;

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
   * Buffer holding information about branch instructions targets
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final BranchTargetBuffer branchTargetBuffer;
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
   * Boolean flag indicating that one of the buffers is full and should simulate according to that state
   */
  private boolean stallFlag;
  
  /**
   * Decode buffer size limit. Usually set to the width of the fetch block.
   * TODO: Make configurable
   */
  private int decodeBufferSize;
  
  /**
   * @param instructionFetchBlock Block fetching N instructions each clock event
   * @param renameMapTableBlock   Class holding mappings from architectural registers to speculative
   * @param branchTargetBuffer    Buffer holding information about branch instructions targets
   * @param decodeBufferSize      Size of the decode buffer
   * @param statistics            Statistics class holding information about the run
   * @param codeBranchInterpreter Interpreter for interpreting branch instructions
   *
   * @brief Constructor
   */
  public DecodeAndDispatchBlock(InstructionFetchBlock instructionFetchBlock,
                                RenameMapTableBlock renameMapTableBlock,
                                BranchTargetBuffer branchTargetBuffer,
                                int decodeBufferSize,
                                SimulationStatistics statistics,
                                CodeBranchInterpreter codeBranchInterpreter)
  {
    this.instructionFetchBlock = instructionFetchBlock;
    this.renameMapTableBlock   = renameMapTableBlock;
    this.statistics            = statistics;
    
    this.codeBuffer = new ArrayList<>();
    this.stallFlag  = false;
    
    this.branchTargetBuffer    = branchTargetBuffer;
    this.decodeBufferSize      = decodeBufferSize;
    this.codeBranchInterpreter = codeBranchInterpreter;
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
   * @param shouldStall New boolean value of the stall flag
   *
   * @brief Sets the stall flag
   */
  public void setStallFlag(boolean shouldStall)
  {
    this.stallFlag = shouldStall;
  }// end of setStallFlag
  //----------------------------------------------------------------------
  
  @Override
  public void simulate(int cycle)
  {
    decode();
    
    // Report map table to statistics
    statistics.reportAllocatedRegisters(renameMapTableBlock.getAllocatedSpeculativeRegistersCount());
    this.stallFlag = false;
  }
  
  /**
   * @brief Simulates decoding and renaming of instructions before dispatching.
   * In normal operation, the buffer should be empty at the beginning of the cycle.
   */
  public void decode()
  {
    // If ROB did not pull all instructions, stall decode block
    if (stallFlag)
    {
      // Decode is stalled. Stall fetch block (prevent fetching new instructions until decode can take the old ones)
      this.instructionFetchBlock.setStallFlag(true);
      return;
    }
    
    // Check if all new destinations can be renamed
    int freeRegisters = renameMapTableBlock.getFreeRegistersCount();
    int pullCount     = instructionFetchBlock.getPullCount();
    if (freeRegisters < pullCount)
    {
      // Not enough registers to rename
      this.instructionFetchBlock.setStallFlag(true);
      this.codeBuffer.clear();
      return;
    }
    
    // Normal for!, because processBranchInstruction can remove instructions
    for (int i = 0; i < pullCount; i++)
    {
      SimCodeModel simCodeModel = this.instructionFetchBlock.getFetchedCode().get(i);
      this.codeBuffer.add(simCodeModel);
      renameSourceRegisters(simCodeModel);
      boolean renameSuccessful = renameDestinationRegister(simCodeModel);
      assert renameSuccessful;
      
      statistics.reportDecodedInstruction(simCodeModel);
      
      // Calculate branch after rename, the computation may change registers (CALL instruction)
      // TODO: maybe, a condition should disallow this in Decode
      if (simCodeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch)
      {
        boolean stop = processBranchInstruction(simCodeModel);
        if (stop)
        {
          break;
        }
      }
    }
  }// end of simulate
  //----------------------------------------------------------------------
  
  /**
   * @brief Clears the decode buffer
   */
  public void flush()
  {
    this.codeBuffer.clear();
  }// end of setFlush
  //----------------------------------------------------------------------
  
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
      boolean shouldRename = !argDesc.writeBack() && argDesc.isRegister();
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
   * @return True if the consequent instructions should be dropped
   * @brief Processes branch instructions in decode block. Can delete from the buffer.
   * @details Some of the branch instructions can be calculated in decode stage.
   */
  private boolean processBranchInstruction(final SimCodeModel codeModel)
  {
    boolean                  flush       = false;
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    
    int     instructionPosition = codeModel.getSavedPc();
    boolean unconditional       = instruction.isUnconditionalJump();
    boolean prediction          = codeModel.isBranchPredicted();
    // -1 means that entry was not predicted
    // TODO: this line is sus
    int         predTarget    = this.branchTargetBuffer.getEntryTarget(instructionPosition);
    OptionalInt realTargetOpt = calculateRealBranchAddress(codeModel);
    
    boolean targetKnown    = realTargetOpt.isPresent();
    boolean conditionKnown = unconditional;
    boolean doJump         = unconditional;
    if (targetKnown && conditionKnown)
    {
      codeModel.setBranchComputedInDecode();
      // The jump can be calculated in decode stage
      // Compare to the prediction and fix if needed
      int     target       = realTargetOpt.getAsInt();
      boolean badTarget    = predTarget != target;
      boolean badCondition = prediction != doJump;
      boolean jumpBad      = badCondition || badTarget;
      
      if (jumpBad)
      {
        // Branch badly predicted,fFix entry in BTB, set correct PC
        codeModel.setBranchTarget(target);
        this.branchTargetBuffer.setEntry(instructionPosition, codeModel, target);
        this.instructionFetchBlock.setPc(target);
        // Drop instructions after branch
        flush = true;
      }
      
      // TODO: A GHT update might be missing here, though the issue may not surface since we only work with unconditional jumps here
    }
    
    return flush;
  }// end of processBranchInstruction
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Branch code model
   *
   * @return Branch jump absolute target if possible to calculate in decode stage, empty otherwise
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
    
    Result<CodeBranchInterpreter.BranchResult> targetRes = codeBranchInterpreter.interpretInstruction(codeModel);
    // I don't think jump target uses division
    assert !targetRes.isException();
    return OptionalInt.of(targetRes.value().target());
  }// end of calculateRealBranchAddress
  //----------------------------------------------------------------------
  
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
