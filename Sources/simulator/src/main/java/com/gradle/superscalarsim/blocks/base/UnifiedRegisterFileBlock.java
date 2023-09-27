/**
 * @file    UnifiedRegisterFileBlock.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class with all used register files and its registers
 *
 * @date  3  February   2021 16:00 (created) \n
 *        28 April      2021 11:45 (revised)
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.blocks.base;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.RegisterFileModel;
import com.gradle.superscalarsim.models.RegisterModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class UnifiedRegisterFileBlock
 * @brief Class contains interface to interact with all register files and its registers
 */
public class UnifiedRegisterFileBlock
{
  /// Multiplier on how many speculative registers should be created based on existing number of ISA registers
  private static final int specRegisterMultiplier = 10;
  /// InitLoader class holding information about instruction and registers
  private InitLoader initLoader;
  /// List of all register files
  private List<RegisterFileModel> registerList;
  /// Map of state of each register
  private final Map<String, RegisterReadinessEnum> readyMap;

  /**
   * @brief Default constructor - You need to call loadRegisters later
   */
  public UnifiedRegisterFileBlock() {
    this.initLoader   = null;
    this.registerList = new ArrayList<>();
    this.readyMap     = new HashMap<>();
  }

  /**
   * @brief Constructor
   * @param [in] loader - InitLoader class holding information about instruction and registers
   */
  public UnifiedRegisterFileBlock(final InitLoader loader)
  {
    this.initLoader   = loader;
    this.registerList = new ArrayList<>();
    this.readyMap     = new HashMap<>();
    loadRegisters(initLoader.getRegisterFileModelList());
  }// end of Constructor
  //----------------------------------------------------------------------

  public List<RegisterFileModel> getRegisterList() {
    return registerList;
  }

  /**
   * @brief Resets the register from the initial register file
   */
  public void refreshRegisters()
  {
    this.registerList.clear();
    this.readyMap.clear();
    initLoader.load();
    loadRegisters(initLoader.getRegisterFileModelList());
  }// end of refreshRegisters
  //----------------------------------------------------------------------

  /**
   * @brief Get all the register file models
   * @return List of all existing register file models
   */
  public List<RegisterFileModel> getAllRegisterFileModels()
  {
    return this.registerList;
  }// end of getAllRegisterFileModels
  //----------------------------------------------------------------------

  /**
   * @brief Get list of registers based on data type provided. Assumes that there is only one register file with provided data type
   * @param [in] dataType - Data type of searched register list
   * @return List of registers
   */
  public final List<RegisterModel> getRegisterList(DataTypeEnum dataType)
  {
    RegisterFileModel registerModelList = this.registerList.stream()
      .filter(registerFileModel -> registerFileModel.getDataType() == dataType).findFirst().orElse(null);
    return registerModelList == null ? new ArrayList<>() : registerModelList.getRegisterList();
  }// end of getRegisterList
  //----------------------------------------------------------------------

  /**
   * @brief Set register state of provided register name (tag)
   * @param [in] registerName  - Name (tag) of the register
   * @param [in] registerState - New state of the register
   */
  public void setRegisterState(final String registerName, final RegisterReadinessEnum registerState)
  {
    this.readyMap.replace(registerName, registerState);
  }// end of setRegisterState
  //----------------------------------------------------------------------

  /**
   * @brief Get value of register based on provided name (tag)
   * @param [in] registerName - Name (tag) of the register
   * @return Double value
   */
  public double getRegisterValue(final String registerName)
  {
    for (RegisterFileModel registerFile : this.registerList)
    {
      RegisterModel resultRegister = registerFile.getRegisterList().stream()
        .filter(registerModel -> registerModel.getName().equals(registerName)).findFirst().orElse(null);
      if (resultRegister != null)
      {
        return resultRegister.getValue();
      }
    }
    throw new IllegalArgumentException("Register " + registerName + " not found");
  }// end of getRegisterValue
  //----------------------------------------------------------------------

  /**
   * @brief Sets register value specified by register name to provided value
   * @param [in] registerName  - Name (tag) of the register
   * @param [in] registerValue - New double value
   */
  public void setRegisterValue(final String registerName, double registerValue)
  {
    for (RegisterFileModel registerFile : this.registerList)
    {
      RegisterModel resultRegister = registerFile.getRegisterList().stream()
        .filter(registerModel -> registerModel.getName().equals(registerName)).findFirst().orElse(null);
      if (resultRegister != null)
      {
        resultRegister.setValue(registerValue);
        return;
      }
    }
  }// end of setRegisterValue
  //----------------------------------------------------------------------

  /**
   * @brief Get whole map of register states
   * @return Map of register states
   */
  public Map<String, RegisterReadinessEnum> getReadyMap()
  {
    return readyMap;
  }// end of getReadyMap
  //----------------------------------------------------------------------

  /**
   * @brief Copies value from speculative register to architectural one and frees the mapping
   * @param [in] fromRegister
   * @param [in] toRegister
   */
  public void copyAndFree(String fromRegister, String toRegister)
  {
    double value = getRegisterValue(fromRegister);
    setRegisterValue(toRegister, value);
    this.readyMap.replace(fromRegister, RegisterReadinessEnum.kFree);
  }// end of copyAndFree
  //----------------------------------------------------------------------

  /**
   * @brief Load all register files to this class and create the speculative one
   * @param [in] registerFileModelList - List of all architectural register files
   */
  public void loadRegisters(final List<RegisterFileModel> registerFileModelList)
  {
    int registerCount = 0;
    for(RegisterFileModel registerFile : registerFileModelList)
    {
      this.registerList.add(registerFile);
      this.loadMap(registerFile.getRegisterList());
      registerCount = registerCount + registerFile.getRegisterList().size();
    }
    this.registerList.add(createSpeculativeRegisters(registerCount * specRegisterMultiplier));
  }// end of loadRegisters
  //----------------------------------------------------------------------

  /**
   * @brief Loads all architectural registers to readyMap
   * @param [in] registerList - Architectural register list
   */
  private void loadMap(final List<RegisterModel> registerList)
  {
    for (RegisterModel register : registerList)
    {
      readyMap.put(register.getName(), RegisterReadinessEnum.kAssigned);
    }
  }// end of loadMap
  //----------------------------------------------------------------------

  /**
   * @brief Creates speculative register file
   * @param [in] size - Number of speculative registers
   * @return New speculative register file
   */
  private RegisterFileModel createSpeculativeRegisters(int size)
  {
    List<RegisterModel> registerModelList = new ArrayList<>();
    for (int i = 0; i < size; i++)
    {
      registerModelList.add(new RegisterModel("t" + i, false, 0));
      readyMap.put("t" + i, RegisterReadinessEnum.kFree);
    }

    return new RegisterFileModel("Speculative register file", "kSpeculative", registerModelList);
  }// end of createSpeculativeRegisters
  //----------------------------------------------------------------------

  /**
   * @brief Checks if specified register if constant
   * @param [in] registerName - Name of the register
   * @return True if register is constant, false otherwise
   */
  public boolean isRegisterConstant(String registerName)
  {
    for (RegisterFileModel registerFile : this.registerList)
    {
      RegisterModel resultRegister = registerFile.getRegisterList().stream()
        .filter(registerModel -> registerModel.getName().equals(registerName)).findFirst().orElse(null);
      if (resultRegister != null)
      {
        return resultRegister.isConstant();
      }
    }
    return false;
  }// end of isRegisterConstant
  //----------------------------------------------------------------------

  public void setRegisterList(List<RegisterFileModel> registerList) {
    this.registerList = registerList;
  }
}
