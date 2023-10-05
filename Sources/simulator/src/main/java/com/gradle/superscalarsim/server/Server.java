/**
 * @file Server.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief undertow server
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

package com.gradle.superscalarsim.server;

import com.gradle.superscalarsim.server.checkConfig.CheckConfigHandler;
import com.gradle.superscalarsim.server.compile.CompileHandler;
import com.gradle.superscalarsim.server.getState.GetStateHandler;
import com.gradle.superscalarsim.server.simulation.SimulationHandler;
import io.undertow.Undertow;

import java.io.IOException;

import static io.undertow.Handlers.path;

public class Server
{
  
  int port = 8000;
  
  public Server(int port)
  {
    this.port = port;
  }
  
  public void start() throws IOException
  {
    Undertow server = Undertow.builder()
                              .addHttpListener(8000, "localhost")
                              .setHandler(path().addPrefixPath("/compile", new MyRequestHandler<>(new CompileHandler()))
                                                .addPrefixPath("/checkConfig", new MyRequestHandler<>(
                                                    new CheckConfigHandler()))
                                                .addPrefixPath("/getState", new MyRequestHandler<>(
                                                    new GetStateHandler()))
                                                .addPrefixPath("/simulation", new MyRequestHandler<>(
                                                    new SimulationHandler())))
                              .build();
    server.start();
  }
}
