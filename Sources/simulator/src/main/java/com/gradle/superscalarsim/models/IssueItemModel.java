/**
 * @file IssueItemModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container for item in Issue window
 * @date 3 February   2020 16:00 (created) \n
 * 16 February  2020 16:00 (revised)
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

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

/**
 * @class IssueItemModel
 * @brief An item in Issue window consisting of instruction and its operands
 */
public class IssueItemModel
{
  /**
   * SimCodeModel of the instruction
   */
  @JsonIdentityReference(alwaysAsId = true)
  public SimCodeModel instruction;
  
  /**
   * @param instruction Instruction to be set
   *
   * @brief Constructor for register operand
   */
  public IssueItemModel(SimCodeModel instruction)
  {
    this.instruction = instruction;
  }// end of Constructor
  
  /**
   * @brief String representation for debugging purposes
   */
  @Override
  public String toString()
  {
    return instruction.toString();
  }// end of toString
}
