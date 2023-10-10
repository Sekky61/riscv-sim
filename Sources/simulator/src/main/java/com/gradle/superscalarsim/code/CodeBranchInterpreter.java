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

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.IInputCodeModel;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.RegisterModel;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * @class CodeBranchInterpreter
 * @brief Interprets branch and jump instruction provided in InputCodeModel class
 */
public class CodeBranchInterpreter
{
  /// Pattern for matching hexadecimal values in argument
  private transient final Pattern hexadecimalPattern;
  /// Pattern for matching decimal values in argument
  private transient final Pattern decimalPattern;
  /// Object of the parser with parsed instructions
  private final CodeParser codeParser;
  /// Array of allowed operations
  private final char[] allowedOperators = {'<', '>', '=', '!'};
  private final UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * @param [in] codeParser - Object of the parser with parsed instructions
   * @param [in] initLoader - InitLoader object with loaded instructions and registers
   *
   * @brief Constructor
   */
  public CodeBranchInterpreter(final CodeParser codeParser, final UnifiedRegisterFileBlock registerFileBlock)
  {
    this.registerFileBlock  = registerFileBlock;
    this.decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    this.codeParser         = codeParser;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] parsedCode          - Parsed instruction from source file to be interpreted
   * @param [in] instructionPosition - Position of interpreted instruction in source file
   *
   * @return OptionalInt with position of next instruction to be loaded, empty if no jump is performed
   * @brief Interprets branch or jump instruction
   */
  public OptionalInt interpretInstruction(final IInputCodeModel parsedCode, int instructionPosition)
  {
    final InstructionFunctionModel instruction = parsedCode.getInstructionFunctionModel();
    
    String[] splitInterpretableAs = instruction.getInterpretableAs().split(":");
    if (splitInterpretableAs.length != 2)
    {
      throw new IllegalArgumentException(
              "InterpretableAs in instruction " + instruction.getName() + " is not valid: " + instruction.getInterpretableAs());
    }
    // Do not jump if condition is not met
    
    // First part of interpretableAs is type (jump, (un)signed condition)
    if (splitInterpretableAs[0].startsWith("jump"))
    {
      // Unconditional. Calculate jump offset
      String jumpExpr = splitInterpretableAs[1];
      if (jumpExpr.equals("imm"))
      {
        // Skip to the jumping
      }
      else if (jumpExpr.equals("rs1+imm"))
      {
        int rs1 = (int) getValueFromOperand(parsedCode.getArgumentByName("rs1").getValue(),
                                            instruction.getInputDataType());
        InputCodeArgument immArgument = parsedCode.getArgumentByName("imm");
        int               jumpOffset  = rs1 + Integer.parseInt(Objects.requireNonNull(immArgument).getValue());
        return OptionalInt.of(jumpOffset);
      }
      else
      {
        throw new IllegalArgumentException("InterpretableAs in instruction " + instruction.getName() + " is not valid");
      }
    }
    else
    {
      // example: signed:rs1 != rs2
      boolean isUnsigned = splitInterpretableAs[0].equals("unsigned");
      boolean expr = interpretExpression(splitInterpretableAs[1], parsedCode, isUnsigned,
                                         instruction.getInputDataType());
      if (!expr)
      {
        // Do not jump, load another instruction in sequence
        return OptionalInt.empty();
      }
    }
    
    // Do jump
    
    InputCodeArgument labelArgument = parsedCode.getArgumentByName("imm");
    if (labelArgument != null)
    {
      //The instruction uses label as jump target offset
      int labelPosition = codeParser.getLabelPosition(labelArgument.getValue());
      if (labelPosition == -1)
      {
        // Literal value (no such label found)
        return OptionalInt.of(Integer.parseInt(labelArgument.getValue()));
      }
      else
      {
        // Label found
        return OptionalInt.of(labelPosition - instructionPosition);
      }
    }
    else
    {
      //The instructions uses register as jump target offset
      InputCodeArgument jumpArgument = parsedCode.getArgumentByName("rs1");
      int returnPosition = (int) getValueFromOperand(Objects.requireNonNull(jumpArgument).getValue(),
                                                     instruction.getInputDataType());
      return OptionalInt.of(returnPosition - instructionPosition + 1);
    }
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] expression - Expression to interpret
   * @param [in] parsedCode - Parsed instruction from source file to be interpreted
   * @param [in] isUnsigned - Flag telling if function should load values from registers in unsigned
   *             or signed representation
   *
   * @return True if condition is met, false otherwise
   * @brief Interprets branch expression
   */
  private boolean interpretExpression(final String expression,
                                      final IInputCodeModel parsedCode,
                                      final boolean isUnsigned,
                                      final DataTypeEnum inputDataType)
  {
    String temp = "";
    
    StringBuilder operandStringBuilder  = new StringBuilder();
    StringBuilder operatorStringBuilder = new StringBuilder();
    
    for (char character : expression.toCharArray())
    {
      if (String.valueOf(this.allowedOperators).indexOf(character) >= 0)
      {
        if (operandStringBuilder.length() != 0)
        {
          temp = operandStringBuilder.toString();
          operandStringBuilder.setLength(0);
        }
        operatorStringBuilder.append(character);
      }
      else if (character != ' ')
      {
        operandStringBuilder.append(character);
      }
    }
    
    String leftOperand  = temp;
    String rightOperand = operandStringBuilder.toString();
    String operator     = operatorStringBuilder.toString();
    
    InputCodeArgument leftOperandArgument  = parsedCode.getArgumentByName(leftOperand);
    InputCodeArgument rightOperandArgument = parsedCode.getArgumentByName(rightOperand);
    
    // If any of operands is not found, it may be an immediate value
    if (leftOperandArgument == null)
    {
      leftOperandArgument = new InputCodeArgument(leftOperand, leftOperand);
    }
    if (rightOperandArgument == null)
    {
      rightOperandArgument = new InputCodeArgument(rightOperand, rightOperand);
    }
    
    return evaluateExpression(Objects.requireNonNull(leftOperandArgument).getValue(),
                              Objects.requireNonNull(rightOperandArgument).getValue(), operator, isUnsigned,
                              inputDataType);
  }// end of interpretExpression
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] leftOperand  - Left operand in expression
   * @param [in] rightOperand - Right operand in expression
   * @param [in] operator     - Operator of the condition
   * @param [in] isUnsigned   - Flag telling if function should load values from registers in unsigned
   *             or signed representation
   *
   * @return True if condition is met, false otherwise
   * @brief Evaluate expression based on provided arguments
   */
  private boolean evaluateExpression(final String leftOperand,
                                     final String rightOperand,
                                     final String operator,
                                     final boolean isUnsigned,
                                     final DataTypeEnum inputDataType)
  {
    long leftOperandValue  = (int) getValueFromOperand(leftOperand, inputDataType);
    long rightOperandValue = (int) getValueFromOperand(rightOperand, inputDataType);
    
    if (isUnsigned)
    {
      leftOperandValue  = Integer.toUnsignedLong((int) leftOperandValue);
      rightOperandValue = Integer.toUnsignedLong((int) rightOperandValue);
    }
    
    return switch (operator)
    {
      case "<" -> leftOperandValue < rightOperandValue;
      case "<=" -> leftOperandValue <= rightOperandValue;
      case "==" -> leftOperandValue == rightOperandValue;
      case "!=" -> leftOperandValue != rightOperandValue;
      case ">=" -> leftOperandValue >= rightOperandValue;
      case ">" -> leftOperandValue > rightOperandValue;
      default -> false;
    };
  }// end of evaluateExpression
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand - Value to be parsed, either immediate (hex or decimal) or register
   * @param [in] dataType - Result data type
   *
   * @return Parsed double value from operand
   * @brief Gets value from string operand
   */
  private double getValueFromOperand(String operand, DataTypeEnum dataType)
  {
    // Checks if value is immediate
    if (hexadecimalPattern.matcher(operand).matches())
    {
      return Long.parseLong(operand.substring(2), 16);
    }
    else if (decimalPattern.matcher(operand).matches())
    {
      return Double.parseDouble(operand);
    }
    
    // operand is a register
    DataTypeEnum[] dataTypeEnums = getFitRegisterTypes(dataType);
    RegisterModel  registerModel = null;
    for (DataTypeEnum possibleDataType : dataTypeEnums)
    {
      registerModel = this.registerFileBlock.getRegisterList(possibleDataType).stream()
              .filter(register -> register.getName().equals(operand)).findFirst().orElse(null);
      if (registerModel != null)
      {
        break;
      }
    }
    
    if (registerModel == null)
    {
      // Not found in register files, look in speculative register file
      registerModel = this.registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative).stream()
              .filter(register -> register.getName().equals(operand)).findFirst().orElse(null);
    }
    
    if (registerModel == null)
    {
      throw new IllegalArgumentException("Register " + operand + " not found");
    }
    
    return registerModel.getValue();
  }// end of getValueFromOperand
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type of the register
   *
   * @return List of datatypes in which input datatype can fit
   * @brief Get list of data types in which specified data type can fit
   */
  private DataTypeEnum[] getFitRegisterTypes(DataTypeEnum dataType)
  {
    return switch (dataType)
    {
      case kInt -> new DataTypeEnum[]{DataTypeEnum.kInt, DataTypeEnum.kLong};
      case kLong -> new DataTypeEnum[]{DataTypeEnum.kLong};
      case kFloat -> new DataTypeEnum[]{DataTypeEnum.kFloat, DataTypeEnum.kDouble};
      case kDouble -> new DataTypeEnum[]{DataTypeEnum.kDouble};
      case kSpeculative -> new DataTypeEnum[]{};
    };
  }// end of getFitRegisterTypes
  //-------------------------------------------------------------------------------------------
}
