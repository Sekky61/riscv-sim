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

import com.gradle.superscalarsim.blocks.CacheStatisticsCounter;
import com.gradle.superscalarsim.code.SimulatedMemory;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.Triplet;
import com.gradle.superscalarsim.models.cache.CacheAccess;
import com.gradle.superscalarsim.models.cache.CacheLineModel;
import com.gradle.superscalarsim.models.cache.ReplacementPolicyModel;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @class Cache
 * @brief Implements cache functionality
 * @details Implementation of the cache with various settings of sizes and replacement policies
 */
public class Cache
{
  private final CacheStatisticsCounter cacheStatisticsCounter;
  
  ///Number of cache lines
  private final int numberOfLines;
  ///Number of lines per index
  private final int associativity;
  ///Size of line in bytes in multiple of 4
  private final int lineSize;
  ///Cache implementation 1st direction is index, second are specific lines depending on associativity
  private final CacheLineModel[][] cache;
  ///Replacement policy implementation
  private final ReplacementPolicyModel replacementPolicy;
  
  public void setMemory(SimulatedMemory memory)
  {
    this.memory = memory;
  }
  
  ///Handler to memory
  private SimulatedMemory memory;
  ///Is the cache write back or write through?
  private final boolean writeBack;
  private final Stack<CacheLineModel> cacheLineHistory;
  /// History stack of all store instruction ids
  private final Stack<Integer> cacheIdHistory;
  /// Stack of last accesses in the cache - used in printing the memory
  private final Stack<CacheAccess> lastAccess;
  
  /// Delay of store access
  private final int storeDelay;
  /// Delay of load access
  private final int loadDelay;
  /// How long does it take to replace a line
  private final int lineReplacementDelay;
  /// Should the line replacement delay be added to store?
  private final boolean addRemainingDelayToStore;
  /// Type of replacement policy used in the cache
  private final ReplacementPoliciesEnum replacementPolicyType;
  /// Cycle in which the last line will stop replacing
  private int cycleEndOfReplacement;
  
  /**
   * @brief Constructor
   * @param memory - Simulated memory
   * @param numberOfLines - Number of cache lines
   * @param associativity - Number of lines per index
   * @param lineSize - Size of line in bytes in multiple of 4
   * @param replacementPolicy - replacement policy used in the cache
   * @param writeBack - Is the cache write back or write through?
   * @param addRemainingDelayToStore - Should the store's delay include line replacement
   * @param storeDelay - Delay of the store operation
   * @param loadDelay - Delay of the load operation
   * @param lineReplacementDelay - How long does it take to replace line
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
               CacheStatisticsCounter cacheStatisticsCounter)
  {
    if (!checkCorrectSettings(numberOfLines, associativity, lineSize))
    {
      throw new IllegalArgumentException();
    }
    this.numberOfLines = numberOfLines;
    this.associativity = associativity;
    this.lineSize      = lineSize;
    cache              = new CacheLineModel[numberOfLines / associativity][associativity];
    //Initialize cache - everything is invalid and clean with value zero for data and tag
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      for (int j = 0; j < associativity; j++)
      {
        cache[i][j] = new CacheLineModel(lineSize, i * associativity + j);
      }
    }
    this.replacementPolicyType = replacementPolicy;
    this.replacementPolicy     = ReplacementPolicyModel.getReplacementPolicyModel(replacementPolicy, numberOfLines,
                                                                                  associativity);
    this.memory                = memory;
    this.writeBack             = writeBack;
    cacheLineHistory           = new Stack<>();
    cacheIdHistory             = new Stack<>();
    this.lastAccess            = new Stack<>();
    this.lastAccess.add(new CacheAccess(0, 0, 0, new Boolean[0], false, 0, 0, 0, 0, new Integer[0], new Integer[0]));
    
    this.storeDelay               = storeDelay;
    this.loadDelay                = loadDelay;
    this.lineReplacementDelay     = lineReplacementDelay;
    this.addRemainingDelayToStore = addRemainingDelayToStore;
    this.cycleEndOfReplacement    = 0;
    
    this.cacheStatisticsCounter = cacheStatisticsCounter;
  }
  
  /**
   * @brief Constructor with all fields
   */
  public Cache(CacheStatisticsCounter cacheStatisticsCounter,
               int numberOfLines,
               int associativity,
               int lineSize,
               CacheLineModel[][] cache,
               ReplacementPolicyModel replacementPolicy,
               SimulatedMemory memory,
               boolean writeBack,
               Stack<CacheLineModel> cacheLineHistory,
               Stack<Integer> cacheIdHistory,
               Stack<CacheAccess> lastAccess,
               int storeDelay,
               int loadDelay,
               int lineReplacementDelay,
               boolean addRemainingDelayToStore,
               ReplacementPoliciesEnum replacementPolicyType,
               int cycleEndOfReplacement)
  {
    this.cacheStatisticsCounter   = cacheStatisticsCounter;
    this.numberOfLines            = numberOfLines;
    this.associativity            = associativity;
    this.lineSize                 = lineSize;
    this.cache                    = cache;
    this.replacementPolicy        = replacementPolicy;
    this.memory                   = memory;
    this.writeBack                = writeBack;
    this.cacheLineHistory         = cacheLineHistory;
    this.cacheIdHistory           = cacheIdHistory;
    this.lastAccess               = lastAccess;
    this.storeDelay               = storeDelay;
    this.loadDelay                = loadDelay;
    this.lineReplacementDelay     = lineReplacementDelay;
    this.addRemainingDelayToStore = addRemainingDelayToStore;
    this.replacementPolicyType    = replacementPolicyType;
    this.cycleEndOfReplacement    = cycleEndOfReplacement;
  }
  
