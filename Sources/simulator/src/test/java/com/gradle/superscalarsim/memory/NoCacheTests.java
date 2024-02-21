package com.gradle.superscalarsim.memory;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.cpu.StopReason;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.gradle.superscalarsim.cpu.AlgorithmTests.quicksortAssembly;

public class NoCacheTests
{
  private SimulationConfig cfg;
  
  @Before
  public void setup()
  {
    cfg                        = SimulationConfig.getDefaultConfiguration();
    cfg.cpuConfig.useCache     = false;
    cfg.cpuConfig.storeLatency = 2;
    cfg.cpuConfig.loadLatency  = 2;
    
    cfg.memoryLocations.add(new MemoryLocation("ptr", 1, DataTypeEnum.kInt, List.of("42")));
  }
  
  @Test
  public void testLoad()
  {
    // Setup + exercise
    // the x0 has to be a register
    cfg.code = "lw x1, ptr(x0)";
    Cpu cpu = new Cpu(cfg);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals(42, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesRead());
    // 4 bytes loaded from main memory. Not 32 for a cache line.
    Assert.assertEquals(4, cpu.cpuState.statistics.mainMemoryLoadedBytes);
    Assert.assertEquals(0, cpu.cpuState.statistics.mainMemoryStoredBytes);
  }
  
  @Test
  public void testTwoLoads()
  {
    // Setup + exercise
    // the x0 has to be a register
    cfg.code = """
            addi x3, x0, ptr
            subi x4, x3, 2
            lw x1, 0(x4)
            lw x2, 0(x3)""";
    Cpu cpu = new Cpu(cfg);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals((42 << 16),
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(42, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesRead());
    // 4 bytes loaded from main memory. Not 32 for a cache line.
    Assert.assertEquals(8, cpu.cpuState.statistics.mainMemoryLoadedBytes);
    Assert.assertEquals(0, cpu.cpuState.statistics.mainMemoryStoredBytes);
  }
  
  @Test
  public void testStore()
  {
    // Setup + exercise
    cfg.code = """
            addi x1, x0, 84
            sw x1, ptr(x0)""";
    Cpu cpu = new Cpu(cfg);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals(84, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesRead());
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesWritten());
    // 4 bytes loaded from main memory. Not 32 for a cache line.
    Assert.assertEquals(0, cpu.cpuState.statistics.mainMemoryLoadedBytes);
    Assert.assertEquals(4, cpu.cpuState.statistics.mainMemoryStoredBytes);
  }
  
  @Test
  public void test_quicksortNoCache()
  {
    // Alignment 2^4
    MemoryLocation arr = new MemoryLocation("arr", 4, DataTypeEnum.kByte,
                                            List.of("1", "16", "3", "10", "4", "6", "7", "8", "9", "5", "11", "12",
                                                    "13", "14", "15", "2"));
    
    cfg.memoryLocations = List.of(arr);
    cfg.code            = quicksortAssembly; // maybe migrate to file or C version
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute(true);
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.stopReason);
    
    long arrPtr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    for (int i = 0; i < 16; i++)
    {
      Assert.assertEquals(i + 1, cpu.cpuState.simulatedMemory.getFromMemory(arrPtr + i));
    }
  }
}
