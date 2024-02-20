package com.gradle.superscalarsim.predictor;

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.NOT_TAKEN;
import static com.gradle.superscalarsim.blocks.branch.BitPredictor.TAKEN;

/**
 * The zero bit predictor is a simple predictor that always predicts taken/not taken based on the configuration
 */
public class ZeroBitPredictorTests
{
  SimulationConfig config;
  
  @Before
  public void setUp() throws Exception
  {
    config                                 = SimulationConfig.getDefaultConfiguration();
    config.cpuConfig.predictorType         = BitPredictor.PredictorType.ZERO_BIT_PREDICTOR;
    config.cpuConfig.predictorDefaultState = TAKEN;
  }
  
  /**
   * The first encountered jump cannot be predicted
   */
  @Test
  public void testMandatoryPredictionFail()
  {
    makeNotTaken();
    setCode("""
                      j label
                      addi x0, x0, 0
                    label:
                    """);
    
    Cpu cpu = new Cpu(config);
    
    cpu.step();
    // The jump is in the fetch, it cannot be predicted even if we wanted to
    cpu.step();
    // In decode, the jump is computed. Consequently, the addi is dropped.
    SimCodeModel j = cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(0);
    Assert.assertEquals(8, j.getBranchTarget());
    Assert.assertEquals(1, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
  }
  
  /**
   * Make the predictor default to not taken
   */
  public void makeNotTaken()
  {
    config.cpuConfig.predictorDefaultState = NOT_TAKEN;
  }
  
  /**
   * Set code to be executed
   */
  public void setCode(String code)
  {
    config.code = code;
  }
  
  @Test
  public void testPredictorNotTakenLoop()
  {
    makeNotTaken();
    setCode("""
                    loop:
                      addi x0, x0, 0
                      beq x0, x0, loop
                    """);
    
    Cpu cpu = new Cpu(config);
    
    // In this test, the jump is always not predicted, but computed in decode.
    cpu.step();
    Assert.assertEquals(12, cpu.cpuState.instructionFetchBlock.getPc());
    // The jump is in the fetch, it cannot be predicted even if we wanted to
    cpu.step();
    // In decode, the jump is not computed - conditionals are not computed in decode.
    SimCodeModel branch = cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(1);
    Assert.assertEquals(-1, branch.getBranchTarget());
    Assert.assertEquals(2, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
    
    // Some time later, the beq appears in the fetch again.
    while (cpu.cpuState.instructionFetchBlock.getPc() != 12)
    {
      cpu.step();
    }
    
    SimCodeModel branch2 = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1);
    Assert.assertEquals("beq", branch2.getInstructionName());
    // It does not predict the jump
    SimCodeModel next = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2);
    Assert.assertEquals("nop", next.getInstructionName());
    
    cpu.step();
    // In decode, no jump is computed.
    Assert.assertFalse(branch2.isBranchPredictedOrComputedInDecode());
  }
  
  /**
   * With static positive prediction, the branch will be predicted taken, even if the last time
   * it was not taken.
   */
  @Test
  public void testPredictorTaken()
  {
    makeTaken();
    setCode("""
                    loop:
                      addi x0, x0, 0
                      bne x0, x0, end # This is never taken, but always predicted (not the first time)
                      beq x0, x0, loop
                    end:
                      subi x0, x0, 1
                    """);
    
    Cpu cpu = new Cpu(config);
    
    cpu.step();
    // Fetch stops before second branch
    Assert.assertEquals(8, cpu.cpuState.instructionFetchBlock.getPc());
    // The bne cannot be predicted, destination is not known
    cpu.step();
    // In decode, the jump is not computed - conditionals are not computed in decode.
    SimCodeModel bne = cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(1);
    Assert.assertEquals(-1, bne.getBranchTarget());
    Assert.assertEquals(2, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
    
    // Some time later, the bne appears in the fetch again.
    // This time, it will predict, fetch the subi and point after subi
    while (cpu.cpuState.instructionFetchBlock.getPc() != 16)
    {
      cpu.step();
    }
    
    SimCodeModel bne2 = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1);
    Assert.assertEquals("bne", bne2.getInstructionName());
    // It does predict the jump
    SimCodeModel next = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2);
    Assert.assertEquals("subi", next.getInstructionName());
  }
  
  /**
   * Make the predictor default to taken
   */
  public void makeTaken()
  {
    config.cpuConfig.predictorDefaultState = TAKEN;
  }
}
