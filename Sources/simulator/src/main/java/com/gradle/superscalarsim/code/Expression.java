/**
 * @file Expression.java
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
 *   <li>'*w' - Multiplication with double destination size</li>
 *   <li>'%' - Modulo</li>
 *   <li>'/' - Division</li>
 *   <li>'&' - Bitwise AND</li>
 *   <li>'|' - Bitwise OR</li>
 *   <li>'^' - Bitwise XOR</li>
 *   <li>'<<' - Bitwise left shift</li>
 *   <li>'>>' - Bitwise right shift</li>
 *   <li>'>>>' - Bitwise unsigned right shift</li>
 *   <li>'sqrt' - Square root</li>
 *   <li>'!' - Bitwise NOT</li>
 *   <li>'>' - Greater than signed</li>
 *   <li>'>=' - Greater than or equal</li>
 *   <li>'<' - Less than</li>
 *   <li>'<=' - Less than or equal</li>
 *   <li>'==' - Equal</li>
 *   <li>'!=' - Not equal</li>
 *   <li>'=' - Assign (left to the right)</li>
 *   <li>'pick' - Pick one of the two variables based on the value of the third variable (false picks the left one)</li>
 *   <li>'float' - Convert to float (does not change the bits, interpret cast)</li>
 *   <li>'bits' - Convert to bits (does not change the bits, interpret cast)</li>
 *   <li>'fclass' - Classify float (returns an int)</li>
 * </ul>
 * <p>
 * Examples of valid expressions:
 * <ul>
 *   <li>\a \b +</li>
 *   <li>\a 10 *</li>
 *   <li>\rs1 \rs2 * \rd =</li>
 * </ul>
 * Notes:
 * <ul>
 *   <li>Spaces are used to separate tokens</li>
 *   <li>Int multiplication results in a long result</li>
 * </ul>
 *
 * @class ExpressionInterpreter
 * @brief Class for interpreting code expressions in reverse polish notation
 */
public class Expression
{
  /**
   * Pattern for matching integer values in argument
   */
  private final static Pattern intPattern;
  
  /**
   * Pattern for matching hexadecimal values in argument
   */
  private final static Pattern hexadecimalPattern;
  
  /**
   * Pattern for matching decimal values in argument
   */
  private final static Pattern decimalPattern;
  
  /**
   * List of supported unary operators
   */
  public static String[] unaryOperators = new String[]{"sqrt", "!", "bits", "float", "fclass"};
  
  /**
   * List of supported binary operators
   */
  public static String[] binaryOperators = new String[]{"+", "-", "*", "*w", "/", "%", "&", "|", "^", "<<", ">>", ">>>", ">", ">=", "<", "<=", "==", "!=", "="};
  
  /**
   * List of all ternary operators
   */
  public static String[] ternaryOperators = new String[]{"pick"};
  
  public static String[] allOperators;
  
  public static String[] baseOperators = new String[]{"bits", "=", "pick", "!", ">", ">=", "<", "<=", "==", "!="};
  
  public static String[] bitwiseOperators = new String[]{"&", "|", "^", "<<", ">>", ">>>"};
  
  public static String[] additionOperators = new String[]{"+", "-"};
  
  public static String[] multiplicationOperators = new String[]{"*", "*w", "%"};
  
  public static String[] divisionOperators = new String[]{"/"};
  
  public static String[] specialOperators = new String[]{"sqrt", "float", "fclass"};
  
  static
  {
    intPattern         = Pattern.compile("-?\\d+[li]?");
    decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?[fd]?");
    hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    
    allOperators = new String[unaryOperators.length + binaryOperators.length];
    System.arraycopy(unaryOperators, 0, allOperators, 0, unaryOperators.length);
    System.arraycopy(binaryOperators, 0, allOperators, unaryOperators.length, binaryOperators.length);
  }
  
