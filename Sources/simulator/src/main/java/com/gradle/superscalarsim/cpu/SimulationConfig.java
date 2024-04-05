/**
 * @file SimulationConfig.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Configuration for the simulation - code, memory, buffers, etc.
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

import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.InputCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the simulation - code, memory, buffers, entry point etc.
 */
public class SimulationConfig
{
  /**
   * Code to run.
   * <p>
   * Must be part of the configuration because it is used to create the initial state
   */
  public String code;
  
  /**
   * Memory locations defined outside the code.
   */
  public List<MemoryLocation> memoryLocations;
  
  /**
   * Configuration of the CPU.
   */
  public CpuConfig cpuConfig;
  
  /**
   * The address of the entry point of the code.
   * Can be a label (string) or a number (int).
   * Address 0 is the default entry point (does not need to be specified in JSON).
   * The second instruction would be at address 4, the third at 8, etc.
   */
  public Object entryPoint;
  
  /**
   * @brief Default constructor. Not useful, because it has no code.
   */
  public SimulationConfig()
  {
    memoryLocations = new ArrayList<>();
    code            = "";
    cpuConfig       = CpuConfig.getDefaultConfiguration();
    entryPoint      = 0;
  }
  
  /**
   * Constructor
   */
  public SimulationConfig(String code, List<MemoryLocation> memoryLocations, CpuConfig cpuConfig, Object entryPoint)
  {
    this.code            = code;
    this.memoryLocations = memoryLocations;
    this.cpuConfig       = cpuConfig;
    this.entryPoint      = entryPoint;
  }
  
  /**
   * @brief Default configuration for the simulation. Used for tests.
   */
  public static SimulationConfig getDefaultConfiguration()
  {
    SimulationConfig config = new SimulationConfig();
    config.cpuConfig = CpuConfig.getDefaultConfiguration();
    return config;
  }
  
  /**
   * @brief Validate the configuration
   */
  public ValidationResult validate()
  {
    List<ConfigError> errorMessages = new ArrayList<>();
    
    // Validate CPU config
    CpuConfigValidator configValidator = new CpuConfigValidator();
    configValidator.validate(cpuConfig);
    
    // Add validation for memory locations and code
    
    if (code == null)
    {
      errorMessages.add(new ConfigError("Code must not be null", "code"));
      code = "";
    }
    
    if (memoryLocations == null)
    {
      errorMessages.add(new ConfigError("Memory locations must not be null", "memoryLocations"));
      memoryLocations = new ArrayList<>();
    }
    
    // Safe to parse code
    StaticDataProvider provider = new StaticDataProvider();
    CodeParser codeParser = new CodeParser(provider.getInstructionFunctionModels(),
                                           provider.getRegisterFile().getRegisterMap(true), new InputCodeModelFactory(),
                                           memoryLocations);
    codeParser.parseCode(code);
    
    if (codeParser.hasErrors())
    {
      codeParser.getErrorMessages().forEach(e -> errorMessages.add(new ConfigError(e.message, "code")));
    }
    
    for (MemoryLocation memoryLocation : memoryLocations)
    {
      if (!memoryLocation.isValid())
      {
        errorMessages.add(new ConfigError("Memory location is not valid", "memoryLocations"));
      }
    }
    
    // Check entry point
    if (entryPoint instanceof String)
    {
      // Check if label exists
      if (!codeParser.getLabels().containsKey(entryPoint))
      {
        errorMessages.add(new ConfigError("Entry point label does not exist", "entryPoint"));
      }
    }
    else if (entryPoint instanceof Integer)
    {
      int entry = (Integer) entryPoint;
      // Check if address is valid
      if (entry < 0)
      {
        errorMessages.add(new ConfigError("Entry point address must be greater than 0", "entryPoint"));
      }
      int maxAddress = 4 * codeParser.getInstructions().size();
      if (entry > maxAddress)
      {
        errorMessages.add(new ConfigError("Entry point address must be pointing to a code", "entryPoint"));
      }
      if (entry % 4 != 0)
      {
        errorMessages.add(new ConfigError("Entry point address must be aligned to 4 bytes", "entryPoint"));
      }
    }
    else
    {
      errorMessages.add(new ConfigError("Entry point must be a label string or an address integer", "entryPoint"));
    }
    
    // Check if every instruction has a FU that can execute it
    outer:
    for (InputCodeModel instruction : codeParser.getInstructions())
    {
      String interpretableAs = instruction.instructionFunctionModel().interpretableAs();
      FunctionalUnitDescription.CapabilityName capabilityName = FunctionalUnitDescription.classifyExpression(
              interpretableAs);
      
      for (var cap : cpuConfig.fUnits)
      {
        if (cap.canExecute(capabilityName))
        {
          break outer;
        }
      }
      
      errorMessages.add(
              new ConfigError("No eligible FU found for instruction: " + instruction.instructionFunctionModel().name(),
                              "config"));
      break;
    }
    
    if (errorMessages.isEmpty() && configValidator.isValid())
    {
      return new ValidationResult(true, new ArrayList<>());
    }
    else
    {
      // Join error messages
      List<ConfigError> errors = configValidator.getErrors();
      errorMessages.addAll(errors);
      return new ValidationResult(false, errorMessages);
    }
  }
  
  public static class ValidationResult
  {
    public boolean valid;
    public List<ConfigError> messages;
    
    public ValidationResult(boolean valid, List<ConfigError> messages)
    {
      this.valid    = valid;
      this.messages = messages;
    }
    
    /**
     * @return A comma separated list of error messages.
     */
    @Override
    public String toString()
    {
      return String.join(", ", messages.stream().map(ConfigError::toString).toList());
    }
  }
}
