/**
 * @file CacheLineModel.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains container class for cache line
 * @date 04 April 2023 14:00 (created)
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
package com.gradle.superscalarsim.models.cache;

/**
 * @class CacheLineModel
 * @brief Container class for cache line
 */
public class CacheLineModel
{
  /**
   * True if this line contains valid data
   */
  boolean valid;
  
  /**
   * True if this line holds modified data
   */
  boolean dirty;
  
  /**
   * Top bits of the address to identify the line
   */
  long tag;
  
  /**
   * Data stored in the line
   */
  byte[] line;
  
  /**
   * Size of the line in bytes
   */
  int lineSize;
  
  /**
   * Index of the index line inside cache.
   * Multiple lines can have same index.
   */
  int index;
  
  /**
   * Address of the first byte
   */
  long baseAddress;
  
  /**
   * @param lineSize size of the line in bytes - must be multiple of 4
   * @param index    index of the memory line
   *
   * @brief Constructor
   */
  public CacheLineModel(int lineSize, int index)
  {
    assert lineSize % 4 == 0;
    
    this.valid       = false;
    this.dirty       = false;
    this.tag         = 0;
    this.line        = new byte[lineSize];
    this.lineSize    = lineSize;
    this.index       = index;
    this.baseAddress = 0;
  }
  
  /**
   * @param index Index inside the line
   * @param size  Size of requested data - 1,2,4
   * @param data  Data to be stored
   *
   * @brief Sets data on the index
   */
  public void setData(int index, int size, int data)
  {
    for (int i = 0; i < size; i++)
    {
      line[index + i] = (byte) ((data >>> (i * 8)) & 0xFF);
    }
  }
  
  public void setData(int index, byte[] data)
  {
    System.arraycopy(data, 0, line, index, data.length);
  }
  
  /**
   * @param index Index inside the line
   * @param size  Size of requested data - 1,2,4
   *
   * @return Data converted to int
   * @brief Get data on the index
   */
  public int getData(int index, int size)
  {
    int data = 0;
    for (int i = 0; i < size; i++)
    {
      data = data | ((line[index + i] & 0xFF) << (i * 8));
    }
    return data;
  }
  
  public byte[] getDataBytes(int index, int size)
  {
    byte[] data = new byte[size];
    System.arraycopy(line, index, data, 0, size);
    return data;
  }
  
  /**
   * @return boolean - valid
   * @brief get if the line is valid
   */
  public boolean isValid()
  {
    return valid;
  }
  
  /**
   * @return boolean - dirty
   * @brief get if the line is dirty
   */
  public boolean isDirty()
  {
    return dirty;
  }
  
  /**
   * @return long - tag
   * @brief get the tag of the line
   */
  public long getTag()
  {
    return tag;
  }
  
  /**
   * @param tag - Tag of the line
   *
   * @brief Sets Tag of the data stored in this line
   */
  public void setTag(long tag)
  {
    this.tag = tag;
  }
  
  /**
   * @param dirty - if line is dirty
   *
   * @brief Sets if this line contains dirty data
   */
  public void setDirty(boolean dirty)
  {
    this.dirty = dirty;
  }
  
  /**
   * @param valid - if line is valid
   *
   * @brief Sets if this line contains valid data
   */
  public void setValid(boolean valid)
  {
    this.valid = valid;
  }
  
  public int getIndex()
  {
    return index;
  }
  
  public long getBaseAddress()
  {
    return baseAddress;
  }
  
  public void setBaseAddress(long baseAddress)
  {
    this.baseAddress = baseAddress;
  }
  
  public byte[] getLineData()
  {
    return line;
  }
  
  public void setLineData(byte[] line)
  {
    this.line = line;
  }
}
