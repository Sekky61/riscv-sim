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

import com.gradle.superscalarsim.blocks.loadstore.SimulatedMemory;
import com.gradle.superscalarsim.code.Symbol;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;

import java.util.ArrayList;
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
   * Label name to name + address object mapping
   */
  Map<String, Symbol> dataLabels;
  /**
   * Locations of memory data
   */
  List<MemoryLocation> locations;
  
  /**
   * Constructor
   */
  public MemoryInitializer(int freeMemoryStart, int stackSize)
  {
    this.freeMemoryStart = freeMemoryStart;
    this.stackSize       = stackSize;
    this.memoryPtr       = freeMemoryStart + stackSize;
    this.dataLabels      = null; // this warns about not loading labels from the code parser
    this.locations       = new ArrayList<>();
  }// end of Constructor
  
  /**
   * Assign address to a location based on already allocated memory and data size
   *
   * @param location Location to assign address to (mutates the object)
   */
  public void addLocation(MemoryLocation location)
  {
    // Align it (ceil to the next multiple of alignment)
    if (location.alignment > 1)
    {
      // 2^alignment
      int alignment = 1 << location.alignment;
      memoryPtr = (memoryPtr + alignment - 1) / alignment * alignment;
    }
    
    Symbol label = dataLabels.get(location.getName());
    label.setValue(RegisterDataContainer.fromValue(memoryPtr));
    
    memoryPtr += location.getByteSize();
    
    locations.add(location);
  }
  
  public void addLocations(List<MemoryLocation> locations)
  {
    for (MemoryLocation location : locations)
    {
      addLocation(location);
    }
  }
  
  /**
   * Supply labels from the code parser. Changing these will change the instruction argument values.
   */
  public void setLabels(Map<String, Symbol> labels)
  {
    dataLabels = labels;
  }
  
  /**
   * Can be called multiple times
   *
   * @param memory Memory to initialize
   *
   * @brief Initializes memory with the locations registered before this call
   */
  public void initializeMemory(SimulatedMemory memory)
  {
    // Second step - fill the memory values
    for (MemoryLocation memoryLocation : locations)
    {
      // Replace labels with addresses
      for (int i = 0; i < memoryLocation.data.size(); i++)
      {
        String value = memoryLocation.data.get(i);
        if (dataLabels.containsKey(value))
        {
          // This solves the labels linking to other labels
          // Save label address
          memoryLocation.data.set(i, String.valueOf(dataLabels.get(value).getAddress()));
        }
      }
      
      // It is now safe to convert the data to bytes
      
      Symbol label   = dataLabels.get(memoryLocation.getName());
      long   address = label.getAddress();
      byte[] data    = memoryLocation.getBytes();
      // Insert data into memory
      memory.insertIntoMemory(address, data);
    }
  }
  
  /**
   * @return pointer to the end of the stack
   */
  public long getStackPointer()
  {
    return freeMemoryStart + stackSize;
  }
  
  /**
   * @return a pointer. After jumping on this pointer, the program will halt.
   * TODO look into this. Isn't this supposed to be another value?
   */
  public long getExitPointer()
  {
    return stackSize;
  }
}