  /**
   * There are two ways of getting results from the expression:
   * 1) The top of the stack after interpreting the expression
   * 2) The Variables from the input list have been mutated while interpreting the expression
   * <p>
   * Throws IllegalArgumentException if the expression is not valid or variables are not
   * valid.
   *
   * @param expression expression to interpret
   * @param variables  variables and their values to use in the expression
   *
   * @return The top of the stack after interpreting the expression
   */
  public static Variable interpret(String expression, List<Variable> variables)
  {
    Stack<Variable> valueStack      = new Stack<>();
    String[]        expressionArray = expression.split(" ");
    for (String token : expressionArray)
    {
      if (isUnaryOperator(token))
      {
        // Pull one from stack, do operation, push back
        Variable variable = valueStack.pop();
        Variable result   = applyUnaryOperator(token, variable);
        valueStack.push(result);
      }
      else if (isBinaryOperator(token))
      {
        // Pull two from stack, do operation, push back
        Variable rVariable = valueStack.pop();
        Variable lVariable = valueStack.pop();
        if (token.equals("="))
        {
          // Special handling for '=' operator (assign to the right/top variable)
          assignVariable(rVariable, lVariable);
        }
        else
        {
          // Apply operator and Push back
          Variable result = applyBinaryOperator(token, lVariable, rVariable);
          valueStack.push(result);
        }
      }
      else if (isTernaryOperator(token))
      {
        // Pull three from stack, do operation, push back
        Variable rVariable = valueStack.pop();
        Variable mVariable = valueStack.pop();
        Variable lVariable = valueStack.pop();
        // Special handling for 'pick' operator
        Variable result = applyTernaryOperator(token, lVariable, mVariable, rVariable);
        valueStack.push(result);
      }
      else if (isVariable(token))
      {
        // Find variable type and value
        String   strippedName = token.substring(1);
        Variable variable     = getVariable(strippedName, variables);
        if (variable == null)
        {
          throw new IllegalArgumentException("Unknown variable: " + token);
        }
        // Push back
        valueStack.push(variable);
      }
      else
      {
        // Try to parse as a constant
        Variable variable = parseConstant(token);
        if (variable == null)
        {
          throw new IllegalArgumentException("Unknown value: " + token);
        }
        // Push back
        valueStack.push(variable);
      }
    }
    
    // The Variables from the input list have been mutated while interpreting the expression
    // One can also use the top of the stack as the result
    if (valueStack.isEmpty())
    {
      return null;
    }
    
    return valueStack.pop();
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
    Object value = variable.value.getValue(variable.type);
    return switch (variable.type)
    {
      case kInt, kUInt -> applyUnaryOperatorInt(operator, (int) value);
      case kLong, kULong -> applyUnaryOperatorLong(operator, (long) value);
      case kFloat -> applyUnaryOperatorFloat(operator, (float) value);
      case kDouble -> applyUnaryOperatorDouble(operator, (double) value);
      case kBool -> applyUnaryOperatorBool(operator, (boolean) value);
      default -> throw new IllegalArgumentException("Unknown type: " + variable.type);
    };
  }
  
  private static boolean isBinaryOperator(String operator)
  {
    return Arrays.asList(binaryOperators).contains(operator);
  }
  
