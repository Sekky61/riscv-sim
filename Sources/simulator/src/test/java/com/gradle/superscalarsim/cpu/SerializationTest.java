/**
 * @file SerializationTest.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests assuring that serialization and deserialization of the CPU state
 * does not change the behavior of the CPU
 * @date 26 Sep      2023 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gradle.superscalarsim.cpu;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SerializationTest
{
  @Test
  public void initialStateSerDeStaysSameTest()
  {
    Cpu    cpu     = new Cpu();
    String jsonnew = cpu.cpuState.serialize();
    
    CpuState deserState = CpuState.deserialize(jsonnew);
    // Serialize again
    String jsonnew2 = deserState.serialize();
    
    Assert.assertEquals(jsonnew, jsonnew2);
  }
  
  /**
   * There was a bug in serialization of labels
   */
  @Test
  public void cpuState_label_serialization()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.code = """
            addi x3, x0, 10000
            loop:
            """;
    
    Cpu cpu = new Cpu(cfg);
    cpu.simulateState(0);
    
    // Exercise
    CpuState stateCopy = cpu.cpuState.deepCopy();
    
    // Assert
    Assert.assertEquals(cpu.cpuState, stateCopy);
  }
  
  @Test
  public void cpuState_afterTicks_deepcopy_theSame()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.code = """
            addi x3, x0, 10000
            addi x8, x0, 50
            sw x8, 16(x0)
            loop:
            beq x3, x0, loopEnd
            lw x8, 16(x0)
            addi x8, x8, 1
            sw x8, 16(x0)
            subi x3, x3, 1
            jal x0, loop
            loopEnd:""";
    Cpu cpu = new Cpu(cfg);
    cpu.simulateState(200);
    
    // Exercise
    CpuState stateCopy = cpu.cpuState.deepCopy();
    
    String meJson    = cpu.cpuState.serialize();
    String otherJson = stateCopy.serialize();
    
    // Assert
    Assert.assertEquals(meJson, otherJson);
  }
  
  @Test
  public void cpuState_afterDeserialization_canPickup()
  {
    CpuConfiguration cfg = CpuConfiguration.getDefaultConfiguration();
    cfg.code = """
            addi x3, x0, 10000
            addi x8, x0, 50
            sw x8,16(x0)
            loop:
            beq x3, x0, loopEnd
            lw x8,16(x0)
            addi x8, x8, 1
            sw x8,16(x0)
            subi x3, x3, 1
            jal x0, loop
            loopEnd:""";
    Cpu cpu = new Cpu(cfg);
    cpu.simulateState(50);
    String stateSerialized = cpu.cpuState.serialize();
    
    CpuState stateDe = CpuState.deserialize(stateSerialized);
    // When asking for the state at time 52, simulation step should only be called twice
    CpuState stateSpy = Mockito.spy(stateDe);
    Cpu      cpu2     = new Cpu(cfg, stateSpy);
    cpu2.simulateState(52);
    
    // Assert
    Mockito.verify(stateSpy, Mockito.times(2)).step();
  }
}
