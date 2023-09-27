/**
 * @file    BranchTargetBuffer.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for holding branch targets
 *
 * @date  1 March   2020 16:00 (created) \n
 *        7 March   2020 20:30 (revised)
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.blocks.branch;

import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.BranchTargetEntryModel;
import com.gradle.superscalarsim.models.IInputCodeModel;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;

import java.util.*;

/**
 * @class BranchTargetBuffer
 * @brief Class holding targets for branch instructions
 */
public class BranchTargetBuffer
{
  /// Buffer for branch instruction targets
  private final Map<Integer, BranchTargetEntryModel> buffer;
  /// Size of the buffer
  private int size;
  /// InitLoader class holding information about instruction and registers
  private InitLoader initLoader;
  /// Stack holding history of all rewritten entries
  private final Stack<BranchTargetEntryModel> previousEntries;

  /**
   * @brief Constructor
   * @param [in] initLoader - InitLoader class holding information about instruction and registers
   * @param [in] size       - Size of the BTB
   */
  public BranchTargetBuffer(InitLoader initLoader, int size)
  {
    this.initLoader      = initLoader;
    // TreeMap is used to have sorted keys - display in GUI
    this.buffer          = new TreeMap<>();
    this.previousEntries = new Stack<>();
    reallocateTargetBuffer(size);
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Resets the BTB
   */
  public void reset()
  {
    this.buffer.clear();
    this.previousEntries.clear();
    reallocateTargetBuffer(size);
  }// end of reset
  //----------------------------------------------------------------------

  BranchTargetEntryModel getBranchEntry(int programCounter) {
    return this.buffer.getOrDefault(programCounter % this.size,
            new BranchTargetEntryModel(-1,false, -1, -1, -1));
  }

  /**
   * @brief Get whole BTB
   * @return List of BTB entries
   */
  public Map<Integer, BranchTargetEntryModel> getBuffer()
  {
    return buffer;
  }// end of getBuffer
  //----------------------------------------------------------------------

  /**
   * @brief Get BTB buffer size
   * @return BTB buffer size
   */
  public int getBufferSize()
  {
    return size;
  }// end of getBufferSize
  //----------------------------------------------------------------------

  /**
   * @brief Reallocates buffer to new size
   * @param [in] size - New size of the buffer
   */
  public void reallocateTargetBuffer(int size)
  {
    this.size = size;
    this.buffer.clear();
  }// end of reallocateTargetBuffer
  //----------------------------------------------------------------------

  /**
   * @brief Sets entry to BTB
   * @param [in] programCounter - Position fo the instruction in program
   * @param [in] codeModel      - Branch code model
   * @param [in] target         - Target of the branch code model
   * @param [in] commitId       - Id marking when branch instruction get committed
   */
  public void setEntry(int programCounter, IInputCodeModel codeModel, int target, int id, int commitId)
  {
    InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
    BranchTargetEntryModel entryModel = new BranchTargetEntryModel(
      programCounter,
      instruction != null && !instruction.isUnconditionalJump(),
      target,
      id,
      commitId
      );

    this.previousEntries.push(getBranchEntry(programCounter));
    this.buffer.put(programCounter % this.size, entryModel);
  }// end of setEntry
  //----------------------------------------------------------------------

  /**
   * @brief Gets entry target specified by the program position
   * @param [in] programCounter - Position of the instruction in program
   * @return Target of the branch instruction
   */
  public int getEntryTarget(int programCounter)
  {
    BranchTargetEntryModel entryModel = getBranchEntry(programCounter);
    if(entryModel == null) // todo redundant
    {
      return -1;
    }
    return entryModel.getPcTag() == programCounter ? entryModel.getTarget() : -1;
  }// end of getEntryTarget
  //----------------------------------------------------------------------

  /**
   * @brief Ccheck if an entry is of unconditional branch instruction
   * @param [in] programCounter - Position of the instruction in the program
   * @return True if the entry is unconditional, false otherwise
   */
  public boolean isEntryUnconditional(int programCounter)
  {
    BranchTargetEntryModel entryModel = getBranchEntry(programCounter);
    return entryModel.getPcTag() == programCounter && !entryModel.isConditional();
  }// end of isEntryUnconditional
  //----------------------------------------------------------------------

  /**
   * @brief Clears entry on the specified position
   * @param [in] programCounter - Position of the instruction in the program
   */
  public void clearEntry(int programCounter)
  {
    this.buffer.remove(programCounter % this.size);
  }// end of clearEntry
  //----------------------------------------------------------------------

  /**
   * @brief Resets entry from memory if ids are equal with the current entry
   * @param [in] programCounter - Position of the instruction in the program
   * @param [in] id         - Id from decode block marking bulk of processed instructions
   * @param [in] commitId       - Id marking when branch instruction get committed
   */
  public void resetEntry(int programCounter, int id, int commitId)
  {
    BranchTargetEntryModel entryModel = getBranchEntry(programCounter);
    if(entryModel.getInstructionId() == id && entryModel.getCommitId() == commitId)
    {
      entryModel = this.previousEntries.pop();
      this.buffer.put(programCounter % this.size, entryModel);
    }
  }// end of resetEntry
  //----------------------------------------------------------------------
}
