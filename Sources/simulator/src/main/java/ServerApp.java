/**
 * @file ServerApp.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Server entry point
 * @date 05 Feb  2024 23:00 (created) \n
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2024 Michal Majer
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

import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.loader.ConfigLoader;
import com.gradle.superscalarsim.server.Server;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "server", description = "Launch HTTP simulation server")
class ServerApp implements Callable<Integer>
{
  @Option(names = "--host", paramLabel = "ADDR", defaultValue = "0.0.0.0", description = "Host to bind to (default: ${DEFAULT-VALUE})")
  public String host = "0.0.0.0";
  @Option(names = "--port", paramLabel = "PORT", defaultValue = "8000", description = "Port to bind to (default: ${DEFAULT-VALUE})")
  public int port = 8000;
  @Option(names = "--timeout-ms", paramLabel = "NUMBER", defaultValue = "30000", description = "Timeout for requests in milliseconds (default: ${DEFAULT-VALUE})")
  int timeout_ms;
  @Option(names = "--gcc-path", paramLabel = "PATH", description = "Path to the GCC compiler")
  String gccPath;
  @ParentCommand
  private App parent;
  
  @Override
  public Integer call()
  {
    // override the configLoader
    if (gccPath != null)
    {
      GccCaller.setCompilerPath(gccPath);
    }
    
    // Handle the server timeout parameter
    Integer timeout_ms = ConfigLoader.serverTimeoutMs;
    if (timeout_ms == null)
    {
      timeout_ms = this.timeout_ms;
    }
    
    Server server = new Server(host, port, timeout_ms);
    try
    {
      server.start();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    return 0;
  }
}
