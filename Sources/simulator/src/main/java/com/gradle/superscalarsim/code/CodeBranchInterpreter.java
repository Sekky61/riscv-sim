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

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.List;
import java.util.OptionalInt;

/**
 * @class CodeBranchInterpreter
 * @brief Interprets branch and jump instructions
 */
public class CodeBranchInterpreter
{
  
  /**
   * @param instructionMemoryBlock Instructions. Needed for label resolving.
   *
   * @brief Constructor
   */
  public CodeBranchInterpreter()
  {
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * The interpreted branch code has the following format: "target:condition".
   * The target is the PC that should be set.
   * The target expression can reference registers, immediate (also label) and also fixed registers (x1).
   * If you need relative jumps, use \pc in the expression.
   * <p>
   * The expression is in the reverse polish notation. See {@link Expression}.
   *
   * @param codeModel Instruction to be interpreted
   *
   * @return OptionalInt with position of next instruction to be loaded, empty if no jump is performed
   * @brief Interprets branch or jump instruction
   */
  public OptionalInt interpretInstruction(final SimCodeModel codeModel)
  {
    final InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    assert instruction != null;
    
    String[] splitInterpretableAs = instruction.getInterpretableAs().split(":");
    assert splitInterpretableAs.length == 2;
    String                    targetExpr    = splitInterpretableAs[0];
    String                    conditionExpr = splitInterpretableAs[1];
    List<Expression.Variable> variables     = codeModel.getVariables();
    
    // Check if condition is met
    Expression.Variable exprResult = Expression.interpret(conditionExpr, variables);
    assert exprResult != null;
    boolean jumpCondition = (boolean) exprResult.value.getValue(DataTypeEnum.kBool);
    if (!jumpCondition)
    {
      // Do not jump.
      return OptionalInt.empty();
    }
    
    // We know that we have to jump, calculate jump target
    Expression.Variable targetVar = Expression.interpret(targetExpr, variables);
    assert targetVar != null;
    int target = (int) targetVar.value.getValue(DataTypeEnum.kInt);
    
    // Return relative position of the instruction to jump to
    return OptionalInt.of(target);
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
}
