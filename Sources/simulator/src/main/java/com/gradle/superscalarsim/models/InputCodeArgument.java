/**
 * @file InputCodeArgument.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container of argument of parsed instruction
 * @date 10 November  2020 17:45 (created) \n
 * 11 November  2020 14:05 (revised)
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
package com.gradle.superscalarsim.models;

/**
 * @class InputCodeArgument
 * @brief Container of argument of parsed instruction
 */
public class InputCodeArgument
{
  /**
   * Name of the argument
   * Example: rs1, imm
   */
  private String name;
  /**
   * Value of the argument
   * Example: x5, 10, name of a label
   */
  private String value;
  
  /**
   * @param [in] name  - Name of the argument
   * @param [in] value - Value of the argument
   *
   * @brief Constructor
   */
  public InputCodeArgument(final String name, final String value)
  {
    this.name  = name;
    this.value = value;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param [in] argument - Object to be copied
   *
   * @brief Copy constructor
   */
  public InputCodeArgument(final InputCodeArgument argument)
  {
    this.name  = argument.getName();
    this.value = argument.getValue();
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @return Argument name
   * @brief Get name of the argument
   */
  public String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------
  
  /**
   * @param [in] name - New name for argument
   *
   * @brief Sets new name to argument
   */
  public void setName(final String name)
  {
    this.name = name;
  }// end of setName
  //------------------------------------------------------
  
  /**
   * @return Argument value
   * @brief Get value of the argument
   */
  public String getValue()
  {
    return value;
  }// end of getValue
  //------------------------------------------------------
  
  /**
   * @param [in] value - New value of argument
   *
   * @brief Sets new value of the argument
   */
  public void setValue(final String value)
  {
    this.value = value;
  }// end of setValue
  //------------------------------------------------------
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    return name + " = " + value;
  }
}
