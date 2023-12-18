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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    return isValid();
  }
  
  /**
   * Validates the ROB and instruction buffer sizes
   */
  private void validateRobAndInstructionBufferSize(CpuConfig cpuConfig)
  {
    if (cpuConfig.robSize < 1)
    {
      errors.add(new Error("ROB size must be greater than 0", "robSize"));
    }
    if (cpuConfig.commitWidth < 1)
    {
      errors.add(new Error("Commit width must be greater than 0", "commitWidth"));
    }
    if (cpuConfig.robSize < cpuConfig.commitWidth)
    {
      errors.add(new Error("ROB size must be greater than or equal to commit width", "robSize"));
    }
    if (cpuConfig.flushPenalty < 0)
    {
      errors.add(new Error("Flush penalty must be greater than or equal to 0", "flushPenalty"));
    }
    
    if (cpuConfig.fetchWidth < 1)
    {
      errors.add(new Error("Fetch width must be greater than 0", "fetchWidth"));
    }
  }
  
  /**
   * Validates the branch predictor configuration
   */
  private void validateBranchPredictor(CpuConfig cpuConfig)
  {
    if (cpuConfig.btbSize < 1)
    {
      errors.add(new Error("BTB size must be greater than 0", "btbSize"));
    }
    if (cpuConfig.phtSize < 1)
    {
      errors.add(new Error("PHT size must be greater than 0", "phtSize"));
    }
    if (cpuConfig.predictorType == null)
    {
      errors.add(new Error("Predictor type must not be null", "predictorType"));
    }
    // Predictor types: 0bit, 1bit, 2bit
    // 0bit:
    if (Objects.equals(cpuConfig.predictorType, "0bit"))
    {
      // For zero bit one of "Taken", "Not Taken".
      if (!cpuConfig.predictorDefault.equals("Taken") && !cpuConfig.predictorDefault.equals("Not Taken"))
      {
        errors.add(new Error("Predictor default must be Taken or Not Taken", "predictorDefault"));
      }
    }
    else if (Objects.equals(cpuConfig.predictorType, "1bit"))
    {
      // For one bit one of "Taken", "Not Taken".
      if (!cpuConfig.predictorDefault.equals("Taken") && !cpuConfig.predictorDefault.equals("Not Taken"))
      {
        errors.add(new Error("Predictor default must be Taken or Not Taken", "predictorDefault"));
      }
    }
    else if (Objects.equals(cpuConfig.predictorType, "2bit"))
    {
      // For two bit one of "Strongly Not Taken", "Weakly Not Taken", "Weakly Taken", "Strongly Taken".
      if (!cpuConfig.predictorDefault.equals("Strongly Not Taken") && !cpuConfig.predictorDefault.equals(
              "Weakly Not Taken") && !cpuConfig.predictorDefault.equals(
              "Weakly Taken") && !cpuConfig.predictorDefault.equals("Strongly Taken"))
      {
        errors.add(new Error(
                "Predictor default must be Strongly Not Taken, Weakly Not Taken, Weakly Taken or Strongly Taken",
                "predictorDefault"));
      }
    }
    else
    {
      errors.add(new Error("Predictor type must be 0bit, 1bit or 2bit", "predictorType"));
    }
  }
  
  /**
   * Validates all FU configurations
   */
  private void validateFus(CpuConfig cpuConfig)
  {
    if (cpuConfig.fUnits == null)
    {
      errors.add(new Error("FU units are null", "fus"));
      return;
    }
    if (cpuConfig.fUnits.isEmpty())
    {
      errors.add(new Error("There must be at least one FU", "fus"));
    }
    for (CpuConfig.FUnit unit : cpuConfig.fUnits)
    {
      validateFu(unit);
    }
  }
  
  /**
   * Validate the cache configuration
   */
  private void validateCache(CpuConfig cpuConfig)
  {
    if (cpuConfig.cacheLines < 1)
    {
      errors.add(new Error("Cache lines must be greater than 0", "cacheLines"));
    }
    if (cpuConfig.cacheLineSize < 1)
    {
      errors.add(new Error("Cache line size must be greater than 0", "cacheLineSize"));
    }
    if (cpuConfig.cacheAssoc < 1)
    {
      errors.add(new Error("Cache associativity must be greater than 0", "cacheAssociativity"));
    }
    if (cpuConfig.cacheAssoc > cpuConfig.cacheLines)
    {
      errors.add(new Error("Cache associativity must be less than or equal to cache lines", "cacheAssociativity"));
    }
    if (cpuConfig.cacheAssoc % 2 != 0)
    {
      // Todo debatable. Maybe powers of 2 only?
      errors.add(new Error("Cache associativity must be even", "cacheAssociativity"));
    }
    if (!cpuConfig.cacheReplacement.equals("LRU") && !cpuConfig.cacheReplacement.equals(
            "FIFO") && !cpuConfig.cacheReplacement.equals("Random"))
    {
      errors.add(new Error("Cache replacement must be LRU or FIFO or Random", "cacheReplacement"));
    }
    if (!cpuConfig.storeBehavior.equals("write-back") && !cpuConfig.storeBehavior.equals("write-through"))
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
    if (cpuConfig.lbSize < 1)
    {
      errors.add(new Error("Load buffer size must be greater than 0", "lbSize"));
    }
    if (cpuConfig.sbSize < 1)
    {
      errors.add(new Error("Store buffer size must be greater than 0", "sbSize"));
    }
    if (cpuConfig.storeLatency < 0)
    {
      errors.add(new Error("Store latency must be non-negative", "storeLatency"));
    }
    if (cpuConfig.loadLatency < 0)
    {
      errors.add(new Error("Load latency must be non-negative", "loadLatency"));
    }
    if (cpuConfig.callStackSize < 1)
    {
      errors.add(new Error("Call stack size must be greater than 0", "callStackSize"));
    }
    // Register file
    if (cpuConfig.speculativeRegisters < 1)
    {
      errors.add(new Error("Speculative registers must be greater than 0", "speculativeRegisters"));
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
  private void validateFu(CpuConfig.FUnit unit)
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
        for (CpuConfig.FUnit.Capability capability : unit.operations)
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
