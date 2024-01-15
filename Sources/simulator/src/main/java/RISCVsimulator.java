/**
 * @file RISCVsimulator.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains wrapper for a main class
 * @date 27 October  2021 15:00 (created) \n
 * 28 April    2021 21:30 (revised)
 * 26 Sep      2023 10:00 (revised)
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

import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.loader.ConfigLoader;
import com.gradle.superscalarsim.server.Server;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "RiscvSimulator", subcommands = ServerApp.class, version = "0.1", description = "RISC-V simulator")
class App implements Runnable
{
  @Option(names = "--register-dir", description = "Directory with register description .json files", scope = CommandLine.ScopeType.INHERIT)
  String registerDir = "./registers/";
  
  @Option(names = "--instruction-dir", description = "Directory with instruction description .json files", scope = CommandLine.ScopeType.INHERIT)
  String instructionDir = "./riscvisa/";
  
  @Option(names = "--gcc-path", description = "Path to the GCC compiler", scope = CommandLine.ScopeType.INHERIT)
  String gccPath;
  
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message", scope = CommandLine.ScopeType.INHERIT)
  boolean helpRequested = false;
  
  /**
   * This method is called when no subcommand is specified (CLI mode)
   */
  public void run()
  {
    // TODO: CLI mode
    System.out.print("No subcommand specified");
  }
  
  /**
   * @brief Application main
   */
  public static void main(String[] args)
  {
    int exitCode = new CommandLine(new App()).execute(args);
    // Do NOT call exit
  }
}


@Command(name = "server", description = "Server subcommand")
class ServerApp implements Callable<Integer>
{
  /**
   * @brief Configuration loader. Runs before any other code.
   */
  static ConfigLoader configLoader = new ConfigLoader();
  
  @CommandLine.ParentCommand
  private App parent;
  
  @Option(names = "--host", description = "Host to listen on")
  String host = "localhost";
  
  @Option(names = "--port", description = "Port to listen on")
  int port = 8000;
  
  @Option(names = "--timeout-ms", description = "Timeout for requests in milliseconds")
  Integer timeout_ms;
  
  @Override
  public Integer call()
  {
    // override the configLoader
    if (parent.gccPath != null)
    {
      GccCaller.compilerPath = parent.gccPath;
    }
    
    // Handle the server timeout parameter
    Integer timeout_ms = ConfigLoader.serverTimeoutMs;
    if (timeout_ms == null)
    {
      if (this.timeout_ms != null)
      {
        timeout_ms = this.timeout_ms;
      }
      else
      {
        throw new RuntimeException("Server timeout not specified");
      }
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
