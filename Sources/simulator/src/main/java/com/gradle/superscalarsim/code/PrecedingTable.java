/**
 * @file PrecedingTable.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains preceding table for interpreter
 * @date 12 November  2020 18:00 (created) \n
 * 27 November  2020 10:12 (revised)
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

import com.gradle.superscalarsim.enums.PrecedingPriorityEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A 'singleton' class for preceding table. Operations are documented in {@link CodeArithmeticInterpreter}
 *
 * @class PrecedingTable
 * @brief Shows priorities of instructions for interpretation
 */
public class PrecedingTable
{
  /// Preceding 2D hash map with instructions in format: instruction on top of stack, currently parsed instruction
  private final Map<String, Map<String, PrecedingPriorityEnum>> precedingTable;
  /// Array of allowed instructions
  private final String[] allowedInstructions;
  /// Array of unary instructions
  private final String[] unaryOperations;
  /// Array of binary instructions
  private final String[] binaryOperations;
  
  /**
   * Private, only one instance of this class is allowed
   *
   * @brief Constructor
   */
  private PrecedingTable()
  {
    String[] allowedInstructionsArray;
    this.precedingTable   = new HashMap<>();
    this.unaryOperations  = new String[]{"++", "--", "!", "#", "<-"};
    this.binaryOperations = new String[]{"+", "-", "*", "/", "%", "&", "|", "^", ">>>", "<<", ">>", "<=", ">=", "==", "<", ">", "<=u", ">=u", "<u", ">u"};
    String[] brackets = new String[]{"(", ")"};
    allowedInstructionsArray = Stream.concat(Arrays.stream(this.unaryOperations), Arrays.stream(this.binaryOperations))
            .toArray(String[]::new);
    allowedInstructionsArray = Stream.concat(Arrays.stream(allowedInstructionsArray), Arrays.stream(brackets))
            .toArray(String[]::new);
    this.allowedInstructions = allowedInstructionsArray;
    setUpLesserPriorityPrecedingLines();
    setUpMajorPriorityPrecedingLines();
    setUpBracketsPriorityPrecedingLines();
    setUpStartingPriorityPrecedingLines();
    setUpUnaryPriorityPrecedingLines();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Sets up priorities for operation with minor priority (left associative)
   */
  private void setUpLesserPriorityPrecedingLines()
  {
    Map<String, PrecedingPriorityEnum> lesserPriority = new HashMap<>();
    lesserPriority.put("+", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("-", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("*", PrecedingPriorityEnum.kPush);
    lesserPriority.put("/", PrecedingPriorityEnum.kPush);
    lesserPriority.put("%", PrecedingPriorityEnum.kPush);
    lesserPriority.put("<<", PrecedingPriorityEnum.kPush);
    lesserPriority.put(">>", PrecedingPriorityEnum.kPush);
    lesserPriority.put(">>>", PrecedingPriorityEnum.kPush);
    lesserPriority.put("&", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("|", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("^", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("<", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("<=u", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put(">=u", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("<u", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put(">u", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("<=", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("==", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put(">=", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put(">", PrecedingPriorityEnum.kEvaluate);
    lesserPriority.put("(", PrecedingPriorityEnum.kPush);
    lesserPriority.put(")", PrecedingPriorityEnum.kPush);
    this.precedingTable.put("+", lesserPriority);
    this.precedingTable.put("-", lesserPriority);
    this.precedingTable.put("&", lesserPriority);
    this.precedingTable.put("|", lesserPriority);
  }// end of setUpLesserPriorityPrecedingLines
  
  /**
   * @brief Sets up priorities for operation with major priority (left associative)
   */
  private void setUpMajorPriorityPrecedingLines()
  {
    Map<String, PrecedingPriorityEnum> majorPriority = new HashMap<>();
    majorPriority.put("+", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("-", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("*", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("/", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("%", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("<<", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">>", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">>>", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("&", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("|", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("^", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("<=u", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">=u", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("<u", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">u", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("<", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("<=", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("==", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">=", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put(">", PrecedingPriorityEnum.kEvaluate);
    majorPriority.put("(", PrecedingPriorityEnum.kPush);
    majorPriority.put(")", PrecedingPriorityEnum.kPush);
    this.precedingTable.put("<<", majorPriority);
    this.precedingTable.put(">>", majorPriority);
    this.precedingTable.put(">>>", majorPriority);
    this.precedingTable.put("*", majorPriority);
    this.precedingTable.put("/", majorPriority);
    this.precedingTable.put("%", majorPriority);
    this.precedingTable.put("<", majorPriority);
    this.precedingTable.put("<=", majorPriority);
    this.precedingTable.put("==", majorPriority);
    this.precedingTable.put(">=", majorPriority);
    this.precedingTable.put(">", majorPriority);
  }// end of setUpMajorPriorityPrecedingLines
  
  /**
   * @brief Sets up priorities for brackets '(' and ')'
   */
  private void setUpBracketsPriorityPrecedingLines()
  {
    Map<String, PrecedingPriorityEnum> leftBracketsPriority = new HashMap<>();
    leftBracketsPriority.put("+", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("-", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("*", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("/", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("%", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("<<", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">>", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">>>", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("&", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("|", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("^", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("(", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("<=u", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">=u", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("<u", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">u", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("<", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("<=", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put("==", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">=", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(">", PrecedingPriorityEnum.kPush);
    leftBracketsPriority.put(")", PrecedingPriorityEnum.kEvaluate);
    this.precedingTable.put("(", leftBracketsPriority);
    
    Map<String, PrecedingPriorityEnum> rightBracketsPriority = new HashMap<>();
    rightBracketsPriority.put("+", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("-", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("*", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("/", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("%", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("<<", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">>", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">>>", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("&", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("|", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("^", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("(", PrecedingPriorityEnum.kError);
    rightBracketsPriority.put("<=u", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">=u", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("<u", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">u", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("<", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("<=", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put("==", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">=", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(">", PrecedingPriorityEnum.kEvaluate);
    rightBracketsPriority.put(")", PrecedingPriorityEnum.kEvaluate);
    this.precedingTable.put(")", rightBracketsPriority);
  }// end of setUpBracketsPriorityPrecedingLines
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Sets up priority for starting symbol on stack
   */
  private void setUpStartingPriorityPrecedingLines()
  {
    Map<String, PrecedingPriorityEnum> startingPriority = new HashMap<>();
    startingPriority.put("+", PrecedingPriorityEnum.kPush);
    startingPriority.put("-", PrecedingPriorityEnum.kPush);
    startingPriority.put("*", PrecedingPriorityEnum.kPush);
    startingPriority.put("/", PrecedingPriorityEnum.kPush);
    startingPriority.put("%", PrecedingPriorityEnum.kPush);
    startingPriority.put("<<", PrecedingPriorityEnum.kPush);
    startingPriority.put(">>", PrecedingPriorityEnum.kPush);
    startingPriority.put(">>>", PrecedingPriorityEnum.kPush);
    startingPriority.put("&", PrecedingPriorityEnum.kPush);
    startingPriority.put("|", PrecedingPriorityEnum.kPush);
    startingPriority.put("^", PrecedingPriorityEnum.kPush);
    startingPriority.put("(", PrecedingPriorityEnum.kPush);
    startingPriority.put(")", PrecedingPriorityEnum.kPush);
    startingPriority.put("++", PrecedingPriorityEnum.kPush);
    startingPriority.put("--", PrecedingPriorityEnum.kPush);
    startingPriority.put("#", PrecedingPriorityEnum.kPush);
    startingPriority.put("<-", PrecedingPriorityEnum.kPush);
    startingPriority.put("!", PrecedingPriorityEnum.kPush);
    startingPriority.put("<=u", PrecedingPriorityEnum.kPush);
    startingPriority.put(">=u", PrecedingPriorityEnum.kPush);
    startingPriority.put("<u", PrecedingPriorityEnum.kPush);
    startingPriority.put(">u", PrecedingPriorityEnum.kPush);
    startingPriority.put("<", PrecedingPriorityEnum.kPush);
    startingPriority.put("<=", PrecedingPriorityEnum.kPush);
    startingPriority.put("==", PrecedingPriorityEnum.kPush);
    startingPriority.put(">=", PrecedingPriorityEnum.kPush);
    startingPriority.put(">", PrecedingPriorityEnum.kPush);
    this.precedingTable.put("$", startingPriority);
  }// end of setUpStartingPriorityPrecedingLines
  
  /**
   * @brief Sets up priorities for unary operations
   */
  private void setUpUnaryPriorityPrecedingLines()
  {
    Map<String, PrecedingPriorityEnum> unaryPriority = new HashMap<>();
    unaryPriority.put("+", PrecedingPriorityEnum.kPush);
    unaryPriority.put("-", PrecedingPriorityEnum.kPush);
    unaryPriority.put("*", PrecedingPriorityEnum.kPush);
    unaryPriority.put("/", PrecedingPriorityEnum.kPush);
    unaryPriority.put("%", PrecedingPriorityEnum.kPush);
    unaryPriority.put("<<", PrecedingPriorityEnum.kPush);
    unaryPriority.put(">>", PrecedingPriorityEnum.kPush);
    unaryPriority.put(">>>", PrecedingPriorityEnum.kPush);
    unaryPriority.put("&", PrecedingPriorityEnum.kPush);
    unaryPriority.put("|", PrecedingPriorityEnum.kPush);
    unaryPriority.put("^", PrecedingPriorityEnum.kPush);
    unaryPriority.put("(", PrecedingPriorityEnum.kPush);
    unaryPriority.put(")", PrecedingPriorityEnum.kPush);
    this.precedingTable.put("++", unaryPriority);
    this.precedingTable.put("--", unaryPriority);
    this.precedingTable.put("!", unaryPriority);
    this.precedingTable.put("#", unaryPriority);
    this.precedingTable.put("<-", unaryPriority);
  }// end of setUpUnaryPriorityPrecedingLines
  //-------------------------------------------------------------------------------------------
  
  public static PrecedingTable getInstance()
  {
    return PrecedingTableHolder.INSTANCE;
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] stackTop      - Operator currently on top of the stack
   * @param [in] readCharacter - Currently read instruction
   *
   * @return Priority represented by PrecedingPriorityEnum value
   * @brief Get evaluation priority based on provided operators
   */
  public PrecedingPriorityEnum getPrecedingPriority(final String stackTop, final String readCharacter)
  {
    return this.precedingTable.get(stackTop).get(readCharacter);
  }// end of getPrecedingPriority
  //-------------------------------------------------------------------------------------------
  
  public String[] getAllowedInstructions()
  {
    return allowedInstructions;
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operation - Operation to be checked
   *
   * @return True if operation is allowed, false otherwise
   * @brief Checks if provided operation is allowed, meaning is in the preceding table
   */
  public boolean isAllowedOperation(final String operation)
  {
    return Arrays.stream(this.allowedInstructions).anyMatch(op -> op.contains(operation));
  }// end of isAllowedOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operation - Operation to be checked
   *
   * @return True if operation is binary, false otherwise
   * @brief Checks if provided instruction is binary (takes two parameters)
   */
  public boolean isBinaryOperation(final String operation)
  {
    return Arrays.asList(this.binaryOperations).contains(operation);
  }// end of isBinaryOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operation - Operation to be checked
   *
   * @return True if operation is unary, false otherwise
   * @brief Checks if provided operation is unary (takes one parameter)
   */
  public boolean isUnaryOperation(final String operation)
  {
    return Arrays.asList(this.unaryOperations).contains(operation);
  }// end of isUnaryOperation
  //-------------------------------------------------------------------------------------------
  
  private static class PrecedingTableHolder
  {
    private static final PrecedingTable INSTANCE = new PrecedingTable();
  }
  //-------------------------------------------------------------------------------------------
}
