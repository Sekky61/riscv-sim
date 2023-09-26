/**
 * @file    CodeTableModel.java
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
 * @brief File contains container class for displaying compiled code
 *
 * @date  18 April  2021 16:00 (created) \n
 *        28 April  2021 18:10 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class CodeTableModel
 * @brief Container class for displaying compiled code entries in left side table
 */
public class CodeTableModel
{
  /// Index of the instruction in the compiled list
  private final int index;
  /// Code line of the instruction
  private final String codeLine;

  /**
   * @brief Constructor
   * @param [in] index    - Index of the instruction in the compiled list
   * @param [in] codeLine - Code line of the instruction
   */
  public CodeTableModel(int index, String codeLine)
  {
    this.index = index;
    this.codeLine = codeLine;
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Get index of the instruction
   * @return Index of the instruction
   */
  public int getIndex()
  {
    return index;
  }// end of getIndex
  //----------------------------------------------------------------------

  /**
   * @brief Get code line of the instruction
   * @return String value of the code line
   */
  public String getCodeLine()
  {
    return codeLine;
  }// end of getCodeLine
  //----------------------------------------------------------------------
}
