/**
 * @file    RegisterFileModel.java
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
 * @brief File contains class of register file
 *
 * @date  27 October  2020 15:00 (created) \n
 *        21  November 2020 19:00 (revised)
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
package com.gradle.superscalarsim.models;

import com.gradle.superscalarsim.enums.DataTypeEnum;

import java.util.List;

/**
 * @class RegisterFileModel
 * @brief Definition of register file
 * @details Class contains definition of register file, which is used for displaying and storing simulation values
 */
public class RegisterFileModel
{
  /// Name of register file
  private final String name;
  /// Data type of register file
  private final DataTypeEnum dataType;
  /// List of registers of register file
  private final List<RegisterModel> registerList;

  // TODO: This acts as default values, meaning file reading does not fail on bad json file
  public RegisterFileModel() {
    this.name = "";
    this.dataType = DataTypeEnum.kInt;
    this.registerList = null;
  }

  /**
   * @brief Constructor
   * @param [in] name         - Register file name
   * @param [in] dataType     - Register file data type
   * @param [in] registerList - List of registers of register file
   */
  public RegisterFileModel(String name, String dataType, List<RegisterModel> registerList)
  {
    this.name = name;
    this.dataType = DataTypeEnum.valueOf(dataType);
    this.registerList = registerList;
  }// end of Constructor
  //------------------------------------------------------

  /**
   * @brief Overrides toString method with custom formating
   * @return String representation of the object
   */
  @Override
  public String toString()
  {
    return "Register file: " + name + '\n' +
      "data type: " + dataType + '\n' +
      "registers: " + '\n' +
      "------------------------------------" + '\n'
      + registerList +
      "------------------------------------" + '\n';
  }// end of toString
  //------------------------------------------------------

  /**
   * @brief Get name of register file
   * @return Name of register file
   */
  public final String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------

  /**
   * @brief Get data type of register file
   * @return Data type of register file
   */
  public final DataTypeEnum getDataType()
  {
    return dataType;
  }// end of getDataType
  //------------------------------------------------------

  /**
   * @brief Get registers in register file
   * @return List of registers in register file
   */
  public final List<RegisterModel> getRegisterList()
  {
    return registerList;
  }// end of getRegisterList
  //------------------------------------------------------
}
