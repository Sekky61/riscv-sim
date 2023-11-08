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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.register.IRegisterFile;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.register.SpeculativeRegisterFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class UnifiedRegisterFileBlock
 * @brief Class contains interface to interact with all register files and its registers
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class UnifiedRegisterFileBlock
{
  /**
   * Multiplier on how many speculative registers should be created based on existing number of ISA registers
   * TODO: Take the total number of registers on construction
   */
  private static final int specRegisterMultiplier = 10;
  
  /**
   * Mapping of names to register objects.
   * Allows to have multiple names for one register.
   * Speculative registers are not included in this map.
   * Also, theoretically faster than searching through the list (O(1) vs O(n)).
   */
  private final Map<String, RegisterModel> registerMap;
  
  /**
   * Speculative register file. Holds all speculative registers.
   */
  private SpeculativeRegisterFile speculativeRegisterFile;
  
  /**
   * Constructor for (de)serialization
   */
  public UnifiedRegisterFileBlock()
  {
    registerMap = new HashMap<>();
  }// end of Constructor
  
  /**
   * @param loader InitLoader class holding information about instruction and registers. Only needed during initialization.
   *
   * @brief Constructor
   */
  public UnifiedRegisterFileBlock(final InitLoader loader)
  {
    this.registerMap = new HashMap<>();
    loadRegisters(loader.getRegisterFileModelList());
    loadAliases(loader.getRegisterAliases());
  }// end of Constructor
  
  /**
   * The provided registers must be copied! Otherwise, the original register files will be modified,
   * as the owner (initLoader) does not get destroyed during backwards simulation.
   *
   * @param registerFileModelList List of all architectural register files
   *
   * @brief Load all register files to this class and create the speculative file
   */
  public void loadRegisters(final List<RegisterFileModel> registerFileModelList)
  {
    int registerCount = 0;
    for (RegisterFileModel registerFile : registerFileModelList)
    {
      RegisterFileModel fileCopy = new RegisterFileModel(registerFile);
      // Put entry into the map for each register
      for (RegisterModel register : fileCopy.getRegisterList())
      {
        this.registerMap.put(register.getName(), register);
      }
      registerCount = registerCount + registerFile.getRegisterList().size();
    }
    speculativeRegisterFile = new SpeculativeRegisterFile(registerCount * specRegisterMultiplier);
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
   * @param registerName Name (tag) of the register
   *
   * @return The register object
   * @brief Get register object based on provided name (tag or arch. name)
   */
  public RegisterModel getRegister(final String registerName)
  {
    RegisterModel reg = this.registerMap.get(registerName);
    if (reg == null)
    {
      return speculativeRegisterFile.getRegister(registerName);
    }
    return reg;
  }// end of getRegisterValue
  //----------------------------------------------------------------------
  
  /**
   * Does not copy the objects. Does not manipulate speculative registers.
   *
   * @param registerList List of register files to set
   *
   * @brief Set all registers
   */
  public void setRegistersWithList(List<RegisterFileModel> registerList)
  {
    registerMap.clear();
    for (RegisterFileModel registerFileModel : registerList)
    {
      for (RegisterModel registerModel : registerFileModel.getRegisterList())
      {
        this.registerMap.put(registerModel.getName(), registerModel);
      }
    }
  }
  //----------------------------------------------------------------------
  
  public IRegisterFile getSpeculativeRegisterFile()
  {
    return speculativeRegisterFile;
  }
  
  /**
   * @param fromRegister Register to copy from
   * @param toRegister   Register to copy to
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
  
}
