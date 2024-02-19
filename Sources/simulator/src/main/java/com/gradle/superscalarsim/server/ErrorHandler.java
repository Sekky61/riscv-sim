/**
 * @file ErrorHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief undertow server error handler
 * @date 08 Feb      2024 9:00 (created)
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

import com.gradle.superscalarsim.app.MyLogger;
import io.undertow.io.Sender;
import io.undertow.server.DefaultResponseListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root error handler for the server.
 * If the error is not handled higher in the chain, this handler will catch it and return a generic JSON error message.
 */
public class ErrorHandler implements HttpHandler
{
  
  private final HttpHandler next;
  
  /**
   * Logger
   */
  private static final Logger logger = MyLogger.initializeLogger("ErrorHandler", Level.INFO);
  
  public ErrorHandler(final HttpHandler next)
  {
    this.next = next;
  }
  
  /**
   * <a href="https://undertow.io/undertow-docs/undertow-docs-2.1.0/index.html#error-handling">Docs</a>
   */
  @Override
  public void handleRequest(final HttpServerExchange exchange) throws Exception
  {
    exchange.addDefaultResponseListener(new DefaultResponseListener()
    {
      @Override
      public boolean handleDefaultResponse(final HttpServerExchange exchange)
      {
        if (!exchange.isResponseChannelAvailable())
        {
          return false;
        }
        if (exchange.getStatusCode() == 500)
        {
          logger.log(Level.SEVERE, "Internal server error");
          final String errorPage = "{\"error\": \"Internal server error\"}";
          exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, "" + errorPage.length());
          exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
          Sender sender = exchange.getResponseSender();
          sender.send(errorPage);
          return true;
        }
        return false;
      }
    });
    next.handleRequest(exchange);
  }
}
