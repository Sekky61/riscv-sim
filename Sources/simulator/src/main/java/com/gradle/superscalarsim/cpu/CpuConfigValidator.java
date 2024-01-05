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
  List<Error> errors;
  
  /**
   * Constructor
   */
  public CpuConfigValidator()
  {
    errors = new ArrayList<>();
  }
  
  public List<Error> getErrors()
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
      errors.add(new Error("CPU configuration is null", "root"));
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
      errors.add(new Error("Core clock frequency must be a positive integer", "coreClockFrequency"));
    }
    if (cpuConfig.cacheClockFrequency < 1)
    {
      errors.add(new Error("Memory clock frequency must be a positive integer", "memoryClockFrequency"));
    }
  }
  
  /**
   * Validates the ROB and instruction buffer sizes
   */
  private void validateRobAndInstructionBufferSize(CpuConfig cpuConfig)
  {
    if (cpuConfig.robSize < 1 || cpuConfig.robSize > 1024)
    {
      errors.add(new Error("ROB size must be between 1 and 1024", "robSize"));
    }
    if (cpuConfig.commitWidth < 1 || cpuConfig.commitWidth > 10)
    {
      errors.add(new Error("Commit width must be between 1 and 10", "commitWidth"));
    }
    if (cpuConfig.robSize < cpuConfig.commitWidth)
    {
      errors.add(new Error("ROB size must be greater than or equal to commit width", "robSize"));
    }
    if (cpuConfig.flushPenalty < 0 || cpuConfig.flushPenalty > 100)
    {
      errors.add(new Error("Flush penalty must be between 0 and 100", "flushPenalty"));
    }
    
    if (cpuConfig.fetchWidth < 1 || cpuConfig.fetchWidth > 10)
    {
      errors.add(new Error("Fetch width must be between 1 and 10", "fetchWidth"));
    }
  }
  
  /**
   * Validates the branch predictor configuration
   */
  private void validateBranchPredictor(CpuConfig cpuConfig)
  {
    if (cpuConfig.btbSize < 1 || cpuConfig.btbSize > 16384)
    {
      errors.add(new Error("BTB size must be between 1 and 16384", "btbSize"));
    }
    if (cpuConfig.phtSize < 1 || cpuConfig.phtSize > 16384)
    {
      errors.add(new Error("PHT size must be between 1 and 16384", "phtSize"));
    }
    if (cpuConfig.predictorType == null)
    {
      errors.add(new Error("Predictor type must not be null", "predictorType"));
    }
    // Predictor types: 0bit, 1bit, 2bit
    if (!List.of("0bit", "1bit", "2bit").contains(cpuConfig.predictorType))
    {
      errors.add(new Error("Predictor type must be 0bit, 1bit, or 2bit", "predictorType"));
    }
    
    // Validate predictor default based on predictor type
    switch (cpuConfig.predictorType)
    {
      case "0bit", "1bit" -> validateBinaryPredictorDefault(cpuConfig.predictorDefault);
      case "2bit" -> validate2BitPredictorDefault(cpuConfig.predictorDefault);
    }
  }
  
  /**
   * Validates all FU configurations
   */
  private void validateFus(CpuConfig cpuConfig)
  {
    if (cpuConfig.fUnits == null)
    {
      errors.add(new Error("Functional Unit (FU) configuration is null", "fus"));
      return;
    }
    if (cpuConfig.fUnits.isEmpty())
    {
      errors.add(new Error("At least one Functional Unit (FU) is required", "fus"));
    }
    for (FunctionalUnitDescription unit : cpuConfig.fUnits)
    {
      validateFu(unit);
    }
  }
  
  /**
   * Validate the cache configuration
   */
  private void validateCache(CpuConfig cpuConfig)
  {
    if (cpuConfig.cacheLines < 1 || cpuConfig.cacheLines > 65536)
    {
      errors.add(new Error("Cache lines must be between 1 and 65536", "cacheLines"));
    }
    if (cpuConfig.cacheLineSize < 1 || cpuConfig.cacheLineSize > 512)
    {
      errors.add(new Error("Cache line size must be between 1 and 512", "cacheLineSize"));
    }
    if (cpuConfig.cacheAssoc < 1)
    {
      errors.add(new Error("Cache associativity must be greater than 0", "cacheAssociativity"));
    }
    if (cpuConfig.cacheAssoc > cpuConfig.cacheLines)
    {
      errors.add(new Error("Cache associativity must be less than or equal to cache lines", "cacheAssociativity"));
    }
    if (!List.of("LRU", "FIFO", "Random").contains(cpuConfig.cacheReplacement))
    {
      errors.add(new Error("Cache replacement must be LRU, FIFO, or Random", "cacheReplacement"));
    }
    if (!List.of("write-through", "write-back").contains(cpuConfig.storeBehavior))
    {
      errors.add(new Error("Store behavior must be write-through or write-back", "storeBehavior"));
    }
    if (cpuConfig.laneReplacementDelay < 0)
    {
      errors.add(new Error("Lane replacement delay must be non-negative", "laneReplacementDelay"));
    }
    if (cpuConfig.cacheAccessDelay < 0)
    {
      errors.add(new Error("Cache access delay must be non-negative", "cacheAccessDelay"));
    }
  }
  
  
  /**
   * Validate the memory configuration
   */
  private void validateMemory(CpuConfig cpuConfig)
  {
    if (cpuConfig.lbSize < 1 || cpuConfig.lbSize > 1024)
    {
      errors.add(new Error("Load buffer size must be between 1 and 1024", "lbSize"));
    }
    if (cpuConfig.sbSize < 1 || cpuConfig.sbSize > 1024)
    {
      errors.add(new Error("Store buffer size must be between 1 and 1024", "sbSize"));
    }
    if (cpuConfig.storeLatency < 0)
    {
      errors.add(new Error("Store latency must be non-negative", "storeLatency"));
    }
    if (cpuConfig.loadLatency < 0)
    {
      errors.add(new Error("Load latency must be non-negative", "loadLatency"));
    }
    if (cpuConfig.callStackSize < 1 || cpuConfig.callStackSize > 65536)
    {
      errors.add(new Error("Call stack size must be between 1 and 65536", "callStackSize"));
    }
    // Register file
    if (cpuConfig.speculativeRegisters < 1 || cpuConfig.speculativeRegisters > 1024)
    {
      errors.add(new Error("Speculative registers count must be between 1 and 1024", "speculativeRegisters"));
    }
  }
  
  /**
   * @return True if the configuration is valid, false otherwise
   */
  public boolean isValid()
  {
    return errors.isEmpty();
  }
  
  private void validateBinaryPredictorDefault(String predictorDefault)
  {
    if (!List.of("Taken", "Not Taken").contains(predictorDefault))
    {
      errors.add(new Error("Predictor default must be Taken or Not Taken", "predictorDefault"));
    }
  }
  
  private void validate2BitPredictorDefault(String predictorDefault)
  {
    if (!List.of("Strongly Not Taken", "Weakly Not Taken", "Weakly Taken", "Strongly Taken").contains(predictorDefault))
    {
      errors.add(new Error(
              "Predictor default must be Strongly Not Taken, Weakly Not Taken, Weakly Taken, or Strongly Taken",
              "predictorDefault"));
    }
  }
  
  /**
   * Validates the FU configuration
   */
  private void validateFu(FunctionalUnitDescription unit)
  {
    if (unit == null)
    {
      errors.add(new Error("FU unit is null", "fu"));
      return;
    }
    switch (unit.fuType)
    {
      case FX, FP ->
      {
        // Check capabilities
        if (unit.operations == null)
        {
          errors.add(new Error("FU capabilities are null", "fuCapabilities"));
          return;
        }
        for (FunctionalUnitDescription.Capability capability : unit.operations)
        {
          if (capability == null)
          {
            errors.add(new Error("FU capability is null", "fuCapabilities"));
            continue;
          }
          if (capability.latency < 0)
          {
            errors.add(new Error("FU capability latency must non-negative", "fuCapabilities"));
          }
        }
      }
      case L_S, Branch, Memory ->
      {
        // Check latency
        if (unit.latency < 0)
        {
          errors.add(new Error("FU latency must non-negative", "fuLatency"));
        }
      }
    }
  }
  
  /**
   * Error item.
   */
  public static class Error
  {
    /**
     * Error message
     */
    public String message;
    
    /**
     * Field that caused the error
     */
    public String field;
    
    public Error(String message, String field)
    {
      this.message = message;
      this.field   = field;
    }
  }
}
