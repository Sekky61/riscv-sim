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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterFileModel;

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
   * Holds loaded register files
   */
  private List<RegisterFileModel> registerFileModelList;
  
  /**
   * Holds loaded ISA for interpreting values and action by simulation code
   */
  private Map<String, InstructionFunctionModel> instructionFunctionModels;
  
  /**
   * Path to the directory with register files
   */
  private String registerFileDirPath;
  
  /**
   * Path to file with instructions definitions
   */
  private String instructionsFilePath;
  
  /**
   * @brief File path with register aliases
   * File structure: array of objects with keys "register" and "alias"
   */
  private String registerAliasesFilePath;
  
  /**
   * The aliases between registers.
   * The key is the architecture name (x0), the value is the alias (zero).
   * Must be a list - register x8 has two aliases (s0 and fp).
   */
  private List<RegisterMapping> registerAliases;
  
  /**
   * Holds error message, if any occurs, otherwise is empty
   */
  private String errorMessage;
  
  /**
   * @brief Constructor
   */
  public InitLoader()
  {
    this.registerFileModelList     = new ArrayList<>();
    this.instructionFunctionModels = new TreeMap<>();
    
    this.registerFileDirPath     = "./registers/";
    this.instructionsFilePath    = "./supportedInstructions.json";
    this.registerAliasesFilePath = "./registerAliases.json";
    
    this.errorMessage = "";
    this.loadFromConfigFiles();
  }// end of Constructor
  
  /**
   * @return List of register files
   * @brief Get loaded register files
   */
  public List<RegisterFileModel> getRegisterFileModelList()
  {
    return registerFileModelList;
  }// end of getRegisterFileModelList
  
  public void setRegisterFileModelList(List<RegisterFileModel> registerFileModelList)
  {
    this.registerFileModelList = registerFileModelList;
  }
  
  /**
   * @brief Calls appropriate subloaders and loads lists from files. The alternative is to set the data using setters.
   */
  public void loadFromConfigFiles()
  {
    try
    {
      loadRegisters();
      loadInstructions();
      loadAliases();
    }
    catch (NullPointerException | IOException e)
    {
      handleNullPointerException();
    }
  }// end of load
  //------------------------------------------------------
  
  /**
   * @throws NullPointerException Thrown in case of empty directory
   * @brief Calls subloader for register files and saves them into list
   */
  private void loadRegisters() throws NullPointerException
  {
    this.registerFileModelList.clear();
    final File              registerFolder = new File(this.registerFileDirPath);
    final RegisterSubloader subloader      = new RegisterSubloader();
    
    for (final File file : Objects.requireNonNull(registerFolder.listFiles()))
    {
      this.registerFileModelList.add(subloader.loadRegisterFile(file.getAbsolutePath()));
    }
  }// end of loadRegisters
  //------------------------------------------------------
  
  /**
   * @throws NullPointerException Thrown in case of empty directory
   * @brief Calls subloader for instruction set and saves it into list
   */
  private void loadInstructions() throws NullPointerException, IOException
  {
    // All instructions are in a single .json file.
    // The structure is a single object with keys being the instruction names and
    // values being the InstructionFunctionModel objects.
    
    Gson   gson   = new Gson();
    Reader reader = Files.newBufferedReader(Paths.get(instructionsFilePath));
    // read to a map
    Map<String, InstructionFunctionModel> instructions = gson.fromJson(reader,
                                                                       new TypeToken<Map<String, InstructionFunctionModel>>()
                                                                       {
                                                                       }.getType());
    // add to list
    this.instructionFunctionModels = instructions;
  }// end of loadInstructions
  
  private void loadAliases()
  {
    Gson   gson   = new Gson();
    Reader reader = null;
    try
    {
      reader = Files.newBufferedReader(Paths.get(registerAliasesFilePath));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    // read
    registerAliases = gson.fromJson(reader, new TypeToken<List<RegisterMapping>>()
    {
    }.getType());
  }
  
  /**
   * @brief Sets error message in case of NullPointerException and prints it into stderr
   */
  private void handleNullPointerException()
  {
    if (registerFileModelList.isEmpty())
    {
      this.errorMessage = "Directory with register files is empty. Aborting...";
    }
    else
    {
      this.errorMessage = "Directory with instructions is empty. Aborting...";
    }
    System.err.println(this.errorMessage);
  }// end of handleNullPointerException
  //------------------------------------------------------
  
  public InstructionFunctionModel getInstructionFunctionModel(String instructionName)
  {
    return getInstructionFunctionModels().get(instructionName);
  }
  //------------------------------------------------------
  
  /**
   * @return Set of instructions in list
   * @brief Get loaded instruction set
   */
  public Map<String, InstructionFunctionModel> getInstructionFunctionModels()
  {
    return instructionFunctionModels;
  }// end of getInstructionFunctionModelList
  //------------------------------------------------------
  
  public void setInstructionFunctionModels(Map<String, InstructionFunctionModel> instructionFunctionModels)
  {
    this.instructionFunctionModels = instructionFunctionModels;
  }
  
  /**
   * @return Error message
   * @brief Get error message in case of load failure
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }// end of getErrorMessage
  
  /**
   * @brief Get register aliases
   */
  public List<RegisterMapping> getRegisterAliases()
  {
    return registerAliases;
  }
  
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
    
    public RegisterMapping(String register, String alias)
    {
      this.register = register;
      this.alias    = alias;
    }
  }
}
