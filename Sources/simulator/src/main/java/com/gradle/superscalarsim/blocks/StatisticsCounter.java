/**
 * @file StatisticsCounter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains class that gather statistics from the simulation
 * @date 27 April  2021 16:00 (created) \n
 * 28 April  2021 17:30 (revised)
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
package com.gradle.superscalarsim.blocks;

/**
 * @class StatisticsCounter
 * @brief Class that contains data from blocks for displaying statistics about the run
 */
public class StatisticsCounter
{
  /// Counter for committed instructions
  private long committedInstructions;
  /// Counter for how many times was simulate() called
  private long clockCycles;
  /// Counter for how many instructions have failed
  private long failedInstructions;
  /// Counter for correctly predicted branching instructions
  private long correctlyPredictedBranches;
  /// Counter for all branch instructions
  private long allBranches;
  
  /**
   * @brief Constructor
   */
  public StatisticsCounter()
  {
    this.resetCounters();
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the counters to the initial values
   */
  public void resetCounters()
  {
    this.committedInstructions      = 0;
    this.clockCycles                = 0;
    this.failedInstructions         = 0;
    this.correctlyPredictedBranches = 0;
    this.allBranches                = 0;
  }// end of resetCounters
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of committed instructions
   */
  public void incrementCommittedInstructions()
  {
    this.committedInstructions++;
  }// end of incrementCommittedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of simulate() calls
   */
  public void incrementClockCycles()
  {
    this.clockCycles++;
  }// end of incrementClockCycles
  //----------------------------------------------------------------------
  
  /**
   * @brief Increment number of failed instructions
   */
  public void incrementFailedInstructions()
  {
    this.failedInstructions++;
  }// end of incrementFailedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of correctly predicted branching instructions
   */
  public void incrementCorrectlyPredictedBranches()
  {
    this.correctlyPredictedBranches++;
  }// end of incrementCorrectlyPredictedBranches
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of all branch instructions that were committed
   */
  public void incrementAllBranches()
  {
    this.allBranches++;
  }// end of incrementAllBranches
  //----------------------------------------------------------------------
  
  /**
   * @brief Decrements number of committed instructions
   */
  public void decrementCommittedInstructions()
  {
    this.committedInstructions--;
  }// end of decrementCommittedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Decrements number of simulate() calls
   */
  public void decrementClockCycles()
  {
    this.clockCycles--;
  }// end of decrementClockCycles
  //----------------------------------------------------------------------
  
  /**
   * @brief Increment number of failed instructions
   */
  public void decrementFailedInstructions()
  {
    this.failedInstructions--;
  }// end of decrementFailedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of correctly predicted branching instructions
   */
  public void decrementCorrectlyPredictedBranches()
  {
    this.correctlyPredictedBranches--;
  }// end of decrementCorrectlyPredictedBranches
  //----------------------------------------------------------------------
  
  /**
   * @brief Increments number of all branch instructions that were committed
   */
  public void decrementAllBranches()
  {
    this.allBranches--;
  }// end of decrementAllBranches
  //----------------------------------------------------------------------
  
  /**
   * @return Float value of CPI
   * @brief Calculates CPI from values inside the class
   */
  public float calculateClocksPerInstruction()
  {
    if (this.committedInstructions <= 0)
    {
      return 0;
    }
    return (float) this.clockCycles / (float) this.committedInstructions;
  }// end of calculateClocksPerInstruction
  //----------------------------------------------------------------------
  
  /**
   * @return Percent value of branch prediction accuracy
   * @brief Calculate prediction accuracy in percent from values inside the class
   */
  public int calculateBranchPredictionPercentage()
  {
    if (this.allBranches <= 0)
    {
      return 100;
    }
    return (int) (((float) this.correctlyPredictedBranches / (float) this.allBranches) * 100.0f);
  }// end of calculateBranchPredictionPercentage
  //----------------------------------------------------------------------
  
  /**
   * @return Number of failed instructions
   * @brief Get number of failed instructions
   */
  public long getFailedInstructions()
  {
    return this.failedInstructions;
  }// end of getFailedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @return Number of committed instructions
   * @brief Get number of committed instructions
   */
  public long getCommittedInstructions()
  {
    return this.committedInstructions;
  }// end of getCommittedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @return Number of committed branch instructions
   * @brief Get all committed branch instructions
   */
  public long getAllBranches()
  {
    return allBranches;
  }// end of getAllBranches
  //----------------------------------------------------------------------
  
  /**
   * @return Number of clock cycles
   * @brief Get number of clock cycles
   */
  public long getClockCycles()
  {
    return clockCycles;
  }// end of getAllBranches
  //----------------------------------------------------------------------
  
  public long getCorrectlyPredictedBranches()
  {
    return correctlyPredictedBranches;
  }
}
