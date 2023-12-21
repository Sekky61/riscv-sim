/**
 * @file CodeInterpreter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains arithmetic interpreter
 * @date 11 November  2020 22:00 (created) \n
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
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.List;

/**
 * For the list of operations, see {@link Expression}
 *
 * @class CodeInterpreter
 * @brief Interprets instruction provided in InputCodeModel class
 */
public class CodeArithmeticInterpreter
{
  /**
   * Storage of labels and their addresses
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InstructionMemoryBlock instructionMemoryBlock;
  
  /**
   * @param registerFileBlock Preceding table for operation priorities
   * @param labelMap          Map of labels and their addresses
   *
   * @brief Constructor
   */
  public CodeArithmeticInterpreter(InstructionMemoryBlock instructionMemoryBlock)
  {
    this.instructionMemoryBlock = instructionMemoryBlock;
  }// end of Constructor
  
  /**
   * @param simCodeModel Executed instruction
   *
   * @return Double value based on interpreted instruction
   * @brief Evaluates expression
   */
  public Expression.Variable interpretInstruction(final SimCodeModel simCodeModel)
  {
    final InstructionFunctionModel instruction = simCodeModel.getInstructionFunctionModel();
    if (instruction == null)
    {
      throw new IllegalArgumentException("Instruction is null");
    }
    
    // Evaluate expression
    String                    expression = instruction.getInterpretableAs();
    List<String>              varNames   = Expression.getVariableNames(expression);
    List<Expression.Variable> variables  = simCodeModel.getVariables(varNames, instructionMemoryBlock.getLabels());
    Expression.interpret(expression, variables);
    
    // return "rd"
    return variables.stream().filter(variable -> variable.tag.equals("rd")).findFirst().orElse(null);
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
}
