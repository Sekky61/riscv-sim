/**
 * @file DebugPrintTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests of some C algorithms
 * @date 29 Jan      2024 9:00 (created)
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

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.instruction.DebugInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DebugPrintTests
{
  private SimulationConfig cpuConfig;
  
  @Before
  public void setup()
  {
    cpuConfig = SimulationConfig.getDefaultConfiguration();
  }
  
  @Test
  public void testDebugCommentParsing()
  {
    // Setup + exercise
    cpuConfig.code = """
            addi x6, x6, 64
            addi x6, x6, 16 #DEBUG"Hello World"
            subi x6, x6, 2 #haha ${
            """;
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute(false);
    
    // Assert
    // #DEBUG is parsed
    Assert.assertEquals("Hello World",
                        cpu.cpuState.instructionMemoryBlock.getInstructionAt(4).getDebugInfo().formatString());
    // Other comments are ignored
    Assert.assertNull(cpu.cpuState.instructionMemoryBlock.getInstructionAt(8).getDebugInfo());
  }
  
  @Test
  public void testFormatter()
  {
    // Setup + exercise
    UnifiedRegisterFileBlock registerFile = new UnifiedRegisterFileBlock(new InitLoader(), 1,
                                                                         new RegisterModelFactory());
    DebugLog debugLog = new DebugLog(registerFile);
    
    debugLog.add(new DebugInfo("Hello World"), 0);
    debugLog.add(new DebugInfo("x5 = ${x5}, x6 = ${x6}"), 5);
    
    // Assert
    Assert.assertEquals("Hello World", debugLog.getEntries().get(0).getMessage());
    Assert.assertEquals("x5 = 0, x6 = 0", debugLog.getEntries().get(1).getMessage());
  }
  
  @Test
  public void testBadFormatString()
  {
    // Setup + exercise
    UnifiedRegisterFileBlock registerFile = new UnifiedRegisterFileBlock(new InitLoader(), 1,
                                                                         new RegisterModelFactory());
    DebugLog debugLog = new DebugLog(registerFile);
    
    debugLog.add(new DebugInfo("Hello ${abcd}"), 0);
    
    // Assert
    Assert.assertEquals("Hello [UNKNOWN]", debugLog.getEntries().get(0).getMessage());
  }
  
  @Test
  public void testDebugPrintFloat()
  {
    // Setup + exercise
    cpuConfig.code = """
              lla a5,X
              flw fa5,0(a5) #DEBUG"floats ${f0} and ${fa5}"
            X:
              .word   1067030938
            """;
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute(false);
    
    // Assert
    Assert.assertEquals("floats 0 and 1.2", cpu.cpuState.debugLog.getEntries().get(0).getMessage());
    Assert.assertTrue(0 < cpu.cpuState.debugLog.getEntries().get(0).getCycle());
  }
  
  @Test
  public void testDebugPrintNegative()
  {
    // Setup + exercise
    cpuConfig.code = """
              subi a5,x0,4 #DEBUG"negative ${a5}"
            """;
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute(false);
    
    // Assert
    String message = cpu.cpuState.debugLog.getEntries().get(0).getMessage();
    Assert.assertTrue(message.equals("negative -4") || message.equals("negative 4294967292"));
    Assert.assertTrue(0 < cpu.cpuState.debugLog.getEntries().get(0).getCycle());
  }
  
  @Test
  public void testDebugPrintUnsigned()
  {
    // Setup + exercise
    cpuConfig.code = """
              addi x1, x0, 0x40000000
              subi x7, x0, 1
              srli x7, x7, 0
              slli x2, x1, 1 #DEBUG"unsigned ${x2} and ${x7}"
            """;
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute(false);
    
    // Assert
    // should be 0b1000000... and 0b11111... so 2147483648 and 4294967295
    Assert.assertEquals(1, cpu.cpuState.debugLog.getEntries().size());
    Assert.assertEquals("unsigned 2147483648 and 4294967295", cpu.cpuState.debugLog.getEntries().get(0).getMessage());
  }
}
