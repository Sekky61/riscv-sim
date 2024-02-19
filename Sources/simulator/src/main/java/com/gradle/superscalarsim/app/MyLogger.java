package com.gradle.superscalarsim.app; /**
 * @file com.gradle.superscalarsim.app.MyLogger.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief A simple logger class
 * @date 19 Feb  2024 23:00 (created) \n
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2024 Michal Majer
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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @brief A simple logger class
 * @details Logs to console with a timestamp and log level.
 * Sample code to initialize the logger:
 * <pre>
 * Logger logger = com.gradle.superscalarsim.app.MyLogger.initializeLogger("com.gradle.superscalarsim.app.MyLogger", Level.INFO);
 * logger.info("This is an informational message.");
 * logger.warning("This is a warning message.");
 * </pre>
 */
public class MyLogger
{
  
  public static Logger initializeLogger(String loggerName, Level level)
  {
    // Create a custom formatter to include timestamp and log level
    SimpleFormatter formatter = new SimpleFormatter()
    {
      private static final String format = "[%1$tF %1$tT] [%2$-7s] [%3$s] %4$s %n";
      
      @Override
      public synchronized String format(java.util.logging.LogRecord lr)
      {
        return String.format(format, new java.util.Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
                             lr.getLoggerName(), lr.getMessage());
      }
    };
    
    // Remove all handlers from the global logger
    Logger    globalLogger = Logger.getLogger("");
    Handler[] handlers     = globalLogger.getHandlers();
    for (Handler handler : handlers)
    {
      handler.setFormatter(formatter);
    }
    
    Logger logger = Logger.getLogger(loggerName);
    logger.setLevel(level);
    
    return logger;
  }
}
