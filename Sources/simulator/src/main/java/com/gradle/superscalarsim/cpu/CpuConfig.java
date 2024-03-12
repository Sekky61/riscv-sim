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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.WEAKLY_TAKEN;

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
   * Provided for organizational purposes.
   * Not used in the simulation. Displayed on frontend.
   */
  String name;
  /**
   * Maximum number of instructions that can be in the ROB
   */
  @JsonProperty(required = true)
  public int robSize;
  
  /**
   * Number of instructions that can be committed in one cycle - commitLimit on the ROB
   */
  @JsonProperty(required = true)
  public int commitWidth;
  
  /**
   * Number of clock cycles the CPU will take to flush the pipeline.
   * For example, if a branch is mispredicted, the CPU will take this many cycles to clear the fetch, decode, and ROB.
   */
  @JsonProperty(required = true)
  public int flushPenalty;
  
  /**
   * Number of instructions that can be fetched in one cycle.
   * Also determines the decode width.
   * Fetch unit can _evaluate_ one branch instruction per cycle. This means the fetch will stop before a second branch.
   */
  @JsonProperty(required = true)
  public int fetchWidth;
  
  /**
   * Number of branch instructions that can be evaluated in one cycle.
   */
  @JsonProperty(required = true)
  public int branchFollowLimit;
  
  /**
   * Branch target buffer size.
   */
  @JsonProperty(required = true)
  public int btbSize;
  
  /**
   * Pattern history table size.
   */
  @JsonProperty(required = true)
  public int phtSize;
  
  /**
   * Type of the predictor held in the PHT.
   * One of ZERO_BIT_PREDICTOR, ONE_BIT_PREDICTOR, TWO_BIT_PREDICTOR.
   */
  @JsonProperty(required = true)
  public BitPredictor.PredictorType predictorType;
  
  /**
   * The initial state of all predictors in the PHT.
   * This state is represented as a number.
   * For zero bit it is either 1 ("Taken"), or 0 ("Not Taken").
   * For one bit it is either 1 ("Taken"), or 0 ("Not Taken").
   * For two bit one of 0 ("Strongly Not Taken"), 1 ("Weakly Not Taken"), 2 ("Weakly Taken"), 3 ("Strongly Taken").
   */
  @JsonProperty(required = true)
  public int predictorDefaultState;
  
  /**
   * Use global history vector in the PHT.
   * The GHV is a register that holds the last N branches. It effects addressing of the PHT.
   */
  @JsonProperty(required = true)
  public boolean useGlobalHistory;
  
  /**
   * Defined function units.
   */
  @JsonProperty(required = true)
  public List<FunctionalUnitDescription> fUnits;
  
  /**
   * Use single level cache.
   */
  @JsonProperty(required = true)
  public boolean useCache;
  
  /**
   * Number of cache lines. Must be a power of two multiple of cacheAssoc (example 8*assoc, 16*assoc).
   */
  @JsonProperty(required = true)
  public int cacheLines;
  
  /**
   * Size of one cache line in bytes.
   */
  @JsonProperty(required = true)
  public int cacheLineSize;
  
  /**
   * Cache associativity.
   */
  @JsonProperty(required = true)
  public int cacheAssoc;
  
  /**
   * Cache replacement policy.
   * One of Random, LRU, FIFO.
   */
  @JsonProperty(required = true)
  public String cacheReplacement;
  
  /**
   * One of write-back, write-through.
   */
  @JsonProperty(required = true)
  public String storeBehavior;
  
  /**
   * Number of cycles that it takes to replace a cache line.
   * New line is loaded into the cache after this many cycles.
   */
  @JsonProperty(required = true)
  public int laneReplacementDelay;
  
  /**
   * Cache access delay in cycles.
   */
  @JsonProperty(required = true)
  public int cacheAccessDelay;
  
  /**
   * Load buffer size.
   */
  @JsonProperty(required = true)
  public int lbSize;
  
  /**
   * Store buffer size.
   */
  @JsonProperty(required = true)
  public int sbSize;
  
  /**
   * Main memory latency for store.
   */
  @JsonProperty(required = true)
  public int storeLatency;
  
  /**
   * Main memory latency for load.
   */
  @JsonProperty(required = true)
  public int loadLatency;
  
  /**
   * Call stack size in bytes.
   * Amount of memory allocated for the call stack.
   */
  @JsonProperty(required = true)
  public int callStackSize;
  
  /**
   * Number of speculative registers.
   * This is in addition to the 32 integer and 32 floating point architectural registers.
   */
  @JsonProperty(required = true)
  public int speculativeRegisters;
  
  /**
   * @brief Core clock frequency in Hz
   */
  @JsonProperty(required = true)
  public int coreClockFrequency;
  
  /**
   * @brief Cache clock frequency in Hz
   */
  @JsonProperty(required = true)
  public int cacheClockFrequency;
  
  public static CpuConfig getDefaultConfiguration()
  {
    CpuConfig config = new CpuConfig();
    // ROB
    config.robSize           = 256;
    config.fetchWidth        = 3;
    config.branchFollowLimit = 1;
    config.commitWidth       = 4;
    config.flushPenalty      = 1;
    // Prediction
    config.btbSize               = 1024;
    config.phtSize               = 100; // TODO: test small PHT (there might be issue with false sharing)
    config.predictorType         = BitPredictor.PredictorType.TWO_BIT_PREDICTOR;
    config.predictorDefaultState = WEAKLY_TAKEN; // "Weakly Taken";
    config.useGlobalHistory      = false;
    // FunctionalUnitDescriptions
    config.fUnits = Arrays.asList(new FunctionalUnitDescription(0, FunctionalUnitDescription.Type.FX, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.addition, 1),
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.bitwise, 1),
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.multiplication, 2),
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.division, 10),
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.special, 2)), "FX"),
                                  new FunctionalUnitDescription(1, FunctionalUnitDescription.Type.FP, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.addition, 1),
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.bitwise, 1),
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.multiplication, 2),
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.division, 2),
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.special, 2)), "FP"),
                                  new FunctionalUnitDescription(2, FunctionalUnitDescription.Type.L_S, 1, "L/S"),
                                  new FunctionalUnitDescription(3, FunctionalUnitDescription.Type.Branch, 2, "Branch"),
                                  new FunctionalUnitDescription(4, FunctionalUnitDescription.Type.Memory, 1, "Memory"));
    
    // Cache
    config.useCache         = true;
    config.cacheLines       = 16;
    config.cacheLineSize    = 32;
    config.cacheAssoc       = 2;
    config.cacheReplacement = "LRU"; // TODO: Other policies have problem deserializing
    config.storeBehavior    = "write-back";
    config.cacheAccessDelay = 1;
    // Memory
    config.storeLatency         = 1;
    config.loadLatency          = 1;
    config.laneReplacementDelay = 10;
    config.lbSize               = 64;
    config.sbSize               = 64;
    config.callStackSize        = 512;
    // Misc
    config.speculativeRegisters = 620;
    config.coreClockFrequency   = 100000000;
    config.cacheClockFrequency  = 100000000;
    return config;
  }
}
