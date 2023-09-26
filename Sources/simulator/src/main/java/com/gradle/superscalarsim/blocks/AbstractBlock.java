/**
 * @file    AbstractBlock.java
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
 * @brief File containing abstract class for simulation blocks to implement
 *
 * @date  1  February  2021 16:00 (created) \n
 *        28 April     2021 17:00 (revised)
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
package com.gradle.superscalarsim.blocks;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * @class AbstractBlock
 * @brief Abstract class, which every block must implement
 */
public interface AbstractBlock extends Serializable
{
  /**
   * @brief Simulates in right direction from InstructionFetch to ROB
   */
  void simulate();

  /**
   * @brief Simulates in reverse direction to enable backwards simulation steps (from ROB to IF)
   */
  void simulateBackwards();

  /**
   * @brief Resets the lists/stacks/variables inside the block
   */
  void reset();
}
