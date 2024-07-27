/**
 * @file ManagerRegistry.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container of all managers
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

import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

/**
 * The class keeps track (through managers) of all instances of certain classes.
 * This is useful for serialization - the JSON can be normalized and the references
 * easily resolved.
 *
 * @brief Container of all managers to be serialized
 */
public class ManagerRegistry
{
  /**
   * Input code model manager
   */
  public InstanceManager<InputCodeModel> inputCodeManager;
  
  /**
   * Sim code model manager
   */
  public InstanceManager<SimCodeModel> simCodeManager;
  
  /**
   * Register model manager
   */
  public InstanceManager<RegisterModel> registerModelManager;
  
  public ManagerRegistry()
  {
    inputCodeManager     = new InstanceManager<>();
    simCodeManager       = new InstanceManager<>();
    registerModelManager = new InstanceManager<>();
  }
}
