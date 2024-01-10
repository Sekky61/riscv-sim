package com.gradle.superscalarsim.cpu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatisticsTests
{
  private SimulationConfig cpuConfig;
  
  @Before
  public void setup()
  {
    cpuConfig = SimulationConfig.getDefaultConfiguration();
  }
  
  /**
   * Test static instruction mix
   */
  @Test
  public void testStaticInstructionMix()
  {
    // Setup + exercise
    cpuConfig.code = "addi x1, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statistics.staticInstructionMix.intArithmetic);
    Assert.assertEquals(0, cpu.cpuState.statistics.staticInstructionMix.floatArithmetic);
    Assert.assertEquals(0, cpu.cpuState.statistics.staticInstructionMix.branch);
    Assert.assertEquals(0, cpu.cpuState.statistics.staticInstructionMix.memory);
  }
  
  /**
   * Test dynamic instruction mix
   */
  @Test
  public void testDynamicInstructionMix()
  {
    // Setup + exercise
    cpuConfig.code = "addi x1, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statistics.dynamicInstructionMix.intArithmetic);
    Assert.assertEquals(0, cpu.cpuState.statistics.dynamicInstructionMix.floatArithmetic);
    Assert.assertEquals(0, cpu.cpuState.statistics.dynamicInstructionMix.branch);
    Assert.assertEquals(0, cpu.cpuState.statistics.dynamicInstructionMix.memory);
  }
  
  /**
   * Test dynamic instruction mix in a loop
   */
  @Test
  public void testDynamicInstructionMixLoop()
  {
    // Setup + exercise
    cpuConfig.code = """
                li t0, 0
                li a1, 40
            .L2:
                addi t0,t0,1
                bne t0,a1,.L2""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // mv is a intArithmetic instruction
    // addi will be called 40 times. It is also intArithmetic
    Assert.assertEquals(42, cpu.cpuState.statistics.dynamicInstructionMix.intArithmetic);
    Assert.assertEquals(40, cpu.cpuState.statistics.dynamicInstructionMix.branch);
  }
}
