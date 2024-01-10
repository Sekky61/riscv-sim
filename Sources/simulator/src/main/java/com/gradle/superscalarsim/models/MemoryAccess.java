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

package com.gradle.superscalarsim.models;

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
   * Address of the memory access
   */
  private long address;
  
  public MemoryAccess(boolean isStore, long address, long data, int size)
  {
    this.isStore = isStore;
    this.address = address;
    this.data    = data;
    this.size    = size;
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
