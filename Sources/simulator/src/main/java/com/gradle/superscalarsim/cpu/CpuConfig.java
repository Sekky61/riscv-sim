/**
 * @file CpuConfiguration.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief All the configuration for the CPU - can be used to create a CpuState
 * @date 26 Sep      2023 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gradle.superscalarsim.code.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @details @JsonIgnoreProperties makes deserialization ignore any extra properties (client sends 'name' of config)
 * @class CpuConfiguration
 * @brief Class representing the CPU configuration
 * Can be used to create a CpuState
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CpuConfig implements Serializable
{
  /**
   * Maximum number of instructions that can be in the ROB
   */
  public int robSize;
  
  /**
   * Number of instructions that can be committed in one cycle - commitLimit on the ROB
   */
  public int commitWidth;
  
  /**
   * Number of clock cycles that the CPU will take to flush the pipeline.
   * For example, if a branch is mispredicted, the CPU will take this many cycles to clear the fetch, decode, and ROB.
   */
  public int flushPenalty;
  
  /**
   * Number of instructions that can be fetched in one cycle.
   * Also determines the decode width.
   * Fetch unit can _evaluate_ one branch instruction per cycle. This means the fetch will stop before a second branch.
   */
  public int fetchWidth;
  
  /**
   * Branch target buffer size.
   */
  public int btbSize;
  
  /**
   * Pattern history table size.
   */
  public int phtSize;
  
  /**
   * Type of the predictor held in the PHT.
   * One of 0bit, 1bit, 2bit.
   */
  public String predictorType;
  
  /**
   * All predictors have this default state.
   * For zero bit one of "Taken", "Not Taken".
   * For one bit one of "Taken", "Not Taken".
   * For two bit one of "Strongly Not Taken", "Weakly Not Taken", "Weakly Taken", "Strongly Taken".
   */
  public String predictorDefault;
  
  /**
   * Use global history vector in the PHT.
   * The GHV is a register that holds the last N branches. It effects addressing of the PHT.
   */
  public boolean useGlobalHistory;
  
  /**
   * Defined function units.
   */
  public List<FUnit> fUnits;
  
  /**
   * Use single level cache.
   */
  public boolean useCache;
  
  /**
   * Number of cache lines.
   */
  public int cacheLines;
  
  /**
   * Size of one cache line in bytes.
   */
  public int cacheLineSize;
  
  /**
   * Cache associativity.
   */
  public int cacheAssoc;
  
  /**
   * Cache replacement policy.
   * One of Random, LRU, FIFO.
   */
  public String cacheReplacement;
  
  /**
   * One of write-back, write-through.
   */
  public String storeBehavior;
  
  /**
   * Number of cycles that it takes to replace a cache line.
   * New line is loaded into the cache after this many cycles.
   */
  public int laneReplacementDelay;
  
  /**
   * Cache access delay in cycles.
   */
  public int cacheAccessDelay;
  
  /**
   * Load buffer size.
   */
  public int lbSize;
  
  /**
   * Store buffer size.
   */
  public int sbSize;
  
  /**
   * Main memory latency for store.
   */
  public int storeLatency;
  
  /**
   * Main memory latency for load.
   */
  public int loadLatency;
  
  /**
   * Call stack size in bytes.
   * Amount of memory allocated for the call stack.
   */
  public int callStackSize;
  
  /**
   * Number of speculative registers.
   * This is in addition to the 32 integer and 32 floating point architectural registers.
   */
  public int speculativeRegisters;
  
  public static CpuConfig getDefaultConfiguration()
  {
    CpuConfig config = new CpuConfig();
    // ROB
    config.robSize      = 256;
    config.fetchWidth   = 3;
    config.commitWidth  = 4;
    config.flushPenalty = 1;
    // Prediction
    config.btbSize          = 1024;
    config.phtSize          = 10;
    config.predictorType    = "2bit";
    config.predictorDefault = "Weakly Taken";
    config.useGlobalHistory = true;
    // FUnits
    config.fUnits = Arrays.asList(new FUnit(0, FUnit.Type.FX,
                                            Arrays.asList(new FUnit.Capability(FUnit.CapabilityName.addition, 1),
                                                          new FUnit.Capability(FUnit.CapabilityName.bitwise, 1),
                                                          new FUnit.Capability(FUnit.CapabilityName.multiplication, 2),
                                                          new FUnit.Capability(FUnit.CapabilityName.division, 2),
                                                          new FUnit.Capability(FUnit.CapabilityName.special, 2)), "FX"),
                                  new FUnit(1, FUnit.Type.FP,
                                            Arrays.asList(new FUnit.Capability(FUnit.CapabilityName.addition, 1),
                                                          new FUnit.Capability(FUnit.CapabilityName.bitwise, 1),
                                                          new FUnit.Capability(FUnit.CapabilityName.multiplication, 2),
                                                          new FUnit.Capability(FUnit.CapabilityName.division, 2),
                                                          new FUnit.Capability(FUnit.CapabilityName.special, 2)), "FP"),
                                  new FUnit(2, FUnit.Type.L_S, 1, "L/S"), new FUnit(3, FUnit.Type.Branch, 2, "Branch"),
                                  new FUnit(4, FUnit.Type.Memory, 1, "Memory"));
    
    // Cache
    config.useCache         = true;
    config.cacheLines       = 16;
    config.cacheLineSize    = 32;
    config.cacheAssoc       = 2;
    config.cacheReplacement = "Random"; // TODO: Other policies have problem deserializing
    config.storeBehavior    = "write-back";
    config.cacheAccessDelay = 1;
    // Memory
    config.storeLatency         = 0;
    config.loadLatency          = 1;
    config.laneReplacementDelay = 10;
    config.lbSize               = 64;
    config.sbSize               = 64;
    config.callStackSize        = 512;
    // Misc
    config.speculativeRegisters = 320;
    return config;
  }
  
  /**
   * @brief Function unit description
   */
  public static class FUnit
  {
    /**
     * AFAIK not used
     */
    public int id;
    
    /**
     * Optional name of the FUnit.
     * Shows up in simulation visualisation, also used for debugging.
     */
    public String name;
    
    /**
     * Latency of the FUnit.
     * Counts only for Branch and Memory FUnits.
     */
    public int latency;
    
    /**
     * Type of the FUnit.
     */
    public Type fuType;
    
    /**
     * Classes of operations that this FUnit can perform.
     * Each class has its own latency.
     */
    public List<Capability> operations;
    
    /**
     * @brief Constructor for deserialization
     */
    public FUnit()
    {
    
    }
    
    /**
     * Constructor for FX and FP FUnits
     */
    public FUnit(int id, Type fuType, List<Capability> operations, String name)
    {
      this(id, fuType, operations);
      this.name = name;
    }
    
    /**
     * Constructor for FX and FP FUnits
     */
    public FUnit(int id, Type fuType, List<Capability> operations)
    {
      this.id         = id;
      this.name       = "FUnit " + id;
      this.fuType     = fuType;
      this.operations = operations;
      // Should not be used
      this.latency = -1;
    }
    
    /**
     * Constructor for L/S, Branch, Memory FUnits
     */
    public FUnit(int id, Type fuType, int latency, String name)
    {
      this(id, fuType, latency);
      this.name = name;
    }
    
    /**
     * Constructor for L/S, Branch, Memory FUnits
     */
    public FUnit(int id, Type fuType, int latency)
    {
      this.id      = id;
      this.name    = "FUnit " + id;
      this.fuType  = fuType;
      this.latency = latency;
    }
    
    /**
     * @return List of operations that this FUnit can perform based on its capabilities
     * {@link Expression}
     */
    public List<String> getAllowedOperations()
    {
      // Base
      List<String> ops = new ArrayList<>(Arrays.asList(Expression.baseOperators));
      // Add operations based on capabilities
      for (Capability capability : operations)
      {
        switch (capability.name)
        {
          case addition -> ops.addAll(Arrays.asList(Expression.additionOperators));
          case bitwise -> ops.addAll(Arrays.asList(Expression.bitwiseOperators));
          case multiplication -> ops.addAll(Arrays.asList(Expression.multiplicationOperators));
          case division -> ops.addAll(Arrays.asList(Expression.divisionOperators));
          case special -> ops.addAll(Arrays.asList(Expression.specialOperators));
        }
      }
      return ops;
    }
    
    /**
     * Types of FUnits
     */
    public enum Type
    {
      FX, FP, L_S, Branch, Memory,
    }
    
    /**
     * Enumeration of kinds of FUnit capabilities
     */
    public enum CapabilityName
    {
      addition, bitwise, multiplication, division, special,
    }
    
    /**
     * Configuration of a capability.
     */
    public static class Capability
    {
      public CapabilityName name;
      public int latency;
      
      public Capability(CapabilityName name, int latency)
      {
        this.name    = name;
        this.latency = latency;
      }
    }
  }
}
