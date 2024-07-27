package com.gradle.superscalarsim.loader;

import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class StaticAssetsTests
{
  /**
   * Static resources are loaded without errors
   */
  @Test
  public void testLoadStaticResources()
  {
    new StaticDataProvider();
    Assert.assertTrue(true);
  }
  
  /**
   * Static resources contain registers
   */
  @Test
  public void testStaticResourcesContainRegisters()
  {
    StaticDataProvider provider     = new StaticDataProvider();
    RegisterFile       registerFile = provider.getRegisterFile();
    
    Assert.assertNotNull(registerFile);
    // 32 general purpose registers + 32 floating point registers
    Assert.assertEquals(64, registerFile.getRegisterCount());
    Assert.assertTrue(registerFile.getRegister("x0").isConstant());
  }
  
  /**
   * Static resources contain instruction set
   */
  @Test
  public void testStaticResourcesContainInstructionSet()
  {
    StaticDataProvider                    provider = new StaticDataProvider();
    Map<String, InstructionFunctionModel> models   = provider.getInstructionFunctionModels();
    InstructionFunctionModel              nop      = models.get("nop");
    
    Assert.assertNotNull(models);
    Assert.assertNotNull(nop);
  }
  
  /**
   * Static resources contain register aliases
   */
  @Test
  public void testStaticResourcesContainRegisterAliases()
  {
    StaticDataProvider provider = new StaticDataProvider();
    RegisterFile       file     = provider.getRegisterFile();
    
    Assert.assertNotNull(file);
    Assert.assertEquals("x0", file.getRegister("zero").getName());
  }
}
