package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import org.junit.Assert;
import org.junit.Test;

public class AsmFilterTests
{
  @Test
  public void test_asmParserFiltersLabels()
  {
    String asmCode = """
            .section	.text.startup.main,"ax",@progbits
            unused:
            subi  sp, sp, 16
            B:
            addi   a0, a0, 1
            j     B
            """;
    
    // Exercise
    CompiledProgram program = AsmParser.parse(asmCode);
    
    for (String line : program.program)
    {
      Assert.assertFalse(line.contains("unused:"));
    }
  }
  
  @Test
  public void test_filterKeepsArrayWithArithmetic()
  {
    String asmCode = """
            .section	.text.startup.main,"ax",@progbits
            lla a4,ptr+4
            addi a4,a4,1
            ptr:
                .zero 16
            """;
    
    // Exercise
    CompiledProgram program = AsmParser.parse(asmCode);
    
    boolean found = false;
    for (String line : program.program)
    {
      if (line.contains("ptr:"))
      {
        found = true;
      }
    }
    Assert.assertTrue(found);
  }
}
