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
  
  /**
   * Set up the cache and memory.
   */
  @Before
  public void setup()
  {
    memory = new SimulatedMemory(0, 0);
    cache  = new Cache(memory, 16, 2, 16, 1, 1, ReplacementPoliciesEnum.RANDOM, true, new SimulationStatistics(0, 1));
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
    simulateCycles(0, 2);
    // Delay is 1 (memory has no delay), so first cycle, the transaction is scheduled, second cycle it is finished
    Assert.assertEquals(0x01020304, cache.getData(128, 4));
  }
  
  @Test
  public void cache_MisalignedWriteAndReadFollowedByAlignedRead()
  {
    cache.scheduleTransaction(MemoryTransaction.store(130, new byte[]{(byte) 0x89, 0x67, 0x45, 0x23}));
    simulateCycles(0, 2);
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
    simulateCycles(0, 2);
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
    simulateCycles(0, 2);
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
    simulateCycles(0, 2);
    // The consumer of the cache takes the results (mandatory)
    cache.finishTransaction(store1.id());
    
    Assert.assertEquals(0x6789L, cache.getData(126, 2));
    Assert.assertEquals(0x2345L, cache.getData(128, 2));
  }
  
  @Test
  public void cache_lru()
  {
    cache = new Cache(memory, 4, 2, 16, 1, 1, ReplacementPoliciesEnum.LRU, true, new SimulationStatistics(0, 1));
    
    //Store first line
    cache.scheduleTransaction(MemoryTransaction.store(0, new byte[]{(byte) 0x44, 0x33, 0x22, 0x11}));
    cache.scheduleTransaction(MemoryTransaction.store(4, new byte[]{(byte) 0x88, 0x77, 0x66, 0x55}));
    cache.scheduleTransaction(MemoryTransaction.store(8, new byte[]{(byte) 0x28, (byte) 0x82, 0x19, (byte) 0x91}));
    cache.scheduleTransaction(MemoryTransaction.store(12, new byte[]{0x11, 0x44, 0x37, 0x73}));
    //Store second line
    cache.scheduleTransaction(MemoryTransaction.store(32, new byte[]{0x33, 0x33, (byte) 0x97, 0x79}));
    cache.scheduleTransaction(MemoryTransaction.store(36, new byte[]{0x45, 0x55, 0x32, 0x32}));
    cache.scheduleTransaction(MemoryTransaction.store(40, new byte[]{0x66, 0x77, (byte) 0x97, (byte) 0x99}));
    cache.scheduleTransaction(MemoryTransaction.store(44, new byte[]{0x63, (byte) 0x94, 0x32, (byte) 0x87}));
    simulateCycles(0, 2);
    
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
    
    //Store third line (should replace the first)
    cache.scheduleTransaction(MemoryTransaction.store(64, new byte[]{0x54, 0x23, (byte) 0x97, 0x31}));
    cache.scheduleTransaction(MemoryTransaction.store(68, new byte[]{0x29, 0x43, (byte) 0x87, 0x65}));
    simulateCycles(2, 1);
    
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
    
    //Read the first line
    Assert.assertEquals(0x11223344L, cache.getData(0, 4));
    Assert.assertEquals(0x55667788L, cache.getData(4, 4));
    Assert.assertEquals(0x91198228L, cache.getData(8, 4));
    Assert.assertEquals(0x73374411L, cache.getData(12, 4));
  }
  
  @Test
  public void cache_lruSmall_dataOnly()
  {
    memory = new SimulatedMemory(0, 0);
    cache  = new Cache(memory, 2, 2, 4, 1, 1, ReplacementPoliciesEnum.LRU, true, new SimulationStatistics(0, 1));
    
    //Store 4 lines
    cache.scheduleTransaction(MemoryTransaction.store(0, new byte[]{(byte) 0x44, 0x33, 0x22, 0x11}));
    cache.scheduleTransaction(MemoryTransaction.store(4, new byte[]{(byte) 0x88, 0x77, 0x66, 0x55}));
    cache.scheduleTransaction(MemoryTransaction.store(8, new byte[]{(byte) 0x28, (byte) 0x82, 0x19, (byte) 0x91}));
    cache.scheduleTransaction(MemoryTransaction.store(12, new byte[]{0x11, 0x44, 0x37, 0x73}));
    simulateCycles(0, 2);
    
    //Read them
    Assert.assertEquals(0x11223344L, cache.getData(0, 4));
    Assert.assertEquals(0x55667788L, cache.getData(4, 4));
    Assert.assertEquals(0x91198228L, cache.getData(8, 4));
    Assert.assertEquals(0x73374411L, cache.getData(12, 4));
    
    //Store 4 lines
    cache.scheduleTransaction(MemoryTransaction.store(16, new byte[]{0x66, 0x55, 0x77, 0x33}, 1));
    cache.scheduleTransaction(MemoryTransaction.store(20, new byte[]{0x44, 0x66, (byte) 0x99, 0x11}, 1));
    cache.scheduleTransaction(MemoryTransaction.store(24, new byte[]{0x46, 0x13, 0x13, 0x13}, 1));
    cache.scheduleTransaction(MemoryTransaction.store(28, new byte[]{0x64, 0x78, 0x79, (byte) 0x98}, 1));
    cache.simulate(1);
    
    //Read them
    Assert.assertEquals(0x33775566L, cache.getData(16, 4));
    Assert.assertEquals(0x11996644L, cache.getData(20, 4));
    Assert.assertEquals(0x13131346L, cache.getData(24, 4));
    Assert.assertEquals(0x98797864L, cache.getData(28, 4));
    
    //Read the first 4 lines
    Assert.assertEquals(0x11223344L, cache.getData(0, 4));
    Assert.assertEquals(0x55667788L, cache.getData(4, 4));
    Assert.assertEquals(0x91198228L, cache.getData(8, 4));
    Assert.assertEquals(0x73374411L, cache.getData(12, 4));
    
    //Check memory
    Assert.assertEquals((byte) 0x44, memory.getFromMemory(0L));
    Assert.assertEquals((byte) 0x33, memory.getFromMemory(1L));
    Assert.assertEquals((byte) 0x22, memory.getFromMemory(2L));
    Assert.assertEquals((byte) 0x11, memory.getFromMemory(3L));
    Assert.assertEquals((byte) 0x88, memory.getFromMemory(4L));
    Assert.assertEquals((byte) 0x77, memory.getFromMemory(5L));
    Assert.assertEquals((byte) 0x66, memory.getFromMemory(6L));
    Assert.assertEquals((byte) 0x55, memory.getFromMemory(7L));
    Assert.assertEquals((byte) 0x28, memory.getFromMemory(8L));
    Assert.assertEquals((byte) 0x82, memory.getFromMemory(9L));
    Assert.assertEquals((byte) 0x19, memory.getFromMemory(10L));
    Assert.assertEquals((byte) 0x91, memory.getFromMemory(11L));
    Assert.assertEquals((byte) 0x11, memory.getFromMemory(12L));
    Assert.assertEquals((byte) 0x44, memory.getFromMemory(13L));
    Assert.assertEquals((byte) 0x37, memory.getFromMemory(14L));
    Assert.assertEquals((byte) 0x73, memory.getFromMemory(15L));
    Assert.assertEquals((byte) 0x66, memory.getFromMemory(16L));
    Assert.assertEquals((byte) 0x55, memory.getFromMemory(17L));
    Assert.assertEquals((byte) 0x77, memory.getFromMemory(18L));
    Assert.assertEquals((byte) 0x33, memory.getFromMemory(19L));
    Assert.assertEquals((byte) 0x44, memory.getFromMemory(20L));
    Assert.assertEquals((byte) 0x66, memory.getFromMemory(21L));
    Assert.assertEquals((byte) 0x99, memory.getFromMemory(22L));
    Assert.assertEquals((byte) 0x11, memory.getFromMemory(23L));
    Assert.assertEquals((byte) 0x46, memory.getFromMemory(24L));
    Assert.assertEquals((byte) 0x13, memory.getFromMemory(25L));
    Assert.assertEquals((byte) 0x13, memory.getFromMemory(26L));
    Assert.assertEquals((byte) 0x13, memory.getFromMemory(27L));
    Assert.assertEquals((byte) 0x64, memory.getFromMemory(28L));
    Assert.assertEquals((byte) 0x78, memory.getFromMemory(29L));
    Assert.assertEquals((byte) 0x79, memory.getFromMemory(30L));
    Assert.assertEquals((byte) 0x98, memory.getFromMemory(31L));
  }
}
