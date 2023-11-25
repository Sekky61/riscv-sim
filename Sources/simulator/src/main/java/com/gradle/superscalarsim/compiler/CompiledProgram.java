/**
 * @file CompiledProgram.java
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

package com.gradle.superscalarsim.compiler;

import java.util.List;

public class CompiledProgram
{
  /**
   * The RISC-V assembly code
   */
  public List<String> program;
  
  /**
   * Indexes of C lines that have corresponding assembly code
   */
  public List<Integer> cLines;
  
  /**
   * Mapping from ASM lines to C lines.
   * The length of this list is the same as the length of the program
   */
  public List<Integer> asmToC;
  
  public CompiledProgram(List<String> program, List<Integer> cLines, List<Integer> asmToC)
  {
    this.program = program;
    this.cLines  = cLines;
    this.asmToC  = asmToC;
  }
}
