/**
 * @file DebugLog.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Messages generated during simulation
 * @date 24 jan      2024 13:00 (created)
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

package com.gradle.superscalarsim.cpu;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.models.instruction.DebugInfo;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Messages generated during simulation. Can be used for debugging.
 * Are shown in the GUI.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class DebugLog
{
  static String unknownRegister = "[UNKNOWN]";
  /**
   * The `${registerName}` pattern. It must be non-greedy, otherwise it would match wrong with multiple registers.
   */
  static Pattern formatPattern = Pattern.compile("[$][{](.*?)[}]");
  /**
   * List of messages
   */
  private List<Entry> entries;
  /**
   * Registers to format the messages
   */
  @JsonIdentityReference(alwaysAsId = true)
  private UnifiedRegisterFileBlock registerFile;
  
  /**
   * Constructor
   */
  public DebugLog(UnifiedRegisterFileBlock registerFile)
  {
    this.registerFile = registerFile;
    this.entries      = new ArrayList<>();
  }
  
  /**
   * @param debugInfo To generate the message to be added
   * @param cycle     Cycle when the message was generated
   *
   * @brief Add message to the log
   */
  public void add(DebugInfo debugInfo, int cycle)
  {
    String message = format(debugInfo);
    entries.add(new Entry(message, cycle));
  }
  
  /**
   * The format string utilizes format like ${r5} to insert the value of register r5.
   * Examples: "Hello ${sp}", "x7 = ${x7}".
   *
   * @return String, formatted.
   */
  private String format(DebugInfo debugInfo)
  {
    String message = debugInfo.formatString();
    if (message == null)
    {
      return null;
    }
    
    return formatPattern.matcher(message) // comment to force formatter break line
            .replaceAll(m ->
                        {
                          String        registerName = m.group(1);
                          RegisterModel register     = registerFile.getRegister(registerName);
                          if (register == null)
                          {
                            return unknownRegister;
                          }
                          return String.valueOf(register.getValueContainer().getStringRepresentation());
                        });
  }
  
  /**
   * @return List of messages
   */
  public List<Entry> getEntries()
  {
    return entries;
  }
  
  public static class Entry
  {
    /**
     * Message
     */
    private String message;
    
    /**
     * Cycle when the message was generated
     */
    private int cycle;
    
    /**
     * @param message Message
     * @param cycle   Cycle when the message was generated
     */
    public Entry(String message, int cycle)
    {
      this.message = message;
      this.cycle   = cycle;
    }
    
    /**
     * @return Message
     */
    public String getMessage()
    {
      return message;
    }
    
    /**
     * @return Cycle when the message was generated
     */
    public int getCycle()
    {
      return cycle;
    }
    
    @Override
    public String toString()
    {
      return "[" + cycle + "] " + message;
    }
  }
}
