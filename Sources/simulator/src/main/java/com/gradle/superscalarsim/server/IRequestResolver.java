/**
 * @file IRequestResolver.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Interface for request resolvers
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @param <T> Request type
 * @param <U> Response type
 *
 * @brief Interface for request resolvers
 * @details Implementors of this interface are responsible for deserializing requests, resolving them and serializing responses.
 * The (de)serialization is per type, so the serialization object can be reused across multiple requests of the same type.
 */
public interface IRequestResolver<T, U>
{
  /**
   * @param request The request to resolve
   *
   * @return The response
   * @throws ServerException If the request contains invalid data
   * @brief Resolve a request
   */
  U resolve(T request) throws ServerException;
  
  /**
   * @param json The input stream containing the JSON
   *
   * @return The deserialized request object
   * @throws IOException If the JSON string is invalid or there is an error reading the stream
   * @brief Deserialize a request from JSON
   */
  T deserialize(InputStream json) throws IOException;
  
  /**
   * @param response The response to serialize
   * @param stream   The output stream to write the JSON to
   *
   * @throws IOException If there is an error writing to the stream
   * @brief Serialize a response to JSON
   */
  void serialize(U response, OutputStream stream) throws IOException;
}
