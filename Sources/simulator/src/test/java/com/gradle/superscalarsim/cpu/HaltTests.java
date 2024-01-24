/**
 * @file HaltTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests for the ABI return instruction
 * @date 23 Jan      2024 13:00 (created)
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
 * GCC generates functions with an ABI. The last instruction of a function is a return instruction.
 * If the function is entry point of te simulation, committing the return instruction will halt the simulation.
 */
public class HaltTests
{
  @Test
  public void test_halts()
  {
    // A simple function with ABI return. The last instruction is a return instruction, it should halt the simulation.
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            main:
                addi    sp,sp,-32
                sw      s0,28(sp)
                addi    s0,sp,32
                li      a5,5
                sw      a5,-20(s0)
                li      a4,9
                lw      a5,-20(s0)
                div     a5,a4,a5
                sw      a5,-24(s0)
                lw      a5,-24(s0)
                mv      a0,a5
                lw      s0,28(sp)
                addi    sp,sp,32
                jr      ra
                    """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // Assert
    Assert.assertEquals(14, cpu.cpuState.statistics.committedInstructions);
    Assert.assertSame(StopReason.kCallStackHalt, cpu.cpuState.reorderBufferBlock.stopReason);
  }
  
  @Test
  public void test_1levelDeepHalt()
  {
    // One level deep call stack.
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            main:
                addi    sp,sp,-32
                sw      ra,28(sp)
                sw      s0,24(sp)
                addi    s0,sp,32
                li      a0,4
                call    f           # 20
                sw      a0,-20(s0)
                lw      a5,-20(s0)
                mv      a0,a5
                lw      ra,28(sp)
                lw      s0,24(sp)   # 40
                addi    sp,sp,32
                jr      ra
            f:
                addi    sp,sp,-32
                sw      s0,28(sp)
                addi    s0,sp,32    # 60
                sw      a0,-20(s0)
                lw      a5,-20(s0)
                slli    a5,a5,1
                mv      a0,a5
                lw      s0,28(sp)   # 80
                addi    sp,sp,32
                jr      ra
                    """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.cpuState.reorderBufferBlock.stopReason);
    // a0 should have 8 as a result (f is a function that multiplies by 2)
    Assert.assertEquals(8, cpu.cpuState.unifiedRegisterFileBlock.getRegister("a0").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void test_1levelDeepHaltWithRet()
  {
    // Same as test_1levelDeepHalt, but optimized and uses ret
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
            main:
              li      a0,8
              ret
            f:
              slli    a0,a0,1
              ret
            """;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.cpuState.reorderBufferBlock.stopReason);
    // a0 should have 8 as a result (f is a function that multiplies by 2)
    Assert.assertEquals(8, cpu.cpuState.unifiedRegisterFileBlock.getRegister("a0").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(2, cpu.cpuState.statistics.committedInstructions);
  }
}
