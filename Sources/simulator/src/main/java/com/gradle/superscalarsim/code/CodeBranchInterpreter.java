/**
 * @file CodeBranchInterpreter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains branch interpreter
 * @date 26 December  2020 14:00 (created) \n
 * 12 May       2021 11:00 (revised)
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
package com.gradle.superscalarsim.code;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;

import java.util.List;
import java.util.OptionalInt;

/**
 * @class CodeBranchInterpreter
 * @brief Interprets branch and jump instructions
 */
public class CodeBranchInterpreter
{
  /**
   * Instructions. Needed for label resolving.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InstructionMemoryBlock instructionMemoryBlock;
  
  /**
   * @param instructionMemoryBlock Instructions. Needed for label resolving.
   *
   * @brief Constructor
   */
  public CodeBranchInterpreter(final InstructionMemoryBlock instructionMemoryBlock)
  {
    this.instructionMemoryBlock = instructionMemoryBlock;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * The interpreted branch code has the following format: "target:condition".
   * The target is the PC that should be set.
   * The target expression can reference registers, immediate (also label) and also fixed registers (x1).
   * <p>
   * The expression is in the reverse polish notation. See {@link Expression}.
   *
   * @param codeModel           Instruction to be interpreted
   * @param instructionPosition Position of interpreted instruction in source file (byte, so PC value, not instruction count)
   *
   * @return OptionalInt with position of next instruction to be loaded, empty if no jump is performed
   * @brief Interprets branch or jump instruction
   */
  public OptionalInt interpretInstruction(final SimCodeModel codeModel, int instructionPosition)
  {
    final InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    
    if (instruction == null)
    {
      throw new IllegalArgumentException("Instruction " + codeModel.getInstructionName() + " not found");
    }
    
    String[] splitInterpretableAs = instruction.getInterpretableAs().split(":");
    if (splitInterpretableAs.length != 2)
    {
      throw new IllegalArgumentException(
              "InterpretableAs in instruction " + instruction.getName() + " is not valid: " + instruction.getInterpretableAs());
    }
    String                    targetExpr    = splitInterpretableAs[0];
    String                    conditionExpr = splitInterpretableAs[1];
    List<String>              varNames      = Expression.getVariableNames(targetExpr + " " + conditionExpr);
    List<Expression.Variable> variables     = codeModel.getVariables(varNames, instructionMemoryBlock.getLabels());
    
    // Check if condition is met
    Expression.Variable exprResult    = Expression.interpret(conditionExpr, variables);
    boolean             jumpCondition = (boolean) exprResult.value.getValue(DataTypeEnum.kBool);
    if (!jumpCondition)
    {
      // Do not jump.
      return OptionalInt.empty();
    }
    
    // We know that we have to jump, calculate jump target
    
    // Label case - label is usually in imm argument, extract the position, so it can be used in expression
    InputCodeArgument labelArgument = codeModel.getArgumentByName("imm");
    if (labelArgument != null)
    {
      // todo immediate values not handled
      String labelName     = labelArgument.getValue();
      int    labelPosition = instructionMemoryBlock.getLabelPosition(labelName);
      if (labelPosition != -1)
      {
        // Label found - note the position in the variable
        Expression.Variable foundLabel = variables.stream().filter(variable -> variable.tag.equals("imm")).findFirst()
                .orElse(null);
        if (foundLabel == null)
        {
          // It was skipped in the extraction
          foundLabel = new Expression.Variable("imm", DataTypeEnum.kInt,
                                               RegisterDataContainer.fromValue(labelPosition));
          variables.add(foundLabel);
        }
        else
        {
          foundLabel.value.setValue(labelPosition, DataTypeEnum.kInt);
        }
      }
    }
    
    // Expression - calculate target
    Expression.Variable targetVar = Expression.interpret(targetExpr, variables);
    if (targetVar == null)
    {
      throw new IllegalArgumentException("Offset expression did not return any value");
    }
    int target = (int) targetVar.value.getValue(DataTypeEnum.kInt);
    
    // Return relative position of the instruction to jump to
    return OptionalInt.of(target - instructionPosition);
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
}
