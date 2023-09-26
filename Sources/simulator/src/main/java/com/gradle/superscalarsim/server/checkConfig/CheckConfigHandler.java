/**
 * @file    CheckConfigHandler.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Handler for the /checkConfig endpoint
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
 
package com.gradle.superscalarsim.server.checkConfig;

import com.cedarsoftware.util.io.JsonReader;
import com.google.gson.Gson;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.serialization.GsonConfiguration;
import com.gradle.superscalarsim.server.IRequestResolver;
import com.gradle.superscalarsim.server.Server;
import com.gradle.superscalarsim.server.compile.CompileRequest;
import com.gradle.superscalarsim.server.compile.CompileResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @brief Handler for the /checkConfig endpoint
 * Parses config and tells if it's valid
 */
public class CheckConfigHandler implements IRequestResolver<CheckConfigRequest, CheckConfigResponse> {

    public CheckConfigResponse resolve(CheckConfigRequest request) {

        CheckConfigResponse response;
        if(request == null || request.config == null)
        {
            response = new CheckConfigResponse(false, new ArrayList<>(List.of("Wrong format. Expected JSON with 'config' object field")));
        } else {
            // Validate
            CpuConfiguration.ValidationResult res = request.config.validate();
            response = new CheckConfigResponse(res.valid, res.messages);
        }
        return response;
    }
}
