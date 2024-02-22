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
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class CpuLoopBenchmark
{
  public static void main(String[] args) throws Exception
  {
    org.openjdk.jmh.Main.main(args);
  }
  
  @Fork(value = 1)
  @Warmup(iterations = 0, time = 1)
  @Measurement(iterations = 1)
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public Cpu shortLoop()
  {
    // 100 iterations on a load/store loop
    String code = """
            addi x16, x0, 100
            label:
              addi x17, x17, 1
              bne x17, x16, label
              subi x18, x0, 1
            """;
    
    SimulationConfig config = SimulationConfig.getDefaultConfiguration();
    config.code = code;
    Cpu cpu = new Cpu(config);
    cpu.execute(false);
    return cpu;
  }
}
