/**
 * @file CpuAdditionBenchmark.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Benchmark for the CPU simulation speed
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

package com.gradle.superscalarsim.benchmarks;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

public class CpuAdditionBenchmark
{
  public static void main(String[] args) throws Exception
  {
    org.openjdk.jmh.Main.main(args);
  }
  
  @Fork(value = 1, warmups = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public Cpu constantSteps()
  {
    // 10000 iters on a load/store loop
    String code = "addi x3 x0 10000\n" + "addi x8 x0 50\n" + "sw x8 x0 16\n" + "loop:\n" + "beq x3 x0 loopEnd\n" + "lw x8 x0 16\n" + "addi x8 x8 1\n" + "sw x8 x0 16\n" + "subi x3 x3 1\n" + "jal x0 loop\n" + "loopEnd:";
    
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.code = code;
    Cpu cpu = new Cpu();
    
    for (int i = 0; i < 100000; i++)
    {
      cpu.step();
    }
    
    return cpu;
  }
  
  @Fork(value = 1, warmups = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public Cpu runProgram()
  {
    // 10000 iters on a load/store loop
    String code = "addi x3 x0 10000\n" + "addi x8 x0 50\n" + "sw x8 x0 16\n" + "loop:\n" + "beq x3 x0 loopEnd\n" + "lw x8 x0 16\n" + "addi x8 x8 1\n" + "sw x8 x0 16\n" + "subi x3 x3 1\n" + "jal x0 loop\n" + "loopEnd:";
    
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.code = code;
    Cpu cpu = new Cpu();
    
    int i = 0;
    while (!cpu.simEnded())
    {
      cpu.step();
      i++;
    }
    
    System.out.printf("Cycles: %d\n", i);
    
    return cpu;
  }
}
