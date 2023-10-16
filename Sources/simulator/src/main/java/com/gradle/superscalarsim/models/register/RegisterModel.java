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
  private RegisterDataContainer value;
  
  /**
   * State of the register in terms of readiness
   * Architecture registers are `kAssigned` by default, speculative ones are `kFree`
   */
  private RegisterReadinessEnum readiness;
  
  /**
   * @param name       Register name
   * @param isConstant Ture in case of static value, false otherwise
   * @param dataType   Register data type
   * @param value      Register floating point value
   * @param readiness  Register readiness
   *
   * @brief Float Constructor
   */
  public RegisterModel(String name,
                       boolean isConstant,
                       DataTypeEnum dataType,
                       float value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, dataType, readiness);
    this.value.setValue(value);
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param name       Register name
   * @param isConstant Ture in case of static value, false otherwise
   * @param dataType   Register data type
   * @param readiness  Register readiness
   *
   * @brief Constructor with default register value
   */
  public RegisterModel(String name, boolean isConstant, DataTypeEnum dataType, RegisterReadinessEnum readiness)
  {
    this.name       = name;
    this.isConstant = isConstant;
    this.dataType   = dataType;
    this.readiness  = readiness;
    this.value      = new RegisterDataContainer();
  }// end of Constructor
  //------------------------------------------------------
  
  public RegisterModel(String name,
                       boolean isConstant,
                       DataTypeEnum dataType,
                       double value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, dataType, readiness);
    this.value.setValue(value);
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param name       Register name
   * @param isConstant Ture in case of static value, false otherwise
   * @param dataType   Register data type
   * @param value      Register integer value
   * @param readiness  Register readiness
   *
   * @brief Integer Constructor
   */
  public RegisterModel(String name,
                       boolean isConstant,
                       DataTypeEnum dataType,
                       int value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, dataType, readiness);
    this.value.setValue(value);
  }// end of Constructor
  //------------------------------------------------------
  
  public RegisterModel(String name,
                       boolean isConstant,
                       DataTypeEnum dataType,
                       long value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, dataType, readiness);
    this.value.setValue(value);
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
    return "register " + name + ", " + (isConstant ? "constant " : "variable ") + "= " + value.getString(
            dataType) + '\n';
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
    return (double) value.getValue(DataTypeEnum.kDouble);
  }// end of getValue
  //------------------------------------------------------
  
  /**
   * @param newValue New value to be set. The type of the value must be one of the following: Integer, Long, Float, Double.
   *
   * @brief Set register value. In case of constant register, value is not changed.
   */
  public <T> void setValue(T newValue)
  {
    if (this.isConstant)
    {
      return;
    }
    Class<?> type = newValue.getClass();
    if (type == Integer.class)
    {
      this.value.setValue((Integer) newValue);
    }
    else if (type == Long.class)
    {
      this.value.setValue((Long) newValue);
    }
    else if (type == Float.class)
    {
      this.value.setValue((Float) newValue);
    }
    else if (type == Double.class)
    {
      this.value.setValue((Double) newValue);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }// end of setValue
  
  public Object getValue(DataTypeEnum type)
  {
    return value.getValue(type);
  }
  
  /**
   * @return Register readiness
   */
  public RegisterReadinessEnum getReadiness()
  {
    return readiness;
  }
  
  /**
   * @param readiness New readiness to be set
   */
  public void setReadiness(RegisterReadinessEnum readiness)
  {
    this.readiness = readiness;
  }
  
  public RegisterDataContainer getValueContainer()
  {
    return value;
  }
  
  public void setValueContainer(RegisterDataContainer container)
  {
    this.value = container;
  }
}
