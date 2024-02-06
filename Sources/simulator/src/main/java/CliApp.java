/**
 * @file CliApp.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief CLI entry point
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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "cli", description = "Launch a simulation from the command line")
class CliApp implements Callable<Integer>
{
  @Option(names = "--cpu", paramLabel = "FILE", description = "Cpu configuration file")
  String cpuConfigFile;
  @Option(names = "--program", paramLabel = "FILE", description = "Program file")
  String programFile;
  @Option(names = "--memory", paramLabel = "FILE", description = "Memory configuration file")
  String memoryConfigFile;
  @ParentCommand
  private App parent;
  
  @Override
  public Integer call()
  {
    return 0;
  }
}
