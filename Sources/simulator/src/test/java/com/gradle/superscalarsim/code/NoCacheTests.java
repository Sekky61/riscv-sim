package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    cpu.execute();
    
    // Assert
    Assert.assertEquals(42, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesRead());
    // 4 bytes loaded from main memory. Not 32 for a cache line.
    Assert.assertEquals(4, cpu.cpuState.statistics.mainMemoryLoadedBytes);
    Assert.assertEquals(0, cpu.cpuState.statistics.mainMemoryStoredBytes);
  }
  
  @Test
  public void testStore()
  {
    // Setup + exercise
    // the x0 has to be a register
    cfg.code = """
            addi x1, x0, 84
            sw x1, ptr(x0)""";
    Cpu cpu = new Cpu(cfg);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(84, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesRead());
    Assert.assertEquals(0, cpu.cpuState.statistics.cache.getBytesWritten());
    // 4 bytes loaded from main memory. Not 32 for a cache line.
    Assert.assertEquals(0, cpu.cpuState.statistics.mainMemoryLoadedBytes);
    Assert.assertEquals(4, cpu.cpuState.statistics.mainMemoryStoredBytes);
  }
}
