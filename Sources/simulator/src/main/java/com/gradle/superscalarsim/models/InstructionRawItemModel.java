/**
 * @file InstructionRawItemModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of instruction format item
 * @date 28 October  2020 13:00 (created) \n
 * 5  November 2020 18:11 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class InstructionRawItemModel
 * @brief Definition of instruction format item
 * @details Future work. Class contains definition of instruction format item, which can be later used for interpreting instruction
 * in I-cache. Instruction has to have just one opcode item.
 */
public class InstructionRawItemModel
{
  /// Name of instruction item (unique in one instruction)
  private final String name;
  /// Value of the lowest bit of item (little endian)
  private final int bitLow;
  /// Value of the highest bit of item (little endian)
  private final int bitHigh;
  /// Value of an item, if it does not have bit string, it references on position in syntax from where to take value
  private final String value;
  
  /**
   * @param [in] name    - Name of an instruction
   * @param [in] bitLow  - Value of the lowest bit of item
   * @param [in] bitHigh - Value of the highest bit of item
   * @param [in] value   - Value of an instruction item
   *
   * @brief Constructor
   */
  public InstructionRawItemModel(String name, int bitLow, int bitHigh, String value)
  {
    this.name    = name;
    this.bitLow  = bitLow;
    this.bitHigh = bitHigh;
    this.value   = value;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formating
   */
  @Override
  public String toString()
  {
    return "name: " + name + ", " + "starts from bit: " + bitLow + ", " + "ends with bit: " + bitHigh + ", " + "value" + ": " + value + '\n';
  }// end of toString
  //------------------------------------------------------
  
  /**
   * @return Instruction item name
   * @brief Get instruction item name
   */
  public String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------
  
  /**
   * @return Value of the lowest bit of item
   * @brief Get value of the lowest bit of item
   */
  public int getBitLow()
  {
    return bitLow;
  }// end of getBitLow
  //------------------------------------------------------
  
  /**
   * @return Value of the highest bit of item
   * @brief Get value of the highest bit of item
   */
  public int getBitHigh()
  {
    return bitHigh;
  }// end of getBitHigh
  //------------------------------------------------------
  
  /**
   * @return Bit size of item as integer value
   * @brief Get bit size of item, which is difference between bitHigh and bitLow
   */
  public int getBitSize()
  {
    return this.bitHigh - this.bitLow;
  }// end of getBitSize
  //------------------------------------------------------
  
  /**
   * @return Value of an instruction item
   * @brief Get value of an item, if it does not have bit string, it references on position in syntax from where to
   * take value
   */
  public String getValue()
  {
    return value;
  }// end of getValue
  //------------------------------------------------------
}
