/**
 * @file    RegisterModel.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of single register field
 *
 * @date  27 October  2020 15:00 (created) \n
 *        5  November 2020 18:22 (revised) \n
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.models;

/**
 * @class RegisterModel
 * @brief Definition of single register in register file
 */
public class RegisterModel
{
  /// Name of register
  private final String name;
  /// Tells if value of register is constant throughout lifetime of register file or not (example register x0 from risc-v)
  private final boolean isConstant;
  /// Value inside of register
  private double value;

  /**
   * @brief Constructor
   * @param [in] name       - Register name
   * @param [in] isConstant - Ture in case of static value, false otherwise
   * @param [in] value      - Register value
   */
  public RegisterModel(String name, boolean isConstant, double value)
  {
    this.name = name;
    this.isConstant = isConstant;
    this.value = value;
  }// end of Constructor
  //------------------------------------------------------

  /**
   * @brief Overrides toString method with custom formating
   * @return String representation of the object
   */
  @Override
  public String toString()
  {
    return "register name: " + name + ", " +
      (isConstant ? "constant " : "variable ") +
      "= " + value + '\n';
  }// end of toString
  //------------------------------------------------------

  /**
   * @brief Get register name
   * @return Register name
   */
  public String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------

  /**
   * @brief Get bool value, if value of the register can be edited or not
   * @return Bool value
   */
  public boolean isConstant()
  {
    return isConstant;
  }// end of isConstant
  //------------------------------------------------------

  /**
   * @brief Get register value
   * @return Value inside register
   */
  public double getValue()
  {
    return value;
  }// end of getValue
  //------------------------------------------------------

  /**
   * @brief Set register value.
   * @param [in] newValue - New value to be set. In case of constant register, value is ignored
   */
  public void setValue(double newValue)
  {
    this.value = this.isConstant ? this.value : newValue;
  }// end of setValue
  //------------------------------------------------------
}
