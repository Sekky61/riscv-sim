/**
 * @file MyRequestHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Base implementation for handlers (CORS, OPTIONS, POST, (de)serialization)
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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gradle.superscalarsim.serialization.GsonConfiguration;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.io.IOException;

/**
 * @class MyRequestHandler
 * @brief Handler class for requests
 */
public class MyRequestHandler<T, U> implements HttpHandler
{
  
  IRequestResolver<T, U> resolver;
  
  public MyRequestHandler(IRequestResolver<T, U> resolver)
  {
    this.resolver = resolver;
  }
  
  @Override
  public void handleRequest(HttpServerExchange exchange) throws IOException
  {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
    addOptions(exchange);
    boolean wasHandled = handleVerb(exchange);
    if (wasHandled)
    {
      return;
    }
    
    // Is it a post request?
    if (!exchange.getRequestMethod().toString().equals("POST"))
    {
      exchange.setStatusCode(405);
      exchange.getResponseSender().send("Method not allowed");
      return;
    }
    
    if (exchange.isInIoThread())
    {
      exchange.dispatch(this);
      return;
    }
    
    exchange.startBlocking();
    
    T compileRequest = (T) JsonReader.jsonToJava(exchange.getInputStream(), null);
    U response       = resolver.resolve(compileRequest);
    
    // Serialize
    String out = JsonWriter.objectToJson(response, GsonConfiguration.getJsonWriterOptions());
    exchange.getResponseSender().send(out);
  }
  
  /**
   * @param exchange The HttpExchange object
   *
   * @brief Add the CORS headers to the response
   */
  public void addOptions(HttpServerExchange exchange)
  {
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "POST, OPTIONS");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "Content-Type");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Max-Age"), "86400");
  }
  
  /**
   * @param exchange The HttpExchange object
   *
   * @return true if the request was handled, false otherwise
   * @throws IOException If an I/O error occurs
   * @brief Handle the request method
   */
  public boolean handleVerb(HttpServerExchange exchange) throws IOException
  {
    // Check that the request method is a POST or OPTIONS
    String method = exchange.getRequestMethod().toString();
    switch (method)
    {
      case "POST" ->
      {/* Allow POST */}
      case "OPTIONS" ->
      {
        // Already has CORS headers
        // Send empty response
        exchange.setStatusCode(200);
        exchange.getResponseSender().send("");
        return true;
      }
      default ->
      {
        System.err.println("Invalid request method: " + method);
        // Close
        exchange.setStatusCode(405);
        exchange.getResponseSender().send("Method not allowed");
        return true;
      }
    }
    return false;
  }
}
