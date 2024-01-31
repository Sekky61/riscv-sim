package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.loadstore.Cache;
import com.gradle.superscalarsim.blocks.loadstore.SimulatedMemory;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;
import com.gradle.superscalarsim.models.util.Triplet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * The memory operations in this test module get scheduled before the cache is run (clock 0).
 * The cache starts responding the next clock (clock 1).
 */
public class CacheTest
{
  /**
   * Load and store latency of 1
   */
  Cache cache;
  
  /**
   * Memory with 0 latency
   */
  SimulatedMemory memory;
  
  SimulationStatistics statistics;
  
  /**
   * Set up the cache and memory.
   */
  @Before
  public void setup()
  {
    statistics = new SimulationStatistics(0, 1);
    memory     = new SimulatedMemory(1, 1, statistics);
    cache      = new Cache(memory, 16, 2, 16, 1, 1, ReplacementPoliciesEnum.RANDOM, true, statistics);
  }
  
  @Test
  public void cache_SplitAddress()
  {
    Triplet<Long, Integer, Integer> returnTriplet = cache.splitAddress(0xff00f3);
    Assert.assertEquals(3, (int) returnTriplet.getThird());
    Assert.assertEquals(7, (int) returnTriplet.getSecond());
    Assert.assertEquals(0x1fe01, (long) returnTriplet.getFirst());
    
    returnTriplet = cache.splitAddress(0x100080);
    Assert.assertEquals(0, (int) returnTriplet.getThird());
    Assert.assertEquals(0, (int) returnTriplet.getSecond());
    Assert.assertEquals(0x2001, (long) returnTriplet.getFirst());
  }
  
  @Test
  public void cache_BasicWriteAndRead()
  {
    // at clock 0, a store is scheduled. Execution is 1+1 clocks
    cache.scheduleTransaction(MemoryTransaction.store(128, new byte[]{0x04, 0x03, 0x02, 0x01}));
    simulateCycles(1, 2);
    // Delay is 2 (1+1), so first cycle, the transaction is scheduled, second cycle it is sent to memory, third cycle it is finished
    Assert.assertEquals(0x01020304, cache.getData(128, 4));
    // One cache line was loaded
    Assert.assertEquals(16, statistics.mainMemoryLoadedBytes);
  }
  
  /**
   * Simulate the cache and memory for a number of cycles.
   *
   * @param from  inclusive
   * @param count number of cycles to simulate
   */
  public void simulateCycles(int from, int count)
  {
    for (int i = from; i < from + count; i++)
    {
      memory.simulate(i);
      cache.simulate(i);
    }
  }
  
  @Test
  public void cache_HitRate()
  {
    Assert.assertEquals(0, statistics.cache.getHitRate(), 0.01);
    
    // At clock 0, somebody schedules a store. At clock 1 the cache starts working on it for 2 clocks
    MemoryTransaction store1 = MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    cache.scheduleTransaction(store1);
    simulateCycles(1, 2);
    cache.finishTransaction(store1.id());
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    // Mandatory cache miss
    Assert.assertEquals(0, statistics.cache.getHits());
    Assert.assertEquals(1, statistics.cache.getMisses());
    Assert.assertEquals(0, statistics.cache.getHitRate(), 0.01);
    
    // Second access to nearby address, in cache. Needs just 1 clock
    MemoryTransaction load1 = MemoryTransaction.load(132, 4, 2);
    cache.scheduleTransaction(load1);
    simulateCycles(3, 1);
    cache.finishTransaction(load1.id());
    Assert.assertEquals(1, statistics.cache.getHits());
    Assert.assertEquals(1, statistics.cache.getMisses());
    Assert.assertEquals(0.5, statistics.cache.getHitRate(), 0.01);
  }
  
