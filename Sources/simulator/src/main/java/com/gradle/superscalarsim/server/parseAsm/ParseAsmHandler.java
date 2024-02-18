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

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.IRequestResolver;
import com.gradle.superscalarsim.server.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @class CompileHandler
 * @brief Handler class for assembly checking requests
 * @details Gets assembly code, checks it for errors and returns them
 */
public class ParseAsmHandler implements IRequestResolver<ParseAsmRequest, ParseAsmResponse>
{
  ObjectReader parseAsmReader = Serialization.getDeserializer().readerFor(ParseAsmRequest.class);
  ObjectWriter parseAsmWriter = Serialization.getSerializer().writerFor(ParseAsmResponse.class);
  
  public ParseAsmResponse resolve(ParseAsmRequest request) throws ServerException
  {
    if (request == null)
    {
      throw new ServerException("root", "Missing request body");
    }
    
    if (request.code == null)
    {
      throw new ServerException("code", "Missing code field");
    }
    
    if (request.config == null)
    {
      throw new ServerException("config", "Missing config field");
    }
    
    if (request.config.memoryLocations == null)
    {
      throw new ServerException("config.memoryLocations", "Missing memoryLocations field");
    }
    
    // Parse the code
    StaticDataProvider provider = new StaticDataProvider();
    CodeParser parser = new CodeParser(provider.getInstructionFunctionModels(),
                                       provider.getRegisterFile().getRegisterMap(true), new InputCodeModelFactory(),
                                       request.config.memoryLocations);
    parser.parseCode(request.code);
    
    return new ParseAsmResponse(parser.success(), parser.getErrorMessages());
  }
  
  @Override
  public ParseAsmRequest deserialize(InputStream json) throws IOException
  {
    return parseAsmReader.readValue(json);
  }
  
  @Override
  public void serialize(ParseAsmResponse response, OutputStream stream) throws IOException
  {
    parseAsmWriter.writeValue(stream, response);
  }
}
