/**
 * @file CodeToken.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains token for parsing ASM code
 * @date 10 October  2023 17:00 (created)
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

/**
 * @brief Represents a token from the code
 * Contains information for parsing the code and reporting errors
 */
public record CodeToken(int line, int columnStart, String text, Type type)
{
  public int columnEnd()
  {
    return columnStart + text.length() - 1;
  }
  
  /**
   * @ String representation of the token for debugging
   */
  @Override
  public String toString()
  {
    return "[" + line + ":" + columnStart + ":" + this.columnEnd() + "] " + text + " (" + type + ")";
  }
  
  /**
   * Symbol is an instruction ("addi", "lw", ...)
   */
  public enum Type
  {
    L_PAREN, R_PAREN, COLON, COMMA, NEWLINE, COMMENT, EOF, STRING, LABEL, DIRECTIVE, SYMBOL
  }
}
