package com.gradle.superscalarsim;

import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.Label;
import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of());
    
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
    CompiledProgram program = AsmParser.parse(asmCode);
    
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
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of());
    
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
    String             cCode  = "int f(int a) { int x = a*2; return x+1; }";
    StaticDataProvider loader = new StaticDataProvider();
    CodeParser         parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of());
    CompiledProgram         program       = AsmParser.parse(compileResult.code);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
    
    Assert.assertTrue(program.labels.contains("f"));
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
    StaticDataProvider loader = new StaticDataProvider();
    CodeParser         parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of("O2"));
    CompiledProgram         program       = AsmParser.parse(compileResult.code);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    try
    {
      // Verify
      Assert.assertTrue(compileResult.success);
      Assert.assertTrue(parser.success());
      Assert.assertFalse(parser.getInstructions().isEmpty());
      
      // There is a square label
      Assert.assertNotNull(parser.getLabels().get("square"));
    }
    catch (AssertionError e)
    {
      System.out.println(e.getMessage());
      System.out.println("Program:");
      System.out.println(asm);
      System.out.println("Error messages:");
      System.out.println(parser.getErrorMessages());
      Assert.fail();
    }
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
    StaticDataProvider loader = new StaticDataProvider();
    CodeParser         parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of("O2"));
    CompiledProgram         program       = AsmParser.parse(compileResult.code);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
    // There is a sum label
    Assert.assertNotNull(parser.getLabels().get("sum"));
  }
  
  @Test
  public void test_c_string_allocation()
  {
    String cCode = """
            char *str = "Hello World!";
            char *str2 = "Second!";
            
            int add(int x) {
              return str[x] + str2[x];
            }
            """;
    StaticDataProvider loader = new StaticDataProvider();
    CodeParser         parser = new CodeParser(loader);
    
    // Exercise
    GccCaller.CompileResult compileResult = GccCaller.compile(cCode, List.of("O2"));
    CompiledProgram         program       = AsmParser.parse(compileResult.code);
    String                  asm           = String.join("\n", program.program);
    parser.parseCode(asm);
    
    // Verify
    Assert.assertTrue(compileResult.success);
    Assert.assertTrue(parser.success());
    Assert.assertFalse(parser.getInstructions().isEmpty());
    // There is a add label
    Assert.assertNotNull(parser.getLabels().get("add"));
    
    // There is a string label
    Label str = parser.getLabels().get("str");
    Assert.assertNotNull(str);
    Assert.assertNotEquals(0, str.getAddress());
    
    
    // The program is loadable
    SimulationConfig cpuConfig = SimulationConfig.getDefaultConfiguration();
    cpuConfig.code = asm;
    Cpu cpu = new Cpu(cpuConfig);
    
    Map<String, Label> labels = cpu.cpuState.instructionMemoryBlock.getLabels();
    Assert.assertNotNull(labels.get("str"));
    Assert.assertEquals("str", labels.get("str").name);
  }
}
