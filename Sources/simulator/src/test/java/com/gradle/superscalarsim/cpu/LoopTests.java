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

import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
  
  /**
   * Simcodes hold references to input code models.
   * When renaming, the original input code model should not be changed.
   */
  @Test
  public void LoopInstructionRenamingTest()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = ExecuteUtil.getLoopProgram(5);
    Cpu cpu = new Cpu(cfg);
    
    // Obtain original value of inputcodemodels
    List<InputCodeModel> parsedCode = cpu.cpuState.instructionMemoryBlock.getCode();
    // Copy the list
    List<InputCodeModel> parsedCodeCopy = List.copyOf(parsedCode);
    
    
    // Count steps
    int steps = 0;
    while (!cpu.simEnded())
    {
      cpu.step();
      steps++;
    }
    
    // Assert that the original input code models are not changed
    Assert.assertEquals(parsedCode, parsedCodeCopy);
  }
  
  /**
   * The program writes numbers from 0 to 19 to memory
   * TODO: go back to it later
   */
  //  @Test
  public void test_write_memory()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            wr:
                addi sp,sp,-32
                sw s0,28(sp)
                addi s0,sp,32
                sw zero,-20(s0)
                j .L2
            .L3:
                lw a5,-20(s0)
                lw a4,-20(s0)
                andi a4,a4,0xff
                sb a4,0(a5)
                lw a5,-20(s0)
                addi a5,a5,1
                sw a5,-20(s0)
            .L2:
                lw a4,-20(s0)
                li a5,19
                ble a4,a5,.L3
                nop
                mv a0,a5
                lw s0,28(sp)
                addi sp,sp,32
                """;
    Cpu cpu = new Cpu(cfg);
    cpu.execute(false);
    
    // Assert that bytes 0 to 19 are written to memory
    for (int i = 0; i < 20; i++)
    {
      Assert.assertEquals(i, cpu.cpuState.simulatedMemory.getFromMemory((long) i));
    }
  }
}
