/**
 * @file    CompileResponse.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Response for the /compile endpoint
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
 
package com.gradle.superscalarsim.server.parseAsm;

import java.util.List;
import java.util.Map;

public class ParseAsmResponse {
    /**
     * @brief True if the compilation was successful
     */
    public boolean success;
    /**
     * @brief Error messages from the compiler
     */
    public String[] errors;

    public ParseAsmResponse() {
        this.success = false;
        this.errors = null;
    }

    public ParseAsmResponse(boolean success, String[] errors) {
        this.success = success;
        this.errors = errors;
    }
}
