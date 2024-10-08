/**
 * @file SpeculativeRegisterFile.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains implementation of speculative register file
 * @date 19 Oct      2023 21:00 (revised)
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;

import java.util.Map;
import java.util.TreeMap;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class SpeculativeRegisterFile implements IRegisterFile
{
  /**
   * Name of register file. Used for logs/debug
   */
  private final String name = "Speculative Register File";
  
  /**
   * Total number of registers in register file.
   */
  private final int numberOfRegisters;
  
  /**
   * Collection of registers.
   * TODO: serializes as ["tg0":"tg0", "tg1":"tg1", ...], which is weird
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final Map<String, RegisterModel> registers;
  
  /**
   * Factory for creating speculative registers.
   * Ignored in serialization, because it is not needed.
   */
  @JsonIgnore
  private final RegisterModelFactory registerModelFactory;
  
  /**
   * Constructor
   */
  public SpeculativeRegisterFile(int numberOfRegisters, RegisterModelFactory registerModelFactory)
  {
    this.numberOfRegisters    = numberOfRegisters;
    this.registerModelFactory = registerModelFactory;
    this.registers            = new TreeMap<>();
  }
  
  /**
   * Default register file constructor.
   * Used for lazy initialization of register file.
   */
  private RegisterModel createRegister(int id)
  {
    return registerModelFactory.createInstance("tg" + id, false, null, 0, RegisterReadinessEnum.kFree);
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
    if (!registerName.startsWith("tg"))
    {
      // Not found
      return null;
    }
    // Remove tg from register name
    int extractedId = Integer.parseInt(registerName.substring(2));
    
    // Check if register exists
    if (extractedId >= numberOfRegisters)
    {
      // Not found
      return null;
    }
    
    // Check if register is already created
    if (!registers.containsKey(registerName))
    {
      // Create register
      registers.put(registerName, createRegister(extractedId));
    }
    
    // Return register
    return registers.get(registerName);
  }
  
  /**
   * @return Number of registers in register file
   */
  @Override
  public int getRegisterCount()
  {
    // Virtually, they are here.
    return numberOfRegisters;
  }
}
