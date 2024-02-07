/**
 * @file GccCaller.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief xxx
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

package com.gradle.superscalarsim.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.loader.ConfigLoader;
import com.gradle.superscalarsim.serialization.Serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @brief Class to call GCC
 */
public class GccCaller
{
  public static String compilerPath = ConfigLoader.gccPath;
  
  /**
   * Map of optimization flags
   */
  public static Map<String, String> optimizeFlags = Map.of("O2", "-O2", "rename", "-frename-registers", "unroll",
                                                           "-funroll-all-loops", "peel", "-fpeel-loops", "inline",
                                                           "-finline-functions", "omit-frame-pointer",
                                                           "-fomit-frame-pointer");
  
  /**
   * <a href="https://gcc.gnu.org/onlinedocs/gcc/Code-Gen-Options.html">GCC options</a>
   * <ul>
   *   <li>`-mno-explicit-relocs` - disable symbolic address splitting (%hi/%lo) for RISC-V</li>
   *   <li>`-ffunction-sections`  - put each function in its own section</li>
   *   <li>`-mstrict-align`       - Generate aligned memory accesses</li>
   *   <li>`-fPIE`                - Generate position independent code (gets rid of PLT)</li>
   *  </ul>
   */
  public static List<String> gccFlags = List.of("-xc", "-march=rv32imfd", "-mabi=ilp32d", "-o", "/dev/stdout", "-S",
                                                "-g", "-fverbose-asm", "-fcf-protection=none", "-fno-stack-protector",
                                                "-fno-asynchronous-unwind-tables", "-mno-explicit-relocs",
                                                "-ffunction-sections", "-fdata-sections", "-fno-dwarf2-cfi-asm",
                                                "-finhibit-size-directive", "-mstrict-align", "-nostdlib",
                                                "-fdiagnostics-format=json", "-fPIE", "-fno-plt",
                                                "-fvisibility=default", "-xc", "-");
  
  public static CompileResult compile(String code, List<String> optimizeFlags)
  {
    ProcessBuilder pb = new ProcessBuilder(getCommand(optimizeFlags));
    
    // Pipe the code into the process, and get the output from stdout
    pb.redirectInput(ProcessBuilder.Redirect.PIPE);
    pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
    pb.redirectError(ProcessBuilder.Redirect.PIPE);
    // Start the process
    Process p = null;
    try
    {
      p = pb.start();
    }
    catch (Exception e)
    {
      return CompileResult.failure("Error starting GCC", null);
    }
    // Write the code to the process
    try
    {
      p.getOutputStream().write(code.getBytes());
      p.getOutputStream().close();
    }
    catch (Exception e)
    {
      return CompileResult.failure("Error writing to GCC", null);
    }
    // Read the output
    String output = null;
    try
    {
      output = new String(p.getInputStream().readAllBytes());
    }
    catch (Exception e)
    {
      return CompileResult.failure("Error reading from GCC", null);
    }
    // Wait for the process to finish
    try
    {
      p.waitFor();
    }
    catch (Exception e)
    {
      return CompileResult.failure("Error waiting for GCC", null);
    }
    // Read the exit value
    int exitValue = p.exitValue();
    if (exitValue != 0)
    {
      // Take error from stderr
      List<Object> error = null;
      try
      {
        String error_string = new String(p.getErrorStream().readAllBytes());
        // error should be a JSON string. Parse it to an object
        ObjectMapper deserializer = Serialization.getDeserializer();
        error = deserializer.readValue(error_string, new TypeReference<>()
        {
        });
        System.err.println("Error from GCC: " + error);
      }
      catch (Exception e)
      {
        return CompileResult.failure("GCC returned non-zero exit value: " + exitValue, null);
      }
      return CompileResult.failure("GCC returned non-zero exit value: " + exitValue, error);
    }
    return CompileResult.success(output);
  }
  
  // /usr/bin/riscv64-linux-gnu-gcc -xc -O0 -march=rv32imfd -mabi=ilp32d -o /dev/stdout -S -g -fverbose-asm -fcf-protection=none -fno-stack-protector -fno-asynchronous-unwind-tables -fno-dwarf2-cfi-asm -nostdlib -xc -
  static List<String> getCommand(List<String> optimizeFlags)
  {
    // Add optimization flags
    List<String> extraFlags = new ArrayList<>();
    for (String flag : optimizeFlags)
    {
      if (GccCaller.optimizeFlags.containsKey(flag))
      {
        extraFlags.add(GccCaller.optimizeFlags.get(flag));
      }
    }
    
    List<String> command = new ArrayList<>();
    command.add(compilerPath);
    command.addAll(extraFlags);
    command.addAll(gccFlags);
    return command;
  }
  
  public static class CompileResult
  {
    public boolean success;
    public String code;
    public String error;
    public List<Object> compilerErrors;
    
    private CompileResult(boolean success, String code, String error, List<Object> compilerErrors)
    {
      this.success        = success;
      this.code           = code;
      this.error          = error;
      this.compilerErrors = compilerErrors;
    }
    
    public static CompileResult success(String code)
    {
      return new CompileResult(true, code, null, null);
    }
    
    public static CompileResult failure(String error, List<Object> compilerErrors)
    {
      return new CompileResult(false, null, error, compilerErrors);
    }
  }
}
