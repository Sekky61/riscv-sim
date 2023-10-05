/**
 * @file CacheAccess.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains container class for information about cache access
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

import com.gradle.superscalarsim.models.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @class CacheAccess
 * @brief Container class for last cache access
 */
public class CacheAccess
{
  /// Clock cycle in which the access executed
  private int clockCycle;
  /// Id of instruction which executed the access
  private int id;
  /// List of hits/misses for all accessed lines
  private List<Boolean> isHit;
  /// Is the access store or load?
  private boolean isStore;
  /// Tag of the access - from address
  private long tag;
  /// Index of the access - from address
  private int index;
  /// Parsed offset of the access
  private int offset;
  /// Data of the access (for store)
  private long data;
  ///Index in whole cache (as if there was no associativity) for all accessed lines
  private List<Integer> cacheIndex;
  ///Offset inside line for all accessed lines
  private List<Integer> lineOffset;
  ///Delay this access had
  private int delay;
  
  ///Current end of replacement before this access happened
  private int endOfReplacement;
  
  public CacheAccess()
  {
  }
  
  /**
   * @class Constructor with tag, index, offset as seperate params
   */
  public CacheAccess(int clockCycle,
                     int endOfReplacement,
                     int id,
                     Boolean[] isHit,
                     boolean isStore,
                     long tag,
                     int index,
                     int offset,
                     long data,
                     Integer[] cacheIndex,
                     Integer[] lineOffset)
  {
    this.clockCycle       = clockCycle;
    this.endOfReplacement = endOfReplacement;
    this.id               = id;
    this.isHit            = new ArrayList<>();
    this.isHit.addAll(Arrays.asList(isHit));
    this.isStore    = isStore;
    this.tag        = tag;
    this.index      = index;
    this.offset     = offset;
    this.data       = data;
    this.cacheIndex = new ArrayList<>();
    this.cacheIndex.addAll(Arrays.asList(cacheIndex));
    this.lineOffset = new ArrayList<>();
    this.lineOffset.addAll(Arrays.asList(lineOffset));
  }
  
  /**
   * @class Constructor with tag, index, offset in a Triplet
   */
  public CacheAccess(int clockCycle,
                     int endOfReplacement,
                     int id,
                     Boolean[] isHit,
                     boolean isStore,
                     Triplet<Long, Integer, Integer> splitAddress,
                     long data,
                     Integer[] cacheIndex,
                     Integer[] lineOffset)
  {
    this.clockCycle       = clockCycle;
    this.endOfReplacement = endOfReplacement;
    this.id               = id;
    this.isHit            = new ArrayList<>();
    this.isHit.addAll(Arrays.asList(isHit));
    this.isStore    = isStore;
    this.tag        = splitAddress.getFirst();
    this.index      = splitAddress.getSecond();
    this.offset     = splitAddress.getThird();
    this.data       = data;
    this.cacheIndex = new ArrayList<>();
    this.cacheIndex.addAll(Arrays.asList(cacheIndex));
    this.lineOffset = new ArrayList<>();
    this.lineOffset.addAll(Arrays.asList(lineOffset));
  }
  
  public int getId()
  {
    return id;
  }
  
  public int getClockCycle()
  {
    return clockCycle;
  }
  
  public Boolean[] isHit()
  {
    return isHit.toArray(Boolean[]::new);
  }
  
  public boolean isStore()
  {
    return isStore;
  }
  
  public long getTag()
  {
    return tag;
  }
  
  public int getIndex()
  {
    return index;
  }
  
  public int getOffset()
  {
    return offset;
  }
  
  public Integer[] getCacheIndex()
  {
    return cacheIndex.toArray(Integer[]::new);
  }
  
  public Integer[] getLineOffset()
  {
    return lineOffset.toArray(Integer[]::new);
  }
  
  public long getData()
  {
    return data;
  }
  
  public void setClockCycle(int clockCycle)
  {
    this.clockCycle = clockCycle;
  }
  
  public void setId(int id)
  {
    this.id = id;
  }
  
  public void setLoadStore(boolean isStore)
  {
    this.isStore = isStore;
  }
  
  public void setIsHit(Boolean[] isHit)
  {
    this.isHit.addAll(Arrays.asList(isHit));
  }
  
  public void setTag(long tag)
  {
    this.tag = tag;
  }
  
  public void setIndex(int index)
  {
    this.index = index;
  }
  
  public void setOffset(int offset)
  {
    this.offset = offset;
  }
  
  public void setData(long data)
  {
    this.data = data;
  }
  
  public void setCacheIndex(Integer[] cacheIndex)
  {
    this.cacheIndex.addAll(Arrays.asList(cacheIndex));
  }
  
  public void setLineOffset(Integer[] lineOffset)
  {
    this.lineOffset.addAll(Arrays.asList(lineOffset));
  }
  
  public void addLineAccess(boolean isHit, int cacheIndex, int lineOffset)
  {
    this.isHit.add(isHit);
    this.cacheIndex.add(cacheIndex);
    this.lineOffset.add(lineOffset);
  }
  
  public void setDelay(int delay)
  {
    this.delay = delay;
  }
  
  public int getDelay()
  {
    return delay;
  }
  
  public int getEndOfReplacement()
  {
    return endOfReplacement;
  }
}
