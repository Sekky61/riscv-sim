/**
 * @file CpuConfigValidator.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Validates the CPU configuration
 * @date 18 Dec      2023 11:00 (created)
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

import com.gradle.superscalarsim.models.FunctionalUnitDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * @class CpuConfigValidator
 * @brief Validates the CPU configuration
 */
public class CpuConfigValidator
{
  /**
   * List of all errors
   */
  List<ConfigError> errors;
  
  /**
   * Constructor
   */
  public CpuConfigValidator()
  {
    errors = new ArrayList<>();
  }
  
  public List<ConfigError> getErrors()
  {
    return errors;
  }
  
  /**
   * Validates the CPU configuration
   *
   * @param cpuConfig CPU configuration to validate
   *
   * @return True if the configuration is valid, false otherwise
   */
  public boolean validate(CpuConfig cpuConfig)
  {
    if (cpuConfig == null)
    {
      errors.add(new ConfigError("CPU configuration is null", "root"));
      return false;
    }
    validateRobAndInstructionBufferSize(cpuConfig);
    validateBranchPredictor(cpuConfig);
    validateFus(cpuConfig);
    validateCache(cpuConfig);
    validateMemory(cpuConfig);
    validateClock(cpuConfig);
    return isValid();
  }
  
  /**
   * Validates the clock configuration
   */
  private void validateClock(CpuConfig cpuConfig)
  {
    if (cpuConfig.coreClockFrequency < 1)
    {
      errors.add(new ConfigError("Core clock frequency must be a positive integer", "coreClockFrequency"));
    }
    if (cpuConfig.cacheClockFrequency < 1)
    {
      errors.add(new ConfigError("Memory clock frequency must be a positive integer", "cacheClockFrequency"));
    }
  }
  
  /**
   * Validates the ROB and instruction buffer sizes
   */
  private void validateRobAndInstructionBufferSize(CpuConfig cpuConfig)
  {
    if (cpuConfig.robSize < 1 || cpuConfig.robSize > 1024)
    {
      errors.add(new ConfigError("ROB size must be between 1 and 1024", "robSize"));
    }
    if (cpuConfig.commitWidth < 1 || cpuConfig.commitWidth > 10)
    {
      errors.add(new ConfigError("Commit width must be between 1 and 10", "commitWidth"));
    }
    if (cpuConfig.robSize < cpuConfig.commitWidth)
    {
      errors.add(new ConfigError("ROB size must be greater than or equal to commit width", "robSize"));
    }
    if (cpuConfig.flushPenalty < 0 || cpuConfig.flushPenalty > 100)
    {
      errors.add(new ConfigError("Flush penalty must be between 0 and 100", "flushPenalty"));
    }
    
    if (cpuConfig.fetchWidth < 1 || cpuConfig.fetchWidth > 10)
    {
      errors.add(new ConfigError("Fetch width must be between 1 and 10", "fetchWidth"));
    }
  }
  
  /**
   * Validates the branch predictor configuration
   */
  private void validateBranchPredictor(CpuConfig cpuConfig)
  {
    if (cpuConfig.btbSize < 1 || cpuConfig.btbSize > 16384)
    {
      errors.add(new ConfigError("BTB size must be between 1 and 16384", "btbSize"));
    }
    if (cpuConfig.phtSize < 1 || cpuConfig.phtSize > 16384)
    {
      errors.add(new ConfigError("PHT size must be between 1 and 16384", "phtSize"));
    }
    if (cpuConfig.predictorType == null)
    {
      errors.add(new ConfigError("Predictor type must not be null", "predictorType"));
    }
    
    // Validate predictor default based on predictor type
    if (!cpuConfig.predictorType.isValidState(cpuConfig.predictorDefaultState))
    {
      errors.add(new ConfigError("Predictor default state is not valid", "predictorDefaultState"));
    }
  }
  
  /**
   * Validates all FU configurations
   */
  private void validateFus(CpuConfig cpuConfig)
  {
    if (cpuConfig.fUnits == null)
    {
      errors.add(new ConfigError("Functional Unit (FU) configuration is null", "fus"));
      return;
    }
    if (cpuConfig.fUnits.isEmpty())
    {
      errors.add(new ConfigError("At least one Functional Unit (FU) is required", "fus"));
    }
    for (FunctionalUnitDescription unit : cpuConfig.fUnits)
    {
      validateFu(unit);
    }
    // Check name uniqueness
    List<String> names = new ArrayList<>();
    for (FunctionalUnitDescription unit : cpuConfig.fUnits)
    {
      if (names.contains(unit.name))
      {
        errors.add(new ConfigError("Functional Unit (FU) name must be unique", "fuName"));
      }
      names.add(unit.name);
    }
  }
  
