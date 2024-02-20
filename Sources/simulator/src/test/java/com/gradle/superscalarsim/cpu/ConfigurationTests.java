/**
 * @file ConfigurationTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests for the CpuConfiguration class
 * @date 20 Jan      2024 10:00 (created)
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

import com.gradle.superscalarsim.blocks.branch.BitPredictor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ConfigurationTests
{
  
  @Test
  public void testDefaultConfiguration_Passes()
  {
    CpuConfig config = CpuConfig.getDefaultConfiguration();
    
    CpuConfigValidator validator = new CpuConfigValidator();
    validator.validate(config);
    Assert.assertTrue(validator.isValid());
  }
  
  @Test
  public void testNoFus_Fails()
  {
    CpuConfig config = CpuConfig.getDefaultConfiguration();
    config.fUnits = new ArrayList<>();
    
    CpuConfigValidator validator = new CpuConfigValidator();
    validator.validate(config);
    Assert.assertFalse(validator.isValid());
  }
  
  @Test
  public void testPredictors()
  {
    CpuConfig config = CpuConfig.getDefaultConfiguration();
    // State 4 does not exist on 2-bit predictor
    config.predictorType         = BitPredictor.PredictorType.TWO_BIT_PREDICTOR;
    config.predictorDefaultState = 4;
    
    CpuConfigValidator validator = new CpuConfigValidator();
    validator.validate(config);
    Assert.assertFalse(validator.isValid());
  }
  
  @Test
  public void testDefaultConfig_BuildsState()
  {
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    Cpu              cpu    = new Cpu(config);
    Assert.assertNotNull(cpu.cpuState);
  }
  
  @Test
  public void testFunctionEntryPoint_Passes()
  {
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.entryPoint = "main";
    config.code       = """
            a:
              add a6,a0,a2
            main:
              addi sp,sp,-16
              li a2,15
            """;
    Cpu cpu = new Cpu(config);
    Assert.assertNotNull(cpu.cpuState);
    
    cpu.execute(false);
    Assert.assertEquals(2, cpu.cpuState.statistics.committedInstructions);
  }
  
  @Test
  public void testNonExistingFunctionEntryPoint_FailsValidation()
  {
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.entryPoint = "b";
    config.code       = """
            a:
              add a6,a0,a2
            main:
              addi sp,sp,-16
              li a2,15
            """;
    Assert.assertFalse(config.validate().valid);
  }
  
  @Test
  public void testNonExistingFunctionEntryPoint_Fails()
  {
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.entryPoint = "b";
    config.code       = """
            a:
              add a6,a0,a2
            main:
              addi sp,sp,-16
              li a2,15
            """;
    Assert.assertThrows(IllegalArgumentException.class, () -> new Cpu(config));
  }
}
