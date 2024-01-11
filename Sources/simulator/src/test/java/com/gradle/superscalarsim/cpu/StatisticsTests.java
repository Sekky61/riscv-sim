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
                li a1, 10
            .L2:
                addi t0,t0,1
                bne t0,a1,.L2""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // mv is a intArithmetic instruction
    // addi will be called 10 times. It is also intArithmetic
    Assert.assertEquals(12, cpu.cpuState.statistics.dynamicInstructionMix.intArithmetic);
    Assert.assertEquals(10, cpu.cpuState.statistics.dynamicInstructionMix.branch);
    
    // Per instruction stats
    Assert.assertEquals(1, cpu.cpuState.statistics.instructionStats.get(0).committedCount);
    Assert.assertEquals(1, cpu.cpuState.statistics.instructionStats.get(1).committedCount);
    Assert.assertEquals(10, cpu.cpuState.statistics.instructionStats.get(2).committedCount);
    Assert.assertEquals(10, cpu.cpuState.statistics.instructionStats.get(3).committedCount);
  }
  
  @Test
  public void testRegAllocationCounter()
  {
    // Setup + exercise
    cpuConfig.code = """
            addi x1, x1, 5
            addi x1, x1, 5
            addi x1, x1, 5
            addi x1, x1, 5""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // At most 4 spec. registers are needed
    Assert.assertEquals(4, cpu.cpuState.statistics.maxAllocatedRegisters);
  }
  
  @Test
  public void testRobNoFlush()
  {
    // Setup + exercise
    cpuConfig.code = """
            addi x1, x1, 5
            addi x1, x1, 5
            addi x1, x1, 5
            addi x1, x1, 5
            sb x1, 0(x0)""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // No flush can occur
    Assert.assertEquals(0, cpu.cpuState.statistics.robFlushes);
  }
  
  @Test
  public void testRobLoopFlush()
  {
    // Setup + exercise
    cpuConfig.cpuConfig.predictorType    = "0bit";
    cpuConfig.cpuConfig.predictorDefault = "Not Taken";
    cpuConfig.code                       = """
            li t0, 0
                li a1, 10
            .L2:
                addi t0,t0,1
                bne t0,a1,.L2""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // 10 loops, 9 bad predictions (last one is correct), 9 flushes
    Assert.assertEquals(9, cpu.cpuState.statistics.robFlushes);
  }
  
  @Test
  public void testFuStats()
  {
    cpuConfig.code = """
            addi t0,t0,1
            addi t0,t0,1""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // There is a "FX" functional unit
    Assert.assertEquals(2, cpu.cpuState.statistics.fuStats.get("FX").busyCycles);
  }
}
