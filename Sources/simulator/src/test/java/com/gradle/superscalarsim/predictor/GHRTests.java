package com.gradle.superscalarsim.predictor;

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.TAKEN;

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
    ghr.shiftValue(true, 0);
    ghr.shiftValue(false, 0);
    ghr.shiftValue(true, 0);
    ghr.shiftValue(false, 0);
    ghr.shiftValue(true, 0);
    ghr.shiftValue(false, 0);
    ghr.shiftValue(true, 0);
    ghr.shiftValue(false, 0);
    
    // Assert
    Assert.assertEquals(0b10101010, ghr.getRegisterValue());
    Assert.assertEquals(0, ghr.getArchitecturalState());
    Assert.assertEquals("10101010", ghr.toString());
  }
  
  @Test
  public void testGHRCommit()
  {
    GlobalHistoryRegister ghr = new GlobalHistoryRegister(8);
    ghr.shiftValue(true, 0);
    ghr.shiftValue(false, 1);
    ghr.shiftValue(true, 2);
    ghr.shiftValue(false, 3);
    ghr.shiftValue(true, 4);
    ghr.shiftValue(false, 5);
    ghr.shiftValue(true, 6);
    ghr.shiftValue(false, 7);
    
    ghr.commit(0);
    // Assert
    Assert.assertEquals(0b10101010, ghr.getRegisterValue());
    Assert.assertEquals(1, ghr.getArchitecturalState());
    
    ghr.commit(2);
    // Assert
    Assert.assertEquals(0b10101010, ghr.getRegisterValue());
    Assert.assertEquals(0b101, ghr.getArchitecturalState());
    
    ghr.commit(8);
    // Assert
    Assert.assertEquals(0b10101010, ghr.getRegisterValue());
    Assert.assertEquals(0b10101010, ghr.getArchitecturalState());
  }
  
  @Test
  public void testGhrMarksBranch()
  {
    config.cpuConfig.predictorDefaultState = TAKEN;
    config.cpuConfig.predictorType         = BitPredictor.PredictorType.ONE_BIT_PREDICTOR;
    config.code                            = """
            beq x0, x0, 0x4""";
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert - though it was predicted, it could not have been taken (mandatory negative prediction)
    Assert.assertEquals(0, cpu.cpuState.globalHistoryRegister.getArchitecturalState());
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
    Assert.assertEquals(0b00000000, cpu.cpuState.globalHistoryRegister.getArchitecturalState());
  }
  
  @Test
  public void testGhrLoop()
  {
    config.cpuConfig.predictorDefaultState = TAKEN;
    config.cpuConfig.predictorType         = BitPredictor.PredictorType.ONE_BIT_PREDICTOR;
    config.code                            = """
              li x1, 7
            loop:
              subi x1, x1, 1
              bne x0, x1, loop
              addi x7, x7, 7""";
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert
    // First, a mandatory negative prediction
    // Then, taken 6 times
    Assert.assertEquals(0b01111110, cpu.cpuState.globalHistoryRegister.getArchitecturalState());
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
