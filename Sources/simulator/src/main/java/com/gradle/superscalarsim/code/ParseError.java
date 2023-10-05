/**
 * @file ParseError.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains ParseError class definition
 * @date 02 October  2023 00:30 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2020  Jan Vavra
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

package com.gradle.superscalarsim.code;


import com.cedarsoftware.util.io.JsonWriter;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Assumes an error is associated with a single location and a single line;
 *
 * @brief A class to hold a single error message
 */
public class ParseError
{
  /**
   * @brief Error kind - 'warning' or 'error'
   */
  public String kind;
  /**
   * Double quotes are escaped, otherwise it would break the JSON. So single quotes are preferred.
   *
   * @brief Error message - a verbose description of the error
   */
  public String message;
  /**
   * @brief The 1-based row index of the error
   */
  public int line;
  /**
   * @brief The 1-based column index of the start of the error
   */
  public int columnStart;
  /**
   * @brief The 1-based column index of the end of the error
   */
  public int columnEnd;
  
  /**
   * @param kind    - 'warning' or 'error'
   * @param message - a verbose description of the error
   *
   * @brief Constructor for ParseError without location
   */
  public ParseError(String kind, String message)
  {
    this.kind    = kind;
    this.message = message;
    this.line    = -1;
  }
  
  /**
   * @param kind      - 'warning' or 'error'
   * @param message   - a verbose description of the error
   * @param locations - The locations in the file.
   *
   * @brief Constructor for ParseError with location
   */
  public ParseError(String kind, String message, int line, int columnStart, int columnEnd)
  {
    this.kind        = kind;
    this.message     = message;
    this.line        = line;
    this.columnStart = columnStart;
    this.columnEnd   = columnEnd;
  }
  
  /**
   * The JSON serializes the object as a subset of the GCC error format.
   * One difference compared to GCC is that GCC serializes file indexes as floats.
   *
   * @brief a custom JSON serializer to mimic the GCC error format
   */
  public static class CustomParseErrorWriter implements JsonWriter.JsonClassWriterEx
  {
    public void write(Object o, boolean showType, Writer output, Map<String, Object> args) throws IOException
    {
      // Simplified JSON representation of the error:
      //
      // {
      //   "kind": "error",
      //   "message": "msg",
      //   "locations": {
      //     "@items": [
      //       "finish": {
      //         "display-column": 15,
      //         "line": 2,
      //       },
      //       "caret": {
      //         "display-column": 14,
      //         "line": 2,
      //       },
      //     ]
      //   },
      // }
      ParseError e = (ParseError) o;
      output.write("\"kind\":\"");
      output.write(StringEscapeUtils.escapeJson(e.kind));
      output.write("\",\"message\":\"");
      // This string must be escaped, otherwise it will break the JSON
      String escapedMessage = StringEscapeUtils.escapeJson(e.message);
      output.write(escapedMessage);
      output.write("\",\"locations\":{");
      output.write("\"@items\":[");
      output.write("{\"finish\":{\"display-column\":");
      output.write(Integer.toString(e.columnEnd));
      output.write(",\"line\":");
      output.write(Integer.toString(e.line));
      output.write("},");
      output.write("\"caret\":{\"display-column\":");
      output.write(Integer.toString(e.columnStart));
      output.write(",\"line\":");
      output.write(Integer.toString(e.line));
      output.write("}}");
      output.write("]}");
    }
  }
}
