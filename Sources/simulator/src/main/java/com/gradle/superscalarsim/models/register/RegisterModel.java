/**
 * @file RegisterModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of single register field
 * @date 27 October  2020 15:00 (created) \n
 * 5  November 2020 18:22 (revised) \n
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
package com.gradle.superscalarsim.models.register;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;

/**
 * @class RegisterModel
 * @brief Definition of single register in register file
 */
public class RegisterModel
{
  /**
   * Name of register
   */
  private final String name;
  
  /**
   * True if the value of register is constant (example: register x0 from risc-v spec)
   */
  private final boolean isConstant;
  
  /**
   * Data type of register (int, float)
   */
  private final DataTypeEnum dataType;
  
  /**
   * Value inside the register
   */
  private double value;
  
  /**
   * State of the register in terms of readiness
   * Architecture registers are `kAssigned` by default, speculative ones are `kFree`
   */
  private RegisterReadinessEnum readiness;
  
  /**
   * @param [in] name       - Register name
   * @param [in] isConstant - Ture in case of static value, false otherwise
   * @param [in] value      - Register value
   *
   * @brief Constructor
   */
  public RegisterModel(String name,
                       boolean isConstant,
                       DataTypeEnum dataType,
                       double value,
                       RegisterReadinessEnum readiness)
  {
    this.name       = name;
    this.isConstant = isConstant;
    this.dataType   = dataType;
    this.value      = value;
    this.readiness  = readiness;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * Copy constructor
   */
  public RegisterModel(RegisterModel register)
  {
    this.name       = register.name;
    this.isConstant = register.isConstant;
    this.dataType   = register.dataType;
    this.value      = register.value;
    this.readiness  = register.readiness;
  }// end of Copy constructor
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formating
   */
  @Override
  public String toString()
  {
    return "register name: " + name + ", " + (isConstant ? "constant " : "variable ") + "= " + value + '\n';
  }// end of toString
  //------------------------------------------------------
  
  /**
   * @return Register name
   * @brief Get register name
   */
  public String getName()
  {
    return name;
  }// end of getName
  //------------------------------------------------------
  
  /**
   * @return Bool value
   * @brief Get bool value, if value of the register can be edited or not
   */
  public boolean isConstant()
  {
    return isConstant;
  }// end of isConstant
  //------------------------------------------------------
  
  /**
   * @brief Get data type of register
   */
  public DataTypeEnum getDataType()
  {
    return dataType;
  }
  
  /**
   * @return Value inside register
   * @brief Get register value
   */
  public double getValue()
  {
    return value;
  }// end of getValue
  //------------------------------------------------------
  
  /**
   * @param [in] newValue - New value to be set. In case of constant register, value is ignored
   *
   * @brief Set register value.
   */
  public void setValue(double newValue)
  {
    this.value = this.isConstant ? this.value : newValue;
  }// end of setValue
  //------------------------------------------------------
  
  /**
   * @return Register readiness
   */
  public RegisterReadinessEnum getReadiness()
  {
    return readiness;
  }
  
  /**
   * @param readiness - New readiness to be set
   */
  public void setReadiness(RegisterReadinessEnum readiness)
  {
    this.readiness = readiness;
  }
}
