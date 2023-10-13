package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Testing if the backward simulation is equal to the forward simulation
 */
public class BackwardSimulationEqualityTest
{
  @Test
  public void test_forwardAndBack_sameAs_forward()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.code = """
            addi x3, x0, 5
            loop:
            beq x3, x0, loopEnd
            subi x3, x3, 1
            jal x0, loop
            loopEnd:""";
    Cpu cpu  = new Cpu(cfg);
    Cpu cpu2 = new Cpu(cfg);
    
    // Exercise
    
    // Run 10 steps, one back
    for (int i = 0; i < 10; i++)
    {
      cpu.step();
    }
    cpu.stepBack();
    
    // Run 9 steps forward on a new cpu
    for (int i = 0; i < 9; i++)
    {
      cpu2.step();
    }
    
    // Assert they are equal
    Assert.assertEquals(cpu.cpuState, cpu2.cpuState);
  }
  
  @Test
  public void test_back_unusualConfig()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.fetchWidth = 1;
    cfg.robSize    = 1;
    cfg.code       = """
            addi x3, x0, 10
            loop:
            beq x3, x0, loopEnd
            subi x3, x3, 1
            addi x4, x4, 100
            jal x0, loop
            loopEnd:""";
    Cpu cpu  = new Cpu(cfg);
    Cpu cpu2 = new Cpu(cfg);
    
    // Exercise
    for (int i = 0; i < 13; i++)
    {
      cpu.step();
    }
    cpu.stepBack();
    cpu.stepBack();
    
    for (int i = 0; i < 11; i++)
    {
      cpu2.step();
    }
    
    // Assert they are equal
    Assert.assertEquals(cpu.cpuState, cpu2.cpuState);
  }
  
  /**
   * Test that the cpu does not step when requesting the initial state
   */
  @Test
  public void test_requesting_initialState()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.code = """
            addi x3, x0, 10
            subi x3, x3, 1""";
    Cpu cpu = new Cpu(cfg);
    cpu.simulateState(4);
    
    // Exercise
    Cpu cpuSpy = Mockito.spy(new Cpu(cfg, cpu.cpuState));
    cpuSpy.simulateState(0);
    
    // Assert that zero simulation steps were called
    Mockito.verify(cpuSpy, Mockito.never()).step();
  }
}
