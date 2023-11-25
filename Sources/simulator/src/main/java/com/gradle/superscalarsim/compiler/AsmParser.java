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

import java.util.*;
import java.util.regex.Pattern;

/**
 * The output of GCC has a lot of irrelevant information. This class filters out the irrelevant information.
 * <ul>
 *   <li>Remove comments</li>
 *   <li>Produce mapping</li>
 *   <li>Filter unused labels</li>
 *   <li>Filter directives not pointed to by remaining labels</li>
 * </ul>
 *
 * @brief Parser for RISC-V assembly code. Filter assembly and produce mapping to C code
 */
public class AsmParser
{
  /**
   * Regex for labels
   */
  static String labelRegex = "^[a-zA-Z0-9_.]+:";
  
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
    List<String> stringLines = splitLines(program);
    
    // Go through the program
    // Take note of .loc [file index] [line] [column]
    List<Line> lines = markCMapping(stringLines);
    
    // Collect used labels
    Set<String> usedLabels = collectUsedLabels(stringLines);
    
    // Remove unused labels
    for (int i = lines.size() - 1; i >= 0; i--)
    {
      String line = lines.get(i).asmLine;
      if (Pattern.matches(labelRegex, line))
      {
        String labelName = line.split(":")[0];
        if (!usedLabels.contains(labelName))
        {
          lines.remove(i);
        }
      }
    }
    
    // Remove directives not pointed to by labels
    for (int i = lines.size() - 1; i >= 0; i--)
    {
      String line = lines.get(i).asmLine;
      if (line.startsWith("."))
      {
        // Check last line
        if (i == 0)
        {
          lines.remove(i);
          continue;
        }
        Line lastLine = lines.get(i - 1);
        if (!Pattern.matches(labelRegex, lastLine.asmLine))
        {
          lines.remove(i);
        }
      }
    }
    
    List<String>  finalLines = new ArrayList<>();
    List<Integer> cLines     = new ArrayList<>();
    List<Integer> asmToC     = new ArrayList<>();
    
    for (Line line : lines)
    {
      finalLines.add(line.asmLine);
      asmToC.add(line.cLine);
    }
    
    return new CompiledProgram(finalLines, cLines, asmToC);
  }
  
  /**
   * Split the program into lines, remove comments, replace \t with spaces, trim
   *
   * @return List of lines
   */
  public static List<String> splitLines(String program)
  {
    List<String> lines = Arrays.asList(program.split("\n"));
    lines = new ArrayList<>(lines);
    for (int i = 0; i < lines.size(); i++)
    {
      lines.set(i, removeComment(lines.get(i)).replace("\t", " ").trim());
    }
    // Remove empty lines
    lines.removeIf(String::isEmpty);
    return lines;
  }
  
  private static List<Line> markCMapping(List<String> lines)
  {
    int        currentCLine = 0;
    List<Line> cleanProgram = new ArrayList<>();
    for (String line : lines)
    {
      // If the line is a .loc, update the current C line
      if (isMappingLine(line))
      {
        // 1 indexed C file, and code editor
        currentCLine = parseMappingLine(line);
      }
      cleanProgram.add(new Line(currentCLine, line));
    }
    return cleanProgram;
  }
  
  private static Set<String> collectUsedLabels(List<String> program)
  {
    Set<String> labels     = collectLabels(program);
    Set<String> usedLabels = new HashSet<>();
    for (String line : program)
    {
      String[] parts = line.split("[ ,()]+");
      // Do not regard the line if it is a directive
      // But do regard it if it is a ".type label, @function"
      if (line.startsWith(".") && !line.contains("@function"))
      {
        continue;
      }
      for (String part : parts)
      {
        if (labels.contains(part))
        {
          usedLabels.add(part);
        }
      }
    }
    return usedLabels;
  }
  
  /**
   * @brief Removes comments from a line
   */
  private static String removeComment(String s)
  {
    return s.split("#")[0];
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
   * @param program The program to look for labels in
   *
   * @return List of labels in the program
   */
  private static Set<String> collectLabels(List<String> program)
  {
    Set<String> labels = new HashSet<>();
    for (String line : program)
    {
      if (Pattern.matches(labelRegex, line))
      {
        String labelName = line.split(":")[0];
        labels.add(labelName);
      }
    }
    return labels;
  }
  
  record Line(int cLine, String asmLine)
  {
  }
}
