/**
 * @file MemoryBlock.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Inteface for memory blocks
 * @date 20 Jan      2024 10:00 (created)
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

package com.gradle.superscalarsim.blocks.loadstore;

import com.gradle.superscalarsim.models.memory.MemoryTransaction;

/**
 * @brief Interface for memory blocks. Supports {@link MemoryTransaction}s.
 */
public interface MemoryBlock
{
  /**
   * Schedule a memory access. It will be finished after the specified number of cycles.
   * The transaction will be mutated by the memory block.
   *
   * @param transaction Memory transaction to schedule
   *
   * @return Number of cycles until the transaction is finished
   */
  int scheduleTransaction(MemoryTransaction transaction);
  
  /**
   * Throws if the requested transaction is not in the list or not finished yet, as it would be a bug.
   *
   * @param id ID of the transaction
   *
   * @brief Remove the operation from the list.
   */
  MemoryTransaction finishTransaction(int id);
  
  /**
   * @param id ID of the transaction
   *
   * @brief Cancel the transaction. It must be present and not finished.
   */
  void cancelTransaction(int id);
}
