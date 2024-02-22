/**
 * @file FunctionalUnitDescription.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief All the configuration for the CPU - can be used to create a CpuState
 * @date 18 Dec      2023 13:00 (created)
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

package com.gradle.superscalarsim.models;

import com.gradle.superscalarsim.code.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @brief Function unit description
 */
public class FunctionalUnitDescription
{
  /**
   * AFAIK not used
   */
  public int id;
  
  /**
   * Name of the FUnit.
   * Shows up in simulation visualisation, also used for debugging and statistics.
   */
  public String name;
  
  /**
   * Latency of the FUnit.
   * Counts only for Branch and Memory FUnits.
   * Set a sensible default anyway, since this latency is used for arithmetic operations (type casts) as well.
   */
  public int latency;
  
  /**
   * Type of the FUnit.
   */
  public Type fuType;
  
  /**
   * Classes of operations that this FUnit can perform.
   * Each class has its own latency.
   */
  public List<Capability> operations;
  
  /**
   * @brief Constructor for deserialization
   */
  public FunctionalUnitDescription()
  {
  
  }
  
  /**
   * Constructor for FX and FP FUnits
   */
  public FunctionalUnitDescription(int id, Type fuType, List<Capability> operations, String name)
  {
    this(id, fuType, operations);
    this.name = name;
  }
  
  /**
   * Constructor for FX and FP FUnits
   */
  public FunctionalUnitDescription(int id, Type fuType, List<Capability> operations)
  {
    this.id         = id;
    this.name       = "FUnit " + id;
    this.fuType     = fuType;
    this.operations = operations;
    // Should not be used
    this.latency = 1;
  }
  
  /**
   * Constructor for L/S, Branch, Memory FUnits
   */
  public FunctionalUnitDescription(int id, Type fuType, int latency, String name)
  {
    this(id, fuType, latency);
    this.name = name;
  }
  
  /**
   * Constructor for L/S, Branch, Memory FUnits
   */
  public FunctionalUnitDescription(int id, Type fuType, int latency)
  {
    this.id      = id;
    this.name    = "FUnit " + id;
    this.fuType  = fuType;
    this.latency = latency;
  }
  
  /**
   * Classify an operation into a capability. Categories have this precedence:
   * <p>
   * special > division > multiplication > addition > bitwise
   * TODO move, or better yet, mark the instruction type in the InstructionFunctionModel
   *
   * @param expr Expression to classify (e.g. "\rs1 \rs2 * \rs3 + \rd =")
   */
  public static CapabilityName classifyOperation(String expr)
  {
    for (String op : Expression.specialOperators)
    {
      if (expr.contains(op))
      {
        return CapabilityName.special;
      }
    }
    for (String op : Expression.divisionOperators)
    {
      if (expr.contains(op))
      {
        return CapabilityName.division;
      }
    }
    for (String op : Expression.multiplicationOperators)
    {
      if (expr.contains(op))
      {
        return CapabilityName.multiplication;
      }
    }
    for (String op : Expression.additionOperators)
    {
      if (expr.contains(op))
      {
        return CapabilityName.addition;
      }
    }
    for (String op : Expression.bitwiseOperators)
    {
      if (expr.contains(op))
      {
        return CapabilityName.bitwise;
      }
    }
    // Probably a type cast, move. Use an adder
    return CapabilityName.addition;
  }
  
  /**
   * @return List of operations that this FUnit can perform based on its capabilities
   * {@link Expression}
   */
  public List<String> getAllowedOperations()
  {
    // Base
    List<String> ops = new ArrayList<>(Arrays.asList(Expression.baseOperators));
    // Add operations based on capabilities
    for (Capability capability : operations)
    {
      switch (capability.name)
      {
        case addition -> ops.addAll(Arrays.asList(Expression.additionOperators));
        case bitwise -> ops.addAll(Arrays.asList(Expression.bitwiseOperators));
        case multiplication -> ops.addAll(Arrays.asList(Expression.multiplicationOperators));
        case division -> ops.addAll(Arrays.asList(Expression.divisionOperators));
        case special -> ops.addAll(Arrays.asList(Expression.specialOperators));
      }
    }
    return ops;
  }
  
  /**
   * Types of FUnits
   */
  public enum Type
  {
    FX, FP, L_S, Branch, Memory,
  }
  
  /**
   * Enumeration of kinds of FUnit capabilities
   */
  public enum CapabilityName
  {
    addition, bitwise, multiplication, division, special,
  }
  
  /**
   * Configuration of a capability.
   */
  public static class Capability
  {
    public CapabilityName name;
    public int latency;
    
    public Capability()
    {
    }
    
    public Capability(CapabilityName name, int latency)
    {
      this.name    = name;
      this.latency = latency;
    }
  }
}
