/**
 * @file ExpressionInterpreter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of code expression interpreter
 * @date 15 Oct      2023 17:00 (created)
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
import com.gradle.superscalarsim.models.register.RegisterDataContainer;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Operations:
 * <ul>
 *   <li>'+' - Addition</li>
 *   <li>'-' - Subtraction</li>
 *   <li>'*' - Multiplication</li>
 *   <li>'<-' - Selecting first operand</li>
 *   <li>'%' - Modulo</li>
 *   <li>'/' - Division</li>
 *   <li>'&' - Bitwise AND</li>
 *   <li>'|' - Bitwise OR</li>
 *   <li>'^' - Bitwise XOR</li>
 *   <li>'<<' - Bitwise left shift</li>
 *   <li>'>>' - Bitwise right shift</li>
 *   <li>'>>>' - Bitwise unsigned right shift</li>
 *   <li>'++' - Increment</li>
 *   <li>'--' - Decrement</li>
 *   <li>'sqrt' - Square root</li>
 *   <li>'!' - Bitwise NOT</li>
 *   <li>'>' - Greater than signed</li>
 *   <li>'>=' - Greater than or equal</li>
 *   <li>'<' - Less than</li>
 *   <li>'<=' - Less than or equal</li>
 *   <li>'==' - Equal</li>
 *   <li>'!=' - Not equal</li>
 * </ul>
 *
 * @class ExpressionInterpreter
 * @brief Class for interpreting code expressions in reverse polish notation
 */
public class Expression
{
  /**
   * List of supported unary operators
   */
  public static String[] unaryOperators = new String[]{"sqrt", "!"};
  
  /**
   * List of supported binary operators
   */
  public static String[] binaryOperators = new String[]{"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>", ">", ">=", "<", "<=", "==", "!=", "="};
  
  /**
   * Pattern for matching integer values in argument
   */
  private static Pattern intPattern;
  
  /**
   * Pattern for matching hexadecimal values in argument
   */
  private static Pattern hexadecimalPattern;
  /**
   * Pattern for matching decimal values in argument
   */
  private static Pattern decimalPattern;
  
  static
  {
    intPattern         = Pattern.compile("-?\\d+");
    decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
  }
  
  /**
   * @brief Constructor
   */
  public Expression()
  {
  }
  
