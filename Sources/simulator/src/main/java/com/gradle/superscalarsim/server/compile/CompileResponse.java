/**
 * @file CompileResponse.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Response for the /compile endpoint
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

package com.gradle.superscalarsim.server.compile;

import java.util.List;
import java.util.Map;

public class CompileResponse
{
  /**
   * @brief Whether the compilation was successful
   */
  public boolean success;
  
  /**
   * The RISC-V assembly code
   */
  public String program;
  
  /**
   * Indexes of C lines that have corresponding assembly code
   */
  public Integer[] cLines;
  
  /**
   * Mapping from ASM lines to C lines.
   * The length of this list is the same as the length of the program
   */
  public Integer[] asmToC;
  
  /**
   * @brief A general, short error message
   */
  public String error;
  
  /**
   * @brief A detailed list of compiler errors
   */
  public List<Map<String, Object>> compilerError;
  
  public CompileResponse()
  {
    this.success       = false;
    this.program       = null;
    this.cLines        = null;
    this.asmToC        = null;
    this.compilerError = null;
  }
  
  public CompileResponse(boolean success,
                         String program,
                         List<Integer> cLines,
                         List<Integer> asmToC,
                         String error,
                         List<Map<String, Object>> compilerError)
  {
    this.success       = success;
    this.program       = program;
    this.cLines        = cLines.toArray(new Integer[0]);
    this.asmToC        = asmToC.toArray(new Integer[0]);
    this.error         = error;
    this.compilerError = compilerError;
  }
  
  public static CompileResponse failure(String error, List<Map<String, Object>> compilerError)
  {
    CompileResponse res = new CompileResponse();
    res.error         = error;
    res.success       = false;
    res.compilerError = compilerError;
    return res;
  }
}
