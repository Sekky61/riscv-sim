/**
 * @file Server.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief undertow server
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

package com.gradle.superscalarsim.server;

import com.gradle.superscalarsim.server.checkConfig.CheckConfigHandler;
import com.gradle.superscalarsim.server.compile.CompileHandler;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmHandler;
import com.gradle.superscalarsim.server.simulate.SimulateHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;

import java.io.IOException;

/**
 * Supports gzip encoding - client must include "Accept-Encoding: gzip" header.
 *
 * @class Server
 * @brief Undertow server with handlers for several HTTP endpoints.
 */
public class Server
{
  
  String host;
  
  /**
   * @brief Port to listen on. Can be configured via command line argument
   */
  int port;
  
  boolean useGzip = true;
  
  public Server(String host, int port)
  {
    this.host = host;
    this.port = port;
  }
  
  public void start() throws IOException
  {
    // Register handlers
    HttpHandler pathHandler = Handlers.path().addPrefixPath("/compile", new MyRequestHandler<>(new CompileHandler()))
            .addPrefixPath("/parseAsm", new MyRequestHandler<>(new ParseAsmHandler()))
            .addPrefixPath("/checkConfig", new MyRequestHandler<>(new CheckConfigHandler()))
            .addPrefixPath("/simulate", new MyRequestHandler<>(new SimulateHandler()));
    HttpHandler baseHandler = pathHandler;
    
    // Add gzip encoding
    if (useGzip)
    {
      baseHandler = new EncodingHandler(
              new ContentEncodingRepository().addEncodingHandler("gzip", new GzipEncodingProvider(), 50,
                                                                 Predicates.parse("max-content-size(5)"))).setNext(
              pathHandler);
    }
    
    Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(baseHandler).build();
    server.start();
    System.out.println("Server started on port " + port);
  }
}
