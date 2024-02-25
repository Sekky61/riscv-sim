package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.cpu.StopReason;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.FunctionalUnitDescription.Capability;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assume.assumeTrue;

/**
 * Run tests for different combinations of capabilities for functional units
 */
@RunWith(Theories.class)
public class FunctionalUnitCapabilityTests
{
  @DataPoints("fxCapabilites")
  public static Capability[] fxCapabilities = { //
          new Capability(FunctionalUnitDescription.CapabilityName.addition, 1), //
          new Capability(FunctionalUnitDescription.CapabilityName.multiplication, 2), //
          new Capability(FunctionalUnitDescription.CapabilityName.division, 3), //
          new Capability(FunctionalUnitDescription.CapabilityName.bitwise, 1), //
          new Capability(FunctionalUnitDescription.CapabilityName.special, 1) //
  }; //
  
  @DataPoints("memoryUnitCount")
  public static int[] memoryUnitCount = {0, 1, 2, 5};
  
  @DataPoints("branchUnitCount")
  public static int[] branchUnitCount = {0, 1, 4};
  
  @DataPoints("lsUnitCount")
  public static int[] lsUnitCount = {0, 1, 3};
  
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
  public void latencyCombinations(@FromDataPoints("fxCapabilites") Capability adderCap1,
                                  @FromDataPoints("fxCapabilites") Capability adderCap2,
                                  @FromDataPoints("fxCapabilites") Capability adderCap3,
                                  @FromDataPoints("memoryUnitCount") int memoryUnitCount,
                                  @FromDataPoints("branchUnitCount") int branchUnitCount,
                                  @FromDataPoints("lsUnitCount") int lsUnitCount,
                                  @FromDataPoints("programs") String program)
  {
    assumeTrue(adderCap1.name != adderCap2.name);
    assumeTrue(adderCap1.name != adderCap3.name);
    assumeTrue(adderCap2.name != adderCap3.name);
    
    boolean anyAdder = adderCap1.name == FunctionalUnitDescription.CapabilityName.addition || adderCap2.name == FunctionalUnitDescription.CapabilityName.addition || adderCap3.name == FunctionalUnitDescription.CapabilityName.addition;
    assumeTrue(anyAdder);
    
    if (program == programs[1])
    {
      assumeTrue(branchUnitCount > 0);
    }
    
    if (program == programs[2])
    {
      assumeTrue(branchUnitCount > 0);
      assumeTrue(lsUnitCount > 0);
      assumeTrue(memoryUnitCount > 0);
    }
    
    SimulationConfig config = new SimulationConfig();
    config.code = program;
    
    List<FunctionalUnitDescription> fUnits = new ArrayList<>();
    int                             id     = 0;
    for (int i = 0; i < memoryUnitCount; i++)
    {
      fUnits.add(new FunctionalUnitDescription(id++, FunctionalUnitDescription.Type.Memory, 1));
    }
    
    for (int i = 0; i < branchUnitCount; i++)
    {
      fUnits.add(new FunctionalUnitDescription(id++, FunctionalUnitDescription.Type.Branch, 1));
    }
    
    for (int i = 0; i < lsUnitCount; i++)
    {
      fUnits.add(new FunctionalUnitDescription(id++, FunctionalUnitDescription.Type.L_S, 1));
    }
    
    // FX capabilities
    List<Capability> capabilities = new ArrayList<>();
    capabilities.add(adderCap1);
    capabilities.add(adderCap2);
    capabilities.add(adderCap3);
    fUnits.add(new FunctionalUnitDescription(id++, FunctionalUnitDescription.Type.FX, capabilities));
    
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
