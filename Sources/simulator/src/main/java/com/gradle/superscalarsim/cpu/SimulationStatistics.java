/**
 * @file SimulationStatistics.java
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
package com.gradle.superscalarsim.cpu;

import com.gradle.superscalarsim.enums.InstructionTypeEnum;

/**
 * @class SimulationStatistics
 * @brief Class that contains data from blocks for displaying statistics about the run
 */
public class SimulationStatistics
{
  /**
   * Static instruction mix
   */
  public InstructionMix staticInstructionMix;
  /**
   * Dynamic instruction mix.
   * Instructions are counted at the moment they are committed.
   */
  public InstructionMix dynamicInstructionMix;
  /**
   * Cache statistics
   */
  public CacheStatistics cache;
  /**
   * Counter for committed instructions.
   * A commited instruction is one that has successfully left ROB.
   */
  private long committedInstructions;
  /**
   * Counter for clocks passed.
   */
  private long clockCycles;
  /**
   * Counter for how many instructions have failed.
   * Failed instructions are those that have been flushed from the pipeline.
   */
  private long failedInstructions;
  /**
   * Counter for correctly predicted branching instructions.
   */
  private long correctlyPredictedBranches;
  /**
   * Counter for all branch instructions.
   */
  private long allBranches;
  /**
   * Number of taken branches
   */
  private long takenBranches;
  
  /**
   * @brief Constructor
   */
  public SimulationStatistics()
  {
    this.cache                 = new CacheStatistics();
    this.staticInstructionMix  = new InstructionMix();
    this.dynamicInstructionMix = new InstructionMix();
  }// end of Constructor
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
   * @brief Increments number of taken branches
   */
  public void incrementTakenBranches()
  {
    this.takenBranches++;
  }
  
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
  
  public long getCorrectlyPredictedBranches()
  {
    return correctlyPredictedBranches;
  }
  
  public long getTakenBranches()
  {
    return takenBranches;
  }
  
  /**
   * @class CacheStatisticsCounter
   * @brief Class that contains data from blocks for displaying statistics from cache about the run
   */
  public static class CacheStatistics
  {
    /**
     * Counter for how many times cache has been accessed (both read and write).
     */
    private int accesses;
    /**
     * Counter for the number of cache hits.
     */
    private int hits;
    /**
     * Counter for the number of cache misses.
     */
    private int misses;
    /**
     * Counter for the total delay caused by cache accesses.
     * TODO
     */
    private int totalDelay;
    
    /**
     * @brief Constructor
     */
    public CacheStatistics()
    {
    }
    
    public void incrementAccesses()
    {
      accesses++;
    }
    
    public void incrementHits(int cycle)
    {
      hits++;
    }
    
    public void incrementMisses(int cycle)
    {
      misses++;
    }
    
    public void incrementTotalDelay(int cycle, int delay)
    {
      totalDelay += delay;
    }
  }
  
  public static class InstructionMix
  {
    public int intArithmetic;
    public int floatArithmetic;
    public int memory;
    public int branch;
    public int other;
    
    public void increment(InstructionTypeEnum type)
    {
      switch (type)
      {
        case kIntArithmetic -> intArithmetic++;
        case kFloatArithmetic -> floatArithmetic++;
        case kLoadstore -> memory++;
        case kJumpbranch -> branch++;
      }
    }
  }
}
