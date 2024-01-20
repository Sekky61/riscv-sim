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
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.Triplet;
import com.gradle.superscalarsim.models.cache.CacheLineModel;
import com.gradle.superscalarsim.models.cache.ReplacementPolicyModel;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @class Cache
 * @brief Implements cache functionality
 * @details Implementation of the cache with various settings of sizes and replacement policies.
 * Physical addresses only.
 * <p>
 * The cache is non-blocking. This means, that the cache can resolve multiple misses at the same time.
 * It can also work on multiple transactions at the same time.
 * </p>
 * <p>
 * Only {@link MemoryAccessUnit} can work with the cache.
 * A MAU can issue one operation at a time. If there are multiple MAUs, each can "work" on one cache access at a time.
 * </p>
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class Cache implements AbstractBlock, MemoryBlock
{
  /**
   * Constant cache ID
   */
  public static final int CACHE_ID = 32;
  
  /**
   * Reference to memory
   */
  @JsonIdentityReference(alwaysAsId = true)
  private SimulatedMemory memory;
  
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
   * Size of line in bytes. Must be a power of 2.
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
   * The replacement policy used in the cache
   */
  private ReplacementPoliciesEnum replacementPolicyType;
  
  /**
   * Store delay of cache in cycles
   */
  private int storeDelay;
  
  /**
   * Load delay of cache in cycles
   */
  private int loadDelay;
  
  /**
   * List of parallel cache operations in progress or recently finished.
   */
  private List<MemoryTransaction> cacheTransactions;
  
  /**
   * List of main memory operations in progress or recently finished.
   */
  private List<MemoryTransaction> memoryTransactions;
  
  /**
   * ID generator for cache accesses
   */
  private int cacheAccessId;
  
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
   * @param statistics               Statistics of the simulation
   *
   * @brief Constructor
   */
  public Cache(SimulatedMemory memory,
               int numberOfLines,
               int associativity,
               int lineSize,
               int storeDelay,
               int loadDelay,
               ReplacementPoliciesEnum replacementPolicy,
               boolean writeBack,
               SimulationStatistics statistics)
  {
    if (!areSettingsCorrect(numberOfLines, associativity, lineSize))
    {
      throw new IllegalArgumentException("Invalid cache settings");
    }
    
    this.memory                = memory;
    this.numberOfLines         = numberOfLines;
    this.associativity         = associativity;
    this.lineSize              = lineSize;
    this.storeDelay            = storeDelay;
    this.loadDelay             = loadDelay;
    this.replacementPolicyType = replacementPolicy;
    this.writeBack             = writeBack;
    this.statistics            = statistics;
    this.replacementPolicy     = ReplacementPolicyModel.getReplacementPolicyModel(replacementPolicy, numberOfLines,
                                                                                  associativity);
    this.cacheTransactions     = new ArrayList<>();
    this.memoryTransactions    = new ArrayList<>();
    this.cacheAccessId         = 55;
    
    //Initialize cache - everything is invalid and clean with value zero for data and tag
    this.cache = new CacheLineModel[numberOfLines / associativity][associativity];
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      for (int j = 0; j < associativity; j++)
      {
        cache[i][j] = new CacheLineModel(lineSize, i * associativity + j);
      }
    }
  }
  
  /**
   * @param numberOfLines Number of cache lines
   * @param associativity Number of lines per index
   * @param lineSize      Size of line in bytes
   *
   * @return True if the settings are valid, false otherwise
   * @brief Checks if the cache parameters are valid
   */
  public boolean areSettingsCorrect(int numberOfLines, int associativity, int lineSize)
  {
    // Line size must be a power of 2
    boolean isLineSizePowerOfTwo = Integer.bitCount(lineSize) == 1;
    return numberOfLines % associativity == 0 && isLineSizePowerOfTwo && numberOfLines % 2 == 0;
  }
  
  /**
   * Flushes the cache - writes dirty lines to memory. No delay.
   * TODO: delay? optionally?
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
   * @param address starting byte of the access (can be misaligned)
   * @param size    Size of the access in bytes (1-8)
   *
   * @return The data from cache.
   * @brief Gets data from cache. For viewing cache during debugging. The value must be present.
   */
  public long getData(long address, int size)
  {
    CacheLineModel line = findLane(address);
    assert line != null;
    byte[] returnData = line.getDataBytes((int) (address & (lineSize - 1)), size);
    long   returnVal  = 0;
    for (int i = 0; i < size; i++)
    {
      // Mask needed because of sign extension
      returnVal |= ((long) returnData[i] & ((1L << 8) - 1)) << (i * 8);
    }
    return returnVal;
  }
  
  /**
   * @return True the cache line if it is in the cache, null otherwise
   */
  public CacheLineModel findLane(long address)
  {
    Triplet<Long, Integer, Integer> addressSplit = splitAddress(address);
    long                            tag          = addressSplit.getFirst();
    int                             index        = addressSplit.getSecond();
    
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
   * @param address Size of line in bytes, must be multiple of 4
   *
   * @return Triplet of tag, index, offset
   * @details Highest bits are tag, then index, then offset
   * @brief Splits address of cache access to tag, index, offset
   */
  public Triplet<Long, Integer, Integer> splitAddress(long address)
  {
    int  offset = (int) (address & (lineSize - 1));
    int  index  = (int) ((address >>> getOffsetBits()) & ((numberOfLines / associativity) - 1));
    long tag    = (address >>> (getOffsetBits() + getIndexBits()));
    return new Triplet<>(tag, index, offset);
  }
  
  /**
   * @return Number of bits needed to index the contents of a cache line.
   */
  public int getOffsetBits()
  {
    // Basically a log2
    return 31 - Integer.numberOfLeadingZeros(lineSize);
  }
  
  /**
   * @return Number of bits needed to index the associativity sets.
   */
  public int getIndexBits()
  {
    // Basically a log2
    return 31 - Integer.numberOfLeadingZeros(numberOfLines / associativity);
  }
  
  @Override
  public void simulate(int cycle)
  {
    for (int i = memoryTransactions.size() - 1; i >= 0; i--)
    {
      MemoryTransaction transaction = memoryTransactions.get(i);
      // Check if the operation is finished this cycle
      int finishCycle = transaction.timestamp() + transaction.latency();
      assert finishCycle >= cycle;
      if (finishCycle == cycle)
      {
        // Main memory transaction finished
        memory.finishTransaction(transaction.id());
        this.memoryTransactions.remove(i);
        if (!transaction.isStore())
        {
          // Save the line that we just loaded into cache
          long           tag  = splitAddress(transaction.address()).getFirst();
          CacheLineModel line = pickLineToUse(transaction.address());
          line.setLineData(transaction.data());
          line.setValid(true);
          line.setDirty(false);
          line.setTag(tag);
          line.setBaseAddress(transaction.address());
        }
      }
    }
    
    // Cache operations
    for (MemoryTransaction transaction : this.cacheTransactions)
    {
      assert !transaction.isFinished(); // All finished transactions should be removed from the list by the requester
      // Check if the operation is finished this cycle
      int finishCycle = transaction.timestamp() + transaction.latency();
      if (finishCycle == cycle)
      {
        // Cache transaction finished, write/read from cache
        if (transaction.isStore())
        {
          executeStore(transaction);
        }
        else
        {
          executeLoad(transaction);
        }
        transaction.finish();
      }
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
    memory.reset();
  }
  
  /**
   * @param transaction The transaction to execute
   *
   * @brief Executes a load transaction (loads from an already present line)
   */
  private void executeLoad(MemoryTransaction transaction)
  {
    long address = transaction.address();
    int  size    = transaction.size();
    assert size <= 8;
    
    Triplet<Long, Integer, Integer> splitAddress = splitAddress(address);
    int                             offset       = splitAddress.getThird();
    
    CacheLineModel line = findLane(address);
    assert line != null;
    
    // todo what if the line disappears from cache later?
    
    boolean isMultiLine = offset + size > lineSize;
    byte[]  data;
    if (isMultiLine)
    {
      // split into two transactions
      int    size1 = lineSize - offset;
      byte[] data1 = line.getDataBytes(offset, size1);
      
      int            address2 = (int) (address + size1);
      CacheLineModel line2    = findLane(address2);
      int            size2    = size - size1;
      byte[]         data2    = line2.getDataBytes(0, size2);
      data = new byte[size];
      System.arraycopy(data1, 0, data, 0, size1);
      System.arraycopy(data2, 0, data, size1, size2);
    }
    else
    {
      data = line.getDataBytes(offset, size);
    }
    transaction.setData(data);
  }
  
  /**
   * Finds a suitable cache line to store.
   * Does not touch the line itself.
   * Updates replacement policy.
   *
   * @param address starting byte of the access (can be misaligned)
   *
   * @return Cache line to use for the new data. Either an empty line is found or a line is replaced.
   */
  public CacheLineModel pickLineToUse(long address)
  {
    Triplet<Long, Integer, Integer> addressSplit = splitAddress(address);
    int                             index        = addressSplit.getSecond();
    // todo elsewhere?
    replacementPolicy.updatePolicy(index, index % associativity);
    // Go through all lines - compare if tag matches
    for (int i = 0; i < associativity; i++)
    {
      CacheLineModel lineCandidate = cache[index][i];
      if (!lineCandidate.isValid())
      {
        return lineCandidate;
      }
    }
    
    // Pick victim, store victim line into memory
    int            victimIndex = replacementPolicy.getLineToReplace(index);
    CacheLineModel line        = cache[index][victimIndex];
    if (line.isDirty())
    {
      CacheLineModel victimLine = cache[index][victimIndex];
      memory.insertIntoMemory(victimLine.getBaseAddress(), victimLine.getLineData());
    }
    
    return line;
  }
  
  /**
   * @param transaction The transaction to execute
   *
   * @brief Executes a store transaction (saves to an already present line)
   */
  private void executeStore(MemoryTransaction transaction)
  {
    long   address = transaction.address();
    byte[] data    = transaction.data();
    int    size    = transaction.size();
    assert size == data.length;
    
    Triplet<Long, Integer, Integer> splitAddress = splitAddress(address);
    int                             offset       = splitAddress.getThird();
    
    CacheLineModel line = findLane(address);
    assert line != null;
    if (writeBack)
    {
      // todo memory transaction here
      line.setDirty(true);
    }
    
    boolean isMultiLine = offset + size > lineSize;
    if (isMultiLine)
    {
      // split into two transactions
      int    size1 = lineSize - offset;
      byte[] data1 = new byte[size1];
      System.arraycopy(data, 0, data1, 0, size1);
      
      int            address2 = (int) (address + size1);
      CacheLineModel line2    = findLane(address2);
      if (writeBack)
      {
        // todo memory transaction here
        line2.setDirty(true);
      }
      int    size2 = size - size1;
      byte[] data2 = new byte[size2];
      System.arraycopy(data, size1, data2, 0, size2);
      line.setData(offset, data1);
      line2.setData(0, data2);
    }
    else
    {
      line.setData(offset, data);
    }
  }
  
  /**
   * Schedule a memory access. It will be finished after the specified number of cycles.
   * The transaction will be mutated by the memory block.
   *
   * @param transaction Memory transaction to schedule
   *
   * @return Number of cycles until the transaction is finished
   */
  @Override
  public int scheduleTransaction(MemoryTransaction transaction)
  {
    // Check if line is in cache
    CacheLineModel line = findLane(transaction.address());
    cacheTransactions.add(transaction);
    boolean isHit      = line != null;
    int     cacheDelay = (transaction.isStore() ? storeDelay : loadDelay);
    transaction.setId(cacheAccessId++);
    
    
    int latency = cacheDelay;
    // todo what if the line disappears from cache later?
    if (!isHit)
    {
      // cache delay and start loading from memory
      // Create a memory transaction for the whole cache line
      latency = cacheDelay + requestCacheLineLoad(transaction.address(), transaction.timestamp());
    }
    
    boolean spansTwoLines = (transaction.address() & (lineSize - 1)) + transaction.size() > lineSize;
    if (spansTwoLines)
    {
      // Schedule second line load. The next address is at the next lineSize boundary
      long           nextAddress = ((transaction.address() >>> getOffsetBits()) + 1) << getOffsetBits();
      CacheLineModel nextLine    = findLane(nextAddress);
      if (nextLine == null)
      {
        isHit = false;
        // Create a memory transaction for the second cache line
        latency = cacheDelay + requestCacheLineLoad(nextAddress, transaction.timestamp());
      }
    }
    
    transaction.setLatency(latency);
    
    if (isHit)
    {
      statistics.cache.incrementHits();
    }
    else
    {
      statistics.cache.incrementMisses();
    }
    if (transaction.isStore())
    {
      statistics.cache.incrementWriteAccesses(transaction.size());
    }
    else
    {
      statistics.cache.incrementReadAccesses(transaction.size());
    }
    
    return latency;
  }
  
  private int requestCacheLineLoad(long address, int timestamp)
  {
    // Create a memory transaction for the whole cache line
    long baseAddress = address & -(1L << getOffsetBits());
    MemoryTransaction lineTransaction = new MemoryTransaction(-1, CACHE_ID, timestamp, baseAddress, null, lineSize,
                                                              false, false);
    memoryTransactions.add(lineTransaction);
    return memory.scheduleTransaction(lineTransaction);
  }
  
  /**
   * Throws if the requested transaction is not in the list or not finished yet, as it would be a bug.
   *
   * @param id ID of the transaction
   *
   * @brief Remove the operation from the list.
   */
  @Override
  public MemoryTransaction finishTransaction(int id)
  {
    MemoryTransaction transaction = findTransaction(id);
    if (transaction == null)
    {
      throw new IllegalArgumentException("No such transaction");
    }
    if (!transaction.isFinished())
    {
      throw new IllegalArgumentException("Transaction not finished yet");
    }
    cacheTransactions.remove(transaction);
    return transaction;
  }
  
  private MemoryTransaction findTransaction(int id)
  {
    for (MemoryTransaction transaction : cacheTransactions)
    {
      if (transaction.id() == id)
      {
        return transaction;
      }
    }
    return null;
  }
  
  /**
   * @param id ID of the transaction
   *
   * @brief Cancel the transaction. It must be present and not finished.
   */
  @Override
  public void cancelTransaction(int id)
  {
    MemoryTransaction transaction = findTransaction(id);
    if (transaction == null)
    {
      throw new IllegalArgumentException("No such transaction");
    }
    if (transaction.isFinished())
    {
      throw new IllegalArgumentException("Transaction already finished");
    }
    cacheTransactions.remove(transaction);
  }
}
