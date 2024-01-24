/**
 * @file InstructionFunctionModelManager.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains interface for object managers
 * @date 07 November  2023 18:00 (created)
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


package com.gradle.superscalarsim.managers;

import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;

import java.util.WeakHashMap;

/**
 * Manages all instruction function models in the simulation
 */
public class InstructionFunctionModelManager implements IInstanceManager<InstructionFunctionModel>
{
  WeakHashMap<InstructionFunctionModel, Object> instances = new WeakHashMap<>();
  
  /**
   * @return WeakHashMap of instances
   * @brief Get the instances
   */
  @Override
  public WeakHashMap<InstructionFunctionModel, Object> getInstances()
  {
    return instances;
  }
}
