/**
 * @file LoadBufferTableModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains container class for Load buffer table entries
 * @date 18 April  2020 15:00 (created) \n
 * 28 April  2020 18:00 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class LoadBufferTableModel
 * @brief Container class for Load buffer table entries
 */
public class LoadBufferTableModel
{
  /// Code line of the load instruction
  private final String codeLine;
  /// String value telling if the address of the load instruction is ready
  private final String addressReady;
  /// String value telling if the data are ready
  private final String dataReady;
  /// String value telling if load was completed by load bypassing
  private final String bypassed;
  /// Load buffer item for comparing and highlighting
  private final LoadBufferItem item;
  
  /**
   * @param [in] codeModel -
   * @param [in] item      -
   *
   * @brief Constructor
   */
  public LoadBufferTableModel(SimCodeModel codeModel, LoadBufferItem item)
  {
    this.codeLine     = codeModel.getRenamedCodeLine();
    this.addressReady = Long.toHexString(item.getAddress());
    this.dataReady    = item.isDestinationReady() ? "READY" : "WAIT";
    this.bypassed     = item.hasBypassed() ? "YES" : "NO";
    this.item         = item;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Dummy Constructor
   */
  public LoadBufferTableModel()
  {
    this.codeLine     = "";
    this.addressReady = "";
    this.dataReady    = "";
    this.bypassed     = "";
    this.item         = null;
  }// end of Dummy Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return Code line of instruction
   * @brief Get code line of the instruction
   */
  public String getCodeLine()
  {
    return codeLine;
  }// end of getCodeLine
  //----------------------------------------------------------------------
  
  /**
   * @return String value of the address
   * @brief Get string state of the address
   */
  public String getAddressReady()
  {
    return addressReady;
  }// end of getAddressReady
  //----------------------------------------------------------------------
  
  /**
   * @return String state of data
   * @brief Get string state of the data
   */
  public String getDataReady()
  {
    return dataReady;
  }// end of getDataReady
  //----------------------------------------------------------------------
  
  /**
   * @return True if address is ready, false otherwise
   * @brief Check if the address is ready
   */
  public boolean isAddressReady()
  {
    return this.addressReady.equals("READY");
  }// end of isAddressReady
  //----------------------------------------------------------------------
  
  /**
   * @return True if data are ready, false otherwise
   * @brief Check if the data are ready
   */
  public boolean isDataReady()
  {
    return this.dataReady.equals("READY");
  }// end of isDataReady
  //----------------------------------------------------------------------
  
  /**
   * @return Load item model
   * @brief Get load item
   */
  public LoadBufferItem getItem()
  {
    return item;
  }// end of getItem
  //----------------------------------------------------------------------
  
  /**
   * @return String state of the bypassed flag
   * @brief Get string state of the bypassed flag
   */
  public String getBypassed()
  {
    return bypassed;
  }// end of getBypassed
  //----------------------------------------------------------------------
}
