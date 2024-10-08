/**
 * @file RegisterFileModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of register file
 * @date 27 October  2020 15:00 (created) \n
 * 21  November 2020 19:00 (revised)
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
package com.gradle.superscalarsim.models.register;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @class RegisterFileModel
 * @brief Definition of register file
 * @details Collection of registers of the same type (integer/floating point)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class RegisterFileModel implements IRegisterFile
{
  /**
   * Name of register file. Used for logs/debug
   */
  private final String name;
  
  /**
   * Data type of register file.
   */
  private final RegisterTypeEnum dataType;
  
  /**
   * List of registers of register file
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final List<RegisterModel> registerList;
  
  /**
   * Constructor for deserialization
   */
  public RegisterFileModel()
  {
    this.name         = "";
    this.dataType     = RegisterTypeEnum.kInt;
    this.registerList = null;
  }
  
  /**
   * @param name         Register file name
   * @param type         Register file type
   * @param registerList List of registers of register file
   *
   * @brief Constructor
   */
  public RegisterFileModel(String name, RegisterTypeEnum type, List<RegisterModel> registerList)
  {
    this.name         = name;
    this.dataType     = type;
    this.registerList = registerList;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * Copy constructor
   */
  public RegisterFileModel(RegisterFileModel registerFile)
  {
    this.name     = registerFile.name;
    this.dataType = registerFile.dataType;
    // Copy registers (deep copy)
    this.registerList = new ArrayList<>();
    for (RegisterModel register : registerFile.registerList)
    {
      this.registerList.add(new RegisterModel(register));
    }
  }// end of Copy constructor
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formatting
   */
  @Override
  public String toString()
  {
    return "Register file: " + name + ", data type: " + dataType + ", " + registerList.size() + " registers";
  }// end of toString
  //------------------------------------------------------
  
  /**
   * @return Name of register file
   * @brief Get name of register file
   */
  public final String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------
  
  /**
   * @return Data type of register file
   * @brief Get data type of register file
   */
  @Override
  public final RegisterTypeEnum getDataType()
  {
    return dataType;
  }// end of getDataType
  //------------------------------------------------------
  
  /**
   * @return List of registers in register file
   * @brief Get registers in register file
   */
  public final List<RegisterModel> getRegisterList()
  {
    return registerList;
  }// end of getRegisterList
  //------------------------------------------------------
  
  /**
   * @param registerName Name of register
   *
   * @return Register, or null if not found
   * @brief Get register by name
   */
  @Override
  public RegisterModel getRegister(String registerName)
  {
    for (RegisterModel register : registerList)
    {
      if (register.getName().equals(registerName))
      {
        return register;
      }
    }
    return null;
  }// end of getRegister
  
  /**
   * @return Number of registers in register file
   */
  @Override
  public int getRegisterCount()
  {
    return registerList.size();
  }
}
