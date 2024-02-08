package com.gradle.superscalarsim;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Test;

public class RegisterTests
{
  @Test
  public void testSaveIntGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123);
    
    int x = (int) registerDataContainer.getValue(DataTypeEnum.kInt);
    Assert.assertEquals(123, x);
  }
  
  /**
   * Are enums objects?
   */
  @Test
  public void testDeepCopyDataType()
  {
    RegisterModel registerModel = new RegisterModel("xx", false, RegisterTypeEnum.kInt, RegisterReadinessEnum.kFree);
    registerModel.setValue(123, DataTypeEnum.kUInt);
    
    // Copy changes its type and readiness
    RegisterModel copy = new RegisterModel(registerModel);
    copy.setValue(456, DataTypeEnum.kInt);
    copy.setReadiness(RegisterReadinessEnum.kExecuted);
    
    // Original is unchanged
    Assert.assertEquals(123, registerModel.getValue(DataTypeEnum.kUInt));
    Assert.assertEquals(DataTypeEnum.kUInt, registerModel.getValueContainer().getCurrentType());
    Assert.assertEquals(RegisterReadinessEnum.kFree, registerModel.getReadiness());
  }
  
  @Test
  public void testSaveLongGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123L);
    
    int x = (int) registerDataContainer.getValue(DataTypeEnum.kInt);
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
    
    int x = (int) registerDataContainer.getValue(DataTypeEnum.kInt);
    Assert.assertEquals(0b01000010111101100000000000000000, x);
  }
  
  @Test
  public void testSaveDoubleGetInt()
  {
    RegisterDataContainer registerDataContainer = new RegisterDataContainer();
    
    registerDataContainer.setValue(123.0);
    
    // The lower bits of the bit representation of the double are all zero.
    int x = (int) registerDataContainer.getValue(DataTypeEnum.kInt);
    Assert.assertEquals(0b00000000_00000000_00000000_00000000, x);
  }
}
