/**
 * @file ReorderBufferItem.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container of Reorder Buffer item
 * @date 26 Oct      2023 22:00 (created)
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

package com.gradle.superscalarsim.models;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import org.jetbrains.annotations.NotNull;

public class ReorderBufferItem implements Comparable<SimCodeModel>
{
  @JsonIdentityReference(alwaysAsId = true)
  public SimCodeModel simCodeModel;
  public ReorderFlags reorderFlags;
  
  public ReorderBufferItem(final SimCodeModel simCodeModel, final ReorderFlags reorderFlags)
  {
    this.simCodeModel = simCodeModel;
    this.reorderFlags = reorderFlags;
  }
  
  /**
   * Copies the comparison from SimCodeModel
   *
   * @param [in] codeModel - Model to be compared to
   *
   * @return -1 if <, 0 if ==, > if 1
   * @brief Comparator function for assigning to priorityQueue
   */
  public int compareTo(@NotNull SimCodeModel codeModel)
  {
    return -Integer.compare(codeModel.getIntegerId(), this.simCodeModel.getIntegerId());
  }// end of compareTo
  //------------------------------------------------------
}
