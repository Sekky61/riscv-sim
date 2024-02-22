package com.gradle.superscalarsim.predictor;

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Global History Register.
 * The default config has the GHR predictor disabled.
 */
public class GHRTests
{
  SimulationConfig config;
  
  @Before
  public void setUp()
  {
    config                            = new SimulationConfig();
    config.cpuConfig.useGlobalHistory = true;
  }
  
  @Test
  public void testGHR()
  {
    GlobalHistoryRegister ghr = new GlobalHistoryRegister(8);
    ghr.shiftValue(true);
    ghr.shiftValue(false);
    ghr.shiftValue(true);
    ghr.shiftValue(false);
    ghr.shiftValue(true);
    ghr.shiftValue(false);
    ghr.shiftValue(true);
    ghr.shiftValue(false);
    
    // Assert
    Assert.assertEquals(0b10101010, ghr.getRegisterValue());
    Assert.assertEquals("10101010", ghr.toString());
  }
  
  @Test
  public void testGhrMarksBranch()
  {
    config.code = """
            beq x0, x0, 0x4""";
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals(0b00000001, cpu.cpuState.globalHistoryRegister.getRegisterValue());
  }
  
  @Test
  public void testGhrDoesNotMarkUnconditional()
  {
    config.code = """
            addi x0, x0, 0x4
            j label
            addi x0, x0, 0x4
            label:""";
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals(0b00000000, cpu.cpuState.globalHistoryRegister.getRegisterValue());
  }
  
  @Test
  public void testGhrLoop()
  {
    config.code = """
              li x1, 7
            loop:
              subi x1, x1, 1
              bne x0, x1, loop
              addi x7, x7, 7""";
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals(0b01111110, cpu.cpuState.globalHistoryRegister.getRegisterValue());
  }
  
  @Test
  public void testGhrPredictorIndexing()
  {
    config.code = """
              li x1, 7
            loop:
              subi x1, x1, 1
              bne x0, x1, loop
              addi x7, x7, 7""";
    Cpu cpu      = new Cpu(config);
    int branchPc = 8;
    
    cpu.step();
    BitPredictor predictor = cpu.cpuState.gShareUnit.getPredictor(branchPc);
    
    while (cpu.cpuState.statistics.committedInstructions < 3)
    {
      cpu.step();
    }
    
    // BNE committed, new history, new predictor
    BitPredictor predictor2 = cpu.cpuState.gShareUnit.getPredictor(branchPc);
    Assert.assertNotEquals(predictor, predictor2);
  }
  
  // TODO: more tests, once the details of GHR are consulted
}
