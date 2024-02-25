package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.cpu.StopReason;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Run tests for different combinations of latencies for functional units
 */
@RunWith(Theories.class)
public class FunctionalUnitsTests
{
  @DataPoints("adderLatencies")
  public static int[] adderLatency = {1, 2, 3};
  
  @DataPoints("multiplierLatencies")
  public static int[] multiplierLatency = {2, 3};
  
  @DataPoints("dividerLatencies")
  public static int[] dividerLatency = {1, 20, 60};
  
  @DataPoints("branchLatencies")
  public static int[] branchLatency = {1, 3, 20};
  
  @DataPoints("memoryLatencies")
  public static int[] memoryLatency = {1, 5, 20};
  
  @DataPoints("lsLatencies")
  public static int[] lsLatency = {1, 2, 15};
  
  @DataPoints("programs")
  public static String[] programs = {"""
            addi x1, x0, 3
            subi x5, x4, 5
            mv x5, x1
            """, """
              addi x3, x0, 3
            loop:
              beq  x3,x0,loopEnd
              subi x3,x3,1
              jal  x0,loop
            loopEnd:
            """, """
            writeMem:
                lla a4,ptr
                li a5,0
                li a3,9
            .L2:
                sb a5,0(a4)
                addi a5,a5,1
                addi a4,a4,1
                bne a5,a3,.L2
                ret
                .align 2
            ptr:
                .zero 9"""};
  
  /**
   * Tests different combinations of latencies for adder, multiplier and divider.
   */
  @Theory
  public void latencyCombinations(@FromDataPoints("adderLatencies") int adderLatency,
                                  @FromDataPoints("multiplierLatencies") int multiplierLatency,
                                  @FromDataPoints("dividerLatencies") int dividerLatency,
                                  @FromDataPoints("branchLatencies") int branchLatency,
                                  @FromDataPoints("memoryLatencies") int memoryLatency,
                                  @FromDataPoints("lsLatencies") int lsLatency,
                                  @FromDataPoints("programs") String program)
  {
    SimulationConfig config = new SimulationConfig();
    config.code = program;
    
    List<FunctionalUnitDescription> fUnits = new ArrayList<>();
    fUnits.add(new FunctionalUnitDescription(0, FunctionalUnitDescription.Type.FX,
                                             List.of(new FunctionalUnitDescription.Capability(
                                                             FunctionalUnitDescription.CapabilityName.addition, adderLatency),
                                                     new FunctionalUnitDescription.Capability(
                                                             FunctionalUnitDescription.CapabilityName.multiplication,
                                                             multiplierLatency),
                                                     new FunctionalUnitDescription.Capability(
                                                             FunctionalUnitDescription.CapabilityName.division,
                                                             dividerLatency))));
    
    fUnits.add(new FunctionalUnitDescription(1, FunctionalUnitDescription.Type.Branch, branchLatency));
    fUnits.add(new FunctionalUnitDescription(2, FunctionalUnitDescription.Type.Memory, memoryLatency));
    fUnits.add(new FunctionalUnitDescription(3, FunctionalUnitDescription.Type.L_S, lsLatency));
    config.cpuConfig.fUnits        = fUnits;
    config.cpuConfig.storeBehavior = "write-through";
    
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    // Assert
    Assert.assertTrue(cpu.stopReason == StopReason.kEndOfCode || cpu.stopReason == StopReason.kCallStackHalt);
    
    // Asserts specific to programs
    if (program == programs[0])
    {
      Assert.assertEquals(3, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt));
    }
    else if (program == programs[1])
    {
      Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
    }
    else if (program == programs[2])
    {
      long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("ptr");
      Assert.assertEquals(0, cpu.cpuState.simulatedMemory.getFromMemory(offset));
      Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset + 1));
      Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset + 2));
      Assert.assertEquals(3, cpu.cpuState.simulatedMemory.getFromMemory(offset + 3));
    }
    else
    {
      Assert.fail("Unknown program");
    }
  }
}
