/**
 * @file ExecuteUtil.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Utility for running the whole program on the CPU
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

public class ExecuteUtil
{
  
  static String artihmeticProgram = """
          addi x3, x4, 5
          add x2, x3, x4
          add x1, x2, x3""";
  
  /**
   * @param loopCount How many times to loop
   *
   * @return The program
   * @brief Get a simple program that loops a given number of times
   */
  static String getLoopProgram(int loopCount)
  {
    return "addi x3, x0, " + loopCount + "\n" + "loop:\n" + "beq x3, x0, loopEnd\n" + "subi x3, x3, 1\n" + "jal x0, loop\n" + "loopEnd:";
  }
}
