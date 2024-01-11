package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.loadstore.Cache;
import com.gradle.superscalarsim.code.SimulatedMemory;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.Pair;
import com.gradle.superscalarsim.models.Triplet;
import org.junit.Assert;
import org.junit.Test;

public class CacheTest
{
  Cache cache;
  SimulatedMemory memory;
  
  @Test
  public void cache_SplitAddress()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
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
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    cache.storeData(128, 0x01020304, 4, 0, 0);
    Assert.assertEquals(0x01020304, (long) cache.getData(128, 4, 0, 0).getSecond());
  }
  
  @Test
  public void cache_BasicReadWriteAndRead()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    Assert.assertEquals(0, (long) cache.getData(128, 4, 0, 0).getSecond());
    cache.storeData(128, 123456789L, 4, 0, 0);
    Assert.assertEquals(123456789L, (long) cache.getData(128, 4, 0, 0).getSecond());
  }
  
  @Test
  public void cache_BasicReplaceReadWriteAndRead()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    Assert.assertEquals(0, (long) cache.getData(128, 4, 0, 0).getSecond());
    cache.storeData(128, 123456789L, 4, 0, 0);
    Assert.assertEquals(123456789L, (long) cache.getData(128, 4, 0, 0).getSecond());
    Assert.assertEquals(0L, (long) cache.getData(1048704, 4, 0, 0).getSecond());
  }
  
  @Test
  public void cache_MisalignedWriteAndReadFollowedByAlignedRead()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    Assert.assertEquals(0, (long) cache.getData(130, 4, 0, 0).getSecond());
    cache.storeData(130, 0x23456789L, 4, 0, 0);
    Assert.assertEquals(0x23456789, (long) cache.getData(130, 4, 0, 0).getSecond());
    Assert.assertEquals(0x00002345, (long) cache.getData(132, 4, 0, 0).getSecond());
  }
  
  @Test
  public void cache_MixedAccesses()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    Assert.assertEquals(0, (long) cache.getData(130, 4, 0, 0).getSecond());
    cache.storeData(130, 0x23456789L, 4, 0, 0);
    cache.storeData(134, 0x1122L, 2, 0, 0);
    Assert.assertEquals(0x23456789, (long) cache.getData(130, 4, 0, 0).getSecond());
    Assert.assertEquals(0x1122, (long) cache.getData(134, 2, 0, 0).getSecond());
    Assert.assertEquals(0x11222345, (long) cache.getData(132, 4, 0, 0).getSecond());
    cache.storeData(133, 0x88L, 1, 0, 0);
    cache.storeData(135, 0xccL, 1, 0, 0);
    Assert.assertEquals(0xcc228845L, (long) cache.getData(132, 4, 0, 0).getSecond());
  }
  
  @Test
  public void cache_64b()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    cache.storeData(128, 0x9988776623456789L, 8, 0, 0);
    Assert.assertEquals(0x23456789L, (long) cache.getData(128, 4, 0, 0).getSecond());
    Assert.assertEquals(0x99887766L, (long) cache.getData(132, 4, 0, 0).getSecond());
    cache.storeData(136, 0x11122233L, 4, 0, 0);
    cache.storeData(140, 0x44455566L, 4, 0, 0);
    Assert.assertEquals(0x4445556611122233L, (long) cache.getData(136, 8, 0, 0).getSecond());
  }
  
  @Test
  public void cache_32bMultipleCacheLines()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 16, 2, 16, ReplacementPoliciesEnum.RANDOM, true, true, 1, 1, 1,
                       new SimulationStatistics(0));
    cache.storeData(126, 0x23456789L, 4, 0, 0);
    Assert.assertEquals(0x23456789L, (long) cache.getData(126, 4, 0, 0).getSecond());
    Assert.assertEquals(0x6789L, (long) cache.getData(126, 2, 0, 0).getSecond());
    Assert.assertEquals(0x2345L, (long) cache.getData(128, 2, 0, 0).getSecond());
  }
  
  @Test
  public void cache_lru()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 4, 2, 16, ReplacementPoliciesEnum.LRU, true, false, 1, 1, 10,
                       new SimulationStatistics(0));
    
    
    //Store first line
    Assert.assertEquals(1, cache.storeData(0, 0x11223344L, 4, 1, 1));
    Assert.assertEquals(1, cache.storeData(4, 0x55667788L, 4, 2, 4));
    Assert.assertEquals(1, cache.storeData(8, 0x91198228L, 4, 3, 7));
    Assert.assertEquals(1, cache.storeData(12, 0x73374411L, 4, 4, 10));
    //Store second line
    Assert.assertEquals(1, cache.storeData(32, 0x79973333L, 4, 5, 13));
    Assert.assertEquals(1, cache.storeData(36, 0x32325545L, 4, 6, 16));
    Assert.assertEquals(1, cache.storeData(40, 0x99977766L, 4, 7, 18));
    Assert.assertEquals(1, cache.storeData(44, 0x87329463L, 4, 8, 22));
    
    //Read first line
    Assert.assertEquals(new Pair<>(1, 0x11223344L), cache.getData(0, 4, 9, 23));
    Assert.assertEquals(new Pair<>(1, 0x55667788L), cache.getData(4, 4, 10, 24));
    Assert.assertEquals(new Pair<>(1, 0x91198228L), cache.getData(8, 4, 11, 25));
    Assert.assertEquals(new Pair<>(1, 0x73374411L), cache.getData(12, 4, 12, 26));
    //Read second line
    Assert.assertEquals(new Pair<>(1, 0x79973333L), cache.getData(32, 4, 13, 27));
    Assert.assertEquals(new Pair<>(1, 0x32325545L), cache.getData(36, 4, 14, 28));
    Assert.assertEquals(new Pair<>(1, 0x99977766L), cache.getData(40, 4, 15, 29));
    Assert.assertEquals(new Pair<>(1, 0x87329463L), cache.getData(44, 4, 16, 30));
    
    //Store third line (should replace the first)
    Assert.assertEquals(1, cache.storeData(64, 0x31972354L, 4, 17, 33));
    Assert.assertEquals(1, cache.storeData(68, 0x65874329L, 4, 18, 35));
    
    //Read third line
    Assert.assertEquals(new Pair<>(4, 0x31972354L), cache.getData(64, 4, 19, 40));
    Assert.assertEquals(new Pair<>(2, 0x65874329L), cache.getData(68, 4, 20, 42));
    Assert.assertEquals(new Pair<>(1, 0x00000000L), cache.getData(72, 4, 21, 44));
    Assert.assertEquals(new Pair<>(1, 0x00000000L), cache.getData(76, 4, 22, 45));
    
    //Read second line (should still be loaded in cache - 1 delay)
    Assert.assertEquals(new Pair<>(1, 0x79973333L), cache.getData(32, 4, 13, 46));
    Assert.assertEquals(new Pair<>(1, 0x32325545L), cache.getData(36, 4, 14, 48));
    Assert.assertEquals(new Pair<>(1, 0x99977766L), cache.getData(40, 4, 15, 49));
    Assert.assertEquals(new Pair<>(1, 0x87329463L), cache.getData(44, 4, 16, 52));
    
    //Read the first line
    Assert.assertEquals(new Pair<>(11, 0x11223344L), cache.getData(0, 4, 23, 53));
    Assert.assertEquals(new Pair<>(9, 0x55667788L), cache.getData(4, 4, 24, 55));
    Assert.assertEquals(new Pair<>(8, 0x91198228L), cache.getData(8, 4, 25, 56));
    Assert.assertEquals(new Pair<>(6, 0x73374411L), cache.getData(12, 4, 26, 58));
  }
  
  @Test
  public void cache_lruSmall_dataOnly()
  {
    memory = new SimulatedMemory();
    cache  = new Cache(memory, 2, 2, 4, ReplacementPoliciesEnum.LRU, true, false, 10, 10, 0,
                       new SimulationStatistics(0));
    
    //Store 4 lines
    cache.storeData(0, 0x11223344L, 4, 1, 1);
    cache.storeData(4, 0x55667788L, 4, 2, 4);
    cache.storeData(8, 0x91198228L, 4, 3, 7);
    cache.storeData(12, 0x73374411L, 4, 4, 10);
    
    //Read them
    Assert.assertEquals(0x11223344L, (long) cache.getData(0, 4, 9, 23).getSecond());
    Assert.assertEquals(0x55667788L, (long) cache.getData(4, 4, 10, 24).getSecond());
    Assert.assertEquals(0x91198228L, (long) cache.getData(8, 4, 11, 25).getSecond());
    Assert.assertEquals(0x73374411L, (long) cache.getData(12, 4, 12, 26).getSecond());
    
    //Store 4 lines
    cache.storeData(16, 0x33775566L, 4, 1, 1);
    cache.storeData(20, 0x11996644L, 4, 2, 4);
    cache.storeData(24, 0x13131346L, 4, 3, 7);
    cache.storeData(28, 0x98797864L, 4, 4, 10);
    
    //Read them
    Assert.assertEquals(0x33775566L, (long) cache.getData(16, 4, 9, 23).getSecond());
    Assert.assertEquals(0x11996644L, (long) cache.getData(20, 4, 10, 24).getSecond());
    Assert.assertEquals(0x13131346L, (long) cache.getData(24, 4, 11, 25).getSecond());
    Assert.assertEquals(0x98797864L, (long) cache.getData(28, 4, 12, 26).getSecond());
    
    //Read the first 4 lines
    Assert.assertEquals(0x11223344L, (long) cache.getData(0, 4, 9, 23).getSecond());
    Assert.assertEquals(0x55667788L, (long) cache.getData(4, 4, 10, 24).getSecond());
    Assert.assertEquals(0x91198228L, (long) cache.getData(8, 4, 11, 25).getSecond());
    Assert.assertEquals(0x73374411L, (long) cache.getData(12, 4, 12, 26).getSecond());
    
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
