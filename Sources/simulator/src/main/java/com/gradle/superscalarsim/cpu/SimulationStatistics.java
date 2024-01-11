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

import java.util.HashMap;
import java.util.Map;

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
   * Functional unit statistics
   */
  public Map<String, FUStats> fuStats;
  /**
   * Counter for committed instructions.
   * A commited instruction is one that has successfully left ROB.
   */
  public long committedInstructions;
  /**
   * Counter for clocks passed.
   */
  public long clockCycles;
  /**
   * Counter for how many instructions have been discarded as a result of branch misprediction.
   * Failed instructions are those that have been flushed from the pipeline.
   */
  public long flushedInstructions;
  /**
   * Counter for how many ROB flushes occured.
   */
  public long robFlushes;
  /**
   * Counter for correctly predicted branching instructions.
   */
  public long correctlyPredictedBranches;
  /**
   * Counter for all conditional branches.
   * Count of unconditional branches can be calculated as (dynamicInstructionMix.branch - conditionalBranches).
   */
  public long conditionalBranches;
  /**
   * Number of taken branches
   */
  public long takenBranches;
  /**
   * Maximal number of allocated speculative registers
   */
  public int maxAllocatedRegisters;
  
  /**
   * @brief Constructor
   */
  public SimulationStatistics()
  {
    this.cache                 = new CacheStatistics();
    this.staticInstructionMix  = new InstructionMix();
    this.dynamicInstructionMix = new InstructionMix();
    this.fuStats               = new HashMap<>();
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Increment busy cycles of FU with given name
   */
  public void incrementBusyCycles(String fuName)
  {
    if (!fuStats.containsKey(fuName))
    {
      fuStats.put(fuName, new FUStats());
    }
    fuStats.get(fuName).incrementBusyCycles();
  }
  
  /**
   * @brief Increments number of committed instructions
   */
  public void incrementCommittedInstructions()
  {
    this.committedInstructions++;
  }// end of incrementCommittedInstructions
  //----------------------------------------------------------------------
  
  /**
   * @brief Report number of allocated registers
   */
  public void reportAllocatedRegisters(int allocatedRegisters)
  {
    if (allocatedRegisters > maxAllocatedRegisters)
    {
      maxAllocatedRegisters = allocatedRegisters;
    }
  }
  
  /**
   * @brief Increments number of taken branches
   */
  public void incrementTakenBranches()
  {
    this.takenBranches++;
  }
  
  /**
   * @brief Increment number of ROB flushes
   * TODO: can a flush be caused by memory forwarding?
   */
  public void incrementRobFlushes()
  {
    this.robFlushes++;
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
    this.flushedInstructions++;
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
   * @brief Increments number of conditional branch instructions that were committed
   */
  public void incrementConditionalBranches()
  {
    this.conditionalBranches++;
  }
  
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
   * @return Number of committed conditional branch instructions
   */
  public long getConditionalBranches()
  {
    return conditionalBranches;
  }// end of getAllBranches
  //----------------------------------------------------------------------
  
  /**
   * @return Number of commited unconditional branch instructions
   */
  public long getUnconditionalBranches()
  {
    return dynamicInstructionMix.branch - conditionalBranches;
  }
  
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
     * Counter for how many times cache has been accessed for read.
     */
    private int readAccesses;
    /**
     * Counter for how many times cache has been accessed for write.
     */
    private int writeAccesses;
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
     * Number of bytes written to cache
     */
    private int bytesWritten;
    /**
     * Number of bytes read from cache
     */
    private int bytesRead;
    
    /**
     * @brief Constructor
     */
    public CacheStatistics()
    {
    }
    
    public int getReadAccesses()
    {
      return readAccesses;
    }
    
    public int getWriteAccesses()
    {
      return writeAccesses;
    }
    
    public int getBytesWritten()
    {
      return bytesWritten;
    }
    
    public int getBytesRead()
    {
      return bytesRead;
    }
    
    public void incrementReadAccesses(int bytesRead)
    {
      readAccesses++;
      this.bytesRead += bytesRead;
    }
    
    public void incrementWriteAccesses(int bytesWritten)
    {
      writeAccesses++;
      this.bytesWritten += bytesWritten;
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
  
  public static class FUStats
  {
    /**
     * The number of cycles that the FU was busy.
     */
    public int busyCycles;
    
    /**
     * @brief Constructor
     */
    public FUStats()
    {
    }
    
    /**
     * @brief Increments number of busy cycles
     */
    public void incrementBusyCycles()
    {
      this.busyCycles++;
    }
  }
}
