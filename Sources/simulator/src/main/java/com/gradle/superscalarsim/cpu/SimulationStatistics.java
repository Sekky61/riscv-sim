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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
   * Per instruction statistics.
   * Indexed as the instructions appear in code (id in InputCodeModel).
   */
  public List<InstructionStats> instructionStats;
  /**
   * Counter for committed instructions.
   * A committed instruction is one that has successfully left ROB.
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
   * Clock frequency (Hz).
   * Used for calculating time based statistics.
   */
  public int clock;
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
   * Amount of data transferred from main memory
   */
  public long mainMemoryLoadedBytes;
  /**
   * Amount of data transferred to main memory
   */
  public long mainMemoryStoredBytes;
  /**
   * Maximal number of allocated speculative registers
   */
  public int maxAllocatedRegisters;
  
  /**
   * @param instructionCount Number of instructions in the code. Use -1 if unknown.
   * @param clockHz          Clock frequency (Hz)
   * @param fUnits           List of functional units (for their names)
   *
   * @brief Constructor
   */
  public SimulationStatistics(int instructionCount, int clockHz, List<FunctionalUnitDescription> fUnits)
  {
    this(instructionCount, clockHz);
    
    for (FunctionalUnitDescription fUnit : fUnits)
    {
      this.fuStats.put(fUnit.name, new FUStats());
    }
  }// end of Constructor
  
  /**
   * @param instructionCount Number of instructions in the code
   * @param clockHz          Clock frequency (Hz)
   *
   * @brief Constructor
   */
  public SimulationStatistics(int instructionCount, int clockHz)
  {
    this.cache                 = new CacheStatistics();
    this.staticInstructionMix  = new InstructionMix();
    this.dynamicInstructionMix = new InstructionMix();
    this.fuStats               = new HashMap<>();
    this.clock                 = clockHz;
    allocateInstructionStats(instructionCount);
  }
  //----------------------------------------------------------------------
  
  /**
   * @brief Allocate new per instruction statistics.
   * Used in tests.
   */
  public void allocateInstructionStats(int instructionCount)
  {
    this.instructionStats = new ArrayList<>();
    for (int i = 0; i < instructionCount; i++)
    {
      this.instructionStats.add(new InstructionStats());
    }
  }
  
  /**
   * @brief Increment busy cycles of FU with given name
   */
  public void incrementBusyCycles(String fuName)
  {
    assert fuName != null; // Null breaks serialization
    if (!fuStats.containsKey(fuName))
    {
      fuStats.put(fuName, new FUStats());
    }
    fuStats.get(fuName).incrementBusyCycles();
  }
  
  /**
   * @brief Increment main memory traffic
   */
  public void incrementMemoryTraffic(boolean isStore, int bytes)
  {
    if (isStore)
    {
      this.mainMemoryStoredBytes += bytes;
    }
    else
    {
      this.mainMemoryLoadedBytes += bytes;
    }
  }
  
  /**
   * @brief Reports a decoded instruction
   */
  public void reportDecodedInstruction(SimCodeModel codeModel)
  {
    InstructionStats statObj = instructionStats.get(codeModel.getCodeId());
    statObj.incrementDecoded();
  }
  
  /**
   * @brief Reports a committed instruction
   */
  public void reportCommittedInstruction(SimCodeModel codeModel)
  {
    this.committedInstructions++;
    this.dynamicInstructionMix.increment(codeModel.getInstructionFunctionModel().getInstructionType());
    
    boolean isBranch = codeModel.getInstructionTypeEnum() == InstructionTypeEnum.kJumpbranch;
    if (isBranch)
    {
      boolean branchActuallyTaken = codeModel.isBranchLogicResult();
      if (branchActuallyTaken)
      {
        incrementTakenBranches();
      }
      if (codeModel.isBranchPredicted() == branchActuallyTaken)
      {
        incrementCorrectlyPredictedBranches();
      }
      
      if (codeModel.isConditionalBranch())
      {
        incrementConditionalBranches();
      }
    }
    
    // Per instruction statistics
    InstructionStats statObj = instructionStats.get(codeModel.getCodeId());
    statObj.incrementCommittedCycles();
    if (isBranch && codeModel.isConditionalBranch())
    {
      if (codeModel.isBranchPredicted() == codeModel.isBranchLogicResult())
      {
        statObj.incrementCorrectlyPredicted();
      }
    }
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
   * @brief Increments number of correctly predicted branching instructions
   */
  public void incrementCorrectlyPredictedBranches()
  {
    this.correctlyPredictedBranches++;
  }// end of incrementCorrectlyPredictedBranches
  
  /**
   * @brief Increments number of conditional branch instructions that were committed
   */
  public void incrementConditionalBranches()
  {
    this.conditionalBranches++;
  }
  
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
  //----------------------------------------------------------------------
  
  /**
   * @brief Increment number of ROB flushes
   * TODO: can a flush be caused by memory forwarding?
   */
  public void incrementRobFlushes()
  {
    this.robFlushes++;
  }
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
    this.flushedInstructions++;
  }// end of incrementFailedInstructions
  
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
   * @return The arithmetic intensity of the code
   */
  @JsonProperty("arithmeticIntensity")
  public double getArithmeticIntensity()
  {
    if (committedInstructions == 0)
    {
      return 0;
    }
    return (double) (dynamicInstructionMix.intArithmetic + dynamicInstructionMix.floatArithmetic) / committedInstructions;
  }
  
  /**
   * @return Prediction accuracy. TODO: which to count
   */
  @JsonProperty("predictionAccuracy")
  public double getPredictionAccuracy()
  {
    if (conditionalBranches == 0)
    {
      return 0;
    }
    return (double) correctlyPredictedBranches / dynamicInstructionMix.branch;
  }
  
  /**
   * @return FLOPS
   */
  @JsonProperty("flops")
  public double getFlops()
  {
    if (clock == 0)
    {
      return 0;
    }
    return (double) (dynamicInstructionMix.intArithmetic + dynamicInstructionMix.floatArithmetic) / clock;
  }
  
  /**
   * @return IPC
   */
  @JsonProperty("ipc")
  public double getIpc()
  {
    if (clockCycles == 0)
    {
      return 0;
    }
    return (double) committedInstructions / clockCycles;
  }
  
  /**
   * @param clock Clock frequency (Hz)
   *
   * @return Wall time
   */
  @JsonProperty("wallTime")
  public double getWallTime()
  {
    return (double) clockCycles / clock;
  }
  
  /**
   * @return Memory throughput (bytes/s)
   */
  @JsonProperty("memoryThroughput")
  public double getMemoryThroughput()
  {
    return (double) (mainMemoryLoadedBytes + mainMemoryStoredBytes) / clock;
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
     * Misaligned access that causes to load 2 cache lines counts as a single miss.
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
    
    public int getHits()
    {
      return hits;
    }
    
    public int getMisses()
    {
      return misses;
    }
    
    /**
     * @return Cache hit rate
     */
    @JsonProperty("hitRate")
    public double getHitRate()
    {
      int all = hits + misses;
      if (all == 0)
      {
        return 0;
      }
      return (double) hits / all;
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
    
    public void incrementHits()
    {
      hits++;
    }
    
    public void incrementMisses()
    {
      misses++;
    }
    
    public void incrementTotalDelay(int delay)
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
      busyCycles = 0;
    }
    
    /**
     * @brief Increments number of busy cycles
     */
    public void incrementBusyCycles()
    {
      this.busyCycles++;
    }
  }
  
  public static class InstructionStats
  {
    /**
     * The number of cycles that instruction was committed.
     */
    public int committedCount;
    
    /**
     * The number of times that instruction was decoded.
     */
    public int decoded;
    /**
     * The number of times that the (jump) instruction was correctly predicted.
     * Zero for all other instructions.
     * The number of times that the (jump) instruction was incorrectly predicted can be calculated as (committedCount - correctlyPredicted).
     */
    public int correctlyPredicted;
    /**
     * Cache misses of this instruction. Zero for all non-memory instructions.
     * Cache hits of this instruction can be calculated as (committedCount - cacheMisses).
     * Misaligned access that causes to load 2 cache lines counts as a single miss.
     */
    public int cacheMisses;
    
    /**
     * Constructor
     */
    public InstructionStats()
    {
    }
    
    /**
     * @brief Increments number of committed cycles
     */
    public void incrementCommittedCycles()
    {
      this.committedCount++;
    }
    
    /**
     * @brief Increments number of times that instruction was decoded
     */
    public void incrementDecoded()
    {
      this.decoded++;
    }
    
    /**
     * @brief Increments number of times that instruction was correctly predicted
     */
    public void incrementCorrectlyPredicted()
    {
      this.correctlyPredicted++;
    }
    
    /**
     * @brief Increments number of cache misses
     */
    public void incrementCacheMisses()
    {
      this.cacheMisses++;
    }
  }
}
