/**
 * @file SimulatedMemory.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class simulating memory
 * @date 17 December  2020 10:00 (created) \n
 * 17 May       2021 17:55 (revised)
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
package com.gradle.superscalarsim.code;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.copyOf;

/**
 * Memory representation: continuous array of bytes
 * TODO Custom serialization to avoid transmitting the whole memory.
 * TODO: some protection against resource exhaustion.
 *
 * @class SimulatedMemory
 * @brief Class simulating memory with read/write capabilities
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class SimulatedMemory implements AbstractBlock
{
  /**
   * Main memory. Grows as needed.
   * Gets serialized to a base64 string.
   */
  @JsonProperty("memoryBase64")
  private byte[] memory;
  
  /**
   * Delay of store access to main memory in clocks.
   */
  private int storeLatency;
  
  /**
   * Delay of load access to main memory in clocks.
   */
  private int loadLatency;
  
  /**
   * List of parallel operations in progress or recently finished.
   */
  private Map<Integer, MemoryTransaction> operations;
  
  /**
   * @brief Constructor
   */
  public SimulatedMemory(int storeLatency, int loadLatency)
  {
    this.storeLatency = storeLatency;
    this.loadLatency  = loadLatency;
    this.memory       = new byte[0];
    this.operations   = new HashMap<>();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param address Address to write to
   * @param value   Value to write
   *
   * @brief Insert a chunk of data into memory
   */
  public void insertIntoMemory(long address, byte[] data)
  {
    if (this.memory.length < address + data.length)
    {
      resizeArray((int) (address + data.length));
    }
    System.arraycopy(data, 0, this.memory, (int) address, data.length);
  }// end of insertIntoMemory
  
  /**
   * @brief Resize array to be at least of specified size
   */
  private void resizeArray(int size)
  {
    this.memory = copyOf(this.memory, size);
  }
  
  /**
   * @param address Hashmap key, pointing into specific place in memory
   *
   * @return Value from hashmap pointed by key
   * @brief Get value from memory
   */
  public byte getFromMemory(long address)
  {
    if (!this.isInMemory(address))
    {
      resizeArray((int) address + 1);
    }
    return this.memory[(int) address];
  }// end of getFromMemory
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param address Hashmap key, pointing into specific place in memory
   *
   * @return True if key has been set in the past, false otherwise
   * @brief Check if some memory space is filled with data
   */
  public boolean isInMemory(long address)
  {
    return address < this.memory.length;
  }// end of isInMemory
  
  /**
   * @param address offset in memory
   * @param size    size of data to get in bytes
   *
   * @return byte array of data from memory
   */
  public byte[] getFromMemory(long address, int size)
  {
    if (!this.isInMemory(address + size))
    {
      resizeArray((int) address + size);
    }
    byte[] returnVal = new byte[size];
    System.arraycopy(this.memory, (int) address, returnVal, 0, size);
    return returnVal;
  }// end of getFromMemory
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Simulate finished memory accesses
   */
  @Override
  public void simulate()
  {
    for (MemoryTransaction transaction : this.operations.values())
    {
      transaction.address();
    }
  }
  
  /**
   * @brief Resets memory to its initial state
   */
  public void reset()
  {
    this.memory = new byte[0];
  }// end of reset
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return Size of the memory in bytes
   */
  @JsonProperty
  public int getSize()
  {
    return this.memory.length;
  }
}
