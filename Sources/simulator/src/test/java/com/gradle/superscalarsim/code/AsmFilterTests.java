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
  
  @Test
  public void test_mapCLines()
  {
    // Orig C code
    //
    //    int arr[128] = {0};
    //
    //    // Notice that iterations are independent
    //    int evenElements()
    //    {
    //      int acc = 42;
    //      for (int i = 0; i < 128; ++i) {
    //        if(i%2 == 0) {
    //          acc += arr[i];
    //        }
    //      }
    //      return acc;
    //    }
    
    String asmCode = """
              .section	.text.evenElements,"ax",@progbits
            	.align	2
            	.globl	evenElements
            	.type	evenElements, @function
            evenElements:
            .LFB0:
            	.file 1 "<stdin>"
            	.loc 1 5 1
            	.loc 1 6 3
            .LVL0:
            	.loc 1 7 3
            .LBB2:
            	.loc 1 7 8
            	.loc 1 7 21
            	lla	a4,arr
            	.loc 1 7 12 is_stmt 0
            	li	a5,0
            .LBE2:
            	.loc 1 6 7
            	li	a0,42
            .LBB3:
            	.loc 1 7 21
            	li	a3,128""";
    
    // Exercise
    CompiledProgram program = AsmParser.parse(asmCode);
    
    // The .loc directive should be parsed into mappings
    
    String  firstAsmLine        = program.program.get(0);
    Integer firstAsmLineMapping = program.asmToC.get(0);
    Assert.assertEquals("evenElements:", firstAsmLine);
    Assert.assertEquals(0, firstAsmLineMapping.intValue());
    
    // Index 3 is the 42 instruction. It should be mapped to line 6
    String  fortyTwoInstruction = program.program.get(3);
    Integer fortyTwoMapping     = program.asmToC.get(3);
    Assert.assertTrue(fortyTwoInstruction.contains("42"));
    Assert.assertEquals(6, fortyTwoMapping.intValue());
  }
}
