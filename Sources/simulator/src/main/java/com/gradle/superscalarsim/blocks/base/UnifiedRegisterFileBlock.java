/**
 * @file UnifiedRegisterFileBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class with all used register files and its registers
 * @date 3  February   2021 16:00 (created) \n
 * 28 April      2021 11:45 (revised)
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
  /**
   * Multiplier on how many speculative registers should be created based on existing number of ISA registers
   * TODO: Take the total number of registers on construction
   */
  private static final int specRegisterMultiplier = 10;
  /**
   * InitLoader class holding information about instruction and registers
   * TODO: remove this reference
   */
  private final InitLoader initLoader;
  /**
   * Mapping of names to register objects
   * Allows to have multiple names for one register
   * Also theoretically faster than searching through the list (O(1) vs O(n))
   */
  private final Map<String, RegisterModel> registerMap;
  /**
   * List of all register files
   * TODO: Remove this list and use a field for each register file type (one for ints, ...)
   */
  private List<RegisterFileModel> registerList;
  
  /**
   * @brief Default constructor - You need to call loadRegisters later
   */
  public UnifiedRegisterFileBlock()
  {
    this.initLoader   = null;
    this.registerList = new ArrayList<>();
    this.registerMap  = null;
  }
  
  /**
   * @param [in] loader - InitLoader class holding information about instruction and registers
   *
   * @brief Constructor
   */
  public UnifiedRegisterFileBlock(final InitLoader loader)
  {
    this.initLoader   = loader;
    this.registerList = new ArrayList<>();
    this.registerMap  = new HashMap<>();
    loadRegisters(initLoader.getRegisterFileModelList());
    loadAliases(initLoader.getRegisterAliases());
  }// end of Constructor
  
  /**
   * The provided registers must be copied! Otherwise, the original register files will be modified,
   * as the owner (initLoader) does not get destroyed during backwards simulation
   *
   * @param [in] registerFileModelList - List of all architectural register files
   *
   * @brief Load all register files to this class and create the speculative one
   */
  public void loadRegisters(final List<RegisterFileModel> registerFileModelList)
  {
    int registerCount = 0;
    for (RegisterFileModel registerFile : registerFileModelList)
    {
      // Copy into registerList
      RegisterFileModel fileCopy = new RegisterFileModel(registerFile);
      this.registerList.add(fileCopy);
      // Put entry into the map for each register
      for (RegisterModel register : fileCopy.getRegisterList())
      {
        this.registerMap.put(register.getName(), register);
      }
      registerCount = registerCount + registerFile.getRegisterList().size();
    }
    this.registerList.add(createSpeculativeRegisters(registerCount * specRegisterMultiplier));
  }// end of loadRegisters
  //----------------------------------------------------------------------
  
  private void loadAliases(List<InitLoader.RegisterMapping> registerAliases)
  {
    for (InitLoader.RegisterMapping alias : registerAliases)
    {
      RegisterModel register = getRegister(alias.register);
      registerMap.put(alias.alias, register);
    }
  }
  //----------------------------------------------------------------------
  
  /**
   * @param [in] size - Number of speculative registers
   *
   * @return New speculative register file
   * @brief Creates speculative register file
   */
  private RegisterFileModel createSpeculativeRegisters(int size)
  {
    // TODO: Do not instantiate all speculative registers ahead of time
    List<RegisterModel> registerModelList = new ArrayList<>();
    for (int i = 0; i < size; i++)
    {
      RegisterModel reg = new RegisterModel("tg" + i, false, DataTypeEnum.kSpeculative, 0, RegisterReadinessEnum.kFree);
      registerModelList.add(reg);
      this.registerMap.put(reg.getName(), reg);
    }
    
    return new RegisterFileModel("Speculative register file", "kSpeculative", registerModelList);
  }// end of createSpeculativeRegisters
  //----------------------------------------------------------------------
  
  /**
   * @param [in] registerName - Name (tag) of the register
   *
   * @return The register object
   * @brief Get object representation of register based on provided name (tag or arch. name)
   */
  public RegisterModel getRegister(final String registerName)
  {
    return this.registerMap.get(registerName);
  }// end of getRegisterValue
  //----------------------------------------------------------------------
  
  public void setRegisterList(List<RegisterFileModel> registerList)
  {
    this.registerList = registerList;
  }
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the register from the initial register file
   */
  public void refreshRegisters()
  {
    this.registerList.clear();
    this.registerMap.clear();
    initLoader.loadFromConfigFiles();
    loadRegisters(initLoader.getRegisterFileModelList());
  }// end of refreshRegisters
  //----------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type of searched register list
   *
   * @return List of registers
   * @brief Get list of registers based on data type provided. Assumes that there is only one register file with
   * provided data type
   */
  public final List<RegisterModel> getRegisterList(DataTypeEnum dataType)
  {
    RegisterFileModel registerModelList = this.registerList.stream()
            .filter(registerFileModel -> registerFileModel.getDataType() == dataType).findFirst().orElse(null);
    return registerModelList == null ? new ArrayList<>() : registerModelList.getRegisterList();
  }// end of getRegisterList
  //----------------------------------------------------------------------
  
  /**
   * @param [in] fromRegister
   * @param [in] toRegister
   *
   * @brief Copies value from speculative register to architectural one and frees the mapping
   */
  public void copyAndFree(String fromRegister, String toRegister)
  {
    RegisterModel fromRegisterModel = getRegister(fromRegister);
    RegisterModel toRegisterModel   = getRegister(toRegister);
    
    double value = fromRegisterModel.getValue();
    toRegisterModel.setValue(value);
    fromRegisterModel.setReadiness(RegisterReadinessEnum.kFree);
  }// end of copyAndFree
  //----------------------------------------------------------------------
  
  /**
   * @return Map of all registers. For testing purposes *only*
   */
  public Map<String, RegisterModel> getRegisterMap()
  {
    return registerMap;
  }
}
