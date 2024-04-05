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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.code.ParseError;

import java.util.List;

/**
 * Response for the /compile endpoint
 */
public class CompileResponse
{
  /**
   * @brief Additional information about result of the compilation. Null if no additional information is available.
   * <ul>
   *   <li> "success" - Compilation succeeded</li>
   *   <li> "warning" - Compilation succeeded, but there were warnings</li>
   *   <li> "c" - C compilation failed</li>
   *   <li> "asm" - Assembly compilation failed (errors are listed)</li>
   *   <li> "internal" - Internal error</li>
   * </ul>
   */
  @JsonProperty(required = true)
  public String status;
  /**
   * @brief A general, short message to the user.
   * Can be null.
   */
  @JsonProperty(required = true)
  public String message;
  /**
   * The RISC-V assembly code. Can be null if the compilation failed.
   */
  @JsonProperty(required = true)
  public String program;
  /**
   * Mapping from ASM lines to C lines.
   * The length of this list is the same as the length of the program.
   * Can be null if the compilation failed.
   * The indexing is one-based.
   */
  @JsonProperty(required = true)
  public List<Integer> asmToC;
  /**
   * @brief A detailed list of compiler errors and warnings from the C compilation.
   * Can be null if the compilation was successful.
   * @details Is of type Object because the type is complex.
   */
  @JsonProperty(required = true)
  public List<Object> compilerError;
  /**
   * @brief Errors and warnings from the analysis of the assembly code.
   * Takes into account memory locations.
   */
  @JsonProperty(required = true)
  public List<ParseError> asmErrors;
  
  public CompileResponse()
  {
    this.status        = null;
    this.program       = null;
    this.asmToC        = null;
    this.message       = null;
    this.compilerError = null;
    this.asmErrors     = null;
  }
  
  public CompileResponse(String status,
                         String message,
                         String program,
                         List<Integer> asmToC,
                         List<Object> compilerError,
                         List<ParseError> asmErrors)
  {
    this.status        = status;
    this.message       = message;
    this.program       = program;
    this.asmToC        = asmToC;
    this.compilerError = compilerError;
    this.asmErrors     = asmErrors;
  }
  
  /**
   * @brief Whether the compilation was successful
   */
  @JsonProperty
  public boolean success()
  {
    return status.equals("success") || status.equals("warning");
  }
}
