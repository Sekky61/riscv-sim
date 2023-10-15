package com.gradle.superscalarsim;

import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import org.junit.Assert;
import org.junit.Test;

public class RegisterTests
{
  @Test
  public void testSaveIntGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123);
    
    int x = registerDataContainer.getValue(Integer.class);
    Assert.assertEquals(123, x);
  }
  
  @Test
  public void testSaveLongGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123L);
    
    int x = registerDataContainer.getValue(Integer.class);
    Assert.assertEquals(123, x);
  }
  
  /**
   * Writing a float to a register and reading it as an int should return the
   * IEEE 754 bit representation of the float.
   */
  @Test
  public void testSaveFloatGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123.0f);
    
    int x = registerDataContainer.getValue(Integer.class);
    Assert.assertEquals(0b01000010111101100000000000000000, x);
  }
  
  @Test
  public void testSaveDoubleGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123.0);
    
    // The lower bits of the bit representation of the double are all zero.
    int x = registerDataContainer.getValue(Integer.class);
    Assert.assertEquals(0b00000000_00000000_00000000_00000000, x);
  }
}
