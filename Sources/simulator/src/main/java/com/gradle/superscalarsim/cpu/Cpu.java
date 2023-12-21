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

import com.gradle.superscalarsim.loader.InitLoader;

import java.io.Serializable;
import java.util.Objects;

/**
 * @class Cpu
 * @brief Class representing the CPU
 */
public class Cpu implements Serializable
{
  /**
   * CPU configuration
   */
  public SimulationConfig configuration;
  
  /**
   * CPU state
   */
  public CpuState cpuState;
  
  /**
   * @brief Loader for initial values - registers, instruction descriptions
   */
  public InitLoader initLoader;
  
  /**
   * Assumes the cpuConfiguration is correct
   *
   * @param simConfig  CPU configuration to use
   * @param cpuState   CPU state to use - can be null
   * @param initLoader registers and instruction definitions - can be null
   */
  public Cpu(SimulationConfig simConfig, CpuState cpuState, InitLoader initLoader)
  {
    this.configuration = simConfig;
    this.initLoader    = Objects.requireNonNullElseGet(initLoader, InitLoader::new);
    this.cpuState      = Objects.requireNonNullElseGet(cpuState,
                                                       () -> new CpuState(this.configuration, this.initLoader));
  }
  
  /**
   * @param cpuConfig CPU configuration to use
   *
   * @brief Create a CPU with a given configuration at the default state (tick 0)
   */
  public Cpu(SimulationConfig simConfig)
  {
    this.configuration = simConfig;
    this.initLoader    = new InitLoader();
    this.cpuState      = new CpuState(this.configuration, this.initLoader);
  }
  
  /**
   * Create a CPU with default state
   */
  public Cpu()
  {
    this.configuration = SimulationConfig.getDefaultConfiguration();
    this.initLoader    = new InitLoader();
    this.cpuState      = new CpuState(this.configuration, this.initLoader);
  }
  
  /**
   * @brief Changes the code in the CPU. Useful for testing. Overwrites the current state.
   */
  public void setCode(String code)
  {
    this.configuration.code = code;
    this.cpuState           = new CpuState(this.configuration, this.initLoader);
  }
  
  public void stepBack()
  {
    simulateState(this.cpuState.tick - 1);
  }
  
  /**
   * @param targetTick Tick of the desired state
   *
   * @brief Runs simulation from given state to the end
   */
  public void simulateState(int targetTick)
  {
    int currentTick = this.cpuState.tick;
    
    // Forward or backward simulation?
    if (targetTick >= currentTick)
    {
      // Forward
      while (!simEnded() && this.cpuState.tick < targetTick)
      {
        step();
      }
    }
    else
    {
      // Backward
      this.cpuState = new CpuState(this.configuration, this.initLoader);
      while (!simEnded() && this.cpuState.tick < targetTick)
      {
        step();
      }
    }
  }
  //-------------------------------------------------------------------------------------------
  
  public boolean simEnded()
  {
    boolean robEmpty      = cpuState.reorderBufferBlock.getReorderQueueSize() == 0;
    boolean pcEnd         = cpuState.instructionFetchBlock.getPc() >= cpuState.instructionMemoryBlock.getCode()
            .size() * 4;
    boolean renameEmpty   = cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty();
    boolean fetchNotEmpty = !cpuState.instructionFetchBlock.getFetchedCode().isEmpty();
    boolean nop = fetchNotEmpty && cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName()
            .equals("nop");
    return robEmpty && pcEnd && renameEmpty && nop;
  }
  
  /**
   * @brief Calls all blocks and tell them to update their values (triggered by GlobalTimer)
   * Runs ROB at the end again
   */
  public void step()
  {
    this.cpuState.step();
  }// end of step
  
  /**
   * @brief Runs simulation from current state to the end
   */
  public void execute()
  {
    while (!simEnded())
    {
      step();
    }
    // Flush cache
    this.cpuState.cache.flush();
  }
}