  /**
   * Validate the cache configuration
   */
  private void validateCache(CpuConfig cpuConfig)
  {
    if (cpuConfig.cacheLines < 1 || cpuConfig.cacheLines > 65536)
    {
      errors.add(new ConfigError("Cache lines must be between 1 and 65536", "cacheLines"));
    }
    if (cpuConfig.cacheLineSize < 1 || cpuConfig.cacheLineSize > 512)
    {
      errors.add(new ConfigError("Cache line size must be between 1 and 512", "cacheLineSize"));
    }
    boolean isLineSizePowerOfTwo = Integer.bitCount(cpuConfig.cacheLineSize) == 1;
    if (!isLineSizePowerOfTwo)
    {
      errors.add(new ConfigError("Cache line size must be a power of two", "cacheLineSize"));
    }
    if (cpuConfig.cacheAssoc < 1)
    {
      errors.add(new ConfigError("Cache associativity must be greater than 0", "cacheAssociativity"));
    }
    if (cpuConfig.cacheAssoc > cpuConfig.cacheLines)
    {
      errors.add(
              new ConfigError("Cache associativity must be less than or equal to cache lines", "cacheAssociativity"));
    }
    if (!List.of("LRU", "FIFO", "Random").contains(cpuConfig.cacheReplacement))
    {
      errors.add(new ConfigError("Cache replacement must be LRU, FIFO, or Random", "cacheReplacement"));
    }
    if (!List.of("write-through", "write-back").contains(cpuConfig.storeBehavior))
    {
      errors.add(new ConfigError("Store behavior must be write-through or write-back", "storeBehavior"));
    }
    if (cpuConfig.laneReplacementDelay < 0)
    {
      errors.add(new ConfigError("Lane replacement delay must be non-negative", "laneReplacementDelay"));
    }
    if (cpuConfig.cacheAccessDelay < 0)
    {
      errors.add(new ConfigError("Cache access delay must be non-negative", "cacheAccessDelay"));
    }
  }
  
  
  /**
   * Validate the memory configuration
   */
  private void validateMemory(CpuConfig cpuConfig)
  {
    if (cpuConfig.lbSize < 1 || cpuConfig.lbSize > 1024)
    {
      errors.add(new ConfigError("Load buffer size must be between 1 and 1024", "lbSize"));
    }
    if (cpuConfig.sbSize < 1 || cpuConfig.sbSize > 1024)
    {
      errors.add(new ConfigError("Store buffer size must be between 1 and 1024", "sbSize"));
    }
    if (cpuConfig.storeLatency < 1)
    {
      errors.add(new ConfigError("Store latency must be positive", "storeLatency"));
    }
    if (cpuConfig.loadLatency < 1)
    {
      errors.add(new ConfigError("Load latency must be positive", "loadLatency"));
    }
    if (cpuConfig.callStackSize < 1 || cpuConfig.callStackSize > 65536)
    {
      errors.add(new ConfigError("Call stack size must be between 1 and 65536", "callStackSize"));
    }
    // Register file
    if (cpuConfig.speculativeRegisters < 1 || cpuConfig.speculativeRegisters > 1024)
    {
      errors.add(new ConfigError("Speculative registers count must be between 1 and 1024", "speculativeRegisters"));
    }
  }
  
  /**
   * @return True if the configuration is valid, false otherwise
   */
  public boolean isValid()
  {
    return errors.isEmpty();
  }
  
  /**
   * Validates the FU configuration
   */
  private void validateFu(FunctionalUnitDescription unit)
  {
    if (unit == null)
    {
      errors.add(new ConfigError("FU unit is null", "fu"));
      return;
    }
    if (unit.name == null || unit.name.isEmpty())
    {
      errors.add(new ConfigError("FU name must not be null or empty", "fuName"));
    }
    switch (unit.fuType)
    {
      case FX, FP ->
      {
        // Check capabilities
        if (unit.operations == null)
        {
          errors.add(new ConfigError("FU capabilities are null", "fuCapabilities"));
          return;
        }
        for (FunctionalUnitDescription.Capability capability : unit.operations)
        {
          if (capability == null)
          {
            errors.add(new ConfigError("FU capability is null", "fuCapabilities"));
            continue;
          }
          if (capability.latency < 0)
          {
            errors.add(new ConfigError("FU capability latency must non-negative", "fuCapabilities"));
          }
        }
      }
      case L_S, Branch, Memory ->
      {
        // Check latency
        if (unit.latency < 0)
        {
          errors.add(new ConfigError("FU latency must non-negative", "fuLatency"));
        }
      }
    }
  }
  
}
