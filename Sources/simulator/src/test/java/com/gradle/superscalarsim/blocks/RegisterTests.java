package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RegisterTests
{
  
  InitLoader initLoader;
  
  UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  
  @Before
  public void setUp()
  {
    initLoader               = new InitLoader();
    unifiedRegisterFileBlock = new UnifiedRegisterFileBlock(initLoader);
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
    Assert.assertEquals(0, unifiedRegisterFileBlock.getRegister("zero").getValue(), 0.01);
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
    Assert.assertEquals(10, sp.getValue(), 0.01);
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
    Assert.assertEquals(10, x2.getValue(), 0.01);
  }
  
  /**
   * Test that register file and register map point to the same objects
   */
  @Test
  public void testRegisterFileAndMapReferenceEquality()
  {
    // Execute
    List<RegisterModel> intlist    = unifiedRegisterFileBlock.getRegisterList(DataTypeEnum.kInt);
    RegisterModel       x2FromList = intlist.get(2);
    
    RegisterModel x2FromMap = unifiedRegisterFileBlock.getRegisterMap().get("x2");
    
    // Assert
    Assert.assertEquals("x2", x2FromList.getName());
    Assert.assertEquals("x2", x2FromMap.getName());
    Assert.assertEquals(x2FromList, x2FromMap);
  }
}
