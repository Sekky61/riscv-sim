/**
 * @file ReorderBufferTableModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container class for reorder buffer table entries
 * @date 25 April     2021 15:00 (created) \n
 * 28 April     2021 19:30 (revised)
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
 * @class ReorderBufferTableModel
 * @brief Container class for reorder buffer table entries
 */
public class ReorderBufferTableModel
{
  /// Code line of the instruction
  private final String codeLine;
  /// String value of the instruction validity bit
  private final String validBit;
  /// String value of the instruction busy bit
  private final String busyBit;
  /// String value of the instruction speculative bit
  private final String speculativeBit;
  /// Reorder flags item of highlighting
  private final ReorderFlags reorderFlags;
  
  /**
   * @param [in] simCodeModel - CodeModel of an instruction used in simulation
   * @param [in] reorderFlags - Reorder flags containing bit flags
   *
   * @brief Constructor
   */
  public ReorderBufferTableModel(SimCodeModel simCodeModel, ReorderFlags reorderFlags)
  {
    this.codeLine       = simCodeModel.getCodeLine();
    this.busyBit        = reorderFlags.isBusy() ? "YES" : "NO";
    this.validBit       = reorderFlags.isValid() ? "YES" : "NO";
    this.speculativeBit = reorderFlags.isSpeculative() ? "YES" : "NO";
    this.reorderFlags   = reorderFlags;
  }// end of Constructor
  //----------------------------------------------------------------------------------
  
  /**
   * @brief Dummy Constructor
   */
  public ReorderBufferTableModel()
  {
    this.codeLine       = "";
    this.busyBit        = "";
    this.validBit       = "";
    this.speculativeBit = "";
    this.reorderFlags   = null;
  }// end of Dummy Constructor
  //----------------------------------------------------------------------------------
  
  /**
   * @return String code line
   * @brief Get code line of this entry
   */
  public String getCodeLine()
  {
    return codeLine;
  }// end of getCodeLine
  //----------------------------------------------------------------------------------
  
  /**
   * @return String value of the valid bit
   * @brief Get string value of the valid bit
   */
  public String getValidBit()
  {
    return validBit;
  }// end of getValidBit
  //----------------------------------------------------------------------------------
  
  /**
   * @return String value of the busy bit
   * @brief Get string value of the busy bit
   */
  public String getBusyBit()
  {
    return busyBit;
  }// end of getBusyBit
  //----------------------------------------------------------------------------------
  
  /**
   * @return String value of the speculative bit
   * @brief Get string value of the speculative bit
   */
  public String getSpeculativeBit()
  {
    return speculativeBit;
  }// end of getSpeculativeBit
  //----------------------------------------------------------------------------------
  
  /**
   * @return Reorder buffer flags used for creating this entry
   * @brief Get reorder buffer flags used for creating this entry
   */
  public ReorderFlags getReorderFlags()
  {
    return reorderFlags;
  }// end of getReorderFlags
  //----------------------------------------------------------------------------------
}
