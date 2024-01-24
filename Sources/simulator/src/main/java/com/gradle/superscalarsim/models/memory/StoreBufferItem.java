/**
 * @file StoreBufferItem.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container class additional info for Store instruction
 * @date 14 March   2021 12:00 (created) \n
 * 17 March   2021 20:00 (revised)
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
package com.gradle.superscalarsim.models.memory;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

/**
 * @class StoreBufferItem
 * @brief Container for all the additional info required for instructions inside of store buffer
 */
public class StoreBufferItem
{
  /**
   * Name of the source register from where store takes the result
   * TODO: change to a reference to the register
   */
  private final String sourceRegister;
  /**
   * ID used when getting correct store for bypassing
   */
  private final int sourceResultId;
  /**
   * The instruction itself
   */
  @JsonIdentityReference(alwaysAsId = true)
  SimCodeModel simCodeModel;
  /**
   * Is the register ready for store instruction
   */
  private boolean sourceReady;
  /**
   * Store result address
   */
  private long address;
  
  /**
   * Is instruction accessing memory
   */
  private boolean isAccessingMemory;
  private int accessingMemoryId;
  /**
   * ID of the MA block in which was memory access done
   */
  private int memoryAccessId;
  /**
   * ID when the instruction failed
   */
  private int memoryFailedId;
  
  /**
   * @param sourceRegister Name of the source register from where store takes the result
   * @param sourceId       ID used when getting correct store for bypassing
   *
   * @brief Constructor
   */
  public StoreBufferItem(SimCodeModel simCodeModel, String sourceRegister, int sourceId)
  {
    this.simCodeModel   = simCodeModel;
    this.sourceRegister = sourceRegister;
    this.sourceReady    = false;
    this.address        = -1;
    this.sourceResultId = sourceId;
    
    this.isAccessingMemory = false;
    this.memoryAccessId    = -1;
    this.memoryFailedId    = -1;
    this.accessingMemoryId = -1;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return True if yes, false if no
   * @brief Is instruction in the MA block
   */
  public boolean isAccessingMemory()
  {
    return isAccessingMemory;
  }// end of isAccessingMemory
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param isAccessingMemory Flag marking memory access
   *
   * @brief Set flag if instruction is accessing memory (MA block)
   */
  public void setAccessingMemory(boolean isAccessingMemory)
  {
    this.isAccessingMemory = isAccessingMemory;
  }// end of setAccessingMemory
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return MA release id
   * @brief Get MA id when was instruction released
   */
  public int getMemoryAccessId()
  {
    return memoryAccessId;
  }// end of getMemoryAccessId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param memoryAccessId MA id when was instruction released
   *
   * @brief Set MA id when was instruction released
   */
  public void setMemoryAccessId(int memoryAccessId)
  {
    this.memoryAccessId = memoryAccessId;
  }// end of setMemoryAccessId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return ID when instruction has failed
   * @brief Get id when instruction has failed
   */
  public int getMemoryFailedId()
  {
    return memoryFailedId;
  }// end of getMemoryFailedId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param memoryFailedId ID when instruction has failed
   *
   * @brief Set id when instruction has failed
   */
  public void setMemoryFailedId(int memoryFailedId)
  {
    this.memoryFailedId = memoryFailedId;
  }// end of setMemoryFailedId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return ID of the time, when instruction got into MA
   * @brief Get id when the load instruction got into MA from load buffer
   */
  public int getAccessingMemoryId()
  {
    return accessingMemoryId;
  }// end of getAccessingMemoryId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param accessingMemoryId New id when the load instruction got into MA from load buffer
   *
   * @brief Set id when the load instruction got into MA from load buffer
   */
  public void setAccessingMemoryId(int accessingMemoryId)
  {
    this.accessingMemoryId = accessingMemoryId;
  }// end of setAccessingMemoryId
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Name of the source register
   * @brief Get name of the source register
   */
  public String getSourceRegister()
  {
    return sourceRegister;
  }// end of getSourceRegister
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return True if yes, false if no
   * @brief Is source register ready to be read from?
   */
  public boolean isSourceReady()
  {
    return sourceReady;
  }// end of isSourceReady
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param destinationReady Flag if source register is ready for reading from
   *
   * @brief Sets flag if source register is ready for reading from
   */
  public void setSourceReady(boolean destinationReady)
  {
    this.sourceReady = destinationReady;
  }// end of setSourceReady
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Store address
   * @brief Get store address
   */
  public long getAddress()
  {
    return address;
  }// end of getAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param address Address to store result into
   *
   * @brief Set store address
   */
  public void setAddress(long address)
  {
    this.address = address;
  }// end of setAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return ID used when getting correct store for bypassing
   * @brief Get id used when getting correct store for bypassing
   */
  public int getSourceResultId()
  {
    return sourceResultId;
  }// end of getSourceResultId
  
  /**
   * @return Instruction itself
   */
  public SimCodeModel getSimCodeModel()
  {
    return simCodeModel;
  }
  //-------------------------------------------------------------------------------------------
  
}
