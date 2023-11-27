/**
 * @file BranchTargetEntryModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container class for BTB entries
 * @date 1  February  2021 16:00 (created) \n
 * 10 March     2021 18:10 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class BranchTargetEntryModel
 * @brief Container class for keeping information about branch instructions and their targets
 */
public class BranchTargetEntryModel
{
  
  /**
   * PC Tag which identifies instruction
   */
  private final int pcTag;
  
  /**
   * Is branch instruction conditional or unconditional
   */
  private final boolean isConditional;
  
  /**
   * Target of the branch instruction
   */
  private final int target;
  
  /**
   * ID from decode block marking bulk of processed instructions
   */
  private final int instructionId;
  
  /**
   * ID marking when branch instruction get committed
   */
  private final int commitId;
  
  /**
   * @param pcTag    PC Tag which identifies instruction
   * @param isBranch Is branch instruction conditional or unconditional
   * @param target   Target of the branch instruction
   * @param bulkId   ID from decode block marking bulk of processed instructions
   * @param commitId ID marking when branch instruction get committed
   *
   * @brief Constructor
   */
  public BranchTargetEntryModel(int pcTag, boolean isConditional, int target, int bulkId, int commitId)
  {
    this.pcTag         = pcTag;
    this.isConditional = isConditional;
    this.target        = target;
    this.instructionId = bulkId;
    this.commitId      = commitId;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return PC Tag
   * @brief Gets PC Tag of the entry
   */
  public int getPcTag()
  {
    return pcTag;
  }// end of getPcTag
  //----------------------------------------------------------------------
  
  /**
   * @return True if branch instruction entry is conditional, false if unconditional
   * @brief Checks if the entry is of conditional or unconditional branch instruction
   */
  public boolean isConditional()
  {
    return isConditional;
  }// end of isBranch
  //----------------------------------------------------------------------
  
  /**
   * @return Target position of the jump
   * @brief Gets branch instruction entry target
   */
  public int getTarget()
  {
    return target;
  }// end of getTarget
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value of bulkId
   * @brief Get id when was entry processed by decode block
   */
  public int getInstructionId()
  {
    return instructionId;
  }// end of getBulkID
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value of commitId
   * @brief Get id when was entry processed by ROB block
   */
  public int getCommitId()
  {
    return commitId;
  }// end of getCommitId
  //----------------------------------------------------------------------
}
