package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegisterTests
{
  
  StaticDataProvider staticDataProvider;
  
  UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  
  @Before
  public void setUp()
  {
    staticDataProvider       = new StaticDataProvider();
    unifiedRegisterFileBlock = new UnifiedRegisterFileBlock(staticDataProvider, 320, new RegisterModelFactory());
  }
  
  /**
   * Tests that registers get loaded correctly from the config file
   */
  @Test
  public void testX0IsConstant()
  {
    // Assert
    Assert.assertTrue(unifiedRegisterFileBlock.getRegister("x0").isConstant());
  }
  
  /**
   * Tests the zero alias
   */
  @Test
  public void testZeroAlias()
  {
    // Assert
    Assert.assertEquals(0, unifiedRegisterFileBlock.getRegister("zero").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * Tests that aliases point to the same register
   */
  @Test
  public void testAliasReferenceEquality()
  {
    // Execute
    RegisterModel x2 = unifiedRegisterFileBlock.getRegister("x2");
    x2.setValue(10);
    
    // Assert
    // x2 should be the same as sp
    RegisterModel sp = unifiedRegisterFileBlock.getRegister("sp");
    Assert.assertEquals(10, (int) sp.getValue(DataTypeEnum.kInt), 0.01);
  }
  
  /**
   * Tests that speculative register can be written to, and can be copied to arch. registers using `copyAndFree`
   */
  @Test
  public void testSpeculativeRegister()
  {
    // Execute
    RegisterModel speculativeRegister = unifiedRegisterFileBlock.getRegister("tg100");
    speculativeRegister.setValue(10);
    
    // Execute
    unifiedRegisterFileBlock.copyAndFree("tg100", "x2");
    
    // Assert
    RegisterModel x2 = unifiedRegisterFileBlock.getRegister("x2");
    Assert.assertEquals(10, (int) x2.getValue(DataTypeEnum.kInt), 0.01);
  }
}
