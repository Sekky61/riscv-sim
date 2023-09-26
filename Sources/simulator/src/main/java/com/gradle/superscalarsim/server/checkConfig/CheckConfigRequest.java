/**
 * @file    CheckConfigRequest.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Parameters for the /checkConfig endpoint request
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
import com.cedarsoftware.util.io.JsonWriter;
import com.google.gson.Gson;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.serialization.GsonConfiguration;
import com.gradle.superscalarsim.server.getState.GetStateRequest;

public class CheckConfigRequest {
    CpuConfiguration config;

    /**
     * @brief Deserialize request from JSON
     */
    static CheckConfigRequest deserialize(String body) {
        try {
            return (CheckConfigRequest) JsonReader.jsonToJava(body);
        } catch (Exception e) {
            System.out.println("Error parsing request");
            System.out.println(body);
            System.out.println(e);
            return null;
        }
    }

    static String serialize(CheckConfigRequest request) {
        return JsonWriter.objectToJson(request);
    }
}