  @Test
  public void cache_MisalignedWriteAndReadFollowedByAlignedRead()
  {
    cache.scheduleTransaction(MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23}));
    simulateCycles(1, 2);
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    Assert.assertEquals(0x00002345, cache.getData(132, 4));
  }
  
  @Test
  public void cache_bigDelay()
  {
    memory = new SimulatedMemory(10, 10, statistics);
    cache  = new Cache(memory, 16, 2, 16, 2, 2, ReplacementPoliciesEnum.RANDOM, true, statistics);
    
    // Start transaction at clock 0
    // It will take 10 clocks to load line from memory, then additional 2 clocks to store it to cache
    cache.scheduleTransaction(MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23}));
    simulateCycles(1, 9);
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(130, 4));
    simulateCycles(10, 1); // 10th clock, cache line loaded
    Assert.assertEquals(0, cache.getData(130, 4));
    simulateCycles(11, 1); // cache still working
    Assert.assertEquals(0, cache.getData(130, 4));
    simulateCycles(12, 1);
    
    // Cache store executed
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    Assert.assertEquals(0x00002345, cache.getData(132, 4));
  }
  
  @Test
  public void cache_RandomPolicyDeterministic()
  {
    int nOfTimesStore1IsKept = 0;
    int tries                = 10;
    
    for (int i = 0; i < tries; i++)
    {
      Cache c1 = new Cache(memory, 16, 2, 16, 1, 1, ReplacementPoliciesEnum.RANDOM, true, statistics);
      fillCacheGroupThreeTimes(c1);
      // The cache should contain either lines from store1 and store3, or store2 and store3
      boolean store1IsKept = true;
      try
      {
        long data = c1.getData(0, 1);
        assert data == 0x02;
      }
      catch (IllegalArgumentException e)
      {
        store1IsKept = false;
      }
      if (store1IsKept)
      {
        nOfTimesStore1IsKept++;
      }
    }
    
    // Either the line of the first store is always kept, or never
    // In my case it was zero (never kept), but that could change in the future
    Assert.assertTrue(nOfTimesStore1IsKept == 0 || nOfTimesStore1IsKept == tries);
  }
  
  /**
   * For a test
   *
   * @param c cache to mutate
   */
  public void fillCacheGroupThreeTimes(Cache c)
  {
    cache = c;
    int groupSpace = 8 * 16; // 8 groups of 16 bytes
    // line of 16B, 2 ways
    MemoryTransaction store1 = MemoryTransaction.store(0, new byte[]{(byte) 0x01});
    MemoryTransaction store2 = MemoryTransaction.store(groupSpace, new byte[]{(byte) 0x02});
    c.scheduleTransaction(store1);
    c.scheduleTransaction(store2);
    simulateCycles(1, 2);
    c.finishTransaction(store1.id());
    c.finishTransaction(store2.id());
    
    // The cache should be full now
    // The next store should replace one of the lines
    MemoryTransaction store3 = MemoryTransaction.store(2 * groupSpace, new byte[]{(byte) 0x03}, 2);
    c.scheduleTransaction(store3);
    simulateCycles(3, 2);
    c.finishTransaction(store3.id());
  }
  
  @Test
  public void cache_MisalignedReadStats()
  {
    MemoryTransaction load1 = MemoryTransaction.load(128 + 16 - 2, 4);
    cache.scheduleTransaction(load1);
    simulateCycles(1, 2);
    cache.finishTransaction(load1.id());
    
    // Should be 1 miss. That's simply the way it is counted
    Assert.assertEquals(1, statistics.cache.getMisses());
    Assert.assertEquals(2 * 16, statistics.mainMemoryLoadedBytes);
  }
  
  @Test
  public void cache_MixedAccesses()
  {
    MemoryTransaction store1 = MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    MemoryTransaction store2 = MemoryTransaction.store(134, new byte[]{0x22, 0x11});
    cache.scheduleTransaction(store1);
    cache.scheduleTransaction(store2);
    simulateCycles(1, 2);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    cache.finishTransaction(store2.id());
    
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    Assert.assertEquals(0x1122, cache.getData(134, 2));
    Assert.assertEquals(0x11222345, cache.getData(132, 4));
    
    cache.scheduleTransaction(MemoryTransaction.store(133, new byte[]{(byte) 0x88}, 2));
    cache.scheduleTransaction(MemoryTransaction.store(135, new byte[]{(byte) 0xcc}, 2));
    simulateCycles(3, 1);
    Assert.assertEquals(0xcc228845L, cache.getData(132, 4));
  }
  
  @Test
  public void cache_64b()
  {
    MemoryTransaction store1 = MemoryTransaction.store(128,
                                                       new byte[]{(byte) 0x89, 0x67, 0x45, 0x23, 0x66, 0x77, (byte) 0x88, (byte) 0x99});
    cache.scheduleTransaction(store1);
    simulateCycles(1, 2);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    
    Assert.assertEquals(0x23456789L, cache.getData(128, 4));
    Assert.assertEquals(0x99887766L, cache.getData(132, 4));
    cache.scheduleTransaction(MemoryTransaction.store(136, new byte[]{(byte) 0x33, 0x22, 0x12, 0x11}, 2));
    cache.scheduleTransaction(MemoryTransaction.store(140, new byte[]{(byte) 0x66, 0x55, 0x45, 0x44}, 2));
    simulateCycles(3, 1);
    Assert.assertEquals(0x4445556611122233L, cache.getData(136, 8));
  }
  
  @Test
  public void cache_32bMultipleCacheLines()
  {
    MemoryTransaction store1 = MemoryTransaction.store(126, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    cache.scheduleTransaction(store1);
    simulateCycles(1, 2);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    
    Assert.assertEquals(0x6789L, cache.getData(126, 2));
    Assert.assertEquals(0x2345L, cache.getData(128, 2));
  }
  
  @Test
  public void cache_lru()
  {
    // 4 lines TOTAL, 2 ways, 16 bytes per line.
    // This means that addresses 0, 32 (!) and 64 (!) are in the same set
    // Storing to 0 and 64, the cache should accommodate both
    cache = new Cache(memory, 4, 2, 16, 1, 1, ReplacementPoliciesEnum.LRU, true, statistics);
    
    //Store first line
    MemoryTransaction store1 = MemoryTransaction.store(0, new byte[]{(byte) 0x44, 0x33, 0x22, 0x11});
    MemoryTransaction store2 = MemoryTransaction.store(4, new byte[]{(byte) 0x88, 0x77, 0x66, 0x55});
    MemoryTransaction store3 = MemoryTransaction.store(8, new byte[]{(byte) 0x28, (byte) 0x82, 0x19, (byte) 0x91});
    MemoryTransaction store4 = MemoryTransaction.store(12, new byte[]{0x11, 0x44, 0x37, 0x73});
    cache.scheduleTransaction(store1);
    cache.scheduleTransaction(store2);
    cache.scheduleTransaction(store3);
    cache.scheduleTransaction(store4);
    //Store second line
    MemoryTransaction store5 = MemoryTransaction.store(32, new byte[]{0x33, 0x33, (byte) 0x97, 0x79});
    MemoryTransaction store6 = MemoryTransaction.store(36, new byte[]{0x45, 0x55, 0x32, 0x32});
    MemoryTransaction store7 = MemoryTransaction.store(40, new byte[]{0x66, 0x77, (byte) 0x97, (byte) 0x99});
    MemoryTransaction store8 = MemoryTransaction.store(44, new byte[]{0x63, (byte) 0x94, 0x32, (byte) 0x87});
    cache.scheduleTransaction(store5);
    cache.scheduleTransaction(store6);
    cache.scheduleTransaction(store7);
    cache.scheduleTransaction(store8);
    simulateCycles(1, 2);
    
    //Read first line
    Assert.assertEquals(0x11223344L, cache.getData(0, 4));
    Assert.assertEquals(0x55667788L, cache.getData(4, 4));
    Assert.assertEquals(0x91198228L, cache.getData(8, 4));
    Assert.assertEquals(0x73374411L, cache.getData(12, 4));
    //Read second line
    Assert.assertEquals(0x79973333L, cache.getData(32, 4));
    Assert.assertEquals(0x32325545L, cache.getData(36, 4));
    Assert.assertEquals(0x99977766L, cache.getData(40, 4));
    Assert.assertEquals(0x87329463L, cache.getData(44, 4));
    
    // Confirm stores
    cache.finishTransaction(store1.id());
    cache.finishTransaction(store2.id());
    cache.finishTransaction(store3.id());
    cache.finishTransaction(store4.id());
    cache.finishTransaction(store5.id());
    cache.finishTransaction(store6.id());
    cache.finishTransaction(store7.id());
    cache.finishTransaction(store8.id());
    
    //Store third line (should replace the first)
    MemoryTransaction store9  = MemoryTransaction.store(64, new byte[]{0x54, 0x23, (byte) 0x97, 0x31}, 2);
    MemoryTransaction store10 = MemoryTransaction.store(68, new byte[]{0x29, 0x43, (byte) 0x87, 0x65}, 2);
    cache.scheduleTransaction(store9);
    cache.scheduleTransaction(store10);
    simulateCycles(3, 2);
    
    //Read third line
    Assert.assertEquals(0x31972354L, cache.getData(64, 4));
    Assert.assertEquals(0x65874329L, cache.getData(68, 4));
    Assert.assertEquals(0x00000000L, cache.getData(72, 4));
    Assert.assertEquals(0x00000000L, cache.getData(76, 4));
    
    //Read second line (should still be loaded in cache - 1 delay)
    Assert.assertEquals(0x79973333L, cache.getData(32, 4));
    Assert.assertEquals(0x32325545L, cache.getData(36, 4));
    Assert.assertEquals(0x99977766L, cache.getData(40, 4));
    Assert.assertEquals(0x87329463L, cache.getData(44, 4));
    
    // Cannot read the first line from cache. It is not there anymore
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(0, 4));
  }
  
  @Test
  public void cache_fifo()
  {
    // 2 groups per 4 bytes
    // This means that addresses 0, 8, 16 are in the same set
    cache = new Cache(memory, 8, 4, 4, 1, 1, ReplacementPoliciesEnum.FIFO, true, statistics);
    
    MemoryTransaction store1 = MemoryTransaction.store(0, new byte[]{0x11, 0x22});
    cache.scheduleTransaction(store1);
    simulateCycles(1, 1);
    
    MemoryTransaction store2 = MemoryTransaction.store(8, new byte[]{0x33, 0x44}, 1);
    cache.scheduleTransaction(store2);
    simulateCycles(2, 1);
    cache.finishTransaction(store1.id());
    
    MemoryTransaction store3 = MemoryTransaction.store(16, new byte[]{0x55, 0x66}, 2);
    cache.scheduleTransaction(store3);
    simulateCycles(3, 1);
    cache.finishTransaction(store2.id());
    
    MemoryTransaction store4 = MemoryTransaction.store(24, new byte[]{0x77, (byte) 0x88}, 3);
    cache.scheduleTransaction(store4);
    simulateCycles(4, 1);
    cache.finishTransaction(store3.id());
    
    // Finish the transactions
    
    simulateCycles(5, 1);
    cache.finishTransaction(store4.id());
    
    // At this point, the associative set is full, all is accessible
    Assert.assertEquals(0x2211, cache.getData(0, 2));
    Assert.assertEquals(0x4433, cache.getData(8, 2));
    Assert.assertEquals(0x6655, cache.getData(16, 2));
    Assert.assertEquals(0x8877, cache.getData(24, 2));
    
    // Now lets store to the same address again
    MemoryTransaction store5 = MemoryTransaction.store(0, new byte[]{0x34, 0x12}, 5);
    cache.scheduleTransaction(store5);
    simulateCycles(6, 1); // 1 because it is already in cache
    cache.finishTransaction(store5.id());
    
    // The first line was written to
    Assert.assertEquals(0x1234, cache.getData(0, 2));
    
    // Let's store more data to the same group (address not in cache)
    MemoryTransaction store6 = MemoryTransaction.store(32, new byte[]{0x56, 0x78}, 7);
    cache.scheduleTransaction(store6);
    simulateCycles(8, 2); // 3 because memory delay
    cache.finishTransaction(store6.id());
    
    // The first line was written to, because it is the oldest
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(0, 2));
  }
  
  @Test
  public void cache_lruSmall_dataOnly()
  {
    // 2 lines, 2 ways, 4 bytes per line.
    // This means that every address is in the same set
    memory = new SimulatedMemory(1, 1, statistics);
    cache  = new Cache(memory, 2, 2, 4, 1, 1, ReplacementPoliciesEnum.LRU, true, statistics);
    
    // Store 4 lines. separated into two sets, because of bug in the cache
    MemoryTransaction store1 = MemoryTransaction.store(0, new byte[]{(byte) 0x44, 0x33, 0x22, 0x11});
    MemoryTransaction store2 = MemoryTransaction.store(4, new byte[]{(byte) 0x88, 0x77, 0x66, 0x55});
    cache.scheduleTransaction(store1);
    cache.scheduleTransaction(store2);
    simulateCycles(1, 2);
    
    cache.finishTransaction(store1.id());
    cache.finishTransaction(store2.id());
    
    MemoryTransaction store3 = MemoryTransaction.store(8, new byte[]{(byte) 0x28, (byte) 0x82, 0x19, (byte) 0x91}, 2);
    MemoryTransaction store4 = MemoryTransaction.store(12, new byte[]{0x11, 0x44, 0x37, 0x73}, 2);
    cache.scheduleTransaction(store3);
    cache.scheduleTransaction(store4);
    
    simulateCycles(3, 2);
    
    //Read them
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(0, 4));
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(4, 4));
    //    Assert.assertEquals(0x11223344L, cache.getData(0, 4));
    //    Assert.assertEquals(0x55667788L, cache.getData(4, 4));
    Assert.assertEquals(0x91198228L, cache.getData(8, 4));
    Assert.assertEquals(0x73374411L, cache.getData(12, 4));
    
    // The addresses 0-7 should be back in main memory, updated
    Assert.assertEquals((byte) 0x44, memory.getFromMemory(0L));
    Assert.assertEquals((byte) 0x33, memory.getFromMemory(1L));
    Assert.assertEquals((byte) 0x22, memory.getFromMemory(2L));
    Assert.assertEquals((byte) 0x11, memory.getFromMemory(3L));
    Assert.assertEquals((byte) 0x88, memory.getFromMemory(4L));
    Assert.assertEquals((byte) 0x77, memory.getFromMemory(5L));
    Assert.assertEquals((byte) 0x66, memory.getFromMemory(6L));
    Assert.assertEquals((byte) 0x55, memory.getFromMemory(7L));
    Assert.assertEquals((byte) 0, memory.getFromMemory(8L));
    Assert.assertEquals((byte) 0, memory.getFromMemory(9L));
    Assert.assertEquals((byte) 0, memory.getFromMemory(10L));
    Assert.assertEquals((byte) 0, memory.getFromMemory(11L));
  }
  
  /**
   * Concurrent requests test.
   * Multiple MAUs, or a retiring store while a load is executing.
   */
  @Test
  public void cache_ConcurrentRequests()
  {
    // at clock 0, a store is scheduled. Execution is 1+1 clocks
    MemoryTransaction store1 = MemoryTransaction.store(128, new byte[]{0x04, 0x03, 0x02, 0x01});
    cache.scheduleTransaction(store1);
    simulateCycles(1, 1);
    MemoryTransaction load1  = MemoryTransaction.load(64, 4, 1);
    MemoryTransaction store2 = MemoryTransaction.store(64, new byte[]{0x05, 0x06, 0x07, 0x08}, 1);
    cache.scheduleTransaction(store2);
    cache.scheduleTransaction(load1);
    simulateCycles(2, 1);
    cache.finishTransaction(store1.id());
    simulateCycles(3, 1);
    cache.finishTransaction(load1.id());
    cache.finishTransaction(store2.id());
    
    // What should happen? An open question, so if you want to, change this test
    // Right now it depends on the order of scheduling the transactions
    
    Assert.assertEquals(0x08070605, cache.getData(64, 4));
    Assert.assertEquals(0x01020304, cache.getData(128, 4));
    
    // The load should have loaded the line from memory
    boolean equals = Arrays.equals(new byte[]{0x05, 0x06, 0x07, 0x08}, load1.data());
    Assert.assertTrue(equals);
  }
  
  
}
