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
import com.gradle.superscalarsim.server.schema.SchemaHandler;
import com.gradle.superscalarsim.server.simulate.SimulateHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
  /**
   * @brief timeout for requests in milliseconds
   */
  int timeout_ms;
  
  boolean useGzip = true;
  
  public Server(String host, int port, int timeout_ms)
  {
    this.host       = host;
    this.port       = port;
    this.timeout_ms = timeout_ms;
  }
  
  public void start() throws IOException
  {
    // Register handlers
    HttpHandler pathHandler = Handlers.path()//
            .addPrefixPath(EndpointName.compile.getPath(),
                           new TimeoutHandler(timeout_ms, new MyRequestHandler<>(new CompileHandler())))//
            .addPrefixPath(EndpointName.parseAsm.getPath(),
                           new TimeoutHandler(timeout_ms, new MyRequestHandler<>(new ParseAsmHandler())))
            .addPrefixPath(EndpointName.checkConfig.getPath(),
                           new TimeoutHandler(timeout_ms, new MyRequestHandler<>(new CheckConfigHandler())))
            .addPrefixPath(EndpointName.simulate.getPath(),
                           new TimeoutHandler(timeout_ms, new MyRequestHandler<>(new SimulateHandler())))
            .addPrefixPath(EndpointName.schema.getPath(),
                           new TimeoutHandler(timeout_ms, new MyRequestHandler<>(new SchemaHandler())));
    HttpHandler baseHandler = pathHandler;
    
    // Add gzip encoding
    if (useGzip)
    {
      baseHandler = new EncodingHandler(
              new ContentEncodingRepository().addEncodingHandler("gzip", new GzipEncodingProvider(), 50)
                      .addEncodingHandler("deflate", new DeflateEncodingProvider(), 10)).setNext(pathHandler);
    }
    
    
    Undertow server = Undertow.builder().addHttpListener(port, host)
            .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 120).setHandler(baseHandler).build();
    server.start();
    System.out.println("Server running on port " + port);
    
    // Handling shutdown
    // The server.start is not blocking, so we need to await the shutdown signal (e.g. SIGINT)
    
    // Create a latch to wait for shutdown signal
    CountDownLatch shutdownLatch = new CountDownLatch(1);
    // Add a shutdown hook. This will be executed when the JVM receives a shutdown signal
    Runtime.getRuntime().addShutdownHook(new Thread(() ->
                                                    {
                                                      System.out.println("Shutting down server...");
                                                      server.stop();
                                                      System.out.println("Server stopped.");
                                                      shutdownLatch.countDown();
                                                    }));
    
    // Wait indefinitely until shutdown signal is received
    try
    {
      shutdownLatch.await();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    // At this point, the app returns to CLI handling and exits
  }
  
  // Handler wrapper with timeout
  static class TimeoutHandler implements HttpHandler
  {
    private final HttpHandler next;
    private final int timeout_ms;
    
    public TimeoutHandler(int timeout_ms, HttpHandler next)
    {
      this.next       = next;
      this.timeout_ms = timeout_ms;
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception
    {
      // Get the scheduler from the exchange
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      
      // Schedule a timeout task
      scheduler.schedule(() ->
                         {
                           if (!exchange.isComplete())
                           {
                             // Timeout handling logic
                             exchange.setStatusCode(500); // Internal Server Error
                             exchange.getResponseSender().send("Request timed out");
                             exchange.endExchange();
                           }
                         }, timeout_ms, TimeUnit.MILLISECONDS);
      
      // Delegate to the next handler
      next.handleRequest(exchange);
    }
  }
}
