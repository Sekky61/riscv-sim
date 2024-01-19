/**
 * @file MemoryTransaction.java
 * @author Michal Majer \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xmajer21@fit.vutbr.cz
 * @brief Class for memory transaction
 * @date 19 Jan      2024 10:00 (created)
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

import java.util.Objects;

/**
 * @class MemoryTransaction
 * @brief Data class describing a memory transaction
 */
public final class MemoryTransaction
{
  private final int id;
  private final int timestamp;
  private final long address;
  private final byte[] data;
  private final int size;
  private final boolean isStore;
  private final boolean isSigned;
  private boolean isFinished = false;
  
  /**
   * Constructor
   */
  public MemoryTransaction(int id,
                           int timestamp,
                           long address,
                           byte[] data,
                           int size,
                           boolean isStore,
                           boolean isSigned)
  {
    if (size < 1 || size > 64)
    {
      throw new IllegalArgumentException("Size must be between 1 and 8");
    }
    this.id        = id;
    this.timestamp = timestamp;
    this.address   = address;
    this.data      = data;
    this.size      = size;
    this.isStore   = isStore;
    this.isSigned  = isSigned;
  }
  
  public int timestamp()
  {
    return timestamp;
  }
  
  public long address()
  {
    return address;
  }
  
  public byte[] data()
  {
    return data;
  }
  
  public int size()
  {
    return size;
  }
  
  public boolean isStore()
  {
    return isStore;
  }
  
  public boolean isSigned()
  {
    return isSigned;
  }
  
  public boolean isFinished()
  {
    return isFinished;
  }
  
  /**
   * @brief Marks the transaction as finished
   */
  public void finish()
  {
    this.isFinished = true;
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass())
    {
      return false;
    }
    var that = (MemoryTransaction) obj;
    return this.timestamp == that.timestamp && this.address == that.address && Objects.equals(this.data,
                                                                                              that.data) && this.size == that.size && this.isStore == that.isStore && this.isSigned == that.isSigned;
  }
  
  @Override
  public int hashCode()
  {
    return Objects.hash(timestamp, address, data, size, isStore, isSigned);
  }
  
  @Override
  public String toString()
  {
    return "MemoryTransaction[" + "timestamp=" + timestamp + ", " + "address=" + address + ", " + "data=" + data + ", " + "size=" + size + ", " + "isStore=" + isStore + ", " + "isSigned=" + isSigned + ']';
  }
  
}
