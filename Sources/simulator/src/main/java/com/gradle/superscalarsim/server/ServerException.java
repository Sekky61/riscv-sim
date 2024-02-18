/**
 * @file ServerException.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Exception thrown by the server during argument parsing, etc.
 * @date 18 Feb     2024 16:00 (created)
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

/**
 * @brief Exception thrown by the server during argument parsing.
 * @details This exception is thrown when the server encounters an error during argument parsing.
 * Examples include missing fields, fields with invalid values, etc.
 * Problems during simulation are handled via different exceptions.
 */
public class ServerException extends Exception
{
  /**
   * @brief The error that caused this exception. Will be shown to the user.
   */
  ServerError error;
  
  public ServerException(ServerError error)
  {
    super(error.message());
    this.error = error;
  }
  
  public ServerException(String field, String message)
  {
    this(new ServerError(field, message));
  }
  
  /**
   * @return The error that caused this exception.
   */
  public ServerError getError()
  {
    return error;
  }
}
