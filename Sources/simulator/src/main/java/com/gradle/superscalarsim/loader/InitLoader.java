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
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterFile;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.serialization.Serialization;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @class InitLoader
 * @brief Loads necessary objects for simulation
 * @details Class which loads register files and instruction set. It also provides methods for accessing loaded data.
 */
public class InitLoader
{
  /**
   * Holds register file with all registers and aliases.
   */
  private RegisterFile registerFile;
  /**
   * Holds loaded ISA for interpreting values and action by simulation code
   */
  private Map<String, InstructionFunctionModel> instructionFunctionModels;
  /**
   * Path to the directory with register files
   */
  private String registerFileDirPath = ConfigLoader.registerFileDirPath;
  /**
   * Path to file with instructions definitions
   */
  private String instructionsFilePath = ConfigLoader.instructionsFilePath;
  /**
   * @brief File path with register aliases
   * File structure: array of objects with keys "register" and "alias"
   */
  private String registerAliasesFilePath = ConfigLoader.registerAliasesFilePath;
  
  /**
   * @brief Constructor
   */
  public InitLoader()
  {
    this.registerFile              = null;
    this.instructionFunctionModels = new TreeMap<>();
    
    this.registerFileDirPath     = "./registers/";
    this.instructionsFilePath    = "./supportedInstructions.json";
    this.registerAliasesFilePath = "./registerAliases.json";
    
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
   * @throws NullPointerException Thrown in case of empty directory
   * @brief Calls subLoader for register files and saves them into list
   */
  private List<RegisterFileModel> loadRegisters() throws NullPointerException
  {
    
    List<RegisterFileModel> registerFileModelList = new ArrayList<>();
    final File              registerFolder        = new File(this.registerFileDirPath);
    final RegisterSubloader subLoader             = new RegisterSubloader();
    
    for (final File file : Objects.requireNonNull(registerFolder.listFiles()))
    {
      registerFileModelList.add(subLoader.loadRegisterFile(file.getAbsolutePath()));
    }
    return registerFileModelList;
  }// end of loadRegisters
  
  private List<RegisterMapping> loadAliases() throws IOException
  {
    Reader reader = null;
    reader = Files.newBufferedReader(Paths.get(registerAliasesFilePath));
    // read
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(reader, new TypeReference<>()
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
    Reader       reader       = Files.newBufferedReader(Paths.get(instructionsFilePath));
    // read to a map
    this.instructionFunctionModels = deserializer.readValue(reader,
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
  
  /**
   * @brief Set register aliases file path
   * File format described in field declaration
   */
  public void setRegisterAliasesFilePath(String registerAliasesFilePath)
  {
    this.registerAliasesFilePath = registerAliasesFilePath;
  }
  
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
