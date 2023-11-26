/**
 * @file CompileHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Handler for the /compile endpoint
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @class CompileHandler
 * @brief Handler class for compile requests
 * Gets C code, calls the compiler, returns ASM for RISC-V
 */
public class CompileHandler implements IRequestResolver<CompileRequest, CompileResponse>, IRequestDeserializer<CompileRequest>
{
  
  public CompileResponse resolve(CompileRequest request)
  {
    
    CompileResponse response;
    if (request == null || request.code == null)
    {
      // Send error
      response = CompileResponse.failure("Wrong request format. Expected JSON with 'code' object field", null);
    }
    else
    {
      // Compile
      GccCaller.CompileResult res = GccCaller.compile(request.code, request.optimizeFlags);
      if (!res.success)
      {
        System.err.println("Failed to compile code");
        response = CompileResponse.failure("GCC returned an error", res.compilerErrors);
      }
      else
      {
        int             cCodeLen            = request.code.split("\n").length;
        CompiledProgram program             = AsmParser.parse(res.code, cCodeLen);
        String          concatenatedProgram = StringUtils.join(program.program, "\n");
        response = new CompileResponse(true, concatenatedProgram, program.asmToC, null, null);
      }
    }
    
    return response;
  }
  
  @Override
  public CompileRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, CompileRequest.class);
  }
}
