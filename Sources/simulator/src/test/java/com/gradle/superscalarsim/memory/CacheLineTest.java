package com.gradle.superscalarsim.memory;

import com.gradle.superscalarsim.models.cache.CacheLineModel;
import org.junit.Assert;
import org.junit.Test;

public class CacheLineTest
{
  
  CacheLineModel cacheLine;
  
  @Test
  public void cacheLine_integer()
  {
    cacheLine = new CacheLineModel(16, 1);
    
    cacheLine.setData(0, 4, 987654321);
    cacheLine.setData(8, 4, 123456789);
    Assert.assertEquals(987654321, cacheLine.getData(0, 4));
    Assert.assertEquals(123456789, cacheLine.getData(8, 4));
    Assert.assertEquals(0, cacheLine.getData(4, 4));
    Assert.assertEquals(0, cacheLine.getData(12, 4));
    
    cacheLine.setData(0, 4, 222233333);
    cacheLine.setData(8, 4, 555577777);
    Assert.assertEquals(222233333, cacheLine.getData(0, 4));
    Assert.assertEquals(555577777, cacheLine.getData(8, 4));
    Assert.assertEquals(0, cacheLine.getData(4, 4));
    Assert.assertEquals(0, cacheLine.getData(12, 4));
  }
  
  @Test
  public void cacheLine_half()
  {
    cacheLine = new CacheLineModel(16, 2);
    
    cacheLine.setData(0, 2, 12345);
    cacheLine.setData(2, 2, 33333);
    cacheLine.setData(4, 2, 11114);
    cacheLine.setData(6, 2, 54321);
    cacheLine.setData(8, 2, 44444);
    cacheLine.setData(14, 2, 56789);
    Assert.assertEquals(12345, cacheLine.getData(0, 2));
    Assert.assertEquals(33333, cacheLine.getData(2, 2));
    Assert.assertEquals(11114, cacheLine.getData(4, 2));
    Assert.assertEquals(54321, cacheLine.getData(6, 2));
    Assert.assertEquals(44444, cacheLine.getData(8, 2));
    Assert.assertEquals(56789, cacheLine.getData(14, 2));
    Assert.assertEquals(0, cacheLine.getData(10, 2));
    Assert.assertEquals(0, cacheLine.getData(12, 2));
    
    cacheLine.setData(0, 2, 22222);
    cacheLine.setData(2, 2, 55555);
    Assert.assertEquals(22222, cacheLine.getData(0, 2));
    Assert.assertEquals(55555, cacheLine.getData(2, 2));
  }
  
  @Test
  public void cacheLine_byte()
  {
    cacheLine = new CacheLineModel(16, 3);
    
    cacheLine.setData(0, 1, 123);
    cacheLine.setData(3, 1, 54);
    cacheLine.setData(8, 1, 44);
    cacheLine.setData(11, 1, 78);
    cacheLine.setData(12, 1, 12);
    cacheLine.setData(15, 1, 32);
    Assert.assertEquals(123, cacheLine.getData(0, 1));
    Assert.assertEquals(54, cacheLine.getData(3, 1));
    Assert.assertEquals(44, cacheLine.getData(8, 1));
    Assert.assertEquals(78, cacheLine.getData(11, 1));
    Assert.assertEquals(12, cacheLine.getData(12, 1));
    Assert.assertEquals(32, cacheLine.getData(15, 1));
    Assert.assertEquals(0, cacheLine.getData(1, 1));
    Assert.assertEquals(0, cacheLine.getData(2, 1));
    Assert.assertEquals(0, cacheLine.getData(4, 1));
    Assert.assertEquals(0, cacheLine.getData(5, 1));
    Assert.assertEquals(0, cacheLine.getData(6, 1));
    Assert.assertEquals(0, cacheLine.getData(7, 1));
    Assert.assertEquals(0, cacheLine.getData(9, 1));
    Assert.assertEquals(0, cacheLine.getData(10, 1));
    Assert.assertEquals(0, cacheLine.getData(13, 1));
    Assert.assertEquals(0, cacheLine.getData(14, 1));
    
    cacheLine.setData(0, 1, 211);
    cacheLine.setData(1, 1, 123);
    Assert.assertEquals(211, cacheLine.getData(0, 1));
    Assert.assertEquals(123, cacheLine.getData(1, 1));
    Assert.assertEquals(0, cacheLine.getData(2, 1));
    Assert.assertEquals(54, cacheLine.getData(3, 1));
    cacheLine.setData(12, 1, 79);
    cacheLine.setData(15, 1, 200);
    Assert.assertEquals(79, cacheLine.getData(12, 1));
    Assert.assertEquals(200, cacheLine.getData(15, 1));
    Assert.assertEquals(0, cacheLine.getData(13, 1));
    Assert.assertEquals(0, cacheLine.getData(14, 1));
  }
  
}
