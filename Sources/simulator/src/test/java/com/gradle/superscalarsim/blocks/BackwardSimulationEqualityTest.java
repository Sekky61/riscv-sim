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
    // There is no easy way to compare the cpuState objects, even the jsons have different order of fields in Maps
    // Let's just probe a few fields
    Assert.assertEquals(cpu.cpuState.tick, cpu2.cpuState.tick);
    Assert.assertEquals(cpu.cpuState.instructionFetchBlock.getPc(), cpu2.cpuState.instructionFetchBlock.getPc());
    Assert.assertEquals(cpu.cpuState.decodeAndDispatchBlock.getCurrentStepId(),
                        cpu2.cpuState.decodeAndDispatchBlock.getCurrentStepId());
    Assert.assertEquals(cpu.cpuState.reorderBufferState.reorderQueue.size(),
                        cpu2.cpuState.reorderBufferState.reorderQueue.size());
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
