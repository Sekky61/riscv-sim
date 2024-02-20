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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static java.util.Arrays.copyOf;

/**
 * @class MemoryTransaction
 * @brief Data class describing a memory transaction
 */
public final class MemoryTransaction
{
  private final int mmuId;
  /**
   * ID (index) of the instruction in code. aka. getCodeId() aka. ID of InputCodeModel
   */
  private final int instructionId;
  private final int timestamp;
  private final long address;
  private final int size;
  private final boolean isStore;
  private final boolean isSigned;
  private int id;
  /**
   * Data to be written to memory or the result of a read.
   * Mutated by memory at transaction completion.
   */
  private byte[] data;
  private boolean isFinished = false;
  private int latency;
  
  public boolean isCancelled()
  {
    return cancelled;
  }
  
  /**
   * A cancelled transaction does not need to be finished.
   * A transaction gets cancelled when the owner of the transaction (instruction) is flushed.
   */
  private boolean cancelled;
  
  /**
   * Copy constructor
   *
   * @param transaction Transaction to be copied
   */
  public MemoryTransaction(MemoryTransaction transaction)
  {
    this.id            = transaction.id;
    this.mmuId         = transaction.mmuId;
    this.instructionId = transaction.instructionId;
    this.timestamp     = transaction.timestamp;
    this.address       = transaction.address;
    this.data          = copyOf(transaction.data, transaction.data.length);
    this.size          = transaction.size;
    this.isStore       = transaction.isStore;
    this.isSigned      = transaction.isSigned;
    this.latency       = transaction.latency;
    this.isFinished    = transaction.isFinished;
    this.isHit         = transaction.isHit;
  }
  
  public boolean isHit()
  {
    return isHit;
  }
  
  public void setHit(boolean hit)
  {
    isHit = hit;
  }
  
  private boolean isHit;
  
  /**
   * Constructor
   *
   * @param instructionId index of instruction in code
   */
  public MemoryTransaction(int id,
                           int mmuId,
                           int instructionId,
                           int timestamp,
                           long address,
                           byte[] data,
                           int size,
                           boolean isStore,
                           boolean isSigned)
  {
    if (size < 1 || size > 64)
    {
      throw new IllegalArgumentException("Size of a memory transaction must be between 1 and 64 bytes.");
    }
    this.id            = id;
    this.mmuId         = mmuId;
    this.instructionId = instructionId;
    this.timestamp     = timestamp;
    this.address       = address;
    this.data          = data;
    this.size          = size;
    this.isStore       = isStore;
    this.isSigned      = isSigned;
    latency            = 0;
    isHit              = false;
  }
  
  /**
   * Constructor for store access. Testing method with invalid ids.
   *
   * @param address Address of the memory access
   * @param data    Data to be stored
   */
  public static MemoryTransaction store(long address, byte[] data)
  {
    return store(address, data, 0);
  }
  
  /**
   * Constructor for store access. Testing method with invalid ids.
   *
   * @param address   Address of the memory access
   * @param data      Data to be stored
   * @param timestamp Timestamp of the memory access
   */
  public static MemoryTransaction store(long address, byte[] data, int timestamp)
  {
    return new MemoryTransaction(-1, -1, -1, timestamp, address, data, data.length, true, false);
  }
  
  /**
   * Constructor for load access. Testing method with invalid ids.
   *
   * @param address Address of the memory access
   * @param size    Size of the data in bytes (1-8)
   */
  public static MemoryTransaction load(long address, int size)
  {
    return load(address, size, 0);
  }
  
  /**
   * Constructor for load access. Testing method with invalid ids.
   *
   * @param address  Address of the memory access
   * @param size     Size of the data in bytes (1-8)
   * @param isSigned True if the data is signed
   */
  public static MemoryTransaction load(long address, int size, int timestamp)
  {
    return new MemoryTransaction(-1, -1, -1, timestamp, address, null, size, false, false);
  }
  
  public int getInstructionId()
  {
    return instructionId;
  }
  
  public int latency()
  {
    return latency;
  }
  
  /**
   * @brief Once latency is known, it is saved here
   */
  public void setLatency(int latency)
  {
    this.latency = latency;
  }
  
  /**
   * ID is set by the memory model
   */
  public void setId(int id)
  {
    this.id = id;
  }
  
  public int id()
  {
    return id;
  }
  
  public void setData(byte[] data)
  {
    this.data = data;
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
  
  public long dataAsLong()
  {
    byte[] expandedData;
    if (data.length == 8)
    {
      expandedData = data;
    }
    else
    {
      // expand data to 8 bytes
      expandedData = new byte[8];
      System.arraycopy(data, 0, expandedData, 0, data.length);
    }
    long returnValLong = ByteBuffer.wrap(expandedData).order(ByteOrder.LITTLE_ENDIAN).getLong();
    
    // Sign extend
    if (isSigned)
    {
      int  sizeBits  = size * 8;
      long validMask = (1L << sizeBits) - 1;
      if (sizeBits >= 64)
      {
        validMask = -1;
      }
      returnValLong = returnValLong & validMask;
      if (((1L << (sizeBits - 1)) & returnValLong) != 0)
      {
        // Fill with sign bit
        long signMask = ~validMask;
        returnValLong = returnValLong | signMask;
      }
    }
    
    return returnValLong;
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
  public int hashCode()
  {
    return Objects.hash(timestamp, address, data, size, isStore, isSigned);
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
  public String toString()
  {
    return "MemoryTransaction[" + "timestamp=" + timestamp + ", " + "address=" + address + ", " + "data=" + data + ", " + "size=" + size + ", " + "isStore=" + isStore + ", " + "isSigned=" + isSigned + ']';
  }
  
  public void setCanceled()
  {
    this.cancelled = true;
  }
}
