package com.gradle.superscalarsim.predictor;

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.NOT_TAKEN;
import static com.gradle.superscalarsim.blocks.branch.BitPredictor.TAKEN;

/**
 * The one bit predictor predicts the outcome that was last seen
 */
public class OneBitPredictorTests
{
  SimulationConfig config;
  
  @Before
  public void setUp() throws Exception
  {
    config                                 = SimulationConfig.getDefaultConfiguration();
    config.cpuConfig.predictorType         = BitPredictor.PredictorType.ONE_BIT_PREDICTOR;
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
  
  /**
   * Make the predictor default to taken
   */
  public void makeTaken()
  {
    config.cpuConfig.predictorDefaultState = TAKEN;
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
    // The jump is in the fetch, it cannot be predicted even if we wanted to
    cpu.step();
    // In decode, the jump is not computed - conditionals are not computed in decode.
    SimCodeModel branch = cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().get(1);
    Assert.assertEquals(-1, branch.getBranchTarget());
    Assert.assertEquals(2, cpu.cpuState.decodeAndDispatchBlock.getCodeBuffer().size());
    
    // Some time later, the beq appears in the fetch again.
    while (cpu.cpuState.instructionFetchBlock.getPc() != 4)
    {
      cpu.step();
    }
    // It fetched addi, beq, addi, stopped at pc=4
    
    SimCodeModel branch2 = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1);
    Assert.assertEquals("beq", branch2.getInstructionName());
    // It does predict the jump, because the last time it was taken.
    SimCodeModel next = cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2);
    Assert.assertEquals("addi", next.getInstructionName());
    Assert.assertTrue(branch2.isBranchPredicted());
    
    cpu.step();
    // The fetch pulled beq, addi (second beq is not fetched, because of fetch limit)
    List<SimCodeModel> fetchedCode = cpu.cpuState.instructionFetchBlock.getFetchedCode();
    Assert.assertEquals("beq", fetchedCode.get(0).getInstructionName());
    Assert.assertEquals("addi", fetchedCode.get(1).getInstructionName());
    Assert.assertEquals("nop", fetchedCode.get(2).getInstructionName());
    Assert.assertTrue(fetchedCode.get(0).isBranchPredicted());
  }
  
  /**
   * If the branch is different from the last time and the initial state is not chosen correctly, the prediction will be always wrong.
   */
  @Test
  public void testPredictorNeverCorrect()
  {
    makeNotTaken();
    setCode("""
                        li a5,0
                        li a3,10
                        li t1,0
                    .L3:
                        andi a4,a5,1
                        bne a4,zero,.L2 # This branch will be taken on iterations 1, 3, 5, 7, 9
                        add t1,t1,a5
                    .L2:
                        addi a5,a5,1
                        bne a5,a3,.L3
                        
                    """);
    // Iteration: 0 1 2 3 4 5 6 7 8 9
    //     Taken: N Y N Y N Y N Y N Y (5/10)
    // Predicted: N N Y N Y N Y N Y N (1 'correct' in the first iteration)
    
    Cpu cpu = new Cpu(config);
    
    // In this test, the jump is always not predicted, but computed in decode.
    cpu.execute(false);
    
    // Check correctness of program - sum of even numbers from 0 to 9
    Assert.assertEquals(2 + 4 + 6 + 8,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t1").getValue(DataTypeEnum.kInt));
    
    // The branch prediction of instruction [4] is always wrong.
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(1, stats.correctlyPredicted);
  }
  
  /**
   * Same as testPredictorNeverCorrect, but the other initial state is chosen.
   */
  @Test
  public void testPredictorNeverCorrect2()
  {
    makeTaken();
    setCode("""
                        li a5,0
                        li a3,10
                        li t1,0
                    .L3:
                        andi a4,a5,1
                        bne a4,zero,.L2 # This branch will be taken on iterations 1, 3, 5, 7, 9
                        add t1,t1,a5
                    .L2:
                        addi a5,a5,1
                        bne a5,a3,.L3
                        
                    """);
    // Iteration: 0 1 2 3 4 5 6 7 8 9
    //     Taken: N Y N Y N Y N Y N Y (5/10)
    // Predicted: * N Y N Y N Y N Y N (* - would have predicted jump, but mandatory prediction fail)
    
    Cpu cpu = new Cpu(config);
    
    // In this test, the jump is always not predicted, but computed in decode.
    cpu.execute(false);
    
    // Check correctness of program - sum of even numbers from 0 to 9
    Assert.assertEquals(2 + 4 + 6 + 8,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t1").getValue(DataTypeEnum.kInt));
    
    // The branch prediction of instruction [4] is always wrong.
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(1, stats.correctlyPredicted);
  }
  
  /**
   * If a branch is never taken, the predictor has 100% accuracy.
   */
  @Test
  public void testPredictorAlwaysCorrect()
  {
    makeNotTaken();
    setCode("""
                        li a5,0
                        li a3,10
                        li t1,0
                    .L3:
                        andi a4,a5,1
                        bne zero,zero,.L2 # This can never be true
                        add t1,t1,a5
                    .L2:
                        addi a5,a5,1
                        bne a5,a3,.L3
                        
                    """);
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(10, stats.correctlyPredicted);
  }
}
