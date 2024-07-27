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
package com.gradle.superscalarsim.models.instruction;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gradle.superscalarsim.code.CodeToken;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import com.gradle.superscalarsim.models.register.RegisterModel;

/**
 * @class InputCodeArgument
 * @brief Container of argument of parsed instruction.
 * Can either be a register, constant or label.
 */
public class InputCodeArgument
{
  /**
   * Parsed constant value of the argument. Uses type info from instruction definition.
   */
  private RegisterDataContainer constantValue;
  /**
   * Name of the argument.
   * Example: rs1, imm, labelName.
   */
  private String name;
  /**
   * Register value of the argument.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private RegisterModel registerValue;
  /**
   * Value of the argument.
   * Example: x5, 10, name of a label.
   */
  @JsonIgnore
  private CodeToken stringValue;
  
  /**
   * @param name  Name of the argument
   * @param value Value of the argument
   *
   * @brief Constructor for textual argument
   */
  public InputCodeArgument(final String name, CodeToken value)
  {
    this.name          = name;
    this.stringValue   = value;
    this.constantValue = null;
    this.registerValue = null;
  }// end of Constructor
  
  /**
   * @brief Constructor for constant argument
   */
  public InputCodeArgument(final String name, final RegisterDataContainer constantValue)
  {
    this.name          = name;
    this.constantValue = constantValue;
    this.stringValue   = new CodeToken(0, 0, constantValue.getStringRepresentation(), CodeToken.Type.SYMBOL);
    this.registerValue = null;
  }// end of Constructor
  
  /**
   * @param name          Name of the argument
   * @param regName       Name of the register
   * @param registerValue Register value of the argument. May be null.
   *
   * @brief Constructor for register argument.
   */
  public InputCodeArgument(final String name, final String regName, final RegisterModel registerValue)
  {
    this.name          = name;
    this.registerValue = registerValue;
    this.stringValue   = new CodeToken(0, 0, regName, CodeToken.Type.SYMBOL);
    this.constantValue = null;
  }// end of Constructor
  
  /**
   * @param argument Object to be copied
   *
   * @brief Copy constructor
   */
  public InputCodeArgument(final InputCodeArgument argument)
  {
    this.name          = argument.getName();
    this.stringValue   = new CodeToken(argument.getValueToken());
    this.registerValue = argument.getRegisterValue();
    if (argument.getConstantValue() != null)
    {
      this.constantValue = new RegisterDataContainer(argument.getConstantValue());
    }
    else
    {
      this.constantValue = null;
    }
  }// end of Constructor
  
  /**
   * @return Argument name
   * @brief Get name of the argument
   */
  public String getName()
  {
    return name;
  }// end of getName
  
  /**
   * @return True if the argument is a register
   */
  public boolean isRegister()
  {
    return registerValue != null;
  }
  
  /**
   * @return Argument value
   * @brief Get value of the argument
   */
  @JsonProperty("stringValue")
  public String getValue()
  {
    return stringValue.text();
  }// end of getValue
  //------------------------------------------------------
  
  /**
   * @return string value in the form of CodeToken
   */
  public CodeToken getValueToken()
  {
    return stringValue;
  }
  
  /**
   * @return Register value of the argument
   */
  public RegisterModel getRegisterValue()
  {
    return registerValue;
  }
  //------------------------------------------------------
  
  /**
   * @brief Set register value of the argument. Used for speculative register renaming.
   */
  public void setRegisterValue(RegisterModel registerValue)
  {
    this.registerValue = registerValue;
    //
    //    this.stringValue   = registerValue.getName();
  }
  //------------------------------------------------------
  
  /**
   * @return Constant value of the argument, if it is a constant
   */
  public RegisterDataContainer getConstantValue()
  {
    return constantValue;
  }
  //------------------------------------------------------
  
  public void setConstantValue(RegisterDataContainer constantValue)
  {
    this.constantValue = constantValue;
  }
  //------------------------------------------------------
  
  /**
   * @param stringValue New value of argument
   *
   * @brief Sets new value of the argument (string). Reference to register or constant value is not changed.
   */
  public void setStringValue(final String stringValue)
  {
    this.stringValue = new CodeToken(0, 0, stringValue, CodeToken.Type.SYMBOL);
  }// end of setValue
  //------------------------------------------------------
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    return name + " = " + stringValue;
  }
}
