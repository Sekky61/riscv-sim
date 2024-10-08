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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.app.MyLogger;
import com.gradle.superscalarsim.cpu.CpuConfig;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.serialization.Serialization;
import com.gradle.superscalarsim.server.ServerException;
import com.gradle.superscalarsim.server.simulate.SimulateHandler;
import com.gradle.superscalarsim.server.simulate.SimulateRequest;
import com.gradle.superscalarsim.server.simulate.SimulateResponse;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "cli", description = "Launch a simulation from the command line")
class CliApp implements Callable<Integer>
{
  /**
   * Logger for this class.
   */
  Logger logger = MyLogger.initializeLogger("Cli", Level.INFO);
  
  /**
   * Exposed for testing purposes.
   */
  public SimulateResponse response;
  @Spec
  CommandSpec spec; // injected by picocli
  @Option(names = "--entry", paramLabel = "LABEL|ADDRESS", description = "Entry point for the program. Any label or address in the program can be used. (default: 0)")
  String entryPoint = "0";
  @Option(names = "--pretty", description = "Pretty print the JSON output.")
  boolean prettyPrint = false;
  @Option(names = "--full-state", description = "Output the full state of the CPU. By default, only the statistics, debug prints and register values are output.")
  boolean fullState = false;
  @Option(names = "--cpu", required = true, paramLabel = "FILE", description = "Cpu configuration file.")
  Path cpuConfigPath;
  @Option(names = "--program", required = true, paramLabel = "FILE", description = "RISC-V assembly program for the CPU to execute.")
  Path programPath;
  @Option(names = "--memory", paramLabel = "FILE", description = "Memory configuration file. 1 or more global arrays to load into memory. Optional (default: empty memory)")
  Path memoryConfigPath;
  @ParentCommand
  private App parent;
  
  @Override
  public Integer call()
  {
    // Start by validating the input. Throws an exception if the input is invalid.
    validate();
    
    String               program;
    CpuConfig            cpuConfig;
    List<MemoryLocation> memoryConfig;
    try
    {
      program      = loadProgram();
      cpuConfig    = loadCpuConfig();
      memoryConfig = loadMemoryConfig();
    }
    catch (IOException e)
    {
      logger.severe("Error: " + e.getMessage());
      throw new RuntimeException(e);
    }
    
    // Parse entry point
    Object entryPointObject = entryPoint;
    try
    {
      entryPointObject = Integer.parseInt(entryPoint);
    }
    catch (NumberFormatException ignored)
    {
      // Not an integer, use as a label
    }
    
    // Reuse the same logic as in the server, to avoid code duplication
    SimulationConfig simulationConfig = new SimulationConfig(program, memoryConfig, cpuConfig, entryPointObject);
    SimulateRequest  request          = new SimulateRequest(simulationConfig, Optional.empty());
    SimulateHandler  handler          = new SimulateHandler();
    try
    {
      response = handler.resolve(request);
    }
    catch (ServerException e)
    {
      // Report the error and exit
      // The only error that can occur here is a configuration error
      logger.severe("Error: " + e.getError().message());
    }
    
    Object resultObject = response;
    if (!fullState)
    {
      resultObject = response.toShortResponse();
    }
    
    // Serialize the response and output it
    ObjectMapper serializer;
    if (prettyPrint)
    {
      serializer = Serialization.enablePrettySerializer();
    }
    else
    {
      serializer = Serialization.getSerializer();
    }
    try
    {
      String output = serializer.writeValueAsString(resultObject);
      // Use provided stream for simpler testing
      spec.commandLine().getOut().println(output);
    }
    catch (JsonProcessingException e)
    {
      logger.severe("Error: " + e.getMessage());
      throw new RuntimeException(e);
    }
    
    return 0;
  }
  
  /**
   * Throwing a ParameterException causes the message to be printed to the console, and
   * the program to exit with a non-zero exit code.
   * Also displays the usage help message.
   *
   * @brief Validate the input
   */
  private void validate()
  {
    isValidPath(cpuConfigPath);
    isValidPath(programPath);
    
    if (memoryConfigPath != null)
    {
      isValidPath(memoryConfigPath);
    }
  }
  
  /**
   * The path to the file is passed as an argument to the CLI.
   *
   * @brief Load the program from a file.
   */
  private String loadProgram() throws IOException
  {
    return Files.readString(programPath);
  }
  
  /**
   * The path to the file is passed as an argument to the CLI.
   *
   * @brief Load the CPU configuration from a file.
   */
  private CpuConfig loadCpuConfig() throws IOException
  {
    try (InputStream inputStream = Files.newInputStream(cpuConfigPath))
    {
      // Deserialize the file into a CpuConfig object
      ObjectMapper deserializer = Serialization.getDeserializer();
      return deserializer.readValue(inputStream, CpuConfig.class);
    }
  }
  
  /**
   * If no path is provided, the memory is empty.
   *
   * @brief Load the memory configuration from a file.
   */
  private List<MemoryLocation> loadMemoryConfig() throws IOException
  {
    if (memoryConfigPath == null)
    {
      return List.of();
    }
    try (InputStream inputStream = Files.newInputStream(memoryConfigPath))
    {
      // Deserialize the file into a list of MemoryLocation objects
      ObjectMapper deserializer = Serialization.getDeserializer();
      return deserializer.readValue(inputStream, new TypeReference<>()
      {
      });
    }
    catch (Exception e)
    {
      logger.severe("Error in file reading memory: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Throws an exception if the path does not exist, is not readable, or is not a regular file.
   *
   * @param path The path to a file
   */
  private void isValidPath(Path path)
  {
    // Check if path exists
    if (!Files.exists(path))
    {
      throw new ParameterException(spec.commandLine(), "File does not exist: " + path);
    }
    
    // Check if path is readable
    if (!Files.isReadable(path))
    {
      throw new ParameterException(spec.commandLine(), "File is not readable: " + path);
    }
    
    // Check if path is a regular file (not a directory)
    if (!Files.isRegularFile(path))
    {
      throw new ParameterException(spec.commandLine(), "File is not a regular file: " + path);
    }
  }
}
