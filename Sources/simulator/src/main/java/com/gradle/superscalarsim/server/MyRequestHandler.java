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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.app.MyLogger;
import com.gradle.superscalarsim.serialization.Serialization;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @class MyRequestHandler
 * @brief Handler class for requests
 */
public class MyRequestHandler<T, U> implements HttpHandler
{
  static Logger logger = MyLogger.initializeLogger("MyRequestHandler", Level.INFO);
  
  IRequestResolver<T, U> resolver;
  
  public <R extends IRequestResolver<T, U>> MyRequestHandler(R resolver)
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
    
    // At this point, the request is in the worker thread
    
    // TODO: If the request times out, it continues to run in the background.
    // this means it will eat resources, until it finishes (which may be never).
    exchange.startBlocking();
    
    // Deserialize
    InputStream           requestJson  = exchange.getInputStream();
    OutputStream          outputStream = exchange.getOutputStream();
    T                     request      = null;
    ByteArrayOutputStream baos         = new ByteArrayOutputStream();
    requestJson.transferTo(baos);
    InputStream firstClone  = new ByteArrayInputStream(baos.toByteArray());
    InputStream secondClone = new ByteArrayInputStream(baos.toByteArray());
    try
    {
      request = resolver.deserialize(firstClone);
    }
    catch (Exception e)
    {
      // Log it
      logger.severe("Cannot parse request: " + e.getMessage());
      // log the request string
      String requestString = new String(secondClone.readAllBytes());
      logger.info("Request: " + requestString);
      // Send back
      sendError(exchange, new ServerError("root", "Cannot parse request", e.getMessage()));
      return;
    }
    
    // Serialize
    try
    {
      U response = resolver.resolve(request);
      System.gc();
      resolver.serialize(response, outputStream);
      //
      logger.info("Request handled successfully: " + response.getClass().getSimpleName());
      exchange.endExchange();
    }
    catch (ServerException e)
    {
      // Send the error as a JSON, log it
      ServerError error = e.getError();
      sendError(exchange, error);
      logger.info("Request error: " + error.message());
    }
    catch (Exception e)
    {
      ServerError error = new ServerError("root", "Internal server error");
      sendError(exchange, error);
      logger.severe("Internal server error: " + e.getMessage());
      // print trace
      e.printStackTrace();
    }
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
   * @brief Handle the request method
   */
  public boolean handleVerb(HttpServerExchange exchange)
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
        logger.info("Invalid request method: " + method);
        // Close
        exchange.setStatusCode(405);
        exchange.getResponseSender().send("Method not allowed");
        return true;
      }
    }
    return false;
  }
  
  /**
   * @brief Send an error response
   */
  public void sendError(HttpServerExchange exchange, ServerError error) throws IOException
  {
    exchange.setStatusCode(400);
    ObjectMapper mapper = Serialization.getSerializer();
    mapper.writeValue(exchange.getOutputStream(), error);
    exchange.endExchange();
  }
}
