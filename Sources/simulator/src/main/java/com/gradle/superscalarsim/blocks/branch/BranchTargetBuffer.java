/**
 * @file BranchTargetBuffer.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for holding branch targets
 * @date 1 March   2020 16:00 (created) \n
 * 7 March   2020 20:30 (revised)
 * 26 Sep      2023 10:00 (revised)
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
package com.gradle.superscalarsim.blocks.branch;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.models.BranchTargetEntryModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

import java.util.Map;
import java.util.TreeMap;

/**
 * @class BranchTargetBuffer
 * @brief Table where each entry holds the target of a branch instruction.
 * The target can be unknown (-1). The table is indexed by the PC of the branch instruction
 * and the tag is compared to determine if the entry is valid or shared.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class BranchTargetBuffer
{
  /**
   * Buffer for branch instruction targets
   */
  private final Map<Integer, BranchTargetEntryModel> buffer;
  
  /**
   * Size of the buffer
   */
  private int size;
  
  /**
   * @param size Size of the BTB
   *
   * @brief Constructor
   */
  public BranchTargetBuffer(int size)
  {
    // TreeMap is used to have sorted keys - display in GUI
    // TODO: change, measure if SparseArray is faster
    this.buffer = new TreeMap<>();
    this.size   = size;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position fo the instruction in program
   * @param codeModel      Branch code model
   * @param target         Target of the branch code model
   *
   * @brief Sets entry to BTB
   */
  public void setEntry(int programCounter, SimCodeModel codeModel, int target)
  {
    assert codeModel != null;
    BranchTargetEntryModel entryModel = new BranchTargetEntryModel(programCounter, codeModel.isConditionalBranch(),
                                                                   target);
    
    this.buffer.put(programCounter % this.size, entryModel);
  }// end of setEntry
  //----------------------------------------------------------------------
  
  BranchTargetEntryModel getBranchEntry(int programCounter)
  {
    return this.buffer.getOrDefault(programCounter % this.size, new BranchTargetEntryModel(-1, false, -1));
  }
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position of the instruction in program
   *
   * @return Target of the branch instruction
   * @brief Gets entry target specified by the program position
   */
  public int getEntryTarget(int programCounter)
  {
    BranchTargetEntryModel entryModel = getBranchEntry(programCounter);
    return entryModel.getPcTag() == programCounter ? entryModel.getTarget() : -1;
  }// end of getEntryTarget
  //----------------------------------------------------------------------
  
  /**
   * @param programCounter Position of the instruction in the program
   *
   * @return True if the entry is unconditional, false otherwise
   * @brief Check if an entry is of unconditional branch instruction
   */
  public boolean isEntryUnconditional(int programCounter)
  {
    BranchTargetEntryModel entryModel = getBranchEntry(programCounter);
    return entryModel.getPcTag() == programCounter && !entryModel.isConditional();
  }// end of isEntryUnconditional
  //----------------------------------------------------------------------
}
