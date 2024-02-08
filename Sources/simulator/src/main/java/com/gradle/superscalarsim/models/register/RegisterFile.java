/**
 * @file RegisterFile.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief The base description of RISC-V register file, with aliases.
 * @date 27 October  2020 15:00 (created) \n
 * 11 November 2020 11:30 (revised)
 * 19 Dec      2023 19:00 (revised)
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

package com.gradle.superscalarsim.models.register;

import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.loader.RegisterMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The base description of RISC-V register file, with aliases.
 * Used for loading register file from JSON, validation outside of simulation.
 */
public class RegisterFile implements IRegisterFile
{
  
  /**
   * Holds loaded register files
   */
  private List<RegisterFileModel> registerFileModelList;
  
  /**
   * The aliases between registers.
   * The key is the architecture name (x0), the value is the alias (zero).
   * Must be a list - register x8 has two aliases (s0 and fp).
   */
  private List<RegisterMapping> registerAliases;
  
  /**
   * @param registerFileModelList List of register files
   * @param registerAliases       List of register aliases
   *
   * @brief Constructor of register file
   */
  public RegisterFile(List<RegisterFileModel> registerFileModelList, List<RegisterMapping> registerAliases)
  {
    this.registerFileModelList = registerFileModelList;
    this.registerAliases       = registerAliases;
  }
  
  /**
   * Deep copy of register file
   */
  public RegisterFile(RegisterFile registerFile)
  {
    this.registerAliases       = registerFile.registerAliases;
    this.registerFileModelList = new ArrayList<>();
    
    for (RegisterFileModel registerFileModel : registerFile.registerFileModelList)
    {
      this.registerFileModelList.add(new RegisterFileModel(registerFileModel));
    }
  }
  
  /**
   * @return Type of register file
   */
  @Override
  public RegisterTypeEnum getDataType()
  {
    return null;
  }
  
  /**
   * @param registerName Name of register
   *
   * @return Register with given name or null if not found
   */
  @Override
  public RegisterModel getRegister(String registerName)
  {
    // Look for an alias first
    for (RegisterMapping registerMapping : registerAliases)
    {
      if (registerMapping.alias.equals(registerName))
      {
        registerName = registerMapping.register;
        break;
      }
    }
    for (RegisterFileModel registerFileModel : registerFileModelList)
    {
      RegisterModel reg = registerFileModel.getRegister(registerName);
      if (reg != null)
      {
        return reg;
      }
    }
    return null;
  }
  
  /**
   * @return Number of registers in register file
   */
  @Override
  public int getRegisterCount()
  {
    int acc = 0;
    for (RegisterFileModel registerFileModel : registerFileModelList)
    {
      acc += registerFileModel.getRegisterCount();
    }
    return acc;
  }
  
  /**
   * @param includeAliases Whether to include aliases in the map.
   *
   * @return A map with all registers, including aliases pointing to the same object.
   */
  public Map<String, RegisterModel> getRegisterMap(boolean includeAliases)
  {
    Map<String, RegisterModel> registerMap = new java.util.HashMap<>();
    for (RegisterFileModel registerFile : registerFileModelList)
    {
      // Place of a previous bug:
      // copying register objects would mean that the original register,
      // which is in the managers Weak map, would be deallocated
      for (RegisterModel register : registerFile.getRegisterList())
      {
        // Put entry into the map for each register
        registerMap.put(register.getName(), register);
      }
    }
    if (!includeAliases || registerAliases == null)
    {
      return registerMap;
    }
    // Add aliases
    for (RegisterMapping alias : registerAliases)
    {
      RegisterModel register = registerMap.get(alias.register);
      registerMap.put(alias.alias, register);
    }
    return registerMap;
  }
}
