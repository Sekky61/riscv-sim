/**
 * @file    StoreBufferItem.java
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
 * @brief File contains container class additional info for Store instruction
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
 * @class StoreBufferItem
 * @brief Container for all the additional info required for instructions inside of store buffer
 */
public class StoreBufferItem
{
  /// Name of the source register from where store takes the result
  private final String sourceRegister;
  /// Id used when getting correct store for bypassing
  private final int sourceResultId;
  /// Is the register ready for store instruction
  private boolean sourceReady;
  /// Store result address
  private long address;

  /// Is instruction accessing memory
  private boolean isAccessingMemory;
  private int accessingMemoryId;
  /// Id of the MA block in which was memory access done
  private int memoryAccessId;
  /// Id when the instruction failed
  private int memoryFailedId;
  /**
   * @brief Constructor
   * @param [in] sourceRegister - Name of the source register from where store takes the result
   * @param [in] sourceId       - Id used when getting correct store for bypassing
   */
  public StoreBufferItem(String sourceRegister, int sourceId)
  {
    this.sourceRegister = sourceRegister;
    this.sourceReady    = false;
    this.address        = -1;
    this.sourceResultId = sourceId;

    this.isAccessingMemory   = false;
    this.memoryAccessId      = -1;
    this.memoryFailedId      = -1;
    this.accessingMemoryId   = -1;
  }// end of Constructor
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

  /**
   * @brief Get name of the source register
   * @return Name of the source register
   */
  public String getSourceRegister()
  {
    return sourceRegister;
  }// end of getSourceRegister
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Is source register ready to be read from?
   * @return True if yes, false if no
   */
  public boolean isSourceReady()
  {
    return sourceReady;
  }// end of isSourceReady
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Sets flag if source register is ready for reading from
   * @param [in] destinationReady - Flag if source register is ready for reading from
   */
  public void setSourceReady(boolean destinationReady)
  {
    this.sourceReady = destinationReady;
  }// end of setSourceReady
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get store address
   * @return Store address
   */
  public long getAddress()
  {
    return address;
  }// end of getAddress
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Set store address
   * @param [in] address - Address to store result into
   */
  public void setAddress(long address)
  {
    this.address = address;
  }// end of setAddress
  //-------------------------------------------------------------------------------------------

  /**
   * @brief Get id used when getting correct store for bypassing
   * @return Id used when getting correct store for bypassing
   */
  public int getSourceResultId()
  {
    return sourceResultId;
  }// end of getSourceResultId
  //-------------------------------------------------------------------------------------------

}
