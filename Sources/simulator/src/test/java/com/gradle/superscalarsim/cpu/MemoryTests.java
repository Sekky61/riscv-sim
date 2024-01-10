package com.gradle.superscalarsim.cpu;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MemoryTests
{
  private SimulationConfig cpuConfig;
  
  @Before
  public void setup()
  {
    cpuConfig = SimulationConfig.getDefaultConfiguration();
  }
  
  @Test
  public void test_allocate_constant()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
  }
  
  @Test
  public void test_allocate_constant_2labels()
  {
    // Setup + exercise
    cpuConfig.code = """
            a:
            b:
            .word 1""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long aOffset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("a");
    long bOffset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("b");
    Assert.assertEquals(aOffset, bOffset);
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(aOffset));
  }
  
  @Test
  public void test_allocate_multiple()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1,2,4""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
    Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset + 4));
    Assert.assertEquals(4, cpu.cpuState.simulatedMemory.getFromMemory(offset + 8));
  }
  
  @Test
  public void test_allocate_multiple2()
  {
    // Setup + exercise
    cpuConfig.code = """
            arr:
            .word 1
            .word 2
            .word 4""";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset));
    Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset + 4));
    Assert.assertEquals(4, cpu.cpuState.simulatedMemory.getFromMemory(offset + 8));
  }
  
  @Test
  public void test_allocate_mixed()
  {
    // Setup + exercise
    cpuConfig.code = """
            g:
            .byte   1
            .zero   3
            .word   42""";
    
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long   offset   = cpu.cpuState.instructionMemoryBlock.getLabelPosition("g");
    byte[] expected = {1, 0, 0, 0, 42, 0, 0, 0};
    Assert.assertArrayEquals(expected, cpu.cpuState.simulatedMemory.getFromMemory(offset, 8));
  }
  
  @Test
  public void test_allocate_from_config()
  {
    // Setup + exercise
    cpuConfig.code = """
            la x6, arr2
            arr:
            .byte 1, 2""";
    cpuConfig.memoryLocations.add(new MemoryLocation("arr2", 4, DataTypeEnum.kInt, List.of("3")));
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    long offset1 = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    Assert.assertEquals(1, cpu.cpuState.simulatedMemory.getFromMemory(offset1));
    Assert.assertEquals(2, cpu.cpuState.simulatedMemory.getFromMemory(offset1 + 1));
    
    long offset2 = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr2");
    Assert.assertEquals(0, offset2 % 16);
    Assert.assertEquals(3, cpu.cpuState.simulatedMemory.getFromMemory(offset2));
    Assert.assertEquals(0, cpu.cpuState.simulatedMemory.getFromMemory(offset2 + 1));
    Assert.assertEquals(0, cpu.cpuState.simulatedMemory.getFromMemory(offset2 + 2));
    Assert.assertEquals(0, cpu.cpuState.simulatedMemory.getFromMemory(offset2 + 3));
  }
}
