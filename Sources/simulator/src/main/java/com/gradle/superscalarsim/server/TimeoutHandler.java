/**
 * @file TimeoutHandler.java
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

import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handler wrapper with timeout for requests.
 * If the request takes longer than the specified timeout, the handler will return a 500.
 * It is not exact, but good enough for timeouts.
 */
class TimeoutHandler implements HttpHandler
{
  private final HttpHandler next;
  private final int timeout_ms;
  
  // Create a single ScheduledExecutorService instance outside the handler
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  public TimeoutHandler(int timeout_ms, HttpHandler next)
  {
    this.next       = next;
    this.timeout_ms = timeout_ms;
  }
  
  /**
   * Immediately calls the next handler and schedules a timeout task.
   */
  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception
  {
    // Schedule a timeout task
    scheduler.schedule(() ->
                       {
                         if (!exchange.isComplete())
                         {
                           // Timeout handling logic
                           exchange.setStatusCode(500); // Internal Server Error
                           final String errorPage = "{\"error\": \"Request timed out\"}";
                           exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, "" + errorPage.length());
                           exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
                           Sender sender = exchange.getResponseSender();
                           sender.send(errorPage);
                           exchange.endExchange();
                         }
                       }, timeout_ms, TimeUnit.MILLISECONDS);
    
    // Delegate to the next handler
    next.handleRequest(exchange);
  }
}