/**
 * @file    SimCodeModelAllocator.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief Allocates SimCodeModel objects. Used for centralizing all SimCodeModel objects in one place for serialization.
 *
 * @date  26 Sep      2023 10:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023 Michal Majer
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
 
package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @class SimCodeModelAllocator
 * @brief Allocates SimCodeModel objects.
 * @details This class is responsible for allocating SimCodeModel objects and keep all references to them in a list.
 *          This is to keep all simcodemodels in one place when serializing.
 */
public class SimCodeModelAllocator {

    private List<SimCodeModel> simCodeModels;

    public SimCodeModelAllocator() {
        simCodeModels = new ArrayList<>();
    }

    public void setSimCodeModels(List<SimCodeModel> simCodeModels) {
        this.simCodeModels = simCodeModels;
    }

    /**
     * @brief Creates SimCodeModel - wrapper for constructor on SimCodeModel
     * @param inputCodeModel
     * @param id
     * @param instructionBulkNumber
     * @return
     */
    public SimCodeModel createSimCodeModel(InputCodeModel inputCodeModel, int id, int instructionBulkNumber) {
        SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, id, instructionBulkNumber);
        simCodeModels.add(simCodeModel);
        return simCodeModel;
    }
}
