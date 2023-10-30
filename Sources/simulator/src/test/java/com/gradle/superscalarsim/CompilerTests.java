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
  public void test_validCProgram_produces_valid_riscv_asm()
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
    
    // TODO: Parser filters out constants
    // .word   0x41c80000                      # float 25
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
  }
}
