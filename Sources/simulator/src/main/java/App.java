/**
 * @file App.java
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

import com.gradle.superscalarsim.app.MyLogger;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @brief Entry point of the application
 * @details See documentation of the Picocli library: <a href="https://picocli.info/quick-guide.html">Docs</a>
 */
@Command(name = "RiscvSimulator", // name of the executable, shows in help
         subcommands = {CommandLine.HelpCommand.class, ServerApp.class, CliApp.class}, // subcommands
         version = "0.1", // version of the app
         description = "RISC-V superscalar simulator") // description of the app, shows in help
class App
{
  /**
   * @brief Application main
   * @details This gets _always_ executed first. Calls one of the subcommands, or prints help.
   */
  public static void main(String[] args)
  {
    MyLogger.initializeLogger("App", Level.INFO);
    Logger.getLogger("App").info("Starting the application");
    CommandLine cl       = new CommandLine(new App());
    int         exitCode = cl.execute(args);
    System.exit(exitCode);
    // Do NOT call exit
  }
}


