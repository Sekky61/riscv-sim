package com.gradle.superscalarsim;

import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.loader.InitLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * @brief Tests for the compiler
 * Note: These tests require GCC riscv toolchain to be installed
 */
public class CompilerTests
{
  
  @Test
  public void test_validCProgram_passes()
  {
    // Setup
    String cCode = "int f(int a) { return a+1; }";
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, false);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertFalse(compileResult.code.isEmpty());
    Assert.assertNull(compileResult.error);
  }
  
  @Test
  public void test_asmParserFiltersLabels()
  {
    String asmCode = """
            A:
            subi  sp, sp, 16
            B:
            addi   a0, a0, 1
            j     B
            """;
    
    // Exercise
    CompiledProgram program = AsmParser.parse(asmCode, 0);
    
    // Verify
    System.out.println(program.program);
    
    for (String line : program.program)
    {
      Assert.assertFalse(line.contains("A:"));
    }
  }
  
  @Test
  public void test_asmParserRemovesDirectives()
  {
    String asmCode = """
            .file   "test.c"
            N:
            .word 25
            M:
            .word 32
            A:
            subi  sp, sp, 16
            lw    a0, 0(N)
            """;
    
    // Exercise
    CompiledProgram program = AsmParser.parse(asmCode, 0);
    
    // Verify
    System.out.println(program.program);
    
    for (String line : program.program)
    {
      Assert.assertFalse(line.contains("A:"));
    }
  }
  
  @Test
  public void test_invalidCProgram_produces_error()
  {
    // Setup
    String cCode = "int f(int a) { return a+1;";
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, false);
    
    // Verify
    Assert.assertFalse(compileResult.success);
    Assert.assertNull(compileResult.code);
    Assert.assertNotNull(compileResult.error);
    Assert.assertNotNull(compileResult.compilerErrors);
    Assert.assertFalse(compileResult.compilerErrors.isEmpty());
  }
  
  @Test
  public void test_simpleCProgram_produces_valid_riscv_asm()
  {
    // Setup
    String     cCode  = "int f(int a) { int x = a*2; return x+1; }";
    InitLoader loader = new InitLoader();
    CodeParser parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, false);
    CompiledProgram         program       = AsmParser.parse(compileResult.code, cCode.split("\n").length);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
  }
  
  @Test
  public void test_validCProgramOptimized_produces_valid_riscv_asm()
  {
    // Setup
    String cCode = """
            int square(int num) {
                double x = 5;
                float y = x * x;
                
                int sh = num >> 7;
                
                int re = sh / 14;
                int re2 = sh % (num+1);
                
                if(y > sh) {
                    return num;
                }
                
                return num * num;
            }""";
    InitLoader loader = new InitLoader();
    CodeParser parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, true);
    CompiledProgram         program       = AsmParser.parse(compileResult.code, cCode.split("\n").length);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
    
    // There is a square label
    Assert.assertNotNull(parser.getLabels().get("square"));
  }
  
  @Test
  public void test_cprogram_with_global_array_produces_valid_riscv_asm()
  {
    // Setup
    String cCode = """
            int arr[10];
            
            int sum() {
                int sum = 0;
                for(int i = 0; i < 10; i++) {
                    sum += arr[i];
                }
                return sum;
            }""";
    InitLoader loader = new InitLoader();
    CodeParser parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, true);
    CompiledProgram         program       = AsmParser.parse(compileResult.code, cCode.split("\n").length);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
    
    // There is a sum label
    Assert.assertNotNull(parser.getLabels().get("sum"));
  }
}
