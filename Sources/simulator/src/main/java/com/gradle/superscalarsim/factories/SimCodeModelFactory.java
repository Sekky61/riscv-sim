/**
 * @file SimCodeModelFactory.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains factory for SimCodeModel
 * @date 08 November  2023 18:00 (created)
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

package com.gradle.superscalarsim.factories;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.managers.SimCodeModelManager;
import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

/**
 * @class InputCodeModelFactory
 * @brief Factory for InputCodeModel
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class SimCodeModelFactory
{
  int id;
  SimCodeModelManager manager;
  
  public SimCodeModelFactory()
  {
    this.manager = null;
  }
  
  public SimCodeModelFactory(SimCodeModelManager manager)
  {
    this.manager = manager;
  }
  
  public SimCodeModel createInstance(InputCodeModel inputCodeModel, int id, int fetchId)
  {
    SimCodeModel instance = new SimCodeModel(inputCodeModel, id, fetchId);
    if (manager != null)
    {
      manager.addInstance(instance);
    }
    return instance;
  }
}
