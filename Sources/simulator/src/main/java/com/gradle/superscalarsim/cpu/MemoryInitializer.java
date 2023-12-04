/**
 * @file MemoryInitializer.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief The complete state of the CPU. Serializable for saving/loading.
 * @date 26 Nov      2023 15:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
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


package com.gradle.superscalarsim.cpu;

import com.gradle.superscalarsim.code.Label;
import com.gradle.superscalarsim.code.SimulatedMemory;

import java.util.List;
import java.util.Map;

/**
 * @class MemoryInitializer
 * @brief Initializes memory with data from MemoryLocation objects
 */
public class MemoryInitializer
{
  /**
   * Amount of memory to leave free at the start of the memory
   */
  public int freeMemoryStart;
  
  /**
   * Amount of memory to reserve for the stack
   */
  public int stackSize;
  
  /**
   * State of the memory allocator
   * Pointer to the next free memory location
   */
  private long memoryPtr;
  
  /**
   * Constructor
   */
  public MemoryInitializer(int freeMemoryStart, int stackSize)
  {
    this.freeMemoryStart = freeMemoryStart;
    this.stackSize       = stackSize;
    this.memoryPtr       = freeMemoryStart + stackSize;
  }// end of Constructor
  
  /**
   * Can be called multiple times
   *
   * @param memory    Memory to initialize
   * @param locations Locations to initialize memory with
   *
   * @brief Initializes memory with data from MemoryLocation objects
   */
  public void initializeMemory(SimulatedMemory memory, List<MemoryLocation> locations, Map<String, Label> labels)
  {
    for (MemoryLocation memoryLocation : locations)
    {
      // Align it (ceil to the next multiple of alignment)
      if (memoryLocation.alignment > 1)
      {
        // 2^alignment
        int alignment = 1 << memoryLocation.alignment;
        memoryPtr = (memoryPtr + alignment - 1) / alignment * alignment;
      }
      byte[] data = new byte[memoryLocation.getBytes().size()];
      for (int i = 0; i < memoryLocation.getBytes().size(); i++)
      {
        data[i] = memoryLocation.getBytes().get(i);
      }
      memory.insertIntoMemory(memoryPtr, data);
      // Save label address
      if (labels.containsKey(memoryLocation.name))
      {
        labels.get(memoryLocation.name).address = (int) memoryPtr;
      }
      else
      {
        throw new RuntimeException("Label " + memoryLocation.name + " not found");
      }
      memoryPtr += memoryLocation.getByteSize();
    }
  }
  
  /**
   * @return pointer to the end of the stack
   */
  public long getStackPointer()
  {
    return freeMemoryStart + stackSize;
  }
}
