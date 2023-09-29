/**
 * @file GetStateHandler.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Handler for the /getState endpoint
 * @date 26 Sep      2023 10:00 (created)
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

package com.gradle.superscalarsim.server.getState;

import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.server.IRequestResolver;

/**
 * @brief Handler for the /getState endpoint
 * Accepts a CpuConfiguration and returns corresponding state
 */
public class GetStateHandler implements IRequestResolver<GetStateRequest, GetStateResponse> {

    @Override
    public GetStateResponse resolve(GetStateRequest request) {
        GetStateResponse response;
        if (request == null) {
            // Send error
            response = new GetStateResponse(null);
        } else {
            // Get state
            response = getState(request);
        }
        return response;
    }

    private GetStateResponse getState(GetStateRequest request) {
        if (request.config == null) {
            Cpu defaultCpu = new Cpu();
            System.out.println("Providing default state");
            return new GetStateResponse(defaultCpu.cpuState);
        }
        CpuConfiguration cfg = request.config;
        CpuConfiguration.ValidationResult validationResult = cfg.validate();
        if (!validationResult.valid) {
            System.err.println("Provided configuration is invalid: " + validationResult.messages);
            return new GetStateResponse(null, validationResult.messages);
        }
        Cpu cpu = new Cpu(request.config);
        return new GetStateResponse(cpu.cpuState);
    }
}
