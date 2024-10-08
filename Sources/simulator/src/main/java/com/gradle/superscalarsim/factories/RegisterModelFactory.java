/**
 * @file RegisterModelFactory.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains factory for RegisterModel
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

package com.gradle.superscalarsim.factories;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.managers.InstanceManager;
import com.gradle.superscalarsim.models.register.RegisterModel;

/**
 * @class InputCodeModelFactory
 * @brief Factory for InputCodeModel
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class RegisterModelFactory
{
  int id;
  InstanceManager<RegisterModel> manager;
  
  public RegisterModelFactory()
  {
    this.manager = null;
  }
  
  public RegisterModelFactory(InstanceManager<RegisterModel> manager)
  {
    this.manager = manager;
  }
  
  public RegisterModel createInstance(String name,
                                      boolean isConstant,
                                      RegisterTypeEnum type,
                                      int value,
                                      RegisterReadinessEnum readiness)
  {
    RegisterModel instance = new RegisterModel(name, isConstant, type, value, readiness);
    if (manager == null)
    {
      //      System.err.println("RegisterModelFactory: manager is null");
    }
    else
    {
      manager.addInstance(instance);
    }
    return instance;
  }
}
