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
 * @brief Parser for RISC-V assembly code. Filter assembly and produce mapping to C code.
 * Intended to parse the output of GCC, not handwritten assembly.
 */
public class AsmParser
{
  /**
   * Regex for labels
   */
  static String labelRegex = "^[a-zA-Z0-9_.]+:";
  
  /**
   * @param program The output of GCC. It is assumed that the program is not null
   *
   * @return The filtered assembly, and a mapping from ASM lines to C lines
   * @brief The parser
   * Takes in the output of GCC, and returns a clean version of the assembly with mapping to C code
   */
  public static CompiledProgram parse(String program)
  {
    assert program != null;
    // Split into lines, remove comments, replace \t with spaces, trim
    List<String> stringLines = splitLines(program);
    
    // Split into sections
    // https://ftp.gnu.org/old-gnu/Manuals/gas-2.9.1/html_chapter/as_7.html#SEC119
    List<Section> sections = splitSections(stringLines);
    // Filter the allocatable sections
    // For some reason, .rodata sometimes does not have the "a" flag
    sections.removeIf(section -> (!section.flags.contains("a") && !section.name.contains("rodata")));
    
    // Collect used labels
    Set<String> usedLabels = collectUsedLabels(stringLines);
    
    // Go through the program, mark the mapping from C to ASM
    List<Line> lines = markCMapping(stringLines);
    
    // Join the lines of the sections. Put the code sections first, then the data sections
    List<Line> joinedLines = new ArrayList<>();
    for (Section section : sections)
    {
      if (section.name.startsWith(".text"))
      {
        List<Line> mappedLines   = markCMapping(section.lines);
        List<Line> filteredLines = filterAsm(mappedLines, usedLabels);
        joinedLines.addAll(filteredLines);
      }
    }
    for (Section section : sections)
    {
      if (!section.name.startsWith(".text"))
      {
        List<Line> mappedLines   = markCMapping(section.lines);
        List<Line> filteredLines = filterAsm(mappedLines, usedLabels);
        joinedLines.addAll(filteredLines);
      }
    }
    
    // Add 4 spaces to the start of each line that is not a label
    for (int i = 0; i < joinedLines.size(); i++)
    {
      String line = joinedLines.get(i).asmLine;
      if (!Pattern.matches(labelRegex, line))
      {
        joinedLines.set(i, new Line(joinedLines.get(i).cLine, "    " + line));
      }
    }
    
    List<String>  finalLines = new ArrayList<>();
    List<Integer> asmToC     = new ArrayList<>();
    
    for (Line line : joinedLines)
    {
      finalLines.add(line.asmLine);
      asmToC.add(line.cLine);
    }
    
    return new CompiledProgram(finalLines, asmToC, usedLabels);
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
  
  /**
   * Split the program into sections. Takes into account the .text and .section directives.
   * Filter empty sections and sections beginning with ".section .debug"
   */
  private static List<Section> splitSections(List<String> stringLines)
  {
    List<Section> sections      = new ArrayList<>();
    int           activeSection = -1;
    for (String line : stringLines)
    {
      if (line.startsWith(".section"))
      {
        // Format: .section name[, "flags"[, @type]]
        String[] split = line.split("[ ,]");
        String   name  = split[1];
        String   flags = split.length > 2 ? split[2] : "";
        
        // If such section already exists, find it and make it active
        boolean found = false;
        for (int i = 0; i < sections.size(); i++)
        {
          if (sections.get(i).name.equals(name))
          {
            activeSection = i;
            found         = true;
            break;
          }
        }
        if (!found)
        {
          sections.add(new Section(name, flags, new ArrayList<>()));
          activeSection = sections.size() - 1;
          
        }
      }
      else if (activeSection != -1)
      {
        sections.get(activeSection).lines.add(line);
      }
    }
    return sections;
  }
  
  /**
   * .loc [file index] [line] [column] are directives that tell us position of the corresponding C code.
   * We will use this information to map the assembly to the C code.
   */
  private static List<Line> markCMapping(List<String> lines)
  {
    int        currentCLine = 0; // 0 is invalid value
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
      if (line.startsWith(".") && !line.contains("@function") && !line.contains(".word"))
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
  
  private static List<Line> filterAsm(List<Line> lines, Set<String> usedLabels)
  {
    List<Line> filteredLines = new ArrayList<>();
    // Remove unused labels
    for (Line value : lines)
    {
      String line = value.asmLine;
      if (Pattern.matches(labelRegex, line))
      {
        String labelName = line.split(":")[0];
        if (!usedLabels.contains(labelName))
        {
          continue;
        }
      }
      filteredLines.add(value);
    }
    
    // Remove directives not pointed to by labels
    List<Line> finalLines = new ArrayList<>();
    boolean    labeled    = false;
    for (Line filteredLine : filteredLines)
    {
      String  line            = filteredLine.asmLine;
      boolean isDirective     = line.startsWith(".") && !line.endsWith(":");
      boolean deleteException = line.startsWith(".loc") || line.startsWith(".file");
      boolean doNotDelete     = line.startsWith(".align");
      // Keep labeled status while encountering .byte, .word, etc.
      if (!isDirective || !isDataDirective(line))
      {
        labeled = Pattern.matches(labelRegex, line);
      }
      if (((!isDirective || labeled) && !deleteException) || doNotDelete)
      {
        finalLines.add(filteredLine);
      }
    }
    
    // Second pass - keep .align only if it is followed by a label and followed by data
    List<Line> linesToFilter = new ArrayList<>();
    for (int i = 0; i < finalLines.size(); i++)
    {
      String line = finalLines.get(i).asmLine;
      if (line.startsWith(".align"))
      {
        String  nextLine          = (i + 1 < finalLines.size()) ? finalLines.get(i + 1).asmLine : "";
        String  nextNextLine      = (i + 2 < finalLines.size()) ? finalLines.get(i + 2).asmLine : "";
        boolean isFollowedByLabel = Pattern.matches(labelRegex, nextLine);
        if (!isFollowedByLabel || !isDataDirective(nextNextLine))
        {
          continue;
        }
      }
      linesToFilter.add(finalLines.get(i));
    }
    return linesToFilter;
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
   * @return Set of labels in the program
   */
  public static Set<String> collectLabels(List<String> program)
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
  
  private static boolean isDataDirective(String line)
  {
    return line.startsWith(".byte") || line.startsWith(".word") || line.startsWith(".hword") || line.startsWith(
            ".ascii") || line.startsWith(".asciz") || line.startsWith(".string") || line.startsWith(".zero");
  }
  
  record Line(int cLine, String asmLine)
  {
  }
  
  record Section(String name, String flags, List<String> lines)
  {
  }
}
