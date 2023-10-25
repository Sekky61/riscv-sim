/**
 * @file GlobalHistoryRegister.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of bit array of branching history
 * @date 1  March   2020 16:00 (created) \n
 * 28 April   2020 12:00 (revised)
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
package com.gradle.superscalarsim.blocks.branch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @class GlobalHistoryRegister
 * @brief Class implements interface of manipulating with the bit array holding history of branching
 * (true if the branch was taken, false if the branch was not taken)
 */
public class GlobalHistoryRegister
{
  /**
   * @brief Bit array.
   * Index 0 is the newest bit. New value is written for every decoded branch instruction.
   */
  private final boolean[] shiftRegister;
  /**
   * History of bit arrays for later indexing to GShare or for bit array repair.
   * Key is the id of an instruction.
   * Value is the bit array shiftRegister.
   */
  private final Map<Integer, boolean[]> history;
  /**
   * Size (in bits) of the GHR
   */
  private final int size;
  
  /**
   * @param [in] size - Size of the bit array
   *
   * @brief Constructor
   */
  public GlobalHistoryRegister(int size)
  {
    this.shiftRegister = new boolean[size];
    this.history       = new HashMap<>();
    this.size          = size;
    
    Arrays.fill(this.shiftRegister, false);
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the GHR and history stack/map
   */
  public void reset()
  {
    this.history.clear();
    Arrays.fill(this.shiftRegister, false);
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @param [in] isJump - Was the branch taken or not?
   *
   * @brief Shifts new bit value into the vector
   */
  public void shiftValue(boolean isJump)
  {
    System.arraycopy(this.shiftRegister, 0, this.shiftRegister, 1, this.shiftRegister.length - 1);
    this.shiftRegister[0] = isJump;
  }// end of shiftValue
  //----------------------------------------------------------------------
  
  /**
   * @param [in] id     - Bulk id the instruction
   * @param [in] isJump - Was the branch taken or not?
   *
   * @brief Shifts speculative bit value into the register while saving the old into the register
   */
  public void shiftSpeculativeValue(int id, boolean isJump)
  {
    boolean[] historyValue = new boolean[this.shiftRegister.length];
    System.arraycopy(this.shiftRegister, 0, historyValue, 0, this.shiftRegister.length);
    this.history.put(id, historyValue);
    System.arraycopy(this.shiftRegister, 0, this.shiftRegister, 1, this.shiftRegister.length - 1);
    this.shiftRegister[0] = isJump;
  }// end of shiftSpeculativeValue
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value of the bit vector
   * @brief Returns the bit array as integer.
   */
  public int getRegisterValueAsInt()
  {
    int result = 0;
    for (int i = 0; i < this.shiftRegister.length; i++)
    {
      result += this.shiftRegister[i] ? Math.pow(2, i) : 0;
    }
    return result;
  }// end of getRegisterValueAsInt
  //----------------------------------------------------------------------
  
  /**
   * @param [in] id - Id of the history bit array (bulk id)
   *
   * @return Integer value of the bit vector
   * @brief Gets bit array as integer from history
   */
  public int getHistoryValueAsInt(int id)
  {
    boolean[] value = this.history.get(id);
    if (value == null)
    {
      return -1;
    }
    int result = 0;
    for (int i = 0; i < value.length; i++)
    {
      result += value[i] ? Math.pow(2, i) : 0;
    }
    return result;
  }// end of getHistoryValueAsInt
  //----------------------------------------------------------------------
  
  /**
   * @param [in] id - Id of the history bit array (bulk id)
   *
   * @brief Removes history value on specified register
   */
  public void removeHistoryValue(int id)
  {
    this.history.remove(id);
  }// end of removeHistoryValue
  //----------------------------------------------------------------------
  
  /**
   * @param id Id of the history bit array (bulk id)
   *
   * @brief Sets history bit array value as current value
   */
  public void setHistoryValueAsCurrent(int id)
  {
    boolean[] historyValue = this.history.get(id);
    this.history.remove(id);
    System.arraycopy(historyValue, 0, this.shiftRegister, 0, historyValue.length);
  }// end of setHistoryValueAsCurrent
  //----------------------------------------------------------------------
}
