/**
 * @file GShareUnit.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class with GShare logic
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

/**
 * @class GShareUnit
 * @brief Class containing GShare logic for indexing correct bit predictor
 */
public class GShareUnit
{
  
  /// Table with all bit predictors
  private PatternHistoryTable patternHistoryTable;
  
  /// Bit array of branching history
  private GlobalHistoryRegister globalHistoryRegister;
  /// Size of the pattern table
  private int size;
  
  /**
   * @brief Constructor
   * @param [in] size                  - Size of the pattern table
   * @param [in] globalHistoryRegister - Bit array of branching history
   */
  public GShareUnit(int size, GlobalHistoryRegister globalHistoryRegister)
  {
    this.patternHistoryTable   = new PatternHistoryTable(size);
    this.globalHistoryRegister = globalHistoryRegister;
    this.size                  = size;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Get the current instance of the Pattern History Table
   * @return Active Pattern History Table object
   */
  public PatternHistoryTable getPatternHistoryTable()
  {
    return patternHistoryTable;
  }// end of getPatternHistoryTable
  //----------------------------------------------------------------------
  
  /**
   * @brief Get predictor from PHT
   * @param [in] programCounter - Position of the branch instruction
   * @return Predictor on the specified index
   */
  public IBitPredictor getPredictor(int programCounter)
  {
    return this.patternHistoryTable.getPredictor(
            (programCounter % size) ^ globalHistoryRegister.getRegisterValueAsInt());
  }// end of getPredictor
  //----------------------------------------------------------------------
  
  /**
   * @brief Get predictor from PHT with older GHT value
   * @param [in] programCounter - Position of the branch instruction
   * @param [in] id             - ID of the bit array history value
   * @return Predictor on the specified index
   */
  public IBitPredictor getPredictorFromOld(int programCounter, int id)
  {
    return this.patternHistoryTable.getPredictor(
            (programCounter % size) ^ globalHistoryRegister.getHistoryValueAsInt(id));
  }// end of getPredictorFromOld
  //----------------------------------------------------------------------
  
  /**
   * @brief Get GHT used by the GShare
   * @return GHT block object
   */
  public GlobalHistoryRegister getGlobalHistoryRegister()
  {
    return globalHistoryRegister;
  }// end of getGlobalHistoryRegister
  //----------------------------------------------------------------------
  
  /**
   * @brief Set new size of the PHT
   * @param [in] size - New PHT size
   */
  public void setSize(int size)
  {
    this.size                = size;
    this.patternHistoryTable = new PatternHistoryTable(size);
  }// end of setSize
  
  public void setGlobalHistoryRegister(GlobalHistoryRegister globalHistoryRegister)
  {
    this.globalHistoryRegister = globalHistoryRegister;
  }
  
  public void setPatternHistoryTable(PatternHistoryTable patternHistoryTable)
  {
    this.patternHistoryTable = patternHistoryTable;
  }
  
  //----------------------------------------------------------------------
}
