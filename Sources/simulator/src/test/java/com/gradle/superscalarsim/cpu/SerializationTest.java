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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gradle.superscalarsim.serialization.GsonConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SerializationTest
{
  
  @Test
  public void afterStepSerDeStaysSameTest()
  {
    // Setup cpu and do 5 steps
    CpuConfiguration cpuConfig = CpuConfiguration.getDefaultConfiguration();
    cpuConfig.code = ExecuteUtil.artihmeticProgram;
    Cpu cpu = new Cpu(cpuConfig);
    
    for (int i = 0; i < 5; i++)
    {
      cpu.step();
    }
    
    // Exercise
    // Serialize and deserialize and serialize again
    String   json       = JsonWriter.objectToJson(cpu.cpuState);
    CpuState deserState = (CpuState) JsonReader.jsonToJava(json);
    String   json2      = JsonWriter.objectToJson(deserState);
    
    // Assert
    Assert.assertEquals(json, json2);
  }
  
  @Test
  public void serDeHasNoEffectOnBehavior()
  {
    // Setup cpu
    CpuConfiguration cpuConfig = CpuConfiguration.getDefaultConfiguration();
    cpuConfig.code = ExecuteUtil.artihmeticProgram;
    Cpu cpu  = new Cpu(cpuConfig);
    Cpu cpu2 = new Cpu(cpuConfig);
    
    // Exercise - step both cpus, serialize and deserialize one of them repeatedly
    for (int i = 0; i < 10; i++)
    {
      cpu.step();
      
      String json = cpu2.cpuState.serialize();
      cpu2.cpuState = CpuState.deserialize(json);
      cpu2.step();
    }
    
    // Assert
    String json        = cpu.cpuState.serialize();
    String jsonPretty  = JsonWriter.formatJson(json);
    String json2       = cpu2.cpuState.serialize();
    String jsonPretty2 = JsonWriter.formatJson(json2);
    
    Assert.assertEquals(jsonPretty, jsonPretty2);
    Assert.assertEquals(cpu.cpuState, cpu2.cpuState);
  }
  
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
  
  @Test
  public void additionTest()
  {
    CpuConfiguration cpuConfig = CpuConfiguration.getDefaultConfiguration();
    cpuConfig.code = "addi x1, x1, 5";
    Cpu cpu    = new Cpu(cpuConfig);
    Cpu cpuSer = new Cpu(cpuConfig);
    
    while (!cpu.simEnded())
    {
      cpu.step();
      
      cpuSer.step();
      String json = cpuSer.cpuState.serialize();
      cpuSer.cpuState = CpuState.deserialize(json);
      
      // Compare each step
      String json1       = cpu.cpuState.serialize();
      String jsonPretty  = JsonWriter.formatJson(json1);
      String json2       = cpuSer.cpuState.serialize();
      String jsonPretty2 = JsonWriter.formatJson(json2);
      Assert.assertEquals(jsonPretty, jsonPretty2);
    }
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
    
    Assert.assertEquals(1, cpuSer.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(5, cpuSer.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
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
    
    String meJson    = JsonWriter.objectToJson(cpu.cpuState, GsonConfiguration.getJsonWriterOptions());
    String otherJson = JsonWriter.objectToJson(stateCopy, GsonConfiguration.getJsonWriterOptions());
    
    String meJsonPretty    = JsonWriter.formatJson(meJson);
    String otherJsonPretty = JsonWriter.formatJson(otherJson);
    
    // Assert
    Assert.assertEquals(meJsonPretty, otherJsonPretty);
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
