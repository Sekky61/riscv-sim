/**
 * @file InputCodeModelFactory.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains factory for InputCodeModel
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

import com.gradle.superscalarsim.managers.InputCodeModelManager;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;

import java.util.List;

/**
 * The factory pattern is used to create instances of InputCodeModel.
 * The factory is used to keep track of all instances of InputCodeModel using the manager.
 * See {@link com.gradle.superscalarsim.managers.ManagerRegistry} for more details.
 *
 * @class InputCodeModelFactory
 * @brief Factory for InputCodeModel
 */
public class InputCodeModelFactory
{
  InputCodeModelManager manager;
  
  public InputCodeModelFactory()
  {
    this.manager = null;
  }
  
  public InputCodeModelFactory(InputCodeModelManager manager)
  {
    this.manager = manager;
  }
  
  /**
   * Factory method for InputCodeModel. See the constructor of InputCodeModel for more details.
   *
   * @param instructionFunctionModel Instruction function model
   * @param arguments                Arguments of the instruction
   * @param codeId                   ID of the instruction
   *
   * @return A new instance of InputCodeModel
   */
  public InputCodeModel createInstance(InstructionFunctionModel instructionFunctionModel,
                                       final List<InputCodeArgument> arguments,
                                       int codeId)
  {
    InputCodeModel instance = new InputCodeModel(instructionFunctionModel, arguments, codeId);
    if (manager == null)
    {
      System.err.println("InputCodeModelFactory: manager is null");
    }
    else
    {
      // TODO: if in debug mode, check uniqueness of codeId
      manager.addInstance(instance);
    }
    return instance;
  }
}
