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

package com.gradle.superscalarsim.server.parseAsm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.ParseError;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestDeserializer;
import com.gradle.superscalarsim.server.IRequestResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @class CompileHandler
 * @brief Handler class for compile requests
 * Gets C code, calls the compiler, returns ASM for RISC-V
 */
public class ParseAsmHandler implements IRequestResolver<ParseAsmRequest, ParseAsmResponse>, IRequestDeserializer<ParseAsmRequest>
{
  
  public ParseAsmResponse resolve(ParseAsmRequest request)
  {
    
    ParseAsmResponse response;
    if (request == null || request.code == null)
    {
      // Send error
      response = new ParseAsmResponse(false, Collections.singletonList(
              new ParseError("error", "Wrong request format. Expected JSON with 'code' object field")));
    }
    else
    {
      // Parse the code
      InitLoader loader = new InitLoader();
      CodeParser parser = new CodeParser(loader);
      
      parser.parseCode(request.code);
      
      if (parser.success())
      {
        // Return success
        response = new ParseAsmResponse(true, null);
      }
      else
      {
        // Return errors
        response = new ParseAsmResponse(false, parser.getErrorMessages());
      }
    }
    
    return response;
  }
  
  @Override
  public ParseAsmRequest deserialize(InputStream json) throws IOException
  {
    ObjectMapper deserializer = Serialization.getDeserializer();
    return deserializer.readValue(json, ParseAsmRequest.class);
  }
}
