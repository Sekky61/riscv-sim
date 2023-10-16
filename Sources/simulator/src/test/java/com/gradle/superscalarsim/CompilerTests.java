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
    CompiledProgram         program       = AsmParser.parse(compileResult.code, 1);
    String                  asm           = String.join("\n", program.program);
    boolean                 success       = parser.parse(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(success);
    Assert.assertFalse(parser.getParsedCode().isEmpty());
  }
}
