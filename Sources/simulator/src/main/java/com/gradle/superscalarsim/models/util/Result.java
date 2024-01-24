/**
 * @file Result.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief The complete state of the CPU. Serializable for saving/loading.
 * @date 24 Jan      2024 14:00 (created)
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

package com.gradle.superscalarsim.models.util;

import com.gradle.superscalarsim.models.instruction.InstructionException;

/**
 * @param value     Value of the result
 * @param exception Exception raised by instruction execution
 *
 * @brief Class representing result of instruction execution. Can be either value or exception.
 */
public record Result<T>(T value, InstructionException exception)
{
  /**
   * Constructor for value
   */
  public Result(T value)
  {
    this(value, null);
  }
  
  /**
   * Constructor for exception
   */
  public Result(InstructionException exception)
  {
    this(null, exception);
  }
  
  /**
   * @return True if the result is value
   */
  public boolean isOk()
  {
    return value != null;
  }
  
  /**
   * @return True if the result is exception
   */
  public boolean isException()
  {
    return exception != null;
  }
  
  /**
   * @return Convert exception to ony other type
   */
  public <U> Result<U> convertException()
  {
    assert isException();
    return (Result<U>) this;
  }
}
