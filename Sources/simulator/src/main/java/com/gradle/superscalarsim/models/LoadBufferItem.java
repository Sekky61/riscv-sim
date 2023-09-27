/**
 * @file    LoadBufferItem.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @brief File contains container class additional info for Load instruction
 *
 * @date  14 March   2021 12:00 (created) \n
 *        28 April   2021 18:00 (revised)
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
 * @class LoadBufferItem
 * @brief Container for all the additional info required for instructions inside of load buffer
 */
public class LoadBufferItem
{
  /// Name of the destination register (result)
  private final String destinationRegister;
  /// Is the register ready for load instruction
  private boolean destinationReady;
  /// Calculated load address
  private long address;
  /// Is instruction accessing memory
  private boolean isAccessingMemory;
  private int accessingMemoryId;
  /// Id of the MA block in which was memory access done
  private int memoryAccessId;
  /// Has instruction bypassed MA?
  private boolean hasBypassed;
  /// Id when the instruction failed
  private int memoryFailedId;

  /**
   * @brief Constructor
   * @param [in] destinationRegister - Register name where the result will be stored
   */
  public LoadBufferItem(String destinationRegister)
  {
    this.destinationRegister = destinationRegister;
    this.destinationReady    = false;
    this.address             = -1;
    this.isAccessingMemory   = false;
    this.memoryAccessId      = -1;
    this.hasBypassed         = false;
    this.memoryFailedId      = -1;
    this.accessingMemoryId   = -1;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get destination register name
   * @return Name of the destination register
   */
  public String getDestinationRegister()
  {
    return destinationRegister;
  }// end of getDestinationRegister
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Is destination register ready to be saved into?
   * @return True if yes, false if no
   */
  public boolean isDestinationReady()
  {
    return destinationReady;
  }// end of isDestinationReady
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set if destination register is ready
   * @param [in] destinationReady - boolean value marking readiness of that register
   */
  public void setDestinationReady(boolean destinationReady)
  {
    this.destinationReady = destinationReady;
  }// end of setDestinationReady
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get load address
   * @return Address from where load data
   */
  public long getAddress()
  {
    return address;
  }// end of getAddress
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set load address
   * @param [in] address - Address from where load data
   */
  public void setAddress(long address)
  {
    this.address = address;
  }// end of setAddress
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set flag if instruction is accessing memory (MA block)
   * @param [in] isAccessingMemory - Flag marking memory access
   */
  public void setAccessingMemory(boolean isAccessingMemory)
  {
    this.isAccessingMemory = isAccessingMemory;
  }// end of setAccessingMemory
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Is instruction in the MA block
   * @return True if yes, false if no
   */
  public boolean isAccessingMemory()
  {
    return isAccessingMemory;
  }// end of isAccessingMemory
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get MA id when was instruction released
   * @return MA release id
   */
  public int getMemoryAccessId()
  {
    return memoryAccessId;
  }// end of getMemoryAccessId
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set MA id when was instruction released
   * @param [in] memoryAccessId - MA id when was instruction released
   */
  public void setMemoryAccessId(int memoryAccessId)
  {
    this.memoryAccessId = memoryAccessId;
  }// end of setMemoryAccessId
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Has instruction bypassed MA?
   * @return True if yes, false if no
   */
  public boolean hasBypassed()
  {
    return hasBypassed;
  }// end of hasBypassed
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set flag if instruction bypassed MA
   * @param [in] hasBypassed - flag if instruction bypassed MA
   */
  public void setHasBypassed(boolean hasBypassed)
  {
    this.hasBypassed = hasBypassed;
  }// end of setHasBypassed
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get id when instruction has failed
   * @return Id when instruction has failed
   */
  public int getMemoryFailedId()
  {
    return memoryFailedId;
  }// end of getMemoryFailedId
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set id when instruction has failed
   * @param [in] memoryFailedId - Id when instruction has failed
   */
  public void setMemoryFailedId(int memoryFailedId)
  {
    this.memoryFailedId = memoryFailedId;
  }// end of setMemoryFailedId
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get id when the load instruction got into MA from load buffer
   * @return Id of the time, when instruction got into MA
   */
  public int getAccessingMemoryId()
  {
    return accessingMemoryId;
  }// end of getAccessingMemoryId
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set id when the load instruction got into MA from load buffer
   * @param [in] accessingMemoryId - New id when the load instruction got into MA from load buffer
   */
  public void setAccessingMemoryId(int accessingMemoryId)
  {
    this.accessingMemoryId = accessingMemoryId;
  }// end of setAccessingMemoryId
  //-------------------------------------------------------------------------------------------
}
