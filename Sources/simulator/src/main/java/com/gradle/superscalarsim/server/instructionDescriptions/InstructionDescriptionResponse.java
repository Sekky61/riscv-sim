/**
 * @file InstructionDescriptionResponse.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Response for the /instructionDescription endpoint
 * @date 08 feb      2024 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
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

package com.gradle.superscalarsim.server.instructionDescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;

import java.util.Map;

/**
 * @brief Response for the /instructionDescription endpoint
 */
public class InstructionDescriptionResponse
{
  /**
   * @brief A map of instruction names to their models
   */
  @JsonProperty(required = true)
  public Map<String, InstructionFunctionModel> models;
  
  public InstructionDescriptionResponse(Map<String, InstructionFunctionModel> models)
  {
    this.models = models;
  }
}
