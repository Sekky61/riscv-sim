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
import com.gradle.superscalarsim.serialization.Serialization;

import java.util.List;

/**
 * @brief Class to call GCC
 */
public class GccCaller
{
  public static String compilerPath = "/usr/bin/riscv64-linux-gnu-gcc-12";
  
  public static CompileResult compile(String code, boolean optimize)
  {
    ProcessBuilder pb = new ProcessBuilder(getCommand(optimize));
    
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
  static List<String> getCommand(boolean optimize)
  {
    return List.of(compilerPath, "-xc", "-O" + (optimize ? "2" : "0"), "-march=rv32imfd", "-mabi=ilp32d", "-o",
                   "/dev/stdout", "-S", "-g", "-fverbose-asm", "-fcf-protection=none", "-fno-stack-protector",
                   "-fno-asynchronous-unwind-tables", "-fno-dwarf2-cfi-asm", "-nostdlib", "-fdiagnostics-format=json",
                   "-fpic", "-xc", "-");
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
