/**
 * @file OperandModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains container for operand from interpreter
 * @date 11 November 2020 22:00 (created) \n
 * 22 November 2020 10:20 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class OperandModel
 * @brief Container for operand value of interpreter
 * @details Class is a container for operand value. Its primary use is to parse value (register or immediate)
 * and find out if it has bit array specifier (in square brackets)
 * and save that into the class for later evaluation. Numbers in brackets should be binary divided by colon ':'
 * or unary.
 */
public class OperandModel
{
  /// Value of the operand (register or immediate)
  private final String value;
  /// Index of the highest bit, -1 if not specified
  private final int bitHigh;
  /// Index of the lowest bit, -1 if not specified
  private final int bitLow;
  
  /**
   * @param [in] operandValue - Raw value of the operand
   * @param [in] bitHigh      - First number specifying end index of the array (highest number)
   * @param [in] bitLow       - Last number specifying start index of the array (lowest number)
   *
   * @brief Default constructor
   */
  public OperandModel(final String operandValue, final int bitHigh, final int bitLow)
  {
    this.value   = operandValue;
    this.bitHigh = bitHigh;
    this.bitLow  = bitLow;
  }// end of Constructor (String, int, int)
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operandValue - Raw value of the operand
   *
   * @brief Override constructor for operandValue without bit array specifier
   */
  public OperandModel(final String operandValue)
  {
    this.value   = operandValue;
    this.bitHigh = -1;
    this.bitLow  = -1;
  }// end of Constructor (String)
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operandValue - Unparsed tag of operand with bit array specifier
   * @param [in] argument     - Argument containing tag and the value of the operand
   *
   * @brief Override constructor for parsing operandValue and bit array specifier
   */
  public OperandModel(final String operandValue, final InputCodeArgument argument)
  {
    if (operandValue.indexOf('[') != -1)
    {
      int      start = operandValue.indexOf('[') + 1;
      int      end   = operandValue.indexOf(']');
      String[] range = operandValue.substring(start, end).split(":");
      this.bitHigh = Integer.parseInt(range[0]);
      this.bitLow  = range.length > 1 ? Integer.parseInt(range[1]) : Integer.parseInt(range[0]);
    }
    else
    {
      this.bitHigh = -1;
      this.bitLow  = -1;
    }
    this.value = operandValue.equals("unknown") ? "0" : argument.getValue();
  }// end of Constructor (String, InputCodeArgument)
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Value of the operand
   * @brief Get value of the operand
   */
  public String getValue()
  {
    return value;
  }// end of getValue
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Index of the highest bit
   * @brief Get highest bit index of the operand
   */
  public int getBitHigh()
  {
    return bitHigh;
  }// end of getBitHigh
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Index of the lowest bit
   * @brief Get lowest bit index of the operand
   */
  public int getBitLow()
  {
    return bitLow;
  }// end of getBitLow
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Bit array range
   * @brief Get bit array range
   */
  public int getBitRange()
  {
    return this.bitHigh - this.bitLow + 1;
  }// end of getBitRange
  //-------------------------------------------------------------------------------------------
}
