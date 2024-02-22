/**
 * @file GlobalHistoryRegister.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of bit array of branching history
 * @date 1  March   2020 16:00 (created) \n
 * 28 April   2020 12:00 (revised)
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

/**
 * @brief Global History Register (also known as Branch History Shift Register). Used for dynamic, global branch prediction.
 * The maximum length of the register is artificially limited to 8 bits.
 * @details A bit array holding history of last n branches (true if the branch was taken, false if the branch was not taken).
 * The value of the register is used to index the table of predictors.
 * The history is updated only by conditional branches.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class GlobalHistoryRegister
{
  /**
   * Size of the GHR in bits
   */
  private final int size;
  /**
   * @brief Bit array.
   * Index 0 is the newest bit. New value is written for every comitted conditional branch.
   * The initial state is all zeros.
   */
  private int shiftRegister;
  
  /**
   * @param size Size of the bit vector. Values 1-8 are allowed.
   *
   * @brief Constructor
   */
  public GlobalHistoryRegister(int size)
  {
    assert size >= 1 && size <= 8;
    this.size          = size;
    this.shiftRegister = 0;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param isJump Was the branch taken or not?
   *
   * @brief Shifts new bit value into the vector
   */
  public void shiftValue(boolean isJump)
  {
    this.shiftRegister = (this.shiftRegister << 1) | (isJump ? 1 : 0);
    int mask = (1 << this.size) - 1;
    this.shiftRegister &= mask;
  }// end of shiftValue
  //----------------------------------------------------------------------
  
  /**
   * @return Integer value of the bit vector
   * @brief Returns the bit array as integer.
   */
  public int getRegisterValue()
  {
    return this.shiftRegister;
  }// end of getRegisterValueAsInt
  //----------------------------------------------------------------------
  
  /**
   * @return The history as a bit array
   */
  @Override
  public String toString()
  {
    return Integer.toBinaryString(this.shiftRegister);
  }// end of toString
}
