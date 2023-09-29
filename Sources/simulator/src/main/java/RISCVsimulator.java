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

import com.gradle.superscalarsim.server.Server;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "RISCVsimulator", mixinStandardHelpOptions = true, version = "RISCVsimulator 0.1", description = "RISCVsimulator")
class SimOptions implements Callable<Integer>
{
  
  @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
  Mode mode;
  
  @Override
  public Integer call() throws Exception
  {
    return 0;
  }
  
  static class Mode
  {
    
    @Option(names = "--server", description = "Launch in server mode")
    boolean server;
  }
  
  @Option(names = "--port", description = "Port to listen on")
  int port = 8000;
  
  @Option(names = "--register-dir", description = "Directory with register description .json files")
  String registerDir = "./registers/";
  
  @Option(names = "--instruction-dir", description = "Directory with instruction description .json files")
  String instructionDir = "./riscvisa/";
  
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
  boolean helpRequested = false;
}

/**
 * @class RISCVsimulator
 * @brief Wrapper for a main class to rename root process to class name
 */
public class RISCVsimulator
{
  /**
   * @brief Application main
   * @param [in] args - Input arguments
   */
  public static void main(String[] args)
  {
    SimOptions  simOptions = new SimOptions();
    CommandLine cli        = new CommandLine(simOptions);
    cli.execute(args);
    
    if (cli.isUsageHelpRequested())
    {
      return;
    }
    
    if (simOptions.mode.server)
    {
      Server server = new Server(simOptions.port);
      try
      {
        server.start();
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    else
    {
      System.out.print("No mode specified");
    }
    
    //System.exit(new CommandLine(new SimOptions()).execute(args));
  }// end of main
  //----------------------------------------------------------------------
  //----------------------------------------------------------------------
}
