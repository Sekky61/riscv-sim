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
import com.gradle.superscalarsim.code.Label;
import com.gradle.superscalarsim.code.ParseError;
import com.gradle.superscalarsim.loader.InitLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the simulation - code, memory, buffers, etc.
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
   * @brief Default constructor for deserialization
   */
  public SimulationConfig()
  {
    memoryLocations = new ArrayList<>();
    code            = "";
  }
  
  /**
   * Constructor
   */
  public SimulationConfig(String code, List<MemoryLocation> memoryLocations, CpuConfig cpuConfig)
  {
    this.code            = code;
    this.memoryLocations = memoryLocations;
    this.cpuConfig       = cpuConfig;
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
    CpuConfigValidator configValidator = new CpuConfigValidator();
    configValidator.validate(cpuConfig);
    List<CpuConfigValidator.Error> errorMessages = new ArrayList<>();
    List<ParseError>               codeErrors    = null;
    
    // Add validation for memory locations and code
    
    if (code == null)
    {
      errorMessages.add(new CpuConfigValidator.Error("Code must not be null", "code"));
    }
    
    // Parse code
    CodeParser codeParser = new CodeParser(new InitLoader());
    codeParser.parseCode(code, getMemoryLocationLabels());
    
    if (!codeParser.success())
    {
      errorMessages.add(new CpuConfigValidator.Error("Code contains errors", "code"));
      codeErrors = codeParser.getErrorMessages();
    }
    
    // Memory allocations
    if (memoryLocations == null)
    {
      errorMessages.add(new CpuConfigValidator.Error("Memory locations must not be null", "memoryLocations"));
    }
    else
    {
      for (MemoryLocation memoryLocation : memoryLocations)
      {
        if (memoryLocation.alignment < 0)
        {
          errorMessages.add(
                  new CpuConfigValidator.Error("Memory location alignment must be greater than 0", "memoryLocations"));
        }
        if (memoryLocation.getBytes() == null)
        {
          errorMessages.add(new CpuConfigValidator.Error("Memory location bytes must not be null", "memoryLocations"));
        }
        if (memoryLocation.name == null || memoryLocation.name.isEmpty())
        {
          errorMessages.add(
                  new CpuConfigValidator.Error("Memory location name must not be null or empty", "memoryLocations"));
        }
      }
    }
    
    if (errorMessages.isEmpty() && configValidator.isValid())
    {
      return new ValidationResult(true, null, null);
    }
    else
    {
      // Join error messages
      List<CpuConfigValidator.Error> errors = configValidator.getErrors();
      errorMessages.addAll(errors);
      return new ValidationResult(false, errorMessages, codeErrors);
    }
  }
  
  /**
   * Extract names of memory locations defined outside the code
   */
  public Map<String, Label> getMemoryLocationLabels()
  {
    Map<String, Label> names = new HashMap<>();
    for (MemoryLocation memoryLocation : memoryLocations)
    {
      names.put(memoryLocation.name, new Label(memoryLocation.name, -1));
    }
    return names;
  }
  
  public static class ValidationResult
  {
    public boolean valid;
    public List<CpuConfigValidator.Error> messages;
    public List<ParseError> codeErrors;
    
    public ValidationResult(boolean valid, List<CpuConfigValidator.Error> messages, List<ParseError> codeErrors)
    {
      this.valid      = valid;
      this.messages   = messages;
      this.codeErrors = codeErrors;
    }
  }
}
