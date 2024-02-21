package com.gradle.superscalarsim.cpu;

import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test stalls due to ROB being full, store/load buffer being full
 */
public class StallTests
{
  @Test
  public void testStallDueToROBFull()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code                 = """
            addi x1, x0, 1
            addi x2, x0, 2
            addi x3, x0, 3
            addi x4, x0, 4
            addi x5, x0, 5
            addi x6, x0, 6
            addi x7, x0, 7
            addi x8, x0, 8
            addi x9, x0, 9
            """;
    cfg.cpuConfig.fetchWidth = 3;
    cfg.cpuConfig.robSize    = 3;
    Cpu cpu = new Cpu(cfg);
    
    cpu.step();
    Assert.assertEquals(3, cpu.cpuState.instructionFetchBlock.getFetchedCode().size());
    cpu.step();
    Assert.assertEquals(3, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
    cpu.step();
    List<SimCodeModel> decode3 = new ArrayList<>(cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer());
    Assert.assertEquals(3, decode3.size());
    Assert.assertEquals(3, cpu.cpuState.reorderBufferBlock.getReorderQueue().count());
    
    cpu.step();
    List<SimCodeModel> decode4 = new ArrayList<>(cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer());
    // The ROB is full, so all the decoded instructions still remain in the decode buffer
    for (int i = 0; i < decode3.size(); i++)
    {
      Assert.assertEquals(decode3.get(i), decode4.get(i));
    }
    
    cpu.step();
    List<SimCodeModel> decode5 = new ArrayList<>(cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer());
    // Still holding
    for (int i = 0; i < decode3.size(); i++)
    {
      Assert.assertEquals(decode3.get(i), decode5.get(i));
    }
    
    while (cpu.cpuState.statistics.committedInstructions == 0)
    {
      cpu.step();
    }
    
    // First instruction should be committed
    Assert.assertEquals(1, cpu.cpuState.statistics.committedInstructions);
    List<SimCodeModel> decodeAfterCommit = new ArrayList<>(cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer());
    // One instruction should be removed from the decode buffer
    Assert.assertEquals(2, decodeAfterCommit.size());
    Assert.assertEquals(decode5.get(1), decodeAfterCommit.get(0));
    Assert.assertEquals(decode5.get(2), decodeAfterCommit.get(1));
    List<SimCodeModel> fetch = cpu.cpuState.instructionFetchBlock.getFetchedCode();
    
    cpu.step();
    Assert.assertEquals(2, cpu.cpuState.statistics.committedInstructions);
    cpu.step();
    Assert.assertEquals(3, cpu.cpuState.statistics.committedInstructions);
    // Decode buffer should be empty, so fetch can be pulled
    for (int i = 0; i < 3; i++)
    {
      Assert.assertEquals(fetch.get(i), cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(i));
    }
  }
  
  @Test
  public void testStallDueToStoreBufferFull()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code                 = """
            sb x1, 0(x0)
            sb x2, 0(x0)
            sb x3, 0(x0)
            sb x4, 0(x0)
            sb x5, 0(x0)
            sb x6, 0(x0)
            """;
    cfg.cpuConfig.fetchWidth = 3;
    cfg.cpuConfig.sbSize     = 3;
    Cpu cpu = new Cpu(cfg);
    
    cpu.step();
    cpu.step();
    cpu.step();
    // Now the store buffer is full
    Assert.assertEquals(3, cpu.cpuState.storeBufferBlock.getQueueSize());
    List<SimCodeModel> decode3 = new ArrayList<>(cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer());
    cpu.step();
    // Nothing got pulled
    for (int i = 0; i < decode3.size(); i++)
    {
      Assert.assertEquals(decode3.get(i), cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(i));
    }
    
    while (cpu.cpuState.statistics.committedInstructions == 0)
    {
      cpu.step();
    }
    
    // One is committed, so one can be pulled
    Assert.assertEquals(1, cpu.cpuState.statistics.committedInstructions);
    Assert.assertEquals(2, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals(3, cpu.cpuState.storeBufferBlock.getQueueSize());
  }
}
