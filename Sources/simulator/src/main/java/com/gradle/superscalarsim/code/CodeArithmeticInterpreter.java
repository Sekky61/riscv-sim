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

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.util.Result;

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
   * @param registerFileBlock Preceding table for operation priorities
   * @param labelMap          Map of labels and their addresses
   *
   * @brief Constructor
   */
  public CodeArithmeticInterpreter()
  {
  }// end of Constructor
  
  /**
   * @param simCodeModel Executed instruction
   *
   * @return Double value based on interpreted instruction
   * @brief Evaluates expression
   */
  public Result<Expression.Variable> interpretInstruction(final SimCodeModel simCodeModel)
  {
    final InstructionFunctionModel instruction = simCodeModel.instructionFunctionModel();
    if (instruction == null)
    {
      throw new IllegalArgumentException("Instruction is null");
    }
    
    // Evaluate expression
    String                      expression = instruction.interpretableAs();
    List<Expression.Variable>   variables  = simCodeModel.getVariables();
    Result<Expression.Variable> result     = Expression.interpret(expression, variables);
    
    if (result.isException())
    {
      return result.convertException();
    }
    
    // return "rd"
    Expression.Variable rd = variables.stream().filter(variable -> variable.tag.equals("rd")).findFirst().orElse(null);
    
    if (rd != null)
    {
      DataTypeEnum resultType = instruction.getArgumentByName("rd").type();
      rd.value.setCurrentType(resultType);
    }
    return new Result<>(rd);
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
}
