/**
 * @file AsmParser.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @brief Parser for RISC-V assembly code
 * Produces clean assembly to be used by the simulator - only relevant lines, labels
 * Also produces mapping from C code to assembly
 */
public class AsmParser
{
  
  /**
   * List of prefixes of lines that should be removed
   */
  static List<String> badPrefixes = new ArrayList<>(
          Arrays.asList("#", ".loc", ".LCFI", ".Ltext", ".file", ".LFB", ".LFE", ".align", ".globl", ".type"));
  
  /**
   * @param program       - The output of GCC
   * @param lengthOfCCode - The length of the C code, in lines
   *
   * @return The filtered assembly, and a mapping from ASM lines to C lines
   * @brief The parser
   * Takes in the output of GCC, and returns a clean version of the assembly with mapping to C code
   */
  public static CompiledProgram parse(String program, int lengthOfCCode)
  {
    // Split into lines, remove comments, replace \t with spaces, trim
    List<String> lines = Arrays.asList(program.split("\n"));
    lines = new ArrayList<>(lines);
    for (int i = 0; i < lines.size(); i++)
    {
      lines.set(i, removeComment(lines.get(i)).replace("\t", " ").trim());
    }
    // Remove empty lines
    lines.removeIf(String::isEmpty);
    
    // Determine the starting and ending line of the program, filter out the rest
    int[]        span         = programSpan(lines);
    List<String> programLines = lines.subList(span[0], span[1]);
    
    // Go through the program
    // Take note of .loc [file index] [line] [column]
    int           currentCLine = 0;
    List<String>  cleanProgram = new ArrayList<>();
    List<Integer> cLines       = new ArrayList<>();
    for (int i = 0; i <= lengthOfCCode; i++)
    {
      cLines.add(0);
    }
    List<Integer> asmToC = new ArrayList<>();
    
    // This if handles the case where the ASM output is empty (has been filtered out)
    if (!programLines.isEmpty())
    {
      asmToC.add(0);
    }
    
    for (String line : programLines)
    {
      // If the line is a .loc, update the current C line
      if (isMappingLine(line))
      {
        // 1 indexed C file, and code editor
        currentCLine = parseMappingLine(line);
        cLines.set(currentCLine, currentCLine);
      }
      
      // Decide whether to add the line to the clean program
      if (keepLine(line))
      {
        // Add tab to the start of the line if it is not a label
        if (!isEntityStart(line))
        {
          line = "\t" + line;
        }
        cleanProgram.add(line);
        asmToC.add(currentCLine);
      }
      
    }
    return new CompiledProgram(cleanProgram, cLines, asmToC);
  }
  
  /**
   * @brief Removes comments from a line
   */
  private static String removeComment(String s)
  {
    return s.split("#")[0];
  }
  
  /**
   * @param program - The output of GCC
   *
   * @return The starting and ending line of the program (inclusive and exclusive)
   * @brief Determines the starting and ending line of the program. Anything outside of this range is removed.
   */
  public static int[] programSpan(List<String> program)
  {
    int     start    = 0;
    int     end      = program.size();
    boolean foundEnd = false;
    for (int i = 0; i < program.size(); i++)
    {
      if (isEntityStart(program.get(i)) && start == 0)
      {
        start = i;
      }
      if (isEntityEnd(program.get(i)))
      {
        end      = i;
        foundEnd = true;
      }
    }
    if (!foundEnd)
    {
      return new int[]{0, 0};
    }
    return new int[]{start, end};
  }
  
  /**
   * @return True if the line contains information about the mapping from C to ASM
   */
  private static boolean isMappingLine(String line)
  {
    // Assuming the line is trimmed, check if it starts with .loc
    return line.startsWith(".loc");
  }
  
  /**
   * @return The line number
   * @brief Extracts the line number from a mapping line
   * Assumes the line is a ".loc" assembly line
   */
  private static int parseMappingLine(String line)
  {
    // example: ".loc 1 2 12"
    String[] parts = line.split(" ");
    return Integer.parseInt(parts[2]);
  }
  
  /**
   * @param line - The line to check
   *
   * @return True if the line should be kept
   * @brief Assembly filter
   */
  private static boolean keepLine(String line)
  {
    // Keep the line if it doesn't start with a bad prefix
    for (String prefix : badPrefixes)
    {
      if (line.startsWith(prefix))
      {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @param line - The line to check
   *
   * @return True if the line is the start of an entity (relevant part of the program)
   */
  public static boolean isEntityStart(String line)
  {
    return Pattern.matches("^\\S+:$", line);
  }
  
  /**
   * @param line - The line to check
   *
   * @return True if the line is the end of an entity
   */
  public static boolean isEntityEnd(String line)
  {
    return line.startsWith(".size");
  }
}
