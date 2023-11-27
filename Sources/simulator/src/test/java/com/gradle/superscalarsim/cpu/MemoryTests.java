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
}
