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
 * @brief The interaction point between prediction and the rest of the CPU.
 * @details Provides static and dynamic prediction, correlated and uncorrelated.
 * <a href="https://courses.cs.washington.edu/courses/csep548/06au/lectures/branchPred.pdf">Useful link about Correlated Predictor.</a>
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
   * TODO: one architectural and a list of bit changes. The bits have to be marked with simcodeid, to know how many to flush.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private GlobalHistoryRegister globalHistoryRegister;
  /**
   * Size of the pattern table
   */
  private int size;
  
  /**
   * True if the global history register is used.
   * If false, the prediction is based only on the program counter and is no longer correlated with the history.
   */
  private boolean useGlobalHistory;
  
  /**
   * @param size                  Size of the pattern table
   * @param useGlobalHistory      True if the global history register is used
   * @param globalHistoryRegister Bit array of branching history
   * @param patternHistoryTable   Table with all bit predictors
   *
   * @brief Constructor
   */
  public GShareUnit(int size,
                    boolean useGlobalHistory,
                    GlobalHistoryRegister globalHistoryRegister,
                    PatternHistoryTable patternHistoryTable)
  {
    this.size                  = size;
    this.useGlobalHistory      = useGlobalHistory;
    this.globalHistoryRegister = globalHistoryRegister;
    this.patternHistoryTable   = patternHistoryTable;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position of the branch instruction
   *
   * @return Predictor on the specified index
   * @brief Get predictor from PHT
   */
  public BitPredictor getPredictor(int programCounter)
  {
    int index = programCounter % size;
    if (useGlobalHistory)
    {
      index ^= globalHistoryRegister.getRegisterValue();
    }
    return this.patternHistoryTable.getPredictor(index);
  }// end of getPredictor
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
