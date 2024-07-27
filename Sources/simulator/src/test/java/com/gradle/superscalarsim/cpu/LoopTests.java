/**
 * @file LoopTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests for loops - studies behavior of larger programs as a whole
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

public class LoopTests
{
  /**
   * jr ra
   */
  @Test
  public void test_jump_on_yourself()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = "jr x0";
    Cpu cpu = new Cpu(cfg);
    
    // Should not throw an exception, loop until infinity
    for (int i = 0; i < 100; i++)
    {
      cpu.step();
    }
    
    Assert.assertTrue(true);
    Assert.assertTrue(cpu.cpuState.statistics.getCommittedInstructions() > 10);
  }
  
  @Test
  public void testLoopArrayWrite()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    String codeTemplate = """
            writeMem:
                lla a4,ptr
                li a5,0
                li a3,%d
            .L2:
                sb a5,0(a4)
                addi a5,a5,1
                addi a4,a4,1
                bne a5,a3,.L2
                ret
                .align 2
            ptr:
                .zero %d""";
    cfg.cpuConfig.storeBehavior = "write-through";
    cfg.code                    = codeTemplate.formatted(17, 17);
    
    Cpu cpu = new Cpu(cfg);
    cpu.execute(false);
    
    int ptr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("ptr");
    for (int i = 0; i < 9; i++)
    {
      Assert.assertEquals(i, cpu.cpuState.simulatedMemory.getFromMemory(ptr + i));
    }
  }
}
