/**
 * @file MemoryTransaction.java
 * @author Michal Majer \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xmajer21@fit.vutbr.cz
 * @brief Class for memory transaction
 * @date 19 Jan      2024 10:00 (created)
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

package com.gradle.superscalarsim.models.memory;

/**
 * @class MemoryTransaction
 * @brief Data class describing a memory transaction
 */
public record MemoryTransaction(int timestamp, long address, byte[] data, int size, boolean isStore, boolean isSigned)
{
  /**
   * Constructor
   */
  public MemoryTransaction
  {
    if (size < 1 || size > 64)
    {
      throw new IllegalArgumentException("Size must be between 1 and 8");
    }
  }
}