  /**
   * Mutates the variables list
   *
   * @param expression expression to interpret
   * @param variables  variables and their values to use in the expression
   */
  public static void interpret(String expression, List<Variable> variables)
  {
    Stack<Variable> valueStack      = new Stack<>();
    String[]        expressionArray = expression.split(" ");
    for (String expressionPart : expressionArray)
    {
      if (isUnaryOperator(expressionPart))
      {
        // Pull one from stack, do operation, push back
        Variable variable = valueStack.pop();
        // Apply operator and Push back
        Variable result = applyUnaryOperator(expressionPart, variable);
        valueStack.push(result);
      }
      else if (isBinaryOperator(expressionPart))
      {
        // Pull two from stack, do operation, push back
        Variable rVariable = valueStack.pop();
        Variable lVariable = valueStack.pop();
        if (expressionPart.equals("="))
        {
          // Special handling for '=' operator
          if (!rVariable.isVariable())
          {
            throw new IllegalArgumentException("Right side of '=' operator must be a variable");
          }
          if (!canBeAssigned(lVariable.type, rVariable.type))
          {
            throw new IllegalArgumentException("Left side of '=' operator must be of the same type as right side");
          }
          rVariable.value = lVariable.value;
        }
        else
        {
          // Apply operator and Push back
          Variable result = applyBinaryOperator(expressionPart, lVariable, rVariable);
          valueStack.push(result);
        }
      }
      else if (isVariable(expressionPart))
      {
        // Find variable type and value
        String   strippedName = expressionPart.substring(1);
        Variable variable     = getVariable(strippedName, variables);
        if (variable == null)
        {
          throw new IllegalArgumentException("Unknown variable: " + expressionPart);
        }
        // Push back
        valueStack.push(variable);
      }
      else
      {
        // It is not an operator, it is a value - parse it
        Variable variable;
        if (intPattern.matcher(expressionPart).matches())
        {
          // It is an int
          int intValue = Integer.parseInt(expressionPart);
          variable = new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(intValue));
        }
        else if (decimalPattern.matcher(expressionPart).matches())
        {
          // It is a float
          // TODO: double
          float floatValue = Float.parseFloat(expressionPart);
          variable = new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue(floatValue));
        }
        else if (hexadecimalPattern.matcher(expressionPart).matches())
        {
          // It is a hex int
          int intValue = Integer.parseInt(expressionPart.substring(2), 16);
          variable = new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(intValue));
        }
        else
        {
          throw new IllegalArgumentException("Unknown value: " + expressionPart);
        }
        // Push back
        valueStack.push(variable);
      }
    }
    
    // The Variables from the input list have been mutated while interpreting the expression
  }
  
  private static boolean isUnaryOperator(String operator)
  {
    return Arrays.asList(unaryOperators).contains(operator);
  }
  
  /**
   * Assumes the operator is a valid unary operator
   *
   * @param operator operator to apply
   * @param variable variable to apply the operator on
   *
   * @return result of the operation
   */
  private static Variable applyUnaryOperator(String operator, Variable variable)
  {
    // Dispatch to correct type processor
    DataTypeEnum type  = variable.type;
    Object       value = variable.value.getValue(type);
    return switch (type)
    {
      case kInt, kUInt -> applyUnaryOperatorInt(operator, (int) value);
      case kLong, kULong -> applyUnaryOperatorLong(operator, (long) value);
      case kFloat -> applyUnaryOperatorFloat(operator, (float) value);
      case kDouble -> applyUnaryOperatorDouble(operator, (double) value);
      case kBool -> applyUnaryOperatorBool(operator, (boolean) value);
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
  
  private static boolean isBinaryOperator(String operator)
  {
    return Arrays.asList(binaryOperators).contains(operator);
  }
  
  /**
   * Unsigned and signed values can be assigned to each other.
   *
   * @return True if assignFrom can be assigned to assignTo
   */
  private static boolean canBeAssigned(DataTypeEnum assignTo, DataTypeEnum assignFrom)
  {
    return switch (assignTo)
    {
      case kInt, kUInt -> assignFrom == DataTypeEnum.kInt || assignFrom == DataTypeEnum.kUInt;
      case kLong, kULong -> assignFrom == DataTypeEnum.kLong || assignFrom == DataTypeEnum.kULong;
      case kFloat -> assignFrom == DataTypeEnum.kFloat;
      case kDouble -> assignFrom == DataTypeEnum.kDouble;
      case kBool -> assignFrom == DataTypeEnum.kBool;
      default -> throw new IllegalArgumentException("Unknown type: " + assignTo);
    };
  }
  
  /**
   * @param operator  operator to apply
   * @param lVariable left variable
   * @param rVariable right variable
   *
   * @return result of the operation.
   */
  private static Variable applyBinaryOperator(String operator, Variable lVariable, Variable rVariable)
  {
    // Special handling for cases with different types of operands
    if (lVariable.type != rVariable.type)
    {
      throw new IllegalArgumentException(
              "Incompatible types: " + lVariable.type + " and " + rVariable.type + " for operator: " + operator);
    }
    
    // Dispatch to correct type processor
    DataTypeEnum type   = lVariable.type;
    Object       value  = lVariable.value.getValue(type);
    Object       value2 = rVariable.value.getValue(type);
    return switch (type)
    {
      case kInt -> applyBinaryOperatorInt(operator, (int) value, (int) value2);
      case kUInt -> applyBinaryOperatorUnsignedInt(operator, (int) value, (int) value2);
      case kLong -> applyBinaryOperatorLong(operator, (long) value, (long) value2);
      case kULong -> applyBinaryOperatorUnsignedLong(operator, (long) value, (long) value2);
      case kFloat -> applyBinaryOperatorFloat(operator, (float) value, (float) value2);
      case kDouble -> applyBinaryOperatorDouble(operator, (double) value, (double) value2);
      case kBool -> applyBinaryOperatorBool(operator, (boolean) value, (boolean) value2);
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
  
  /**
   * Variables start with a '\' character
   *
   * @param expressionPart expression part to check
   *
   * @return True if the expression part is a variable
   */
  private static boolean isVariable(String expressionPart)
  {
    return expressionPart.startsWith("\\");
  }
  
  private static Variable getVariable(String tag, List<Variable> variables)
  {
    for (Variable variable : variables)
    {
      if (variable.tag.equals(tag))
      {
        return variable;
      }
    }
    return null;
  }
  
  private static Variable applyUnaryOperatorInt(String operator, int value)
  {
    throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kInt);
  }
  
  private static Variable applyUnaryOperatorLong(String operator, long value)
  {
    throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kLong);
  }
  
  private static Variable applyUnaryOperatorFloat(String operator, float value)
  {
    return switch (operator)
    {
      case "sqrt" -> new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue((float) Math.sqrt(value)));
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kFloat);
    };
  }
  
  private static Variable applyUnaryOperatorDouble(String operator, double value)
  {
    return switch (operator)
    {
      case "sqrt" -> new Variable("", DataTypeEnum.kDouble, RegisterDataContainer.fromValue(Math.sqrt(value)));
      default -> throw new IllegalArgumentException(
              "Unknown operator: " + operator + " for type: " + DataTypeEnum.kDouble);
    };
  }
  
  private static Variable applyUnaryOperatorBool(String operator, boolean value)
  {
    return switch (operator)
    {
      case "!" -> new Variable("", DataTypeEnum.kBool, RegisterDataContainer.fromValue(!value));
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kBool);
    };
  }
  
  private static Variable applyBinaryOperatorInt(String operator, int value, int value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case "&" -> Variable.fromValue(value & value2);
      case "|" -> Variable.fromValue(value | value2);
      case "^" -> Variable.fromValue(value ^ value2);
      case "<<" -> Variable.fromValue(value << value2);
      case ">>" -> Variable.fromValue(value >> value2);
      case ">>>" -> Variable.fromValue(value >>> value2);
      case ">" -> Variable.fromValue(value > value2);
      case ">=" -> Variable.fromValue(value >= value2);
      case "<" -> Variable.fromValue(value < value2);
      case "<=" -> Variable.fromValue(value <= value2);
      case "==" -> Variable.fromValue(value == value2);
      case "!=" -> Variable.fromValue(value != value2);
      case "=" -> null;
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kInt);
    };
  }
  
  private static Variable applyBinaryOperatorUnsignedInt(String operator, int value, int value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case "&" -> Variable.fromValue(value & value2);
      case "|" -> Variable.fromValue(value | value2);
      case "^" -> Variable.fromValue(value ^ value2);
      case "<<" -> Variable.fromValue(value << value2);
      case ">>" -> Variable.fromValue(value >> value2);
      case ">>>" -> Variable.fromValue(value >>> value2);
      case ">" -> Variable.fromValue(Integer.compareUnsigned(value, value2) > 0);
      case ">=" -> Variable.fromValue(Integer.compareUnsigned(value, value2) >= 0);
      case "<" -> Variable.fromValue(Integer.compareUnsigned(value, value2) < 0);
      case "<=" -> Variable.fromValue(Integer.compareUnsigned(value, value2) <= 0);
      case "==" -> Variable.fromValue(Integer.compareUnsigned(value, value2) == 0);
      case "!=" -> Variable.fromValue(Integer.compareUnsigned(value, value2) != 0);
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kUInt);
    };
  }
  
  private static Variable applyBinaryOperatorLong(String operator, long value, long value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case "&" -> Variable.fromValue(value & value2);
      case "|" -> Variable.fromValue(value | value2);
      case "^" -> Variable.fromValue(value ^ value2);
      case "<<" -> Variable.fromValue(value << value2);
      case ">>" -> Variable.fromValue(value >> value2);
      case ">>>" -> Variable.fromValue(value >>> value2);
      case ">" -> Variable.fromValue(value > value2);
      case ">=" -> Variable.fromValue(value >= value2);
      case "<" -> Variable.fromValue(value < value2);
      case "<=" -> Variable.fromValue(value <= value2);
      case "==" -> Variable.fromValue(value == value2);
      case "!=" -> Variable.fromValue(value != value2);
      case "=" -> null;
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kLong);
    };
  }
  
  private static Variable applyBinaryOperatorUnsignedLong(String operator, long value, long value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case "&" -> Variable.fromValue(value & value2);
      case "|" -> Variable.fromValue(value | value2);
      case "^" -> Variable.fromValue(value ^ value2);
      case "<<" -> Variable.fromValue(value << value2);
      case ">>" -> Variable.fromValue(value >> value2);
      case ">>>" -> Variable.fromValue(value >>> value2);
      case ">" -> Variable.fromValue(Long.compareUnsigned(value, value2) > 0);
      case ">=" -> Variable.fromValue(Long.compareUnsigned(value, value2) >= 0);
      case "<" -> Variable.fromValue(Long.compareUnsigned(value, value2) < 0);
      case "<=" -> Variable.fromValue(Long.compareUnsigned(value, value2) <= 0);
      case "==" -> Variable.fromValue(Long.compareUnsigned(value, value2) == 0);
      case "!=" -> Variable.fromValue(Long.compareUnsigned(value, value2) != 0);
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kULong);
    };
  }
  
  private static Variable applyBinaryOperatorFloat(String operator, float value, float value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case ">" -> Variable.fromValue(value > value2);
      case ">=" -> Variable.fromValue(value >= value2);
      case "<" -> Variable.fromValue(value < value2);
      case "<=" -> Variable.fromValue(value <= value2);
      case "==" -> Variable.fromValue(value == value2);
      case "!=" -> Variable.fromValue(value != value2);
      case "=" -> null;
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kFloat);
    };
  }
  
  private static Variable applyBinaryOperatorDouble(String operator, double value, double value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "/" -> Variable.fromValue(value / value2);
      case "%" -> Variable.fromValue(value % value2);
      case ">" -> Variable.fromValue(value > value2);
      case ">=" -> Variable.fromValue(value >= value2);
      case "<" -> Variable.fromValue(value < value2);
      case "<=" -> Variable.fromValue(value <= value2);
      case "==" -> Variable.fromValue(value == value2);
      case "!=" -> Variable.fromValue(value != value2);
      case "=" -> null;
      default -> throw new IllegalArgumentException(
              "Unknown operator: " + operator + " for type: " + DataTypeEnum.kDouble);
    };
  }
  
  private static Variable applyBinaryOperatorBool(String operator, boolean value, boolean value2)
  {
    return switch (operator)
    {
      case "==" -> Variable.fromValue(value == value2);
      case "!=" -> Variable.fromValue(value != value2);
      case "=" -> null;
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kBool);
    };
  }
  
  /**
   * Omitting the tag means the object is a nameless value (used in evaluation).
   *
   * @param tag   name of the variable
   * @param value value of the variable
   *
   * @brief a key-value pair for storing variables. Example: rs1 = 5
   */
  public static class Variable
  {
    public String tag;
    public DataTypeEnum type;
    public RegisterDataContainer value;
    
    /**
     * @brief Constructor
     */
    public Variable(String tag, DataTypeEnum type, RegisterDataContainer value)
    {
      this.tag   = tag;
      this.type  = type;
      this.value = value;
    }
    
    public static Variable fromValue(Object value)
    {
      DataTypeEnum type = DataTypeEnum.fromJavaClass(value.getClass());
      if (type == null)
      {
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
      }
      return new Variable("", type, RegisterDataContainer.fromValue(value));
    }
    
    public boolean isVariable()
    {
      return tag != null && !tag.isEmpty();
    }
  }
}