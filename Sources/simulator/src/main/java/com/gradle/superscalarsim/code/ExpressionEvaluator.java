/**
 * @file ExpressionEvaluator.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains parser for parsing and validating ASM code
 * @date 29 Jan  2024 17:00 (created)
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

import java.util.HashMap;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ExpressionEvaluator
{
  
  private static final HashMap<String, Integer> operatorPrecedence;
  
  static
  {
    operatorPrecedence = new HashMap<>();
    operatorPrecedence.put("*", 2);
    operatorPrecedence.put("/", 2);
    operatorPrecedence.put("+", 1);
    operatorPrecedence.put("-", 1);
    operatorPrecedence.put("<<", 3);
    operatorPrecedence.put(">>", 3);
  }
  
  static String operators = "[-+*/]|<<|>>";
  static String decNumbers = "-?\\d+(\\.\\d+)?";
  static String hexNumbers = "0x\\p{XDigit}+";
  static String binaryNumbers = "0b[01]+";
  static String number = hexNumbers + "|" + binaryNumbers + "|" + decNumbers;
  static Pattern pattern = Pattern.compile(number + "|" + operators + "|[\\w\\.]+");
  
  public static boolean isOperator(String token)
  {
    return token.matches(operators);
  }
  
  public static String[] tokenize(String expression)
  {
    Stream<MatchResult> results = pattern.matcher(expression).results();
    return results.map(MatchResult::group).toArray(String[]::new);
  }
  
  public static long evaluate(String expression)
  {
    return evaluate(tokenize(expression));
  }
  
  public static long evaluate(String[] tokens)
  {
    Stack<Long>   operandStack  = new Stack<>();
    Stack<String> operatorStack = new Stack<>();
    
    for (String token : tokens)
    {
      if (token.matches(decNumbers))
      {
        
        operandStack.push(Long.parseLong(token));
      }
      else if (token.matches(hexNumbers))
      {
        operandStack.push(Long.parseLong(token.substring(2), 16));
      }
      else if (token.matches(binaryNumbers))
      {
        operandStack.push(Long.parseLong(token.substring(2), 2));
      }
      else if (token.matches(operators))
      {
        String operator = token;
        while (!operatorStack.isEmpty() && operatorPrecedence.get(token) <= operatorPrecedence.get(
                operatorStack.peek()))
        {
          long   operand2 = operandStack.pop();
          long   operand1 = operandStack.pop();
          String o        = operatorStack.pop();
          switch (o)
          {
            case "+":
              operandStack.push(operand1 + operand2);
              break;
            case "-":
              operandStack.push(operand1 - operand2);
              break;
            case "*":
              operandStack.push(operand1 * operand2);
              break;
            case "/":
              operandStack.push(operand1 / operand2);
              break;
            case "<<":
              operandStack.push(operand1 << operand2);
              break;
            case ">>":
              operandStack.push(operand1 >> operand2);
              break;
          }
        }
        operatorStack.push(token);
      }
    }
    
    while (!operatorStack.isEmpty())
    {
      long   operand2 = operandStack.pop();
      long   operand1 = operandStack.pop();
      String o        = operatorStack.pop();
      switch (o)
      {
        case "+":
          operandStack.push(operand1 + operand2);
          break;
        case "-":
          operandStack.push(operand1 - operand2);
          break;
        case "*":
          operandStack.push(operand1 * operand2);
          break;
        case "/":
          operandStack.push(operand1 / operand2);
          break;
        case "<<":
          operandStack.push(operand1 << operand2);
          break;
        case ">>":
          operandStack.push(operand1 >> operand2);
          break;
      }
    }
    
    return operandStack.pop();
  }
}


