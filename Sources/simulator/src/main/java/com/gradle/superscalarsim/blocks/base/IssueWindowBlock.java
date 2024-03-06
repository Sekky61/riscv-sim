/**
 * @file AbstractIssueWindowBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains abstract class for all Issue Windows
 * @date 9  February   2021 16:00 (created) \n
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class AbstractIssueWindowBlock
 * @brief Abstract class, containing interface and shared logic for all Issuing windows
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class IssueWindowBlock implements AbstractBlock
{
  /**
   * List of all instructions dispatched to this window, with their arguments.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<SimCodeModel> issuedInstructions;
  /**
   * ID counter specifying which issue window did the instruction took
   */
  protected int windowId;
  /**
   * List of all function units associated with this window
   */
  private List<AbstractFunctionUnitBlock> functionUnitBlockList;
  /**
   * Type of the instructions this window can hold.
   * Arithmetic is further differentiated into int and float.
   */
  private InstructionTypeEnum instructionType;
  
  /**
   * @param registerFileBlock Class containing all registers, that simulator uses
   *
   * @brief Constructor
   */
  public IssueWindowBlock(InstructionTypeEnum instructionType)
  {
    this.issuedInstructions    = new ArrayList<>();
    this.functionUnitBlockList = new ArrayList<>();
    
    this.instructionType = instructionType;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param instruction Instruction description
   *
   * @return True if compatible, false otherwise.
   * @brief Checks if provided instruction is compatible with this window.
   * TODO: Where should the conversion instructions execute (float to int, eg.)?
   */
  public boolean canHold(InstructionFunctionModel instruction)
  {
    return instruction.getInstructionType() == this.instructionType;
  }
  
  /**
   * @return Issue Instruction list
   * @brief Gets Issued Instruction list
   */
  public List<SimCodeModel> getIssuedInstructions()
  {
    return issuedInstructions;
  }// end of getIssuedInstructions
  
  /**
   * @brief Simulates issuing instructions to FUs
   * Shared behavior for all issue windows
   */
  @Override
  public void simulate(int cycle)
  {
    removeFailedInstructions();
    
    // If there are any issues with skipping, this is the place to fix it - simCodes are removed form iterated list
    outer:
    for (int i = 0; i < this.issuedInstructions.size(); i++)
    {
      SimCodeModel currentModel = this.issuedInstructions.get(i);
      if (!currentModel.isReadyToExecute())
      {
        continue;
      }
      
      // Iterate the function units
      boolean eligibleFound = false;
      for (AbstractFunctionUnitBlock functionUnitBlock : functionUnitBlockList)
      {
        
        boolean isMatch = functionUnitBlock.canExecuteInstruction(currentModel);
        // Can instruction be issued?
        if (!isMatch)
        {
          continue;
        }
        eligibleFound = true;
        
        if (functionUnitBlock.isBusy())
        {
          // FU is taken
          continue;
        }
        
        // Instruction is ready for execution and there is a free FU -> issue the instruction
        functionUnitBlock.setSimCodeModel(currentModel);
        functionUnitBlock.setDelayBasedOnInstruction();
        // Remove the instruction from the list
        this.issuedInstructions.remove(i);
        i--;
        // This FU is taken
        continue outer;
      }
      
      // If the instruction got here, it is ready to execute, but there is no free FU
      // A wrong configuration for the given code
      if (!eligibleFound)
      {
        throw new IllegalStateException(
                "No eligible FU found for instruction: " + currentModel.getInstructionFunctionModel().getName());
      }
    }
    
    this.windowId = cycle;
  }
  
  /**
   * @brief Checks for instructions that were removed because of bad prediction and removes them from the window
   */
  private void removeFailedInstructions()
  {
    // Iterate backwards to avoid messing up the indices
    for (int i = this.issuedInstructions.size() - 1; i >= 0; i--)
    {
      SimCodeModel codeModel = this.issuedInstructions.get(i);
      if (codeModel.hasFailed())
      {
        this.issuedInstructions.remove(i);
      }
    }
  }// end of checkForFailedInstructions
  
  //----------------------------------------------------------------------
  
  /**
   * @param codeModel Instruction to be added
   *
   * @brief Adds new instruction to window list
   */
  public void dispatchInstruction(SimCodeModel codeModel)
  {
    this.issuedInstructions.add(codeModel);
    codeModel.setIssueWindowId(this.windowId);
  }// end of dispatchInstruction
  //----------------------------------------------------------------------
  
  /**
   * @param functionUnitBlock Function unit to be added
   *
   * @brief Adds new function unit to the list
   */
  public void addFunctionUnit(AbstractFunctionUnitBlock functionUnitBlock)
  {
    this.functionUnitBlockList.add(functionUnitBlock);
  }
}
