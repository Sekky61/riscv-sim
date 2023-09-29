/**
 * @file Cpu.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief xxx
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

import java.io.Serializable;

/**
 * @class Cpu
 * @brief Class representing the CPU
 */
public class Cpu implements Serializable
{
  public CpuState cpuState;
  
  /**
   * Assumes the cpuConfiguration is correct
   *
   * @param cpuConfiguration - CPU configuration to use
   */
  public Cpu(CpuConfiguration cpuConfiguration)
  {
    this.cpuState = new CpuState(cpuConfiguration);
  }
  
  /**
   * Create a CPU with a given state
   *
   * @param cpuState - CPU state to use. Does not need to be wired up.
   */
  public Cpu(CpuState cpuState)
  {
    this.cpuState = cpuState;
  }
  
  /**
   * Create a CPU with default state
   */
  public Cpu()
  {
    setDefaultState();
  }
  
  /**
   * Run the simulation for a given number of steps
   *
   * @param maxSteps Maximum number of steps to run
   *
   * @return Number of steps executed (may be less than maxSteps if simulation ended)
   */
  public int run(int maxSteps)
  {
    int steps = 0;
    while (!simEnded() && steps < maxSteps)
    {
      step();
      steps++;
    }
    return steps;
  }
  
  /**
   * @brief Calls all blocks and tell them to update their values (triggered by GlobalTimer)
   * Runs ROB at the end again
   */
  public void step()
  {
    cpuState.step();
  }// end of step
  //-------------------------------------------------------------------------------------------
  
  public void stepBack()
  {
    cpuState.stepBack();
  }
  
  public void execute()
  {
    while (!simEnded())
    {
      step();
    }
  }
  
  public boolean simEnded()
  {
    boolean robEmpty      = cpuState.reorderBufferBlock.getReorderQueue().isEmpty();
    boolean pcEnd         = cpuState.instructionFetchBlock.getPcCounter() >= cpuState.codeParser.getParsedCode().size();
    boolean renameEmpty   = cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty();
    boolean fetchNotEmpty = !cpuState.instructionFetchBlock.getFetchedCode().isEmpty();
    boolean nop = fetchNotEmpty && cpuState.instructionFetchBlock.getFetchedCode()
            .get(0)
            .getInstructionName()
            .equals("nop");
    return robEmpty && pcEnd && renameEmpty && nop;
  }
  
  // Load a program into the CPU. Needs to be build() first.
  public boolean loadProgram(String code)
  {
    return cpuState.codeParser.parse(code);
  }
  
  public void setDefaultState()
  {
    this.cpuState = new CpuState();
    this.cpuState.initState();
  }
}

