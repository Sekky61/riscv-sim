package com.gradle.superscalarsim.predictor;

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.*;

/**
 * The two bit predictor adjusts to some patterns.
 */
public class TwoBitPredictorTests
{
  SimulationConfig config;
  
  @Before
  public void setUp() throws Exception
  {
    config                                 = SimulationConfig.getDefaultConfiguration();
    config.cpuConfig.predictorType         = BitPredictor.PredictorType.TWO_BIT_PREDICTOR;
    config.cpuConfig.predictorDefaultState = STRONGLY_TAKEN;
  }
  
  /**
   * Make the predictor default to weakly taken
   */
  public void makeWeaklyTaken()
  {
    config.cpuConfig.predictorDefaultState = WEAKLY_TAKEN;
  }
  
  /**
   * Make the predictor default to strongly taken
   */
  public void makeStronglyTaken()
  {
    config.cpuConfig.predictorDefaultState = STRONGLY_TAKEN;
  }
  
  /**
   * Make the predictor default to weakly not taken
   */
  public void makeWeaklyNotTaken()
  {
    config.cpuConfig.predictorDefaultState = WEAKLY_NOT_TAKEN;
  }
  
  /**
   * In this test, the jump is not predicted two times, then it is predicted always.
   */
  @Test
  public void testPredictorStronglyNotTakenLoop()
  {
    makeStronglyNotTaken();
    setCode("""
                    loop:
                      addi x0, x0, 0
                      beq x0, x0, loop
                    """);
    
    Cpu cpu = new Cpu(config);
    
    while (true)
    {
      cpu.step();
      
      SimulationStatistics.InstructionStats beq = cpu.cpuState.statistics.instructionStats.get(1);
      switch (beq.committedCount)
      {
        case 0:
          break;
        case 1, 2:
          Assert.assertEquals(0, beq.correctlyPredicted);
          break;
        case 3:
          Assert.assertEquals(1, beq.correctlyPredicted);
          break;
        case 4:
          Assert.assertEquals(2, beq.correctlyPredicted);
          return;
      }
    }
  }
  
  /**
   * Variation on testPredictorStronglyNotTakenLoop.
   * In this test, the jump is always predicted (apart from the first time, mandatory prediction fail).
   */
  @Test
  public void testPredictorWeaklyTakenLoop()
  {
    makeWeaklyTaken();
    setCode("""
                    loop:
                      addi x0, x0, 0
                      beq x0, x0, loop
                    """);
    
    Cpu cpu = new Cpu(config);
    
    while (true)
    {
      cpu.step();
      
      SimulationStatistics.InstructionStats beq = cpu.cpuState.statistics.instructionStats.get(1);
      switch (beq.committedCount)
      {
        case 0:
          break;
        case 1:
          Assert.assertEquals(0, beq.correctlyPredicted);
          break;
        case 2:
          Assert.assertEquals(1, beq.correctlyPredicted);
          break;
        case 3:
          Assert.assertEquals(2, beq.correctlyPredicted);
          return;
      }
    }
  }
  
  /**
   * Make the predictor default to strongly not taken
   */
  public void makeStronglyNotTaken()
  {
    config.cpuConfig.predictorDefaultState = STRONGLY_NOT_TAKEN;
  }
  
  /**
   * Set code to be executed
   */
  public void setCode(String code)
  {
    config.code = code;
  }
  
  /**
   * The two bit predictor cannot adjust to the +-+-+- pattern.
   * The best it can do is to predict every or no jump.
   */
  @Test
  public void testPredictorHalfCorrect()
  {
    makeStronglyTaken();
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
    // Predicted: * y Y y Y y Y y Y y (5+1/10) (*: Y but mandatory negative prediction)
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Check correctness of program - sum of even numbers from 0 to 9
    Assert.assertEquals(2 + 4 + 6 + 8,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t1").getValue(DataTypeEnum.kInt));
    
    // The branch prediction of instruction [4] is always wrong.
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(5 + 1,
                        stats.correctlyPredicted); // 5 correct, 1 mandatory prediction fail, which turns out to be correct
  }
  
  /**
   * Same as testPredictorHalfCorrect, but the StronglyNotTaken initial state is chosen.
   */
  @Test
  public void testPredictorHalfCorrect2()
  {
    makeStronglyNotTaken();
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
    // Predicted: N N n N n N n N n N (5/10)
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Check correctness of program - sum of even numbers from 0 to 9
    Assert.assertEquals(2 + 4 + 6 + 8,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t1").getValue(DataTypeEnum.kInt));
    
    // The branch prediction of instruction [4] is always wrong.
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(5, stats.correctlyPredicted);
  }
  
  /**
   * Same as testPredictorHalfCorrect, but the WeaklyTaken initial state is chosen.
   * This results in 0% correct predictions.
   */
  @Test
  public void testPredictorNeverCorrect()
  {
    makeWeaklyTaken();
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
    // Predicted: * n y n y n y n y n (0/10) (would have said 'y' on the first iteration, but mandatory negative prediction)
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Check correctness of program - sum of even numbers from 0 to 9
    Assert.assertEquals(2 + 4 + 6 + 8,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t1").getValue(DataTypeEnum.kInt));
    
    // The branch prediction of instruction [4] is always wrong.
    SimulationStatistics.InstructionStats stats = cpu.cpuState.statistics.instructionStats.get(4);
    Assert.assertEquals(10, stats.committedCount);
    Assert.assertEquals(1, stats.correctlyPredicted); // Just the (first) mandatory negative prediction was wrong
  }
  
  /**
   * The two bit predictor has inertia, so it has one bad prediction per inner loop.
   * <p>
   * Original code:
   * <pre>
   *   int main(){
   *   int volatile x = 0;
   *   for (int i = 0; i < 10; i++) {
   *      for (int j = 0; j < 10; j++) {
   *        x += i*j;
   *      }
   *   }
   *   return x;
   * }
   * </pre>
   */
  @Test
  public void testNestedLoop()
  {
    makeWeaklyTaken();
    setCode("""
                        li a2,0
                        li a1,10
                    outer:
                        li a3,0
                        li a4,10
                    inner:
                        lw a5,12(sp)
                        addi a4,a4,-1
                        add t0,a3,a5
                        sw t0,12(sp)
                        add a3,a3,a2
                        bne a4,zero,inner # [9]
                        addi a2,a2,1
                        bne a2,a1,outer # [11]
                    """);
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Check correctness of program - sum of i*j for i,j in [0,9]
    int result = 0;
    for (int i = 0; i < 10; i++)
    {
      for (int j = 0; j < 10; j++)
      {
        result += i * j;
      }
    }
    Assert.assertEquals(result,
                        (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("t0").getValue(DataTypeEnum.kInt));
    
    // The inner loop is executed 10 times, so 10 prediction fails
    SimulationStatistics.InstructionStats inner = cpu.cpuState.statistics.instructionStats.get(9);
    Assert.assertEquals(100, inner.committedCount);
    Assert.assertEquals(90 - 1,
                        inner.correctlyPredicted); // First one was the mandatory prediction fail, counts as incorrect
    
    // The outer loop is executed once, so 1 prediction fail + 1 mandatory prediction fail
    SimulationStatistics.InstructionStats outer = cpu.cpuState.statistics.instructionStats.get(11);
    Assert.assertEquals(10, outer.committedCount);
    Assert.assertEquals(10 - 2,
                        outer.correctlyPredicted); // 1 prediction fail (last one) + 1 mandatory negative prediction
  }
}
