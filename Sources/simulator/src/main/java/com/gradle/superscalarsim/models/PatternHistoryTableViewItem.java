/**
 * @file PatternHistoryTableViewItem.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains container class for pattern history table entries
 * @date 18 April  2020 15:00 (created) \n
 * 28 April  2020 19:00 (revised)
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
 * @class PatternHistoryTableViewItem
 * @brief Container class for pattern history table entries
 */
public class PatternHistoryTableViewItem
{
  /// Index of the pattern history table entry
  private final int index;
  /// String value of the bit vector state
  private final String bitVector;
  /// Flag if the entry was changed and should be highlighted
  private boolean changed;
  
  /**
   * @brief Constructor
   * @param [in] index     - Index of the pattern history table entry
   * @param [in] bitVector - String value of the bit vector state
   */
  public PatternHistoryTableViewItem(int index, String bitVector)
  {
    this.index     = index;
    this.bitVector = bitVector;
    this.changed   = false;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Dummy Constructor
   */
  public PatternHistoryTableViewItem()
  {
    this.index     = -1;
    this.bitVector = "";
    this.changed   = false;
  }// end of Dummy Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Get index of the entry
   * @return Index of the entry
   */
  public int getIndex()
  {
    return index;
  }// end of getIndex
  //----------------------------------------------------------------------
  
  /**
   * @brief Get string value of the bit vector
   * @return String value of the bit vector
   */
  public String getBitVector()
  {
    return bitVector;
  }// end of getBitVector
  //----------------------------------------------------------------------
  
  /**
   * @brief Get boolean value of the changed flag
   * @return True if entry was changed, false otherwise
   */
  public boolean isChanged()
  {
    return changed;
  }// end of isChanged
  //----------------------------------------------------------------------
  
  /**
   * @brief Set changed flag
   * @param [in] changed - Changed flag, true if entry was changed, false otherwise
   */
  public void setChanged(boolean changed)
  {
    this.changed = changed;
  }// end of setChanged
  //----------------------------------------------------------------------
}
