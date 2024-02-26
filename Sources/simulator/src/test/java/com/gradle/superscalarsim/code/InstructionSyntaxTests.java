package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test printing of instructions
 */
public class InstructionSyntaxTests
{
  @Test
  public void testTemplate()
  {
    StaticDataProvider       dataProvider = new StaticDataProvider();
    InstructionFunctionModel addi         = dataProvider.getInstructionFunctionModel("addi");
    
    List<String> template = addi.getSyntaxTemplate();
    List<String> expected = List.of("addi ", "rd", ",", "rs1", ",", "imm");
    Assert.assertEquals(expected, template);
  }
  
  @Test
  public void testLoadStoreTemplate()
  {
    StaticDataProvider       dataProvider = new StaticDataProvider();
    InstructionFunctionModel lw           = dataProvider.getInstructionFunctionModel("lw");
    
    List<String> template = lw.getSyntaxTemplate();
    List<String> expected = List.of("lw ", "rd", ",", "imm", "(", "rs1", ")");
    Assert.assertEquals(expected, template);
  }
  
  @Test
  public void testTemplateNoArgs()
  {
    StaticDataProvider       dataProvider = new StaticDataProvider();
    InstructionFunctionModel nop          = dataProvider.getInstructionFunctionModel("nop");
    
    List<String> template = nop.getSyntaxTemplate();
    List<String> expected = List.of("nop ");
    Assert.assertEquals(expected, template);
  }
}
