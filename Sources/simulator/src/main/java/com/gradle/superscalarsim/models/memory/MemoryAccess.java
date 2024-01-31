/**
 * @file MemoryAccess.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief Class desctibing a memory access
 * @date 26 Sep      2023 10:00 (created)
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

package com.gradle.superscalarsim.models.memory;

/**
 * Generated at each memory access (load/store).
 *
 * @class MemoryAccess
 * @brief Class describing a memory access
 */
public class MemoryAccess
{
  /**
   * True if store, false if load
   */
  private boolean isStore;
  
  /**
   * Data to be stored or data loaded
   */
  private long data;
  
  /**
   * Size of the data in bytes (1-8)
   */
  private int size;
  /**
   * True if the data is signed
   */
  private boolean isSigned;
  /**
   * Address of the memory access
   */
  private long address;
  
  /**
   * Constructor
   *
   * @param isStore  True if store, false if load
   * @param address  Address of the memory access
   * @param data     Data to be stored
   * @param size     Size of the data in bytes (1-8)
   * @param isSigned True if the data is signed
   */
  public MemoryAccess(boolean isStore, long address, long data, int size, boolean isSigned)
  {
    this.isStore  = isStore;
    this.address  = address;
    this.data     = data;
    this.size     = size;
    this.isSigned = isSigned;
  }
  
  /**
   * Constructor for store access
   *
   * @param address  Address of the memory access
   * @param data     Data to be stored
   * @param size     Size of the data in bytes (1-8)
   * @param isSigned True if the data is signed
   */
  public static MemoryAccess store(long address, int size, long data, boolean isSigned)
  {
    return new MemoryAccess(true, address, data, size, isSigned);
  }
  
  /**
   * Constructor for load access
   *
   * @param address  Address of the memory access
   * @param size     Size of the data in bytes (1-8)
   * @param isSigned True if the data is signed
   */
  public static MemoryAccess load(long address, int size, boolean isSigned)
  {
    return new MemoryAccess(false, address, 0, size, isSigned);
  }
  
  public boolean isSigned()
  {
    return isSigned;
  }
  
  public boolean isStore()
  {
    return isStore;
  }
  
  public long getData()
  {
    return data;
  }
  
  public void setData(long data)
  {
    this.data = data;
  }
  
  public int getSize()
  {
    return size;
  }
  
  public void setSize(int size)
  {
    this.size = size;
  }
  
  public void setLoadStore(boolean isStore)
  {
    this.isStore = isStore;
  }
  
  public long getAddress()
  {
    return address;
  }
  
  public void setAddress(long address)
  {
    this.address = address;
  }
}
