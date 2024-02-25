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

import java.util.ArrayList;
import java.util.List;

/**
 * @brief Global History Register (also known as Branch History Shift Register). Used for dynamic, global branch prediction.
 * The maximum length of the register is artificially limited to 8 bits.
 * @details A bit array holding history of last n branches (true if the branch was taken, false if the branch was not taken).
 * The value of the register is used to index the table of predictors.
 * The history is updated only by conditional branches in the _fetch_ stage.
 * The GHR is updated speculatively, therefore it must be restore-able to a previous state.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class GlobalHistoryRegister
{
  /**
   * Size of the GHR in bits
   */
  private final int size;
  /**
   * @brief History of the shift register. The first element is the architectural state of the register.
   * The rest of the elements are the changes of the register. The changes are used for restoration after a flush.
   */
  private List<Register> shiftRegisters;
  
  /**
   * @param size Size of the bit vector. Values 1-8 are allowed.
   *
   * @brief Constructor
   */
  public GlobalHistoryRegister(int size)
  {
    assert size >= 1 && size <= 8;
    this.size = size;
    
    // The initial state of the shift register is all zeros
    this.shiftRegisters = new ArrayList<>();
    this.shiftRegisters.add(new Register(0, -1));
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * Get the architectural state of the shift register
   */
  public int getArchitecturalState()
  {
    assert !this.shiftRegisters.isEmpty();
    return this.shiftRegisters.get(0).shiftRegister;
  }
  
  /**
   * @param isJump Was the branch taken or not?
   *
   * @brief Shifts new bit value into the vector
   */
  public void shiftValue(boolean isJump, int codeId)
  {
    int lastRegisterValue = getRegisterValue();
    int newRegisterValue  = (lastRegisterValue << 1) | (isJump ? 1 : 0);
    int mask              = (1 << size) - 1;
    newRegisterValue &= mask;
    shiftRegisters.add(new Register(newRegisterValue, codeId));
  }
  
  /**
   * @return Current integer value of the bit vector.
   * @brief Returns the bit array as integer.
   */
  public int getRegisterValue()
  {
    return shiftRegisters.get(shiftRegisters.size() - 1).shiftRegister;
  }// end of getRegisterValueAsInt
  //----------------------------------------------------------------------
  
  /**
   * @brief Called when part of the history is to be restored. This happens after a flush.
   */
  public void flush(int lastValidCodeId)
  {
    // They are sorted, so find the point where to cut and remove the rest
    int i = shiftRegisters.size() - 1;
    while (i > 0 && shiftRegisters.get(i).codeId > lastValidCodeId)
    {
      i--;
    }
    shiftRegisters = shiftRegisters.subList(0, i + 1);
    assert !shiftRegisters.isEmpty();
  }
  
  /**
   * Fix a bad prediction. Called when committing a conditional branch and the prediction was wrong.
   * Flush must have been called, so this change will be at the top of the list.
   */
  public void fixPrediction(boolean isJump, int codeId)
  {
    Register register = shiftRegisters.get(shiftRegisters.size() - 1);
    assert register.codeId == codeId;
    
    int lastRegisterValue = register.shiftRegister;
    // set or clear the last bit
    int newRegisterValue = isJump ? lastRegisterValue | 1 : lastRegisterValue & ~1;
    shiftRegisters.remove(shiftRegisters.size() - 1);
    shiftRegisters.add(new Register(newRegisterValue, codeId));
  }
  
  /**
   * @brief Called when a conditional branch is committed. The speculative history is confirmed.
   * @details This keeps the list short.
   */
  public void commit(int codeId)
  {
    // Set new base, new architectural state
    // So find an index, take the sublist
    int i = 0;
    while (i < shiftRegisters.size() - 1 && shiftRegisters.get(i).codeId < codeId)
    {
      i++;
    }
    shiftRegisters = shiftRegisters.subList(i, shiftRegisters.size());
    assert !shiftRegisters.isEmpty();
  }
  
  /**
   * @return The history as a bit array
   */
  @Override
  public String toString()
  {
    return Integer.toBinaryString(getRegisterValue());
  }// end of toString
  
  /**
   * @param shiftRegister The value of the shift register. A bit array.
   * @param codeId        The code id of the conditional branch instruction that caused the shift. Used for restoration/confirmation.
   *
   * @brief An entry for state of the shift register
   * @details Index 0 is the newest bit. New value is written for every committed conditional branch. The initial state is all zeros.
   */
  record Register(int shiftRegister, int codeId)
  {
  }
}
