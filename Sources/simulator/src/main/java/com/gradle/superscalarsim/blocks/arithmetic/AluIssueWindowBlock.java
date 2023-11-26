/**
 * @file AluIssueWindowBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for ALU Issue window
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
package com.gradle.superscalarsim.blocks.arithmetic;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.blocks.base.AbstractFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.AbstractIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.code.Expression;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.InstructionFunctionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @class AluIssueWindowBlock
 * @brief Specific Issue window class for all ALU FUs (processing int or long instructions)
 */
public class AluIssueWindowBlock extends AbstractIssueWindowBlock
{
  /// List for all function units associated with this window
  @JsonIdentityReference(alwaysAsId = true)
  private List<ArithmeticFunctionUnitBlock> functionUnitBlockList;
  
  public AluIssueWindowBlock()
  {
  
  }
  
  /**
   * @param [in] blockScheduleTask - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] loader            - Initial loader of interpretable instructions and register files
   * @param [in] registerFileBlock - Class containing all registers, that simulator uses
   * @param [in] precedingTable    - Preceding table with all allowed instructions
   *
   * @brief Constructor
   */
  public AluIssueWindowBlock(UnifiedRegisterFileBlock registerFileBlock)
  {
    super(registerFileBlock);
    this.functionUnitBlockList = new ArrayList<>();
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instruction - instruction to be issued
   *
   * @return Suitable function unit
   * @brief Selects suitable function unit for certain instruction
   */
  @Override
  public AbstractFunctionUnitBlock selectSufficientFunctionUnit(InstructionFunctionModel instruction)
  {
    boolean                     anySupported         = false;
    ArithmeticFunctionUnitBlock selectedFunctionUnit = null;
    for (ArithmeticFunctionUnitBlock functionBlock : this.functionUnitBlockList)
    {
      boolean isEmpty = functionBlock.isFunctionUnitEmpty();
      boolean isSupported = isInstructionSupported(functionBlock.getAllowedOperators(),
                                                   instruction.getInterpretableAs());
      anySupported = anySupported || isSupported;
      if (isEmpty && isSupported)
      {
        selectedFunctionUnit = functionBlock;
        break;
      }
    }
    
    if (!anySupported)
    {
      throw new RuntimeException("No supported FUs for instruction: " + instruction.getInterpretableAs());
    }
    return selectedFunctionUnit;
  }// end of selectSufficientFunctionUnit
  //----------------------------------------------------------------------
  
  /**
   * @param [in] instructionType - Type of the instruction (branch, arithmetic, eg.)
   *
   * @return True if compatible, false otherwise
   * @brief Checks if provided instruction type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectInstructionType(InstructionTypeEnum instructionType)
  {
    return instructionType == InstructionTypeEnum.kArithmetic;
  }// end of isCorrectInstructionType
  //----------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type (int, float, eg.)
   *
   * @return True if compatible, false otherwise
   * @brief Checks if provided data type is compatible with this window and its FUs
   */
  @Override
  public boolean isCorrectDataType(DataTypeEnum dataType)
  {
    return dataType == DataTypeEnum.kInt || dataType == DataTypeEnum.kLong || dataType == DataTypeEnum.kUInt || dataType == DataTypeEnum.kULong;
  }// end of isCorrectDataType
  //----------------------------------------------------------------------
  
  /**
   * @brief Set number of FUs to FU for correct id creation
   */
  @Override
  public void setFunctionUnitCountInUnits()
  {
    for (ArithmeticFunctionUnitBlock functionUnitBlock : this.functionUnitBlockList)
    {
      functionUnitBlock.setFunctionUnitCount(this.functionUnitBlockList.size());
    }
  }// end of setFunctionUnitCountInUnits
  //----------------------------------------------------------------------
  
  /**
   * @param allowedInstructions      Array of allowed instructions by the FU
   * @param interpretableInstruction String containing the description of the instruction in interpretable form
   *
   * @return True if FU instruction pool supports instruction, false otherwise
   * @brief Checks if provided instruction can be executed by any function unit
   */
  private boolean isInstructionSupported(List<String> allowedInstructions, String interpretableInstruction)
  {
    List<String> requiredOperators = new ArrayList<>();
    for (String token : interpretableInstruction.split(" "))
    {
      // Compare it with all operators
      if (Arrays.asList(Expression.allOperators).contains(token))
      {
        requiredOperators.add(token);
      }
    }
    
    // Check if all required operators are supported by FU
    for (String requiredOperator : requiredOperators)
    {
      if (!allowedInstructions.contains(requiredOperator))
      {
        return false;
      }
    }
    return true;
  }// end of isInstructionSupported
  //----------------------------------------------------------------------
  
  /**
   * @param [in] functionUnitBlock - FU to bind with this window
   *
   * @brief Associate function block with this window
   */
  public void setFunctionUnitBlock(ArithmeticFunctionUnitBlock functionUnitBlock)
  {
    functionUnitBlock.setFunctionUnitId(this.functionUnitBlockList.size());
    this.functionUnitBlockList.add(functionUnitBlock);
    this.setFunctionUnitCountInUnits();
  }// end of setFunctionUnitBlock
  //----------------------------------------------------------------------
}
