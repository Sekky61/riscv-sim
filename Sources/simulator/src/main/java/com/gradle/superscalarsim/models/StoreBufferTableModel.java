/**
 * @file    StoreBufferTableModel.java
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
 * @brief File contains container class for Store buffer table entry
 *
 * @date  14 March   2021 12:00 (created) \n
 *        17 March   2021 20:00 (revised)
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

/**
 * @class StoreBufferTableModel
 * @brief Container class for Store buffer table entry
 */
public class StoreBufferTableModel
{
  /// Code line of the instruction
  private final String codeLine;
  /// String value if the address is ready
  private final String addressReady;
  /// String value if the data are ready
  private final String dataReady;

  /**
   * @brief Constructor
   * @param [in] codeModel - CodeModel containing information about the simulated instruction
   * @param [in] item      - Store buffer item containing additional store buffer data
   */
  public StoreBufferTableModel(SimCodeModel codeModel, StoreBufferItem item)
  {
    this.codeLine     = codeModel.getRenamedCodeLine();
    this.addressReady = Long.toHexString(item.getAddress());
    this.dataReady    = item.isSourceReady() ? "READY" : "WAIT";
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Dummy constructor
   */
  public StoreBufferTableModel()
  {
    this.codeLine     = "";
    this.addressReady = "";
    this.dataReady    = "";
  }// end of Dummy Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Get code line
   * @return String code line
   */
  public String getCodeLine()
  {
    return codeLine;
  }// end of getCodeLine
  //----------------------------------------------------------------------

  /**
   * @brief Get string value of the state of the store address
   * @return String value if the store value is ready
   */
  public String getAddressReady()
  {
    return addressReady;
  }// end of getAddressReady
  //----------------------------------------------------------------------

  /**
   * @brief Get string value if the store data are ready
   * @return String value if the store data are ready
   */
  public String getDataReady()
  {
    return dataReady;
  }// end of getDataReady
  //----------------------------------------------------------------------

  /**
   * @brief Check if the address is ready
   * @return True if the address is ready, false otherwise
   */
  public boolean isAddressReady()
  {
    return this.addressReady.equals("READY");
  }// end of isAddressReady
  //----------------------------------------------------------------------

  /**
   * @brief Checks if the data are ready
   * @return True if the data are ready, false otherwise
   */
  public boolean isDataReady()
  {
    return this.dataReady.equals("READY");
  }// end of isDataReady
  //----------------------------------------------------------------------
}
