/**
 * @file InstructionMemoryBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains storage of instructions for simulation
 * @date 10 November  2020 18:00 (created) \n
 * 12 May       2020 11:00 (revised)
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
package com.gradle.superscalarsim.blocks.base;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.models.InputCodeModel;

import java.util.List;
import java.util.Map;

/**
 * @class InstructionMemoryBlock
 * @brief Holds instructions for simulation
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class InstructionMemoryBlock
{
  /**
   * Nop instruction is instantiated once and reused, to have all SimCodeModel objects point to the same object.
   *
   * @brief Nop instruction
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InputCodeModel nop;
  /**
   * List of parsed instructions
   */
  @JsonIdentityReference(alwaysAsId = true)
  private List<InputCodeModel> code;
  /**
   * The strings are without the colon at the end.
   * Label can point after the last instruction.
   *
   * @brief List of all labels
   */
  private Map<String, Integer> labels;
  
  /**
   * @param code   List of parsed instructions
   * @param labels List of all labels
   * @param nop    Nop instruction
   *
   * @brief Constructor
   */
  public InstructionMemoryBlock(List<InputCodeModel> code, Map<String, Integer> labels, InputCodeModel nop)
  {
    this.code   = code;
    this.labels = labels;
    this.nop    = nop;
  }// end of Constructor
  
  
  /**
   * Get the position of the label in the memory. (Assumes the label exists and instructions are 4 bytes long)
   *
   * @param label Label name to search for. (example: "loop")
   *
   * @return Position of the label in the code, or -1 if the label does not exist.
   */
  public int getLabelPosition(String label)
  {
    Integer index = labels.get(label);
    if (index == null)
    {
      return -1;
    }
    return index;
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * Get instruction at the given PC. Assumes an instruction is 4 bytes long.
   *
   * @param pc Program counter.
   *
   * @return Instruction at the given PC, or a nop if the PC is out of range.
   */
  public InputCodeModel getInstructionAt(int pc)
  {
    assert pc % 4 == 0;
    int index = pc / 4;
    // getParsedCode so it is mockable
    if (index < 0 || index >= getCode().size())
    {
      return this.nop;
    }
    return getCode().get(index);
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return List of parsed instructions
   * @brief Get list of parsed instructions
   */
  public List<InputCodeModel> getCode()
  {
    return code;
  }// end of getParsedCode
  
  public void setCode(List<InputCodeModel> code)
  {
    this.code = code;
  }
  
  public void setLabels(Map<String, Integer> labels)
  {
    this.labels = labels;
  }
  
  public Map<String, Integer> getLabels()
  {
    return labels;
  }
  
  public InputCodeModel getNop()
  {
    return nop;
  }
}
