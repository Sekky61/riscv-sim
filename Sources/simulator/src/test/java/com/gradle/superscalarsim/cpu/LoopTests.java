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

import com.gradle.superscalarsim.models.InputCodeModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LoopTests
{
  
  /**
   * Simcodes hold references to input code models.
   * When renaming, the original input code model should not be changed.
   */
  @Test
  public void LoopInstructionRenamingTest()
  {
    Cpu     cpu     = new Cpu();
    boolean success = cpu.loadProgram(ExecuteUtil.getLoopProgram(5));
    Assert.assertTrue(success);
    // Obtain original value of inputcodemodels
    List<InputCodeModel> parsedCode = cpu.cpuState.codeParser.getParsedCode();
    // Copy the list
    List<InputCodeModel> parsedCodeCopy = List.copyOf(parsedCode);
    
    
    // Count steps
    int steps = 0;
    while (!cpu.simEnded())
    {
      cpu.step();
      steps++;
    }
    
    // Assert
    Assert.assertEquals(28, steps);
    // Assert that the original input code models are not changed
    Assert.assertEquals(parsedCode, parsedCodeCopy);
  }
}
