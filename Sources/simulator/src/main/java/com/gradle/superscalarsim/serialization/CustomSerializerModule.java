/**
 * @file CustomSerializerModule.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container for processed line of code
 * @date 08 November  2023 17:45 (created)
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

package com.gradle.superscalarsim.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gradle.superscalarsim.managers.InputCodeModelManager;
import com.gradle.superscalarsim.managers.InstructionFunctionModelManager;

public class CustomSerializerModule extends SimpleModule
{
  public CustomSerializerModule()
  {
    addSerializer(InputCodeModelManager.class, new ManagerSerializer());
    addSerializer(InstructionFunctionModelManager.class, new ManagerSerializer());
  }
}
