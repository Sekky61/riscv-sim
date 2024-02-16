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

package com.gradle.superscalarsim;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.serialization.Serialization;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class CpuAdditionBenchmark
{
  public static void main(String[] args) throws Exception
  {
    org.openjdk.jmh.Main.main(args);
  }
  
  /**
   * Microbenchmark loading of assets
   */
  @Fork(value = 1)
  @Warmup(iterations = 1, time = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public Cpu loadAssets()
  {
    Cpu cpu = new Cpu();
    return cpu;
  }
  
  @Fork(value = 1)
  @Warmup(iterations = 1, time = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void initSerializer()
  {
    Serialization.getSerializer();
  }
  
  @Fork(value = 1)
  @Warmup(iterations = 1, time = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public Cpu constantSteps()
  {
    // 1000 iters on a load/store loop
    String code = """
            addi x3, x0, 1000
            addi x8, x0, 50
            sw x8, 16(x0)
            loop:
            beq x3, x0, loopEnd
            lw x8, 16(x0)
            addi x8, x8, 1
            sw x8, x0, 16
            subi x3, x3, 1
            jal x0, loop
            loopEnd:""";
    
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.code = code;
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    
    return cpu;
  }
}
