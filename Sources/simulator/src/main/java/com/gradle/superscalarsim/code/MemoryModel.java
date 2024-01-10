/**
 * @file MemoryModel.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container class for cache line
 * @date 06 April 2023 14:25 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Jakub Horky
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
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.loadstore.Cache;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.models.MemoryAccess;
import com.gradle.superscalarsim.models.Pair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @class MemoryModel
 * @brief Class implementing common functions for accessing cache or memory, holds the cache or memory
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class MemoryModel
{
  /**
   * Cache implementation
   */
  @JsonIdentityReference(alwaysAsId = true)
  Cache cache;
  /**
   * Memory - only when there is no cache - cache holds its own memory
   */
  @JsonIdentityReference(alwaysAsId = true)
  SimulatedMemory memory;
  private SimulationStatistics statistics;
  /**
   * Last access
   */
  private MemoryAccess lastAccess;
  
  /**
   * @brief Constructor - Memory model holds only memory
   */
  public MemoryModel(SimulatedMemory simulatedMemory)
  {
    this.memory = simulatedMemory;
    this.cache  = null;
  }
  
  /**
   * @brief Constructor - Memory model holds cache
   */
  public MemoryModel(Cache cache, SimulationStatistics statistics)
  {
    this.memory     = null;
    this.cache      = cache;
    this.statistics = statistics;
  }
  
  /**
   * @param address      starting byte of the access (can be misaligned)
   * @param data         data to be stored
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which this access is happening
   *
   * @return delay caused by this access
   * @brief Sets data to memory
   */
  public int store(long address, long data, int size, int id, int currentCycle)
  {
    this.lastAccess = new MemoryAccess(true, address, data, size);
    if (cache != null)
    {
      int delay = cache.storeData(address, data, size, id, currentCycle);
      statistics.cache.incrementTotalDelay(currentCycle, delay);
      return delay;
    }
    
    // Store without cache
    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.putLong(data);
    byte[] bytes = byteBuffer.array();
    memory.insertIntoMemory(address, bytes);
    return 0;
  }
  
  /**
   * @param address      starting byte of the access (can be misaligned)
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which the access happened
   *
   * @return Pair of delay of this access and data
   * @brief Gets data from memory
   */
  public Pair<Integer, Long> load(long address, int size, int id, int currentCycle)
  {
    byte[] bytes = new byte[8];
    int    delay = 0;
    if (cache != null)
    {
      // Use cache
      Pair<Integer, byte[]> returnVal = cache.getDataBytes(address, size, id, currentCycle);
      delay = returnVal.getFirst();
      System.arraycopy(returnVal.getSecond(), 0, bytes, 0, returnVal.getSecond().length);
      statistics.cache.incrementTotalDelay(currentCycle, returnVal.getFirst());
    }
    else
    {
      // Use memory
      byte[] readBytes = memory.getFromMemory(address, size);
      System.arraycopy(readBytes, 0, bytes, 0, readBytes.length);
    }
    long returnValLong = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    this.lastAccess = new MemoryAccess(false, address, returnValLong, size);
    return new Pair<>(delay, returnValLong);
  }
  
  /**
   * @brief Reset the state of underlying memory
   */
  public void reset()
  {
    if (cache != null)
    {
      cache.reset();
    }
    else
    {
      memory.reset();
    }
  }
  
  public Cache getCache()
  {
    return cache;
  }
  
  public void setCache(Cache cache)
  {
    this.cache = cache;
  }
}
