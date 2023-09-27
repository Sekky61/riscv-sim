/**
 * @file    SimulationRequest.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Parameters for the /simulation endpoint request
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
 
package com.gradle.superscalarsim.server.simulation;

import com.cedarsoftware.util.io.JsonReader;
import com.google.gson.Gson;
import com.gradle.superscalarsim.cpu.CpuState;
import com.gradle.superscalarsim.serialization.GsonConfiguration;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigRequest;
import com.gradle.superscalarsim.server.getState.GetStateRequest;

// Payload of /simulation request
public class SimulationRequest {
    int steps;
    CpuState state;
}
