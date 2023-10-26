/**
 * @file ReorderBufferState.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief State of the ROB. Separated from ROB block to make it easily readable from the serialized form
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

package com.gradle.superscalarsim.blocks.base;

import com.gradle.superscalarsim.models.ReorderBufferItem;

import java.util.ArrayDeque;

/**
 * @class ReorderBufferState
 * @brief Class containing all state of Reorder Buffer
 * ROB state is separated from ROB block to make it easily readable from the serialized form
 */
public class ReorderBufferState
{
  /// Queue of scheduled instruction in backend
  public final ArrayDeque<ReorderBufferItem> reorderQueue;
  /// Numerical limit, how many instruction can be committed in one tick
  public int commitLimit;
  /// Id counter for Ids, when was instruction committed/ready
  public int commitId;
  /// Bit value marking if all instructions that are pulled to ROB should be speculative or not
  public boolean speculativePulls;
  /// Reorder buffer size limit
  public int bufferSize;
  
  public ReorderBufferState()
  {
    this.reorderQueue = new ArrayDeque<>();
    
    this.commitId         = 0;
    this.speculativePulls = false;
    
    this.commitLimit = 4;
    this.bufferSize  = 256;
  }
}
