/**
 * @file RegisterModelManager.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains manager for RegisterModel instances
 * @date 09 November  2023 16:00 (created)
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

import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.WeakHashMap;

/**
 * Manages all register models in the simulation
 */
public class RegisterModelManager implements IInstanceManager<RegisterModel>
{
  WeakHashMap<RegisterModel, Object> instances = new WeakHashMap<>();
  
  /**
   * @return WeakHashMap of instances
   * @brief Get the instances
   */
  @Override
  public WeakHashMap<RegisterModel, Object> getInstances()
  {
    return instances;
  }
}
