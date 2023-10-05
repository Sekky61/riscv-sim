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

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.PrecedingPriorityEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @class CodeInterpreter
 * @brief Interprets instruction provided in InputCodeModel class
 */
public class CodeArithmeticInterpreter
{
  /// Pattern for matching hexadecimal values in argument
  private transient final Pattern hexadecimalPattern;
  /// Pattern for matching decimal values in argument
  private transient final Pattern decimalPattern;
  /// InitLoader object with loaded instructions and registers
  private final InitLoader initLoader;
  /// Preceding table object
  private final PrecedingTable precedingTable;
  /// Stack used by interpreter to store operations with less priority
  private final Stack<String> operationStack;
  /// Stack to store values loaded from expression from left to right
  private final Stack<String> valueStack;
  private final UnifiedRegisterFileBlock registerFileBlock;
  /// String value of last evaluated lValue
  private String temporaryTag;
  /// Value of last evaluated lValue
  private double temporaryValue;
  
  /**
   * @param [in] initLoader     - Initial loader of interpretable instructions and register files
   * @param [in] precedingTable - Preceding table for operation priorities
   *
   * @brief Constructor
   */
  public CodeArithmeticInterpreter(final InitLoader initLoader,
                                   final PrecedingTable precedingTable,
                                   final UnifiedRegisterFileBlock registerFileBlock)
  {
    this.initLoader         = initLoader;
    this.registerFileBlock  = registerFileBlock;
    this.decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    this.precedingTable     = precedingTable;
    this.operationStack     = new Stack<>();
    this.valueStack         = new Stack<>();
    this.temporaryTag       = "";
    this.temporaryValue     = 0.0;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] parsedCode - Parsed instruction from code file to be interpreted
   *
   * @return Double value based on interpreted instruction
   * @brief Evaluates expressions divided by semicolon ';'
   */
  public double interpretInstruction(final IInputCodeModel parsedCode)
  {
    final InstructionFunctionModel instruction = parsedCode.getInstructionFunctionModel();
    if (instruction == null)
    {
      return Double.NaN;
    }
    
    String[] splitInterpretable = instruction.getInterpretableAs().split(";");
    for (String command : splitInterpretable)
    {
      int    equalIndex = command.indexOf('=');
      String lValue     = command.substring(0, equalIndex);
      String expression = command.substring(equalIndex + 1);
      
      // Find result arg (for example rd:t0)
      InputCodeArgument resultArg = parsedCode.getArguments()
                                              .stream()
                                              .filter(arg -> lValue.contains(arg.getName()))
                                              .findFirst()
                                              .orElse(null);
      OperandModel resultOperand = new OperandModel(lValue, resultArg);
      double result = evaluateExpression(expression, instruction.getInputDataType(), instruction.getOutputDataType(),
                                         parsedCode.getArguments());
      
      this.temporaryTag   = resultOperand.getValue();
      this.temporaryValue = resultOperand.getBitHigh() == -1 ? result : writeSpecificBits(resultOperand, (long) result,
                                                                                          (long) this.temporaryValue);
    }
    
    return Double.parseDouble(convertDoubleToDatatype(this.temporaryValue, instruction.getOutputDataType()));
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] expression     - Expression to be interpreted
   * @param [in] inputDataType  - Data type of expression
   * @param [in] outputDataType - Data type of the result
   * @param [in] argumentList   - List of arguments provided by parsed instruction
   *
   * @return Double value of expression
   * @brief Evaluates one expression
   */
  private double evaluateExpression(final String expression,
                                    final DataTypeEnum inputDataType,
                                    final DataTypeEnum outputDataType,
                                    final List<InputCodeArgument> argumentList)
  {
    // Clear stacks for new expression
    operationStack.clear();
    valueStack.clear();
    operationStack.push("$");
    
    char[]        expressionCharArray   = expression.toCharArray();
    StringBuilder valueStringBuilder    = new StringBuilder();
    StringBuilder operatorStringBuilder = new StringBuilder();
    
    // Load until the expressionCharArray is read
    for (char character : expressionCharArray)
    {
      // Current char is part of allowed operation
      if (this.precedingTable.isAllowedOperation(operatorStringBuilder.toString() + character))
      {
        if (!valueStringBuilder.isEmpty())
        {
          valueStack.push(valueStringBuilder.toString());
        }
        valueStringBuilder.setLength(0);
        operatorStringBuilder.append(character);
      }
      // Current char is not part of operation, but we have loaded some operation
      else if (!operatorStringBuilder.isEmpty())
      {
        String operation = operatorStringBuilder.toString();
        operatorStringBuilder.setLength(0);
        if (this.precedingTable.isAllowedOperation(String.valueOf(character)))
        {
          operatorStringBuilder.append(character);
        }
        else
        {
          valueStringBuilder.append(character);
        }
        evaluateAllUnaryOperations(argumentList, inputDataType);
        PrecedingPriorityEnum priority = this.precedingTable.getPrecedingPriority(operationStack.peek(), operation);
        switch (priority)
        {
          case kError:
            return Double.NaN;
          case kEvaluate:
            String stackOperation = operationStack.pop();
            double result = evaluateOperation(stackOperation, argumentList, inputDataType);
            valueStack.push(convertDoubleToDatatype(result, inputDataType));
            operationStack.push(operation);
            break;
          case kPush:
            operationStack.push(operation);
            break;
        }
      }
      // Currently read char is part of value (register or immediate)
      else
      {
        valueStringBuilder.append(character);
      }
    }// End of array reading
    
    // Empty out the operation stack
    if (!valueStringBuilder.isEmpty())
    {
      valueStack.push(valueStringBuilder.toString());
    }
    String operation = operatorStringBuilder.isEmpty() ? operationStack.pop() : operatorStringBuilder.toString();
    
    // Evaluate every operation on stack
    while (!operation.equals("$"))
    {
      double result = evaluateOperation(operation, argumentList, inputDataType);
      valueStack.push(convertDoubleToDatatype(result, inputDataType));
      operation = operationStack.pop();
    }// End of operation stack emptier
    
    // Overload last value on stack and return
    String resultValue = valueStack.pop();
    InputCodeArgument resultArg = argumentList.stream()
                                              .filter(arg -> resultValue.equals(arg.getName()))
                                              .findFirst()
                                              .orElse(null);
    return getValueFromOperand(resultArg == null ? resultValue : resultArg.getValue(), outputDataType);
  }// end of evaluateExpression
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] argumentList  - List of arguments provided by parsed instruction
   * @param [in] inputDataType - Data type of the expression
   *
   * @brief Evaluates all previously read unary operation for immediate evaluation
   */
  private void evaluateAllUnaryOperations(final List<InputCodeArgument> argumentList, final DataTypeEnum inputDataType)
  {
    while (this.precedingTable.isUnaryOperation(operationStack.peek()))
    {
      String stackOperation = operationStack.pop();
      double result         = evaluateOperation(stackOperation, argumentList, inputDataType);
      valueStack.push(convertDoubleToDatatype(result, inputDataType));
    }
  }// end of evaluateAllUnaryOperations
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operation     - Operation to be interpreted
   * @param [in] argumentList  - List of arguments provided by parsed instruction
   * @param [in] inputDataType - Data type of the expression
   *
   * @return Double value of interpreted operation
   * @brief Decides what type of operation was given and prepares operators for interpretation
   */
  private double evaluateOperation(final String operation,
                                   final List<InputCodeArgument> argumentList,
                                   final DataTypeEnum inputDataType)
  {
    if (precedingTable.isBinaryOperation(operation))
    {
      String operand2 = valueStack.pop();
      String operand1 = valueStack.empty() ? "unknown" : valueStack.pop();
      InputCodeArgument arg2 = argumentList.stream()
                                           .filter(arg -> operand2.startsWith(arg.getName()))
                                           .findFirst()
                                           .orElse(null);
      InputCodeArgument arg1 = argumentList.stream()
                                           .filter(arg -> operand1.startsWith(arg.getName()))
                                           .findFirst()
                                           .orElse(null);
      OperandModel operandModel2 = arg2 == null ? new OperandModel(operand2) : new OperandModel(operand2, arg2);
      OperandModel operandModel1 = arg1 == null ? new OperandModel(operand1) : new OperandModel(operand1, arg1);
      return processOperation(operandModel1, operandModel2, operation, inputDataType);
    }
    else if (precedingTable.isUnaryOperation(operation))
    {
      String operand = valueStack.pop();
      InputCodeArgument argument = argumentList.stream()
                                               .filter(arg -> operand.startsWith(arg.getName()))
                                               .findFirst()
                                               .orElse(null);
      OperandModel operandModel = new OperandModel(operand, argument);
      return processOperation(operandModel, null, operation, inputDataType);
    }
    else if (operation.equals(")"))
    {
      String currentOperation = operationStack.pop();
      while (!currentOperation.equals("("))
      {
        double result = evaluateOperation(currentOperation, argumentList, inputDataType);
        valueStack.push(String.valueOf(result));
        currentOperation = operationStack.pop();
      }
      return Double.parseDouble(valueStack.pop());
    }
    
    return Double.NaN;
  }// end of evaluateOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand1      - First operand
   * @param [in] operand2      - Second operand
   * @param [in] operator      - Operation to be interpreted
   * @param [in] inputDataType - Data type of operation
   *
   * @return Double value of interpreted operation
   * @brief Interpret operation
   */
  private double processOperation(@Nullable final OperandModel operand1,
                                  @Nullable final OperandModel operand2,
                                  @NotNull final String operator,
                                  @NotNull final DataTypeEnum inputDataType)
  {
    double operandValue1 = operand1 != null ? getValueFromOperand(operand1.getValue(), inputDataType) : 0.0;
    double operandValue2 = operand2 != null ? getValueFromOperand(operand2.getValue(), inputDataType) : 0.0;
    operandValue1 = operand1 == null || operand1.getBitHigh() == -1 ? operandValue1 : selectSpecificBits(operand1,
                                                                                                         operandValue1);
    operandValue2 = operand2 == null || operand2.getBitHigh() == -1 ? operandValue2 : selectSpecificBits(operand2,
                                                                                                         operandValue2);
    
    return switch (inputDataType)
    {
      case kInt -> processIntOperation((int) operandValue1, (int) operandValue2, operator);
      case kLong -> processLongOperation((long) operandValue1, (long) operandValue2, operator);
      case kFloat -> processFloatOperation((float) operandValue1, (float) operandValue2, operator);
      case kDouble -> processDoubleOperation(operandValue1, operandValue2, operator);
      case kSpeculative -> Double.NaN;
    };
  }// end of processOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand1 - First operand
   * @param [in] operand2 - Second operand
   * @param [in] operator - Operation to be interpreted
   *
   * @return Double value of 32-bit integer operation
   * @brief Process 32-bit integer operation and keeps overflow capabilities
   */
  private double processIntOperation(final int operand1, final int operand2, final String operator)
  {
    return switch (operator)
    {
      case "+" -> operand1 + operand2;
      case "-" -> operand1 - operand2;
      case "*" -> operand1 * operand2;
      case "<-" -> operand1;
      case "%" -> operand1 % operand2;
      case "/" -> operand2 != 0 ? operand1 / operand2 : Double.NaN;
      case "&" -> operand1 & operand2;
      case "|" -> operand1 | operand2;
      case "<<" -> operand1 << operand2;
      case ">>" -> operand1 >> operand2;
      case ">>>" -> operand1 >>> operand2;
      case "++" -> operand1 + 1;
      case "--" -> operand1 - 1;
      case "#" -> Math.sqrt(operand1);
      case "!" -> ~operand1;
      case ">" -> operand1 > operand2 ? 1.0 : 0.0;
      case ">=" -> operand1 >= operand2 ? 1.0 : 0.0;
      case "<" -> operand1 < operand2 ? 1.0 : 0.0;
      case "<=" -> operand1 <= operand2 ? 1.0 : 0.0;
      case "==" -> operand1 == operand2 ? 1.0 : 0.0;
      default -> Double.NaN;
    };
  }// end of processIntOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand1 - First operand
   * @param [in] operand2 - Second operand
   * @param [in] operator - Operation to be interpreted
   *
   * @return Double value of 64-bit integer operation
   * @brief Process 64-bit integer operation and keeps overflow capabilities
   */
  private double processLongOperation(final long operand1, final long operand2, final String operator)
  {
    return switch (operator)
    {
      case "+" -> operand1 + operand2;
      case "-" -> operand1 - operand2;
      case "*" -> operand1 * operand2;
      case "<-" -> operand1;
      case "%" -> operand1 % operand2;
      case "/" -> operand2 != 0 ? operand1 / operand2 : Double.NaN;
      case "&" -> operand1 & operand2;
      case "|" -> operand1 | operand2;
      case "<<" -> operand1 << operand2;
      case ">>" -> operand1 >> operand2;
      case ">>>" -> operand1 >>> operand2;
      case "++" -> operand1 + 1;
      case "--" -> operand1 - 1;
      case "#" -> Math.sqrt(operand1);
      case "!" -> ~operand1;
      case ">" -> operand1 > operand2 ? 1.0 : 0.0;
      case ">=" -> operand1 >= operand2 ? 1.0 : 0.0;
      case "<" -> operand1 < operand2 ? 1.0 : 0.0;
      case "<=" -> operand1 <= operand2 ? 1.0 : 0.0;
      case "==" -> operand1 == operand2 ? 1.0 : 0.0;
      default -> Double.NaN;
    };
  }// end of processLongOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand1 - First operand
   * @param [in] operand2 - Second operand
   * @param [in] operator - Operation to be interpreted
   *
   * @return Double value of 32-bit float operation
   * @brief Process 32-bit float operation and keeps overflow capabilities
   */
  private double processFloatOperation(final float operand1, final float operand2, final String operator)
  {
    return switch (operator)
    {
      case "+" -> operand1 + operand2;
      case "-" -> operand1 - operand2;
      case "*" -> operand1 * operand2;
      case "<-" -> operand1;
      case "%" -> operand1 % operand2;
      case "/" -> operand2 != 0 ? operand1 / operand2 : Double.NaN;
      case "++" -> operand1 + 1;
      case "--" -> operand1 - 1;
      case "#" -> Math.sqrt(operand1);
      case ">" -> operand1 > operand2 ? 1.0 : 0.0;
      case ">=" -> operand1 >= operand2 ? 1.0 : 0.0;
      case "<" -> operand1 < operand2 ? 1.0 : 0.0;
      case "<=" -> operand1 <= operand2 ? 1.0 : 0.0;
      case "==" -> operand1 == operand2 ? 1.0 : 0.0;
      default -> Double.NaN;
    };
  }// end of processFloatOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand1 - First operand
   * @param [in] operand2 - Second operand
   * @param [in] operator - Operation to be interpreted
   *
   * @return Double value of 64-bit double operation
   * @brief Process 64-bit double operation and keeps overflow capabilities
   */
  private double processDoubleOperation(final double operand1, final double operand2, final String operator)
  {
    return switch (operator)
    {
      case "+" -> operand1 + operand2;
      case "-" -> operand1 - operand2;
      case "*" -> operand1 * operand2;
      case "<-" -> operand1;
      case "%" -> operand1 % operand2;
      case "/" -> operand2 != 0 ? operand1 / operand2 : Double.NaN;
      case "++" -> operand1 + 1;
      case "--" -> operand1 - 1;
      case "#" -> Math.sqrt(operand1);
      case ">" -> operand1 > operand2 ? 1.0 : 0.0;
      case ">=" -> operand1 >= operand2 ? 1.0 : 0.0;
      case "<" -> operand1 < operand2 ? 1.0 : 0.0;
      case "<=" -> operand1 <= operand2 ? 1.0 : 0.0;
      case "==" -> operand1 == operand2 ? 1.0 : 0.0;
      default -> Double.NaN;
    };
  }// end of processDoubleOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operandModel  - OperandModel object of lValue
   * @param [in] newValue      - Long value of evaluated expression
   * @param [in] previousValue - Long value of previous result to be written into
   *
   * @return Long value with written bits
   * @brief Writes bits in specified bit range
   */
  private long writeSpecificBits(final OperandModel operandModel, final long newValue, final long previousValue)
  {
    //Get new bits in given range
    long bitResult = selectSpecificBits(operandModel, newValue);
    long offset    = operandModel.getBitLow();
    bitResult = bitResult << offset;
    
    //Get low bits of actual value and write them into the new bits
    OperandModel dummyModel = new OperandModel("", operandModel.getBitLow() - 1, 0);
    long         lowValue   = selectSpecificBits(dummyModel, previousValue);
    bitResult = bitResult | lowValue;
    
    // Make zeroes in place of new result and write new result in place of zeroes
    long bitValue = previousValue >> (operandModel.getBitHigh() + 1);
    bitValue = bitValue << (operandModel.getBitHigh() + 1);
    return bitValue | bitResult;
  }// end of writeSpecificBits
  //-------------------------------------------------------------------------------------------
  
  /**
   * Selects bits from value
   *
   * @param [in] operandModel - OperandModel object of the value
   * @param [in] actualValue  - Long value to select bits fromunnessesary
   *
   * @return Long value of selected bits in operandModels range
   */
  private long selectSpecificBits(final OperandModel operandModel, final double actualValue)
  {
    long actualLongValue = (long) actualValue;
    long mask            = 0;
    for (int i = operandModel.getBitLow(); i <= operandModel.getBitHigh(); i++)
    {
      mask += (long) Math.pow(2, i);
    }
    return actualLongValue & mask;
  }// end of selectSpecificBits
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand  - Value to be parsed, either immediate (hex or decimal) or register
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
    
    // If value is value of previously calculated expression
    if (temporaryTag.equals(operand))
    {
      return temporaryValue;
    }
    
    DataTypeEnum[] dataTypeEnums = getFitRegisterTypes(dataType);
    RegisterModel  registerModel = null;
    for (DataTypeEnum possibleDataType : dataTypeEnums)
    {
      registerModel = this.registerFileBlock.getRegisterList(possibleDataType).stream().filter(
          register -> register.getName().equals(operand)).findFirst().orElse(null);
      if (registerModel != null)
      {
        break;
      }
    }
    
    if (registerModel == null)
    {
      registerModel = this.registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative).stream().filter(
          register -> register.getName().equals(operand)).findFirst().orElse(null);
    }
    return registerModel != null ? registerModel.getValue() : Double.NaN;
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
  
  /**
   * @param [in] value    - Value to be overloaded
   * @param [in] dataType - Data type to overload value into
   *
   * @return String of overloaded value
   * @brief Overload value to expected data type
   */
  private String convertDoubleToDatatype(final double value, final DataTypeEnum dataType)
  {
    if (Double.isNaN(value))
    {
      return String.valueOf(Double.NaN);
    }
    switch (dataType)
    {
      case kInt ->
      {
        int convertedValue = (int) value;
        return String.valueOf(convertedValue);
      }
      case kLong ->
      {
        long convertedValue = (long) value;
        return String.valueOf(convertedValue);
      }
      case kFloat ->
      {
        float convertedValue = (float) value;
        return String.valueOf(convertedValue);
      }
      case kDouble ->
      {
        return String.valueOf(value);
      }
    }
    return String.valueOf(Double.NaN);
  }// end of convertDoubleToDatatype
  //-------------------------------------------------------------------------------------------
}
