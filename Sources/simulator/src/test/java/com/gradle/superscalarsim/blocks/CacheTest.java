package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.loadstore.Cache;
import com.gradle.superscalarsim.blocks.loadstore.SimulatedMemory;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.Triplet;
import com.gradle.superscalarsim.models.memory.MemoryTransaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    cache.scheduleTransaction(MemoryTransaction.store(128, new byte[]{0x04, 0x03, 0x02, 0x01}));
    simulateCycles(0, 3);
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
    
    MemoryTransaction store1 = MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    cache.scheduleTransaction(store1);
    simulateCycles(0, 3);
    cache.finishTransaction(store1.id());
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    // Mandatory cache miss
    Assert.assertEquals(0, statistics.cache.getHits());
    Assert.assertEquals(1, statistics.cache.getMisses());
    Assert.assertEquals(0, statistics.cache.getHitRate(), 0.01);
    
    // Second access to nearby address, in cache
    MemoryTransaction load1 = MemoryTransaction.load(132, 4, 3);
    cache.scheduleTransaction(load1);
    simulateCycles(3, 2);
    cache.finishTransaction(load1.id());
    Assert.assertEquals(1, statistics.cache.getHits());
    Assert.assertEquals(1, statistics.cache.getMisses());
    Assert.assertEquals(0.5, statistics.cache.getHitRate(), 0.01);
  }
  
  @Test
  public void cache_MisalignedWriteAndReadFollowedByAlignedRead()
  {
    cache.scheduleTransaction(MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23}));
    simulateCycles(0, 3);
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
    simulateCycles(0, 10);
    Assert.assertThrows(IllegalArgumentException.class, () -> cache.getData(130, 4));
    simulateCycles(10, 1);
    Assert.assertEquals(0, cache.getData(130, 4));
    simulateCycles(11, 1);
    Assert.assertEquals(0, cache.getData(130, 4));
    simulateCycles(12, 1);
    
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    Assert.assertEquals(0x00002345, cache.getData(132, 4));
  }
  
  @Test
  public void cache_MixedAccesses()
  {
    MemoryTransaction store1 = MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    MemoryTransaction store2 = MemoryTransaction.store(134, new byte[]{0x22, 0x11});
    cache.scheduleTransaction(store1);
    cache.scheduleTransaction(store2);
    simulateCycles(0, 3);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    cache.finishTransaction(store2.id());
    
    Assert.assertEquals(0x23456789, cache.getData(130, 4));
    Assert.assertEquals(0x1122, cache.getData(134, 2));
    Assert.assertEquals(0x11222345, cache.getData(132, 4));
    
    cache.scheduleTransaction(MemoryTransaction.store(133, new byte[]{(byte) 0x88}, 1));
    cache.scheduleTransaction(MemoryTransaction.store(135, new byte[]{(byte) 0xcc}, 1));
    simulateCycles(2, 1);
    Assert.assertEquals(0xcc228845L, cache.getData(132, 4));
  }
  
  @Test
  public void cache_64b()
  {
    MemoryTransaction store1 = MemoryTransaction.store(128,
                                                       new byte[]{(byte) 0x89, 0x67, 0x45, 0x23, 0x66, 0x77, (byte) 0x88, (byte) 0x99});
    cache.scheduleTransaction(store1);
    simulateCycles(0, 3);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    
    Assert.assertEquals(0x23456789L, cache.getData(128, 4));
    Assert.assertEquals(0x99887766L, cache.getData(132, 4));
    cache.scheduleTransaction(MemoryTransaction.store(136, new byte[]{(byte) 0x33, 0x22, 0x12, 0x11}, 1));
    cache.scheduleTransaction(MemoryTransaction.store(140, new byte[]{(byte) 0x66, 0x55, 0x45, 0x44}, 1));
    simulateCycles(2, 1);
    Assert.assertEquals(0x4445556611122233L, cache.getData(136, 8));
  }
  
  @Test
  public void cache_32bMultipleCacheLines()
  {
    MemoryTransaction store1 = MemoryTransaction.store(126, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23});
    cache.scheduleTransaction(store1);
    simulateCycles(0, 3);
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
    simulateCycles(0, 3);
    
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
    MemoryTransaction store9  = MemoryTransaction.store(64, new byte[]{0x54, 0x23, (byte) 0x97, 0x31}, 3);
    MemoryTransaction store10 = MemoryTransaction.store(68, new byte[]{0x29, 0x43, (byte) 0x87, 0x65}, 3);
    cache.scheduleTransaction(store9);
    cache.scheduleTransaction(store10);
    simulateCycles(3, 3);
    
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
    simulateCycles(0, 1);
    
    MemoryTransaction store2 = MemoryTransaction.store(8, new byte[]{0x33, 0x44}, 1);
    cache.scheduleTransaction(store2);
    simulateCycles(1, 1);
    
    MemoryTransaction store3 = MemoryTransaction.store(16, new byte[]{0x55, 0x66}, 2);
    cache.scheduleTransaction(store3);
    simulateCycles(2, 1);
    cache.finishTransaction(store1.id());
    
    MemoryTransaction store4 = MemoryTransaction.store(24, new byte[]{0x77, (byte) 0x88}, 3);
    cache.scheduleTransaction(store4);
    simulateCycles(3, 1);
    cache.finishTransaction(store2.id());
    
    // Finish the transactions
    
    simulateCycles(4, 1);
    cache.finishTransaction(store3.id());
    
    simulateCycles(5, 1);
    cache.finishTransaction(store4.id());
    
    // At this point, the associative set is full, all is accessible
    Assert.assertEquals(0x2211, cache.getData(0, 2));
    Assert.assertEquals(0x4433, cache.getData(8, 2));
    Assert.assertEquals(0x6655, cache.getData(16, 2));
    Assert.assertEquals(0x8877, cache.getData(24, 2));
    
    // Now lets store to the same address again
    MemoryTransaction store5 = MemoryTransaction.store(0, new byte[]{0x34, 0x12}, 6);
    cache.scheduleTransaction(store5);
    simulateCycles(6, 2); // 2 because it is already in cache
    cache.finishTransaction(store5.id());
    
    // The first line was written to
    Assert.assertEquals(0x1234, cache.getData(0, 2));
    
    // Let's store more data to the same group (address not in cache)
    MemoryTransaction store6 = MemoryTransaction.store(32, new byte[]{0x56, 0x78}, 8);
    cache.scheduleTransaction(store6);
    simulateCycles(8, 3); // 3 because memory delay
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
    simulateCycles(0, 3);
    
    cache.finishTransaction(store1.id());
    cache.finishTransaction(store2.id());
    
    MemoryTransaction store3 = MemoryTransaction.store(8, new byte[]{(byte) 0x28, (byte) 0x82, 0x19, (byte) 0x91}, 3);
    MemoryTransaction store4 = MemoryTransaction.store(12, new byte[]{0x11, 0x44, 0x37, 0x73}, 3);
    cache.scheduleTransaction(store3);
    cache.scheduleTransaction(store4);
    
    simulateCycles(3, 3);
    
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
}
