/**
 * @file    GlobalHistoryRegister.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of bit array of branching history
 *
 * @date  1  March   2020 16:00 (created) \n
 *        28 April   2020 12:00 (revised)
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.blocks.branch;

import com.gradle.superscalarsim.models.GlobalHistoryReleaseModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @class GlobalHistoryRegister
 * @brief Class implements interface of manipulating with the bit array holding history of branching
 *        (true if the branch was taken, false if the branch was not taken)
 */
public class GlobalHistoryRegister
{
  /// Bit array
  private boolean[] shiftRegister;
  /// History of bit arrays for later indexing to GShare or for bit array repair
  private final Map<Integer,boolean[]> history;
  /// Stack of released histories used for backward simulation
  private final Stack<GlobalHistoryReleaseModel> releasedHistories;
  /// Size (in bits) of the GHR
  private int size;

  /**
   * @brief Constructor
   * @param [in] size - Size of the bit array
   */
  public GlobalHistoryRegister(int size)
  {
    this.shiftRegister     = new boolean[size];
    this.history           = new HashMap<>();
    this.releasedHistories = new Stack<>();
    this.size              = size;

    Arrays.fill(this.shiftRegister, false);
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Resets the GHR and history stack/map
   */
  public void reset()
  {
    this.history.clear();
    this.releasedHistories.clear();
    Arrays.fill(this.shiftRegister, false);
  }// end of reset
  //----------------------------------------------------------------------

  /**
   * @brief Get register size (in bits)
   * @return Size of the GHR
   */
  public int getRegisterSize()
  {
    return size;
  }// end of getRegisterSize
  //----------------------------------------------------------------------

  /**
   * @brief Resize the register to specified size (and resets it)
   * @param [in] size - New size of the register
   */
  public void resizeRegister(int size)
  {
    this.shiftRegister  = new boolean[size];
    this.size           = size;
    reset();
  }// end of resizeRegister
  //----------------------------------------------------------------------

  /**
   * @brief Shifts new bit value into the vector
   * @param [in] isJump - Was the branch taken or not?
   */
  public void shiftValue(boolean isJump)
  {
    System.arraycopy(this.shiftRegister, 0, this.shiftRegister, 1, this.shiftRegister.length-1);
    this.shiftRegister[0] = isJump;
  }// end of shiftValue
  //----------------------------------------------------------------------

  /**
   * @brief Shifts speculative bit value into the register while saving the old into the register
   * @param [in] id     - Bulk id the instruction
   * @param [in] isJump - Was the branch taken or not?
   */
  public void shiftSpeculativeValue(int id, boolean isJump)
  {
    boolean[] historyValue = new boolean[this.shiftRegister.length];
    System.arraycopy(this.shiftRegister, 0, historyValue, 0, this.shiftRegister.length);
    this.history.put(id,historyValue);
    System.arraycopy(this.shiftRegister, 0, this.shiftRegister, 1, this.shiftRegister.length-1);
    this.shiftRegister[0] = isJump;
  }// end of shiftSpeculativeValue
  //----------------------------------------------------------------------

  /**
   * @brief Get the register value as a string of bits
   * @return String representation of the GHR
   */
  public String getRegisterValueAsVectorString()
  {
    StringBuilder builder = new StringBuilder();
    for (boolean bit : this.shiftRegister)
    {
      builder.append(bit ? "1" : "0");
    }
    return builder.reverse().toString();
  }// end of getRegisterValueAsVectorString
  //----------------------------------------------------------------------

  /**
   * @brief Returns the bit array as integer
   * @return Integer value of the bit vector
   */
  public int getRegisterValueAsInt()
  {
    int result = 0;
    for (int i = 0; i < this.shiftRegister.length; i++)
    {
      result += this.shiftRegister[i] ? Math.pow(2,i) : 0;
    }
    return result;
  }// end of getRegisterValueAsInt
  //----------------------------------------------------------------------

  /**
   * @brief Gets bit array as integer from history
   * @param [in] id - Id of the history bit array (bulk id)
   * @return Integer value of the bit vector
   */
  public int getHistoryValueAsInt(int id)
  {
    boolean[] value = this.history.get(id);
    if(value == null)
    {
      return -1;
    }
    int result = 0;
    for (int i = 0; i < value.length; i++)
    {
      result += value[i] ? Math.pow(2,i) : 0;
    }
    return result;
  }// end of getHistoryValueAsInt
  //----------------------------------------------------------------------

  /**
   * @brief Removes history value on specified register
   * @param [in] id - Id of the history bit array (bulk id)
   */
  public void removeHistoryValue(int id)
  {
    boolean[] historyValue = this.history.get(id);
    this.releasedHistories.push(new GlobalHistoryReleaseModel(id, historyValue));
    this.history.remove(id);
  }// end of removeHistoryValue
  //----------------------------------------------------------------------

  /**
   * @brief Sets history bit array value as current value
   * @param [in] id - Id of the history bit array (bulk id)
   */
  public void setHistoryValueAsCurrent(int id)
  {
    boolean[] historyValue = this.history.get(id);
    this.releasedHistories.push(new GlobalHistoryReleaseModel(id, historyValue));
    this.history.remove(id);
    System.arraycopy(historyValue, 0, this.shiftRegister, 0, historyValue.length);
  }// end of setHistoryValueAsCurrent
  //----------------------------------------------------------------------

  /**
   * @brief Reverts value from released stack back to history map
   * @param [in] id - Id of the history bit array (code id)
   */
  public void revertToHistory(int id)
  {
    if (id == this.releasedHistories.peek().getId())
    {
      GlobalHistoryReleaseModel model = this.releasedHistories.pop();
      this.history.put(model.getId(), model.getHistory());
    }
  }// end of revertToHistory
  //----------------------------------------------------------------------

  /**
   * @brief Revert value from history to main bit array
   * @param [in] id - Id of the history bit array (bulk id)
   */
  public void revertFromHistory(int id)
  {
    boolean[] historyValue = this.history.get(id);
    this.history.remove(id);
    System.arraycopy(historyValue, 0, this.shiftRegister, 0, historyValue.length);
  }// end of revertFromHistory
  //----------------------------------------------------------------------
}
