package com.gradle.superscalarsim.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.serialization.Serialization;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Deserialization of the MemoryLocation class
 */
public class MemoryLocationTests
{
  ObjectMapper deserializer;
  
  /**
   * Setup creates a deserializer
   */
  @Before
  public void setup()
  {
    deserializer = Serialization.getDeserializer();
  }
  
  /**
   * Test default deserialization of a MemoryLocation
   */
  @Test
  public void testDefaultDeserialization() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/basic.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    
    Assert.assertNotNull(memoryLocations);
    Assert.assertEquals(1, memoryLocations.size());
    
    MemoryLocation ml = memoryLocations.get(0);
    Assert.assertNotNull(ml.names);
    Assert.assertEquals(1, ml.names.size());
    Assert.assertEquals("Array", ml.names.get(0));
    Assert.assertEquals(8, ml.alignment);
    
    Assert.assertNotNull(ml.dataTypes);
    Assert.assertEquals(1, ml.dataTypes.size());
    Assert.assertEquals(DataTypeEnum.kInt, ml.dataTypes.get(0).dataType());
    Assert.assertEquals(0, ml.dataTypes.get(0).startOffset());
    
    Assert.assertNotNull(ml.data);
    Assert.assertEquals(6, ml.data.size());
    Assert.assertEquals("0", ml.data.get(0));
    Assert.assertEquals("1", ml.data.get(1));
    Assert.assertEquals("2", ml.data.get(2));
    Assert.assertEquals("3", ml.data.get(3));
    Assert.assertEquals("4", ml.data.get(4));
    Assert.assertEquals("5", ml.data.get(5));
  }
  
  /**
   * name.json is the same as basic.json, with name expressed differently
   */
  @Test
  public void testName() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/basic.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    MemoryLocation ml = memoryLocations.get(0);
    
    String json2 = loadTestResource("/memoryLocations/name.json");
    List<MemoryLocation> memoryLocations2 = deserializer.readValue(json2, new TypeReference<>()
    {
    });
    MemoryLocation ml2 = memoryLocations2.get(0);
    
    // Assert. Compare by value
    Assert.assertEquals(ml.names.get(0), ml2.names.get(0));
    Assert.assertEquals(ml.alignment, ml2.alignment);
    Assert.assertEquals(ml.dataTypes.get(0).dataType(), ml2.dataTypes.get(0).dataType());
    Assert.assertEquals(ml.dataTypes.get(0).startOffset(), ml2.dataTypes.get(0).startOffset());
    Assert.assertEquals(ml.data.get(0), ml2.data.get(0));
    Assert.assertEquals(ml.data.get(1), ml2.data.get(1));
  }
  
  /**
   * dataType.json is the same as basic.json, with dataType expressed differently
   */
  @Test
  public void testDataType() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/basic.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    MemoryLocation ml = memoryLocations.get(0);
    
    String json2 = loadTestResource("/memoryLocations/dataType.json");
    List<MemoryLocation> memoryLocations2 = deserializer.readValue(json2, new TypeReference<>()
    {
    });
    MemoryLocation ml2 = memoryLocations2.get(0);
    
    // Assert. Compare by value
    Assert.assertEquals(ml.names.get(0), ml2.names.get(0));
    Assert.assertEquals(ml.alignment, ml2.alignment);
    Assert.assertEquals(ml.dataTypes.get(0).dataType(), ml2.dataTypes.get(0).dataType());
    Assert.assertEquals(ml.dataTypes.get(0).startOffset(), ml2.dataTypes.get(0).startOffset());
    Assert.assertEquals(ml.data.get(0), ml2.data.get(0));
    Assert.assertEquals(ml.data.get(1), ml2.data.get(1));
  }
  
  @Test
  public void testConstant() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/constant.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    MemoryLocation ml = memoryLocations.get(0);
    
    Assert.assertEquals(10, ml.data.size());
    for (int i = 0; i < 10; i++)
    {
      Assert.assertEquals("9", ml.data.get(i));
    }
  }
  
  /**
   * The random memory location should always have the same value
   */
  @Test
  public void testRandom() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/random.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    MemoryLocation ml = memoryLocations.get(0);
    
    // 7 to 10 inclusive, 8 numbers
    List<String> expected = List.of("9", "7", "9", "10", "10", "10", "10", "10");
    Assert.assertEquals(8, ml.data.size());
    for (int i = 0; i < 8; i++)
    {
      Assert.assertEquals(expected.get(i), ml.data.get(i));
    }
  }
  
  /**
   * The random memory location should always have the same value
   */
  @Test
  public void testCsv() throws JsonProcessingException
  {
    String json = loadTestResource("/memoryLocations/csv.json");
    List<MemoryLocation> memoryLocations = deserializer.readValue(json, new TypeReference<>()
    {
    });
    MemoryLocation ml = memoryLocations.get(0);
    
    // 4x4 csv with values 1,2,3...,8,9,1,2...
    Assert.assertEquals(16, ml.data.size());
    for (int i = 0; i < 16; i++)
    {
      Assert.assertEquals(String.valueOf((i % 9) + 1), ml.data.get(i));
    }
  }
  
  /**
   * Utility to load a test resource file
   */
  public String loadTestResource(String path)
  {
    InputStream is = MemoryLocationTests.class.getResourceAsStream(path);
    if (is == null)
    {
      throw new RuntimeException("Resource not found: " + path);
    }
    // Read the file contents and return it as a string
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
