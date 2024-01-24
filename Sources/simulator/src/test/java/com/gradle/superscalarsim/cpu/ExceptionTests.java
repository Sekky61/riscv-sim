/**
 * @file ExceptionTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests for exceptions in the simulator
 * @date 24 Jan      2024 13:00 (created)
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

import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test exceptions like divide by zero, invalid memory access, etc.
 * The simulator must stop after committing an exception, so not if the instruction is speculative.
 */
public class ExceptionTests
{
  @Test
  public void test_divZero()
  {
    // A simple function with ABI return. The last instruction is a return instruction, it should halt the simulation.
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            main:
                addi    x10, x0, 6
                addi    x11, x0, 0
                div     x12, x10, x11
                addi    x13, x0, 1
                    """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // We have stopped
    Assert.assertSame(StopReason.kException, cpu.stopReason);
    Assert.assertEquals(3, cpu.cpuState.statistics.committedInstructions);
    // Cpu state
    Assert.assertEquals(0, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x13").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void test_divZeroSpeculative()
  {
    // A div that is not committed cannot cause an exception.
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            main:
                addi    x10, x0, 6
                addi    x11, x0, 0
                beq     x10, x10, label
                div     x12, x10, x11
            label:
                addi    x13, x0, 1
                    """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // We have stopped (ask the cpu why stopped, not ROB!)
    Assert.assertEquals(4, cpu.cpuState.statistics.committedInstructions);
    Assert.assertSame(StopReason.kEndOfCode, cpu.stopReason);
    // Cpu state
    Assert.assertEquals(1, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x13").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void test_badMemoryLoad()
  {
    // A simple function with ABI return. The last instruction is a return instruction, it should halt the simulation.
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            lw      x10, -5(x0)
                """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // We have stopped
    Assert.assertSame(StopReason.kException, cpu.stopReason);
    Assert.assertEquals(1, cpu.cpuState.statistics.committedInstructions);
    // Cpu state
    Assert.assertEquals(0, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x13").getValue(DataTypeEnum.kInt));
  }
}