  /**
   * @param to   variable to assign to
   * @param from variable to assign from
   *
   * @brief Assign value from one variable to another, if possible
   */
  private static void assignVariable(Variable to, Variable from)
  {
    if (!to.isVariable())
    {
      throw new IllegalArgumentException("Right side of '=' operator must be a variable");
    }
    if (!canBeAssigned(to.type, from.type))
    {
      throw new IllegalArgumentException("Left side of '=' operator must be of the same type as right side");
    }
    // Assign value, with some special handling for boolean
    // Alternatively add the cast operator
    if (from.type == DataTypeEnum.kBool && to.type != DataTypeEnum.kBool)
    {
      boolean value = (boolean) from.value.getValue(DataTypeEnum.kBool);
      switch (to.type)
      {
        case kInt, kUInt -> to.value = RegisterDataContainer.fromValue(value ? 1 : 0);
        case kLong, kULong -> to.value = RegisterDataContainer.fromValue(value ? 1L : 0L);
        case kFloat -> to.value = RegisterDataContainer.fromValue(value ? 1.0f : 0.0f);
        case kDouble -> to.value = RegisterDataContainer.fromValue(value ? 1.0 : 0.0);
        default -> throw new IllegalArgumentException("Unknown type: " + to.type);
      }
    }
    else if ((to.type == DataTypeEnum.kInt || to.type == DataTypeEnum.kUInt) && (from.type == DataTypeEnum.kLong || from.type == DataTypeEnum.kULong))
    {
      // long to int - truncate
      long value    = (long) from.value.getValue(from.type);
      int  intValue = (int) value;
      to.value = RegisterDataContainer.fromValue(intValue);
    }
    else
    {
      to.value.copyFrom(from.value);
    }
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
      // Special case: MULHSU (multiply high signed unsigned)
      if ((operator.equals("*") || operator.equals(
              "*w")) && lVariable.type == DataTypeEnum.kInt && rVariable.type == DataTypeEnum.kUInt)
      {
        int  lValueInt = (int) lVariable.value.getValue(DataTypeEnum.kInt);
        long lValue    = (long) lValueInt;
        int  rValueInt = (int) rVariable.value.getValue(DataTypeEnum.kUInt);
        long rValue    = unsignedIntToLong(rValueInt);
        return Variable.fromValue(lValue * rValue);
      }
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
  
  private static boolean isTernaryOperator(String operator)
  {
    return Arrays.asList(ternaryOperators).contains(operator);
  }
  
  private static Variable applyTernaryOperator(String operator,
                                               Variable lVariable,
                                               Variable mVariable,
                                               Variable rVariable)
  {
    if (operator.equals("pick"))
    {
      // The pick operator is used to select one of the two variables based on the value of the third variable
      boolean condition = (boolean) rVariable.value.getValue(DataTypeEnum.kBool);
      return condition ? mVariable : lVariable;
    }
    else
    {
      throw new IllegalArgumentException("Unknown operator: " + operator);
    }
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
  
  /**
   * @param tag       Tag of the variable to search for (example: "rd", not "\rd")
   * @param variables List of variables to search in
   *
   * @return Found variable or null if not found
   */
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
  
  /**
   * @param constant constant to parse (e.g. 10.1f)
   *
   * @return Variable with the parsed value or null if the constant is not valid
   * @brief Parse a constant value - supports boolean, int (dec and hex), float and double.
   */
  public static Variable parseConstant(String constant)
  {
    Variable variable = null;
    if (constant.equals("true") || constant.equals("false"))
    {
      // It is a boolean
      boolean boolValue = Boolean.parseBoolean(constant);
      variable = new Variable("", DataTypeEnum.kBool, RegisterDataContainer.fromValue(boolValue));
    }
    else if (intPattern.matcher(constant).matches())
    {
      if (constant.endsWith("l"))
      {
        // It is a long
        long longValue = Long.parseUnsignedLong(constant.substring(0, constant.length() - 1));
        variable = new Variable("", DataTypeEnum.kLong, RegisterDataContainer.fromValue(longValue));
      }
      else
      {
        // It is an int
        int intValue = Integer.parseInt(constant);
        variable = new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(intValue));
      }
    }
    else if (decimalPattern.matcher(constant).matches())
    {
      // It is a float/double
      if (constant.endsWith("f"))
      {
        // Float
        constant = constant.substring(0, constant.length() - 1);
        float floatValue = Float.parseFloat(constant);
        variable = new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue(floatValue));
      }
      else
      {
        // double
        constant = constant.substring(0, constant.length() - 1);
        double doubleValue = Double.parseDouble(constant);
        variable = new Variable("", DataTypeEnum.kDouble, RegisterDataContainer.fromValue(doubleValue));
      }
    }
    else if (hexadecimalPattern.matcher(constant).matches())
    {
      // It is a hex int
      int intValue = Integer.parseUnsignedInt(constant.substring(2), 16);
      variable = new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(intValue));
    }
    return variable;
  }
  
  private static Variable applyUnaryOperatorInt(String operator, int value)
  {
    return switch (operator)
    {
      case "!" -> new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(~value));
      case "float" -> new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue(value));
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kInt);
    };
  }
  
  private static Variable applyUnaryOperatorLong(String operator, long value)
  {
    return switch (operator)
    {
      case "!" -> new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(~value));
      case "float" -> new Variable("", DataTypeEnum.kDouble, RegisterDataContainer.fromValue(value));
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kLong);
    };
  }
  
  private static Variable applyUnaryOperatorFloat(String operator, float value)
  {
    return switch (operator)
    {
      case "sqrt" -> new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue((float) Math.sqrt(value)));
      case "bits" -> new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(Float.floatToIntBits(value)));
      case "float" -> new Variable("", DataTypeEnum.kFloat, RegisterDataContainer.fromValue(value));
      case "fclass" -> new Variable("", DataTypeEnum.kInt, RegisterDataContainer.fromValue(Fclass.classify(value)));
      default ->
              throw new IllegalArgumentException("Unknown operator: " + operator + " for type: " + DataTypeEnum.kFloat);
    };
  }
  
  private static Variable applyUnaryOperatorDouble(String operator, double value)
  {
    return switch (operator)
    {
      case "sqrt" -> new Variable("", DataTypeEnum.kDouble, RegisterDataContainer.fromValue(Math.sqrt(value)));
      case "bits" -> new Variable("", DataTypeEnum.kLong,
                                  RegisterDataContainer.fromValue(Double.doubleToLongBits(value)));
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
  
  /**
   * Unsigned and signed values can be assigned to each other.
   *
   * @return True if assignFrom can be assigned to assignTo
   */
  private static boolean canBeAssigned(DataTypeEnum assignTo, DataTypeEnum assignFrom)
  {
    return switch (assignTo)
    {
      case kInt, kUInt -> assignFrom == DataTypeEnum.kInt || assignFrom == DataTypeEnum.kUInt || assignFrom == DataTypeEnum.kBool || assignFrom == DataTypeEnum.kLong || assignFrom == DataTypeEnum.kULong;
      case kLong, kULong -> assignFrom == DataTypeEnum.kLong || assignFrom == DataTypeEnum.kULong || assignFrom == DataTypeEnum.kBool;
      case kFloat -> assignFrom == DataTypeEnum.kFloat || assignFrom == DataTypeEnum.kBool;
      case kDouble -> assignFrom == DataTypeEnum.kDouble || assignFrom == DataTypeEnum.kBool;
      case kBool -> assignFrom == DataTypeEnum.kBool;
      default -> throw new IllegalArgumentException("Unknown type: " + assignTo);
    };
  }
  
  private static long unsignedIntToLong(int i)
  {
    return i & 0x0000_0000_ffff_ffffL;
  }
  
  private static Variable applyBinaryOperatorInt(String operator, int value, int value2)
  {
    return switch (operator)
    {
      case "+" -> Variable.fromValue(value + value2);
      case "-" -> Variable.fromValue(value - value2);
      case "*" -> Variable.fromValue(value * value2);
      case "*w" -> Variable.fromValue((long) value * (long) value2);
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
      case "*w" ->
      {
        long l = unsignedIntToLong(value);
        long r = unsignedIntToLong(value2);
        yield Variable.fromValue(l * r);
      }
      case "/" -> Variable.fromValue(Integer.divideUnsigned(value, value2));
      case "%" -> Variable.fromValue(Integer.remainderUnsigned(value, value2));
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
      case "/" -> Variable.fromValue(Long.divideUnsigned(value, value2));
      case "%" -> Variable.fromValue(Long.remainderUnsigned(value, value2));
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
   * @param expression expression to parse
   *
   * @return list of variable names in the expression without duplicates and without the leading '\' character
   */
  public static List<String> getVariableNames(String expression)
  {
    String[] expressionArray = expression.split(" ");
    return Arrays.stream(expressionArray).filter(Expression::isVariable).distinct().map(s -> s.substring(1)).toList();
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
    
    @Override
    public String toString()
    {
      String       typeStr  = type == null ? "null" : type.toString();
      DataTypeEnum typeType = this.type == null ? DataTypeEnum.kULong : this.type;
      return tag + ":" + typeStr + ":" + value.getString(typeType);
    }
  }
}
