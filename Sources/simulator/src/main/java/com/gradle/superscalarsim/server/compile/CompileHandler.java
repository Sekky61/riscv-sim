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

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestResolver;
import com.gradle.superscalarsim.server.ServerException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @class CompileHandler
 * @brief Handler class for compile requests
 * Gets C code, calls the compiler, returns ASM for RISC-V
 */
public class CompileHandler implements IRequestResolver<CompileRequest, CompileResponse>
{
  ObjectReader compileReqReader = Serialization.getDeserializer().readerFor(CompileRequest.class);
  ObjectWriter compileRespWriter = Serialization.getSerializer().writerFor(CompileResponse.class);
  
  public CompileResponse resolve(CompileRequest request) throws ServerException
  {
    if (request == null)
    {
      throw new ServerException("root", "Missing request body");
    }
    
    if (request.code == null)
    {
      throw new ServerException("code", "Missing code");
    }
    
    if (request.optimizeFlags == null)
    {
      throw new ServerException("optimizeFlags", "Missing optimizeFlags");
    }
    
    // Compile
    GccCaller.CompileResult res = GccCaller.compile(request.code, request.optimizeFlags);
    if (!res.success)
    {
      return new CompileResponse("c", res.error, null, null, res.compilerErrors, null);
    }
    
    CompiledProgram program             = AsmParser.parse(res.code);
    String          concatenatedProgram = StringUtils.join(program.program, "\n");
    
    // Compilation OK, now asm check
    
    StaticDataProvider provider = new StaticDataProvider();
    CodeParser parser = new CodeParser(provider.getInstructionFunctionModels(),
                                       provider.getRegisterFile().getRegisterMap(true), new InputCodeModelFactory(),
                                       request.memoryLocations);
    parser.parseCode(concatenatedProgram);
    
    if (!parser.success())
    {
      if (parser.containsErrors())
      {
        return new CompileResponse("asm", "ASM contains errors", null, null, null, parser.getErrorMessages());
      }
      else
      {
        // Warnings
        return new CompileResponse("warning", "ASM contains warnings", concatenatedProgram, program.asmToC, null,
                                   parser.getErrorMessages());
      }
    }
    
    return new CompileResponse("success", "Compilation successful", concatenatedProgram, program.asmToC, null, null);
  }
  
  @Override
  public CompileRequest deserialize(InputStream json) throws IOException
  {
    return compileReqReader.readValue(json);
  }
  
  @Override
  public void serialize(CompileResponse response, OutputStream stream) throws IOException
  {
    compileRespWriter.writeValue(stream, response);
  }
}
