package com.gradle.superscalarsim.cpu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MemoryTests
{
  private CpuConfiguration cpuConfig;
  
  @Before
  public void setup()
  {
    cpuConfig = CpuConfiguration.getDefaultConfiguration();
  }
  
  @Test
  public void test_allocate_constant()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
  }
  
  @Test
  public void test_allocate_multiple()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1,2,4""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
    Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset + 4));
    Assert.assertEquals(4, cpu.cpuState.simulatedMemory.getFromMemory(offset + 8));
  }
  
  @Test
  public void test_allocate_multiple2()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1
            .word 2
            .word 4""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
    Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset + 4));
    Assert.assertEquals(4, cpu.cpuState.simulatedMemory.getFromMemory(offset + 8));
  }
}
