/**
 * @file CacheCode.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains top level of cache implementation
 * @date 04 April 2023 13:40 (created)
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

package com.gradle.superscalarsim.blocks.loadstore;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.code.SimulatedMemory;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.Triplet;
import com.gradle.superscalarsim.models.cache.CacheAccess;
import com.gradle.superscalarsim.models.cache.CacheLineModel;
import com.gradle.superscalarsim.models.cache.ReplacementPolicyModel;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.Stack;

/**
 * @class Cache
 * @brief Implements cache functionality
 * @details Implementation of the cache with various settings of sizes and replacement policies
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class Cache
{
  @JsonIdentityReference(alwaysAsId = true)
  private SimulationStatistics statistics;
  
  /**
   * Number of cache lines
   */
  private int numberOfLines;
  
  /**
   * Number of lines per index
   */
  private int associativity;
  
  /**
   * Size of line in bytes. Must be a multiple of 4
   */
  private int lineSize;
  
  /**
   * Cache storage.
   * First direction is index.
   * Second is specific lines depending on associativity
   */
  private CacheLineModel[][] cache;
  
  /**
   * Replacement policy implementation
   */
  private ReplacementPolicyModel replacementPolicy;
  
  /**
   * True if the cache write back, false if write through
   */
  private boolean writeBack;
  
  /**
   * Stack of last accesses in the cache - used in printing the memory
   * TODO: Maybe move this to statistics?
   */
  private Stack<CacheAccess> lastAccess;
  
  /**
   * Delay of store access
   */
  private int storeDelay;
  
  /**
   * Delay of load access
   */
  private int loadDelay;
  
  /**
   * How long does it take to replace a line
   */
  private int lineReplacementDelay;
  
  /**
   * True if the line replacement delay should be added to store
   */
  private boolean addRemainingDelayToStore;
  
  /**
   * The replacement policy used in the cache
   */
  private ReplacementPoliciesEnum replacementPolicyType;
  
  /**
   * Reference to memory
   */
  @JsonIdentityReference(alwaysAsId = true)
  private SimulatedMemory memory;
  
  /**
   * Cycle in which the last line will stop replacing
   */
  private int cycleEndOfReplacement;
  
  /**
   * @brief Constructor for (de)serialization
   */
  public Cache()
  {
  }
  
  /**
   * @param memory                   Simulated memory
   * @param numberOfLines            Number of cache lines
   * @param associativity            Number of lines per index
   * @param lineSize                 Size of line in bytes in multiple of 4
   * @param replacementPolicy        replacement policy used in the cache
   * @param writeBack                Is the cache write back or write through?
   * @param addRemainingDelayToStore Should the store's delay include line replacement
   * @param storeDelay               Delay of the store operation
   * @param loadDelay                Delay of the load operation
   * @param lineReplacementDelay     How long does it take to replace line
   *
   * @brief Constructor
   */
  public Cache(SimulatedMemory memory,
               int numberOfLines,
               int associativity,
               int lineSize,
               ReplacementPoliciesEnum replacementPolicy,
               boolean writeBack,
               boolean addRemainingDelayToStore,
               int storeDelay,
               int loadDelay,
               int lineReplacementDelay,
               SimulationStatistics statistics)
  {
    if (!areSettingsCorrect(numberOfLines, associativity, lineSize))
    {
      throw new IllegalArgumentException("Invalid cache settings");
    }
    
    this.numberOfLines            = numberOfLines;
    this.associativity            = associativity;
    this.lineSize                 = lineSize;
    this.replacementPolicyType    = replacementPolicy;
    this.memory                   = memory;
    this.writeBack                = writeBack;
    this.storeDelay               = storeDelay;
    this.loadDelay                = loadDelay;
    this.lineReplacementDelay     = lineReplacementDelay;
    this.addRemainingDelayToStore = addRemainingDelayToStore;
    this.cycleEndOfReplacement    = 0;
    this.statistics               = statistics;
    this.replacementPolicy        = ReplacementPolicyModel.getReplacementPolicyModel(replacementPolicy, numberOfLines,
                                                                                     associativity);
    
    //Initialize cache - everything is invalid and clean with value zero for data and tag
    this.cache = new CacheLineModel[numberOfLines / associativity][associativity];
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      for (int j = 0; j < associativity; j++)
      {
        cache[i][j] = new CacheLineModel(lineSize, i * associativity + j);
      }
    }
    this.lastAccess = new Stack<>();
    this.lastAccess.add(new CacheAccess(0, 0, 0, new Boolean[0], false, 0, 0, 0, 0, new Integer[0], new Integer[0]));
  }
  
  /**
   * @param numberOfLines Number of cache lines
   * @param associativity Number of lines per index
   * @param lineSize      Size of line in bytes
   *
   * @return boolean - True if the values are valid, false otherwise
   * @brief Checks if the cache parameters are valid
   */
  public boolean areSettingsCorrect(int numberOfLines, int associativity, int lineSize)
  {
    return numberOfLines % associativity == 0 && lineSize % 4 == 0 && numberOfLines % 2 == 0;
  }
  
  /**
   * Flushes the cache - writes dirty lines to memory
   */
  public void flush()
  {
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      for (int j = 0; j < associativity; j++)
      {
        CacheLineModel line = cache[i][j];
        if (line.isDirty())
        {
          //Store victim line into memory
          memory.insertIntoMemory(line.getBaseAddress(), line.getLineData());
          line.setDirty(false);
          line.setValid(false);
        }
      }
    }
  }
  
  /**
   * @param isHit         True if this access was a hit, false (miss) otherwise
   * @param matchedAccess Access which matched this one (dummy one in case the access didn't match anything)
   * @param currentCycle  Current cycle of execution
   *
   * @return delay of this access coming from line replacement
   * @brief Computes the remaining delay with info from last access to the same line
   */
  private int remainingDelay(boolean isHit, CacheAccess matchedAccess, int currentCycle)
  {
    if (isHit)
    {
      //Add remaining delay of the previous access if this was a hit
      return Math.max(0, matchedAccess.getClockCycle() + matchedAccess.getDelay() - currentCycle);
    }
    else
    {
      //Add line replacement delay + if some line is already replacing add it's time too
      cycleEndOfReplacement = (cycleEndOfReplacement <= currentCycle) ? currentCycle + lineReplacementDelay : cycleEndOfReplacement + lineReplacementDelay;
      return Math.max(0, cycleEndOfReplacement - currentCycle);
    }
  }
  
  /**
   * @param isHit        Was this access cache hit or cache miss
   * @param isStore      Store or Load
   * @param currentCycle Current cycle of execution
   * @param tag          Tag of the access - to uniquely identify line
   * @param index        index to uniquely identify line
   *
   * @return delay of this access coming from line replacement
   * @brief Computes the remaining delay of line replacement influencing this access
   */
  private int computeRemainingDelay(boolean isHit, boolean isStore, int currentCycle, long tag, int index)
  {
    int storeAccessDelay = (addRemainingDelayToStore) ? Math.max(0, cycleEndOfReplacement - currentCycle) : 0;
    //Start from last access before current access
    for (int i = lastAccess.size() - 1; i > 0; i--)
    {
      CacheAccess tmpAccess = lastAccess.get(i - 1);
      int tmpAccessDelayEnd = Math.max(tmpAccess.getClockCycle(),
                                       tmpAccess.getEndOfReplacement()) + tmpAccess.getDelay();
      if (tmpAccessDelayEnd <= currentCycle)
      {
        //If tmpAccesses delay ended before current clock cycle it's save to short-circuit the computation
        int delay = remainingDelay(isHit, tmpAccess, currentCycle);
        lastAccess.get(lastAccess.size() - 1).setDelay(lastAccess.get(lastAccess.size() - 1).getDelay() + delay);
        return (!isStore) ? delay : storeAccessDelay;
      }
      else if (tmpAccess.getClockCycle() < currentCycle)
      {
        //If the tag matches check if index matches
        if (tmpAccess.getTag() == tag)
        {
          //Check all accessed indexes of tmpAccess in case of previous misaligned over two lines
          for (int j = 0; j < tmpAccess.getCacheIndex().length; j++)
          {
            if (tmpAccess.getIndex() + j == index)
            {
              int delay = remainingDelay(isHit, tmpAccess, currentCycle);
              lastAccess.get(lastAccess.size() - 1).setDelay(lastAccess.get(lastAccess.size() - 1).getDelay() + delay);
              return (!isStore) ? delay : storeAccessDelay;
            }
          }
        }
        //If the tag doesn't match check if tmpAccess was misaligned over last cache line
        else if (index == 0 && tmpAccess.getTag() - 1 == tag && tmpAccess.getIndex() == numberOfLines / associativity - 1 && tmpAccess.getCacheIndex().length > 1)
        {
          int delay = remainingDelay(isHit, tmpAccess, currentCycle);
          lastAccess.get(lastAccess.size() - 1).setDelay(lastAccess.get(lastAccess.size() - 1).getDelay() + delay);
          return (!isStore) ? delay : storeAccessDelay;
        }
      }
    }
    //No previous access matched - this is a miss
    cycleEndOfReplacement = (cycleEndOfReplacement <= currentCycle) ? currentCycle + lineReplacementDelay : cycleEndOfReplacement + lineReplacementDelay;
    return (!isStore) ? lineReplacementDelay : 0;
  }
  
  private int getLog(int value, int log)
  {
    return (int) (Math.log((float) (value)) / Math.log(log));
  }
  
  
  /**
   * @param address Size of line in bytes, must be multiple of 4
   *
   * @return Triplet of tag, index, offset
   * @details Highest bits are tag, then index, then offset
   * @brief Splits address of cache access to tag, index, offset
   */
  public Triplet<Long, Integer, Integer> splitAddress(long address)
  {
    int  offset = (int) (address & (lineSize - 1));
    int  index  = (int) ((address >>> getLog(lineSize, 2)) & ((numberOfLines / associativity) - 1));
    long tag    = (address >>> (getLog(lineSize, 2) + getLog(numberOfLines / associativity, 2)));
    return new Triplet<>(tag, index, offset);
  }
  
  /**
   * @param line         get data from selected line
   * @param address      starting byte of the access (can be misaligned)
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which the access happened
   *
   * @return Pair of delay of this access and data
   * @brief Gets data from multiple lines (minimum one)
   */
  private Pair<Integer, byte[]> getDataFromLines(CacheLineModel line, long address, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    int                             offset          = splittedAddress.getThird();
    if ((address % lineSize) + size <= lineSize)
    {
      //Access is inside a single cache line
      byte[] data = line.getDataBytes(offset, size);
      return new Pair<>(0, data);
    }
    else
    {
      // Access spans two cache lines
      long   sizeInsideCurrentLine = (lineSize - (address % lineSize));
      byte[] bottomPart            = line.getDataBytes(offset, (int) sizeInsideCurrentLine);
      Pair<Integer, byte[]> tmpReturnVal = getDataBytes(address + sizeInsideCurrentLine,
                                                        size - (int) sizeInsideCurrentLine, id, currentCycle);
      // Join the two parts
      byte[] returnData = new byte[size];
      System.arraycopy(bottomPart, 0, returnData, 0, (int) sizeInsideCurrentLine);
      System.arraycopy(tmpReturnVal.getSecond(), 0, returnData, (int) sizeInsideCurrentLine,
                       size - (int) sizeInsideCurrentLine);
      return new Pair<>(tmpReturnVal.getFirst(), returnData);
    }
  }
  
  /**
   * @param address      starting byte of the access (can be misaligned)
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which the access happened
   *
   * @return Pair of delay of this access and data. For debugging mostly.
   * @brief Gets data from cache
   */
  public Pair<Integer, Long> getData(long address, int size, int id, int currentCycle)
  {
    Pair<Integer, byte[]> data = getDataBytes(address, size, id, currentCycle);
    // Transform to long, Little Endian
    byte[] returnData = data.getSecond();
    long   returnVal  = 0;
    for (int i = 0; i < size; i++)
    {
      // Mask needed because of sign extension
      returnVal |= ((long) returnData[i] & ((1L << 8) - 1)) << (i * 8);
    }
    return new Pair<>(data.getFirst(), returnVal);
  }
  
  /**
   * @return True the cache line if it is in the cache, null otherwise
   */
  public CacheLineModel findLane(long address)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    long                            tag             = splittedAddress.getFirst();
    int                             index           = splittedAddress.getSecond();
    
    for (int i = 0; i < associativity; i++)
    {
      CacheLineModel line = cache[index][i];
      if (line.getTag() == tag && line.isValid())
      {
        return line;
      }
    }
    return null;
  }
  
  /**
   * Finds a suitable cache line to store.
   * Does not touch the line itself.
   * Updates replacement policy.
   *
   * @return Cache line to use for the new data. Either an empty line is found or a line is replaced.
   */
  public CacheLineModel pickLineToUse(long address)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    // Go through all lines - compare if tag matches
    for (int i = 0; i < associativity; i++)
    {
      CacheLineModel lineCandidate = cache[splittedAddress.getSecond()][i];
      if (!lineCandidate.isValid())
      {
        return lineCandidate;
      }
    }
    
    // Pick victim, store victim line into memory
    int            victimIndex = replacementPolicy.getLineToReplace(splittedAddress.getSecond());
    CacheLineModel line        = cache[splittedAddress.getSecond()][victimIndex];
    if (line.isDirty())
    {
      CacheLineModel victimLine = cache[splittedAddress.getSecond()][victimIndex];
      memory.insertIntoMemory(victimLine.getBaseAddress(), victimLine.getLineData());
    }
    
    return line;
  }
  
  /**
   * @param address      starting byte of the access (can be misaligned)
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which the access happened
   *
   * @return Pair of delay of this access and data
   * @brief Gets data from cache
   */
  public Pair<Integer, byte[]> getDataBytes(long address, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    
    // Save last access for visualization
    if (lastAccess.peek().getId() != id)
    {
      statistics.cache.incrementReadAccesses(size);
      this.lastAccess.add(
              new CacheAccess(currentCycle, cycleEndOfReplacement, id, new Boolean[0], false, splittedAddress, 0,
                              new Integer[0], new Integer[0]));
    }
    
    CacheLineModel line  = findLane(address);
    boolean        isHit = line != null;
    if (isHit)
    {
      statistics.cache.incrementHits();
    }
    else
    {
      // Not found, need to load from memory -> miss
      statistics.cache.incrementMisses();
      line = pickLineToUse(address);
      
      long baseMemoryAddress = address & -(1L << getLog(lineSize, 2));
      line.setLineData(memory.getFromMemory(baseMemoryAddress, lineSize));
      line.setDirty(false);
      line.setValid(true);
      line.setTag(splittedAddress.getFirst());
      line.setBaseAddress(baseMemoryAddress);
    }
    
    int selectedLine = line.getIndex() % associativity;
    replacementPolicy.updatePolicy(splittedAddress.getSecond(), line.getIndex() % associativity);
    lastAccess.peek().addLineAccess(isHit, splittedAddress.getSecond() * associativity + selectedLine,
                                    splittedAddress.getThird());
    
    Pair<Integer, byte[]> tmpReturnVal = getDataFromLines(line, address, size, id, currentCycle);
    return new Pair<>(tmpReturnVal.getFirst() + loadDelay + computeRemainingDelay(isHit, false, currentCycle,
                                                                                  splittedAddress.getFirst(),
                                                                                  splittedAddress.getSecond()),
                      tmpReturnVal.getSecond());
  }
  
  /**
   * @param line    set data to this line
   * @param address starting byte of the access (can be misaligned)
   * @param offset  starting byte of the access inside the line
   * @param size    Size of the access in bytes (1-8)
   *
   * @brief Sets data to selected line
   */
  private void setDataToLine(CacheLineModel line, long address, final int offset, long data, final int size)
  {
    // If the cache is write-through also store data to memory
    if (!writeBack)
    {
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.putLong(data);
      // Take first size bytes
      byte[] memoryBytes = new byte[size];
      System.arraycopy(buffer.array(), 0, memoryBytes, 0, size);
      memory.insertIntoMemory(address, memoryBytes);
    }
    
    // Is address aligned?
    if (offset % size == 0 && Set.of(1, 2, 4).contains(size))
    {
      line.setData(offset, size, (int) data);
    }
    else
    {
      // Address is misaligned or bigger than int
      long mask = 255L;
      for (int j = 0; j < size; j++)
      {
        line.setData(offset + j, 1, (int) (data & (mask)));
        data = data >> 8;
      }
    }
  }
  
  
  /**
   * @param line         set data to this line
   * @param address      starting byte of the access (can be misaligned)
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which this access is happening
   *
   * @return delay caused by this access
   * @brief Sets data to selected line (accesses another line in case the access spans two lines)
   */
  private int setDataToLines(CacheLineModel line, long address, long data, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splitAddress = splitAddress(address);
    
    int offset = splitAddress.getThird();
    
    if (writeBack)
    {
      line.setDirty(true);
    }
    
    if ((address % lineSize) + size <= lineSize)
    {
      // Access is inside a single cache line
      setDataToLine(line, address, offset, data, size);
      return 0;
    }
    else
    {
      // Access spans two cache lines
      long sizeInsideCurrentLine = (lineSize - (address % lineSize));
      setDataToLine(line, address, offset, data, (int) sizeInsideCurrentLine);
      return storeData(address + sizeInsideCurrentLine, data >> sizeInsideCurrentLine * 8,
                       size - (int) sizeInsideCurrentLine, id, currentCycle);
    }
  }
  
  /**
   * @param address      starting byte of the access (can be misaligned)
   * @param data         data to be stored
   * @param size         Size of the access in bytes (1-8)
   * @param id           ID of accessing instruction
   * @param currentCycle Cycle in which this access is happening
   *
   * @return delay caused by this access
   * @brief Sets data to cache
   */
  public int storeData(long address, long data, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splitAddress = splitAddress(address);
    statistics.cache.incrementWriteAccesses(size);
    
    long tag    = splitAddress.getFirst();
    int  index  = splitAddress.getSecond();
    int  offset = splitAddress.getThird();
    
    // Save last access for visualization
    if (lastAccess.peek().getId() != id)
    {
      lastAccess.add(new CacheAccess(currentCycle, cycleEndOfReplacement, id, new Boolean[0], true, splitAddress, 0,
                                     new Integer[0], new Integer[0]));
    }
    
    CacheLineModel line  = findLane(address);
    boolean        isHit = line != null;
    
    if (isHit)
    {
      // Found the line
      statistics.cache.incrementHits();
    }
    else
    {
      statistics.cache.incrementMisses();
      // Load new line from memory
      line = pickLineToUse(address);
      long baseMemoryAddress = address & -(1L << getLog(lineSize, 2));
      line.setLineData(memory.getFromMemory(baseMemoryAddress, lineSize));
      line.setValid(true);
      line.setDirty(false);
      line.setTag(tag);
      line.setBaseAddress(baseMemoryAddress);
    }
    
    replacementPolicy.updatePolicy(index, line.getIndex() % associativity);
    lastAccess.peek().addLineAccess(isHit, line.getIndex(), offset);
    int tmpReturn = setDataToLines(line, address, data, size, id, currentCycle);
    return storeDelay + tmpReturn + computeRemainingDelay(isHit, true, currentCycle, tag, index);
  }
  
  /**
   * @brief Resets the cache state
   */
  public void reset()
  {
    //Initialize cache - everything is invalid and clean with value zero for data and tag
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      for (int j = 0; j < associativity; j++)
      {
        cache[i][j] = new CacheLineModel(lineSize, i * associativity + j);
      }
    }
    memory.reset();
    cycleEndOfReplacement = 0;
    lastAccess.clear();
    this.lastAccess.add(new CacheAccess(0, 0, 0, new Boolean[0], false, 0, 0, 0, 0, new Integer[0], new Integer[0]));
  }
}
