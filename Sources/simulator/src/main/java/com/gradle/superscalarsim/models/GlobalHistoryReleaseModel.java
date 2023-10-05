/**
 * @file GlobalHistoryReleaseModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container class global history released bit vectors
 * @date 9  March    2021 16:00 (created) \n
 * 10 March    2021 19:20 (revised)
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
 * @class GlobalHistoryReleaseModel
 * @brief Container class holding global history bit vectors and bulk ID of released histories
 */
public class GlobalHistoryReleaseModel
{
  /// Bulk id of the history
  private final int id;
  /// Bit vector from GHR
  private final boolean[] history;
  
  /**
   * @param [in] id      - Bulk id of the history
   * @param [in] history - Bit vector from GHR
   *
   * @brief Constructor
   */
  public GlobalHistoryReleaseModel(int id, boolean[] history)
  {
    this.id      = id;
    this.history = history;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return Id of the history bit array (bulk id)
   * @brief Get id of the saved history entry
   */
  public int getId()
  {
    return id;
  }// end of getId
  //----------------------------------------------------------------------
  
  /**
   * @return History bit array
   * @brief Get saved history bit array
   */
  public boolean[] getHistory()
  {
    return history;
  }// end of getHistory
  //----------------------------------------------------------------------
}
