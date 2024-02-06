/**
 * @file InitLoader.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains initialization loader of registers and instructions used in simulation
 * @date 27 October  2020 15:00 (created) \n
 * 11 November 2020 11:30 (revised)
 * 26 Sep      2023 10:00 (revised)
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
package com.gradle.superscalarsim.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterFile;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.serialization.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @class InitLoader
 * @brief Loads necessary objects for simulation
 * @details Class which loads register files and instruction set. It also provides methods for accessing loaded data.
 */
public class InitLoader
{
  /**
   * @brief Resource path to a file with supported instructions
   */
  public String supportedInstructionsResourcePath = "/supportedInstructions.json";
  /**
   * @brief Resource path to a file with register aliases
   * File structure: array of objects with keys "register" and "alias"
   */
  public String registerAliasesResourcePath = "/registerAliases.json";
  /**
   * Path to the directory in resources with individual register files
   */
  public String registerFileResourceDirPath = "/registerFiles.json";
  /**
   * Holds register file with all registers and aliases.
   */
  private RegisterFile registerFile;
  /**
   * Holds loaded ISA for interpreting values and action by simulation code
   */
  private Map<String, InstructionFunctionModel> instructionFunctionModels;
  
  /**
   * @brief Constructor
   */
  public InitLoader()
  {
    this.registerFile              = null;
    this.instructionFunctionModels = new TreeMap<>();
    
    try
    {
      this.loadFromConfigFiles();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }// end of Constructor
  
  /**
   * @brief Calls appropriate subLoaders and loads lists from files. The alternative is to set the data using setters.
   * TODO: try the paths while starting the server
   */
  public void loadFromConfigFiles() throws IOException
  {
    List<RegisterFileModel> registerFileModels = loadRegisters();
    List<RegisterMapping>   registerMappings   = loadAliases();
    this.registerFile = new RegisterFile(registerFileModels, registerMappings);
    loadInstructions();
  }// end of load
  
  /**
   * @throws IOException Thrown in case of invalid file or file not found
   * @brief Calls subLoader for register files and saves them into list
   */
  private List<RegisterFileModel> loadRegisters() throws IOException
  {
    InputStream  s            = this.getClass().getResourceAsStream(registerFileResourceDirPath);
    ObjectMapper deserializer = Serialization.getDeserializer();
    List<RegisterFileModel> file = deserializer.readValue(s, new TypeReference<>()
    {
    });
    
    // Arch. registers are assigned by default
    for (RegisterFileModel registerFileModel : file)
    {
      for (RegisterModel register : registerFileModel.getRegisterList())
      {
        register.setReadiness(RegisterReadinessEnum.kAssigned);
      }
    }
    
    return file;
  }
  
  private List<RegisterMapping> loadAliases() throws IOException
  {
    InputStream  s            = this.getClass().getResourceAsStream(registerAliasesResourcePath);
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(s, new TypeReference<>()
    {
    });
  }
  
  /**
   * @throws IOException Thrown in case of invalid file or file not found
   * @brief Reads the configuration file and loads instructions
   */
  private void loadInstructions() throws IOException
  {
    // All instructions are in a single .json file.
    // The structure is a single object with keys being the instruction names and
    // values being the InstructionFunctionModel objects.
    ObjectMapper deserializer = Serialization.getDeserializer();
    
    // Read the resource /supportedInstructions.json
    InputStream s = this.getClass().getResourceAsStream(supportedInstructionsResourcePath);
    
    // read to a map
    this.instructionFunctionModels = deserializer.readValue(s,
                                                            new TypeReference<Map<String, InstructionFunctionModel>>()
                                                            {
                                                            });
  }// end of loadInstructions
  
  /**
   * @brief Constructor, but does not load registers from files
   */
  public InitLoader(List<RegisterFileModel> registerFileModels, List<RegisterMapping> registerAliases)
  {
    this.registerFile              = new RegisterFile(registerFileModels, registerAliases);
    this.instructionFunctionModels = new TreeMap<>();
    try
    {
      loadInstructions();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return Register file
   */
  public RegisterFile getRegisterFile()
  {
    return registerFile;
  }// end of getRegisterFile
  //------------------------------------------------------
  
  public InstructionFunctionModel getInstructionFunctionModel(String instructionName)
  {
    return getInstructionFunctionModels().get(instructionName);
  }
  
  /**
   * @return loaded instruction set
   */
  public Map<String, InstructionFunctionModel> getInstructionFunctionModels()
  {
    return instructionFunctionModels;
  }// end of getInstructionFunctionModelList
  //------------------------------------------------------
  
  /**
   * @brief Testing purposes only
   */
  public void setInstructionFunctionModels(Map<String, InstructionFunctionModel> instructionFunctionModels)
  {
    this.instructionFunctionModels = instructionFunctionModels;
  }
  //------------------------------------------------------
  
  public static class RegisterMapping
  {
    public String register;
    public String alias;
    
    /**
     * @brief Default constructor for deserialization
     */
    RegisterMapping()
    {
    }
    
    public RegisterMapping(String register, String alias)
    {
      this.register = register;
      this.alias    = alias;
    }
  }
}
