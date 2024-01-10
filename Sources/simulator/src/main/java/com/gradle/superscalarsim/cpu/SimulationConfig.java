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
  CpuConfig cpuConfig;
  
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
  public CpuConfig.ValidationResult validate()
  {
    CpuConfig.ValidationResult result        = cpuConfig.validate();
    List<String>               errorMessages = new ArrayList<>();
    List<ParseError>           codeErrors    = null;
    
    // Add validation for memory locations and code
    
    if (code == null)
    {
      errorMessages.add("Code must not be null");
    }
    
    // Parse code
    CodeParser codeParser = new CodeParser(new InitLoader());
    codeParser.parseCode(code, getMemoryLocationLabels());
    
    if (!codeParser.success())
    {
      errorMessages.add("Code parsing failed");
      codeErrors = codeParser.getErrorMessages();
    }
    
    // Memory allocations
    if (memoryLocations == null)
    {
      errorMessages.add("Memory locations must not be null");
    }
    else
    {
      for (MemoryLocation memoryLocation : memoryLocations)
      {
        if (memoryLocation.alignment < 0)
        {
          errorMessages.add("Memory location alignment must be greater than 0");
        }
        if (memoryLocation.getBytes() == null)
        {
          errorMessages.add("Memory location value must not be null");
        }
        if (memoryLocation.name == null || memoryLocation.name.isEmpty())
        {
          errorMessages.add("Memory location name must not be empty");
        }
      }
    }
    
    
    if (errorMessages.isEmpty() && result.messages == null)
    {
      return new CpuConfig.ValidationResult(true, null, null);
    }
    else
    {
      // Join error messages
      if (result.messages != null)
      {
        errorMessages.addAll(result.messages);
      }
      return new CpuConfig.ValidationResult(false, errorMessages, codeErrors);
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
}
