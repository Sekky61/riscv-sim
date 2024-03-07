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
import com.gradle.superscalarsim.blocks.loadstore.SimulatedMemory;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;

/**
 * @class MemoryModel
 * @brief Class implementing common functions for accessing cache or memory, holds the cache or memory.
 * Uses cache if it is present, otherwise uses memory.
 * TODO move elsewhere
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
  /**
   * Statistics of simulation
   */
  private SimulationStatistics statistics;
  
  /**
   * @brief Constructor
   * Provide both memory and cache, only one will be used.
   */
  public MemoryModel(Cache cache, SimulatedMemory simulatedMemory, SimulationStatistics statistics)
  {
    this.memory     = simulatedMemory;
    this.cache      = cache;
    this.statistics = statistics;
  }
  
  /**
   * @param tr Memory transaction to schedule
   *
   * @return Number of cycles until the transaction is finished
   * @brief Schedule a memory access. It will be finished after the specified number of cycles, after which the requester must take the result.
   */
  public int execute(MemoryTransaction tr)
  {
    if (cache != null)
    {
      // Use cache
      return cache.scheduleTransaction(tr);
    }
    else
    {
      // Use memory
      return memory.scheduleTransaction(tr);
    }
  }
  
  /**
   * @param id ID of the transaction to finish and take the result of
   *
   * @return Finished transaction
   * @brief Call at the clock cycle when the transaction is finished, not before or after
   */
  public MemoryTransaction finishTransaction(int id)
  {
    if (cache != null)
    {
      // Use cache
      return cache.finishTransaction(id);
    }
    else
    {
      // Use memory
      return memory.finishTransaction(id);
    }
  }
  
  /**
   * A debug function.
   *
   * @brief Get the data at address, regardless if it is in cache or memory
   */
  public byte[] getData(long address, int size)
  {
    if (cache != null)
    {
      // Use cache
      try
      {
        long   res  = cache.getData(address, size);
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++)
        {
          data[i] = (byte) (res & 0xFF);
          res >>= 8;
        }
        return data;
      }
      catch (Exception e)
      {
        // Ignore, try memory
      }
    }
    
    // Use memory
    return memory.getFromMemory(address, size);
  }
}