  /**
   * @brief Computes the remaining delay with info from last access to the same line
   * @param isHit - Was this access cache hit or cache miss
   * @param matchedAccess - Access which matched this one (dummy one in case the access didn't match anything)
   * @param currentCycle - Current cycle of execution
   * @return delay of this access coming from line replacement
   */
  private int remainingDelay(boolean isHit, CacheAccess matchedAccess, int currentCycle)
  {
    if (isHit)
    {//Add remaining delay of the previous access if this was a hit
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
   * @brief Computes the remaining delay of line replacement influencing this access
   * @param isHit - Was this access cache hit or cache miss
   * @param isStore - Store or Load
   * @param currentCycle - Current cycle of execution
   * @param tag - Tag of the access - to uniquely identify line
   * @param index - index to uniquely identify line
   * @return delay of this access coming from line replacement
   */
  private int computeRemainingDelay(boolean isHit, boolean isStore, int currentCycle, long tag, int index)
  {
    int storeAccessDelay = (addRemainingDelayToStore) ? Math.max(0, cycleEndOfReplacement - currentCycle) : 0;
    //Start from last access before current access
    for (int i = lastAccess.size() - 1; i > 0; i--)
    {
      CacheAccess tmpAccess         = lastAccess.get(i - 1);
      int         tmpAccessDelayEnd = Math.max(tmpAccess.getClockCycle(),
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
  
  
  /**
   * @brief Checks if the values of the cache are set valid
   * @param numberOfLines - Number of cache lines
   * @param associativity - Number of lines per index
   * @param lineSize - Size of line in bytes, must be multiple of 4
   * @return boolean - Are these settings valid?
   */
  public boolean checkCorrectSettings(int numberOfLines, int associativity, int lineSize)
  {
    return numberOfLines % associativity == 0 && lineSize % 4 == 0 && numberOfLines % 2 == 0;
  }
  
  private int getLog(int value, int log)
  {
    return (int) (Math.log((float) (value)) / Math.log(log));
  }
  
  
  /**
   * @brief Splits address of cache access to tag, index, offset
   * @param address - Size of line in bytes, must be multiple of 4
   * @return Triplet of tag, index, offset
   */
  public Triplet<Long, Integer, Integer> splitAddress(long address)
  {
    int  offset = (int) (address & (lineSize - 1));
    int  index  = (int) ((address >>> getLog(lineSize, 2)) & ((numberOfLines / associativity) - 1));
    long tag    = (address >>> (getLog(lineSize, 2) + getLog(numberOfLines / associativity, 2)));
    return new Triplet<>(tag, index, offset);
  }
  
  /**
   * @brief Gets data from specific line
   * @param line - get data from selected line
   * @param offset - starting byte of the access (can be misaligned)
   * @param size - Size of the access in bytes (1-8)
   * @return Data
   */
  private long getDataFromLine(CacheLineModel line, int offset, int size)
  {
    //Is address aligned?
    if (offset % size == 0 && Set.of(1, 2, 4).contains(size))
    {
      //Convert to long without sign extension
      return ((long) line.getData(offset, size) & ((1L << 32) - 1));
    }
    //Address is misaligned
    else
    {
      long returnData = line.getData(offset + size - 1, 1);
      for (int j = size - 1; j > 0; j--)
      {
        returnData = returnData << 8;
        returnData |= line.getData(offset + j - 1, 1);
      }
      return returnData;
    }
  }
  
  /**
   * @brief Gets data from multiple lines (minimum one)
   * @param line - get data from selected line
   * @param address - starting byte of the access (can be misaligned)
   * @param size - Size of the access in bytes (1-8)
   * @param id - Id of accessing instruction
   * @param currentCycle - Cycle in which the access happened
   * @return Pair of delay of this access and data
   */
  private Pair<Integer, Long> getDataFromLines(CacheLineModel line, long address, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    //Access is inside a single cache line
    if ((address % lineSize) + size <= lineSize)
    {
      return new Pair<>(0, getDataFromLine(line, splittedAddress.getThird(), size));
    }
    //Access spans two cache lines
    else
    {
      long                sizeInsideCurrentLine = (lineSize - (address % lineSize));
      long                bottomPart            = getDataFromLine(line, splittedAddress.getThird(),
                                                                  (int) sizeInsideCurrentLine);
      Pair<Integer, Long> tmpReturnVal          = getData(address + sizeInsideCurrentLine,
                                                          size - (int) sizeInsideCurrentLine, id, currentCycle);
      return new Pair<>(tmpReturnVal.getFirst(),
                        (tmpReturnVal.getSecond() << (size - sizeInsideCurrentLine) * 8) | bottomPart);
    }
  }
  
  /**
   * @brief Gets data from cache
   * @param address - starting byte of the access (can be misaligned)
   * @param size - Size of the access in bytes (1-8)
   * @param id - Id of accessing instruction
   * @param currentCycle - Cycle in which the access happened
   * @return Pair of delay of this access and data
   */
  public Pair<Integer, Long> getData(long address, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    
    //Save last access for visualization
    if (lastAccess.peek().getId() != id)
    {
      cacheStatisticsCounter.incrementAccesses();
      this.lastAccess.add(
              new CacheAccess(currentCycle, cycleEndOfReplacement, id, new Boolean[0], false, splittedAddress, 0,
                              new Integer[0], new Integer[0]));
    }
    
    //Go through all lines - compare if tag matches
    int emptyLine = -1;
    for (int i = 0; i < associativity; i++)
    {
      CacheLineModel line = cache[splittedAddress.getSecond()][i];
      if (line.getTag() == splittedAddress.getFirst() && line.isValid())
      {
        cacheStatisticsCounter.incrementHits(currentCycle);
        
        replacementPolicy.updatePolicy(id, splittedAddress.getSecond(), i);
        lastAccess.peek()
                  .addLineAccess(true, splittedAddress.getSecond() * associativity + i, splittedAddress.getThird());
        Pair<Integer, Long> tmpReturnVal = getDataFromLines(line, address, size, id, currentCycle);
        return new Pair<>(tmpReturnVal.getFirst() + loadDelay + computeRemainingDelay(true, false, currentCycle,
                                                                                      splittedAddress.getFirst(),
                                                                                      splittedAddress.getSecond()),
                          tmpReturnVal.getSecond());
      }
      if (emptyLine == -1 && !line.isValid())
      {
        emptyLine = i;
      }
    }
    int selectedLine;
    if (emptyLine == -1)
    {
      selectedLine = replacementPolicy.getLineToReplace(id, splittedAddress.getSecond());
      if (cache[splittedAddress.getSecond()][selectedLine].isDirty())
      {
        //Store victim line into memory
        for (int i = 0; i < lineSize; i++)
        {
          memory.insertIntoMemory(cache[splittedAddress.getSecond()][selectedLine].getBaseAddress() + i,
                                  (byte) cache[splittedAddress.getSecond()][selectedLine].getData(i, 1), id);
        }
      }
    }
    else
    {
      selectedLine = emptyLine;
    }
    cacheStatisticsCounter.incrementMisses(currentCycle);
    
    //Store current cache line in history for backward simulation
    cacheLineHistory.add(cache[splittedAddress.getSecond()][selectedLine]);
    cacheIdHistory.add(id);
    cache[splittedAddress.getSecond()][selectedLine].saveToHistory(id);
    
    long baseMemoryAddress = address & -(1L << getLog(lineSize, 2));
    //Load new line from memory
    for (int i = 0; i < lineSize; i++)
    {
      cache[splittedAddress.getSecond()][selectedLine].setData(i, 1, memory.getFromMemory(baseMemoryAddress + i));
    }
    cache[splittedAddress.getSecond()][selectedLine].setDirty(false);
    cache[splittedAddress.getSecond()][selectedLine].setValid(true);
    cache[splittedAddress.getSecond()][selectedLine].setTag(splittedAddress.getFirst());
    cache[splittedAddress.getSecond()][selectedLine].setBaseAddress(baseMemoryAddress);
    
    replacementPolicy.updatePolicy(id, splittedAddress.getSecond(), selectedLine);
    lastAccess.peek().addLineAccess(false, splittedAddress.getSecond() * associativity + selectedLine,
                                    splittedAddress.getThird());
    Pair<Integer, Long> tmpReturnVal = getDataFromLines(cache[splittedAddress.getSecond()][selectedLine], address, size,
                                                        id, currentCycle);
    return new Pair<>(tmpReturnVal.getFirst() + loadDelay + computeRemainingDelay(false, false, currentCycle,
                                                                                  splittedAddress.getFirst(),
                                                                                  splittedAddress.getSecond()),
                      tmpReturnVal.getSecond());
  }
  
  /**
   * @brief Sets data to selected line
   * @param line - set data to this line
   * @param address - starting byte of the access (can be misaligned)
   * @param size - Size of the access in bytes (1-8)
   * @param offset - starting byte of the access inside the line
   * @param id - Id of accessing instruction
   */
  private void setDataToLine(CacheLineModel line, long address, final int offset, long data, final int size, int id)
  {
    //If the cache is write through also store data to memory
    if (!writeBack)
    {
      long memoryData = data;
      for (int i = 0; i < size; i++)
      {
        memory.insertIntoMemory(address + i, (byte) memoryData, id);
        memoryData = memoryData >> 8;
      }
    }
    
    //Is address aligned?
    if (offset % size == 0 && Set.of(1, 2, 4).contains(size))
    {
      line.setData(offset, size, (int) data);
    }
    //Address is misaligned or bigger than int
    else
    {
      long mask = 255L;
      for (int j = 0; j < size; j++)
      {
        line.setData(offset + j, 1, (int) (data & (mask)));
        data = data >> 8;
      }
    }
  }
  
  
  /**
   * @brief Sets data to selected line (accesses another line in case the access spans two lines)
   * @param line - set data to this line
   * @param address - starting byte of the access (can be misaligned)
   * @param size - Size of the access in bytes (1-8)
   * @param id - Id of accessing instruction
   * @param currentCycle - Cycle in which this access is happening
   * @return delay caused by this access
   */
  private int setDataToLines(CacheLineModel line, long address, long data, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    
    if (writeBack)
    {
      line.setDirty(true);
    }
    
    //Access is inside a single cache line
    if ((address % lineSize) + size <= lineSize)
    {
      setDataToLine(line, address, splittedAddress.getThird(), data, size, id);
      return 0;
    }
    //Access spans two cache lines
    else
    {
      long sizeInsideCurrentLine = (lineSize - (address % lineSize));
      setDataToLine(line, address, splittedAddress.getThird(), data, (int) sizeInsideCurrentLine, id);
      return storeData(address + sizeInsideCurrentLine, data >> sizeInsideCurrentLine * 8,
                       size - (int) sizeInsideCurrentLine, id, currentCycle);
    }
  }
  
  /**
   * @brief Sets data to cache
   * @param address - starting byte of the access (can be misaligned)
   * @param data - data to be stored
   * @param size - Size of the access in bytes (1-8)
   * @param id - Id of accessing instruction
   * @param currentCycle - Cycle in which this access is happening
   * @return delay caused by this access
   */
  public int storeData(long address, long data, int size, int id, int currentCycle)
  {
    Triplet<Long, Integer, Integer> splittedAddress = splitAddress(address);
    
    //Save last access for visualization
    if (lastAccess.peek().getId() != id)
    {
      cacheStatisticsCounter.incrementAccesses();
      lastAccess.add(new CacheAccess(currentCycle, cycleEndOfReplacement, id, new Boolean[0], true, splittedAddress, 0,
                                     new Integer[0], new Integer[0]));
    }
    
    //Go through all lines - compare if tag matches
    int emptyLine = -1;
    for (int i = 0; i < associativity; i++)
    {
      CacheLineModel line = cache[splittedAddress.getSecond()][i];
      if (line.getTag() == splittedAddress.getFirst() && line.isValid())
      {
        cacheStatisticsCounter.incrementHits(currentCycle);
        
        //Store current cache line in history for backward simulation
        cacheLineHistory.add(line);
        cacheIdHistory.add(id);
        line.saveToHistory(id);
        
        replacementPolicy.updatePolicy(id, splittedAddress.getSecond(), i);
        lastAccess.peek()
                  .addLineAccess(true, splittedAddress.getSecond() * associativity + i, splittedAddress.getThird());
        int tmpReturn = setDataToLines(line, address, data, size, id, currentCycle);
        return storeDelay + tmpReturn + computeRemainingDelay(true, true, currentCycle, splittedAddress.getFirst(),
                                                              splittedAddress.getSecond());
      }
      if (emptyLine == -1 && !line.isValid())
      {
        emptyLine = i;
      }
    }
    int selectedLine;
    if (emptyLine == -1)
    {
      selectedLine = replacementPolicy.getLineToReplace(id, splittedAddress.getSecond());
      if (cache[splittedAddress.getSecond()][selectedLine].isDirty())
      {
        //Store victim line into memory
        for (int i = 0; i < lineSize; i++)
        {
          memory.insertIntoMemory(cache[splittedAddress.getSecond()][selectedLine].getBaseAddress() + i,
                                  (byte) cache[splittedAddress.getSecond()][selectedLine].getData(i, 1), id);
        }
      }
    }
    else
    {
      selectedLine = emptyLine;
    }
    cacheStatisticsCounter.incrementMisses(currentCycle);
    
    //Store current cache line in history for backward simulation
    cacheLineHistory.add(cache[splittedAddress.getSecond()][selectedLine]);
    cacheIdHistory.add(id);
    cache[splittedAddress.getSecond()][selectedLine].saveToHistory(id);
    
    long baseMemoryAddress = address & -(1L << getLog(lineSize, 2));
    //Load new line from memory
    for (int i = 0; i < lineSize; i++)
    {
      cache[splittedAddress.getSecond()][selectedLine].setData(i, 1, memory.getFromMemory(baseMemoryAddress + i));
    }
    cache[splittedAddress.getSecond()][selectedLine].setValid(true);
    cache[splittedAddress.getSecond()][selectedLine].setDirty(false);
    cache[splittedAddress.getSecond()][selectedLine].setTag(splittedAddress.getFirst());
    cache[splittedAddress.getSecond()][selectedLine].setBaseAddress(baseMemoryAddress);
    
    replacementPolicy.updatePolicy(id, splittedAddress.getSecond(), selectedLine);
    lastAccess.peek().addLineAccess(false, splittedAddress.getSecond() * associativity + selectedLine,
                                    splittedAddress.getThird());
    
    int tmpReturn = setDataToLines(cache[splittedAddress.getSecond()][selectedLine], address, data, size, id,
                                   currentCycle);
    return storeDelay + tmpReturn + computeRemainingDelay(false, true, currentCycle, splittedAddress.getFirst(),
                                                          splittedAddress.getSecond());
  }
  
  /**
   * @brief Restores current line from history
   * @param id - Current clock cycle
   */
  public void revertHistory(int id)
  {
    while (!lastAccess.isEmpty() && lastAccess.peek().getId() == id)
    {
      cacheStatisticsCounter.decrementTotalDelay();
      cacheStatisticsCounter.decrementAccesses();
      for (boolean hitMiss : lastAccess.peek().isHit())
      {
        if (hitMiss)
        {
          cacheStatisticsCounter.decrementHits();
        }
        else
        {
          cacheStatisticsCounter.decrementMisses();
        }
      }
      this.cycleEndOfReplacement = lastAccess.peek().getEndOfReplacement();
      //This should not actually be called multiple times
      lastAccess.pop();
    }
    
    while (!cacheIdHistory.isEmpty() && (cacheIdHistory.peek() == id))
    {
      cacheLineHistory.peek().revertHistory(id);
      memory.revertHistory(id);
      replacementPolicy.revertHistory(id);
      
      cacheIdHistory.pop();
      cacheLineHistory.pop();
    }
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
    cacheLineHistory.clear();
    cacheIdHistory.clear();
    memory.reset();
    cycleEndOfReplacement = 0;
    lastAccess.clear();
    this.lastAccess.add(new CacheAccess(0, 0, 0, new Boolean[0], false, 0, 0, 0, 0, new Integer[0], new Integer[0]));
  }
  
  /**
   * @brief Gets full cache data
   */
  public CacheLineModel[][] getCacheContent()
  {
    return cache;
  }
  
  /**
   * @brief Gets full memory
   */
  public Map<Long, Byte> getMemoryMap()
  {
    return memory.getMemoryMap();
  }
  
  public int getNumberOfLines()
  {
    return numberOfLines;
  }
  
  public int getAssociativity()
  {
    return associativity;
  }
  
  public CacheAccess getLastAccess()
  {
    return lastAccess.peek();
  }
  
  /**
   * @brief Gets size of the lines in bytes (magnitude of 4)
   */
  public int getLineSize()
  {
    return lineSize;
  }
  
  /**
   * @brief Gets setup delay of store access
   */
  public int getStoreDelay()
  {
    return storeDelay;
  }
  
  /**
   * @brief Gets setup delay of load access
   */
  public int getLoadDelay()
  {
    return loadDelay;
  }
  
  /**
   * @brief Gets how long does it take to replace a line
   */
  public int getLineReplacementDelay()
  {
    return lineReplacementDelay;
  }
  
  /**
   * @brief Gets if line replacement delay should be added to store
   */
  public boolean getAddRemainingDelayToStore()
  {
    return addRemainingDelayToStore;
  }
  
  /**
   * @brief Gets current replacement policy
   */
  public ReplacementPoliciesEnum getReplacementPolicyType()
  {
    return replacementPolicyType;
  }
  
  /**
   * @brief Is store behaviour set to write-back or write-through?
   */
  public boolean isWriteBack()
  {
    return writeBack;
  }
}
