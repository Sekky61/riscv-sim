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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * @class GShareUnit
 * @brief Class containing GShare logic for indexing correct bit predictor
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class GShareUnit
{
  
  /**
   * Table with all bit predictors
   */
  @JsonIdentityReference(alwaysAsId = true)
  private PatternHistoryTable patternHistoryTable;
  
  /**
   * Bit array of branching history
   */
  @JsonIdentityReference(alwaysAsId = true)
  private GlobalHistoryRegister globalHistoryRegister;
  /**
   * Size of the pattern table
   */
  private int size;
  
  /**
   * @param size                  Size of the pattern table
   * @param globalHistoryRegister Bit array of branching history
   * @param patternHistoryTable   Table with all bit predictors
   *
   * @brief Constructor
   */
  public GShareUnit(int size, GlobalHistoryRegister globalHistoryRegister, PatternHistoryTable patternHistoryTable)
  {
    this.patternHistoryTable   = patternHistoryTable;
    this.globalHistoryRegister = globalHistoryRegister;
    this.size                  = size;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return Active Pattern History Table object
   * @brief Get the current instance of the Pattern History Table
   */
  public PatternHistoryTable getPatternHistoryTable()
  {
    return patternHistoryTable;
  }// end of getPatternHistoryTable
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position of the branch instruction
   *
   * @return Predictor on the specified index
   * @brief Get predictor from PHT
   */
  public IBitPredictor getPredictor(int programCounter)
  {
    return this.patternHistoryTable.getPredictor(
            (programCounter % size) ^ globalHistoryRegister.getRegisterValueAsInt());
  }// end of getPredictor
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position of the branch instruction
   * @param id             ID of the bit array history value
   *
   * @return Predictor on the specified index
   * @brief Get predictor from PHT with older GHT value
   */
  public IBitPredictor getPredictorFromOld(int programCounter, int id)
  {
    return this.patternHistoryTable.getPredictor(
            (programCounter % size) ^ globalHistoryRegister.getHistoryValueAsInt(id));
  }// end of getPredictorFromOld
  //----------------------------------------------------------------------
  
  /**
   * @return GHT block object
   * @brief Get GHT used by the GShare
   */
  public GlobalHistoryRegister getGlobalHistoryRegister()
  {
    return globalHistoryRegister;
  }// end of getGlobalHistoryRegister
  
  //----------------------------------------------------------------------
}
