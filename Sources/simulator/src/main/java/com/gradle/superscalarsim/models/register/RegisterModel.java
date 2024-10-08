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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.models.Identifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * @class RegisterModel
 * @brief Definition of single register in register file. It also holds data needed for renaming (references to speculative registers).
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class RegisterModel implements Identifiable
{
  /**
   * Name of register. Assumed to be unique by the serialization.
   */
  private String name;
  
  /**
   * True if the value of register is constant (example: register x0 from risc-v spec)
   */
  private boolean isConstant;
  
  /**
   * Data type of register (int, float)
   */
  private RegisterTypeEnum type;
  
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
   * Reference count. Relevant only for speculative registers.
   */
  private int referenceCount;
  
  /**
   * List of renames to speculative registers. Relevant only for architectural registers.
   */
  @JsonIdentityReference(alwaysAsId = true)
  List<RegisterModel> renames;
  
  /**
   * The architectural register that this speculative register is mapped to. Relevant only for speculative registers.
   */
  @JsonIdentityReference(alwaysAsId = true)
  private RegisterModel architecturalRegister;
  
  /**
   * @brief Default constructor for deserialization
   */
  public RegisterModel()
  {
    renames = new ArrayList<>();
  }
  
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
                       RegisterTypeEnum type,
                       float value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, type, readiness);
    this.value.setValue(value);
  }// end of Constructor
  
  /**
   * @param name       Register name
   * @param isConstant Ture in case of static value, false otherwise
   * @param type       Register type
   * @param readiness  Register readiness
   *
   * @brief Constructor with default register value
   */
  public RegisterModel(String name, boolean isConstant, RegisterTypeEnum type, RegisterReadinessEnum readiness)
  {
    this.name             = name;
    this.isConstant       = isConstant;
    this.type             = type;
    this.readiness        = readiness;
    this.value            = new RegisterDataContainer();
    renames               = new ArrayList<>();
    architecturalRegister = null;
  }// end of Constructor
  //------------------------------------------------------
  
  public RegisterModel(String name,
                       boolean isConstant,
                       RegisterTypeEnum type,
                       double value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, type, readiness);
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
                       RegisterTypeEnum type,
                       int value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, type, readiness);
    this.value.setValue(value);
  }// end of Constructor
  //------------------------------------------------------
  
  public RegisterModel(String name,
                       boolean isConstant,
                       RegisterTypeEnum type,
                       long value,
                       RegisterReadinessEnum readiness)
  {
    this(name, isConstant, type, readiness);
    this.value.setValue(value);
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * Copy constructor
   * TODO: test
   */
  public RegisterModel(RegisterModel register)
  {
    this.name       = register.name;
    this.isConstant = register.isConstant;
    this.type       = register.type;
    this.readiness  = register.readiness;
    this.value      = new RegisterDataContainer(register.value);
    
    this.referenceCount   = register.referenceCount;
    this.renames          = new ArrayList<>(register.renames);
    architecturalRegister = register.architecturalRegister;
  }// end of Copy constructor
  //------------------------------------------------------
  
  public int getReferenceCount()
  {
    return referenceCount;
  }
  
  /**
   * @return String representation of the object
   * @brief Overrides toString method with custom formating
   */
  @Override
  public String toString()
  {
    return "register " + name + (isConstant ? ", const" : "") + " = " + value.getStringRepresentation();
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
  @JsonIgnore
  public boolean isConstant()
  {
    return isConstant;
  }// end of isConstant
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
  
  /**
   * @param bits New value to be set
   */
  public void setBits(long bits)
  {
    value.setValue(bits);
  }
  
  /**
   * @brief Set register type metadata
   */
  public void setValue(long bits, DataTypeEnum type)
  {
    this.value.setValue(bits);
    this.value.setCurrentType(type);
  }
  
  /**
   * @param type Type to cast to (example: `Integer.class`)
   *
   * @return Value of register, cast to given type. Consumer must cast the result to the correct type.
   */
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
  
  /**
   * @return Reference to the value container. Changing this object changes the value of the register.
   */
  public RegisterDataContainer getValueContainer()
  {
    return value;
  }
  
  /**
   * @param register Register to copy from
   *
   * @brief Copy value from another register (this one is being assigned). Does not change the identity of data container.
   * Does not change constant register.
   */
  public void copyFrom(RegisterModel register)
  {
    if (isConstant)
    {
      return;
    }
    this.value.copyFrom(register.value);
  }
  
  /**
   * @return Unique identifier of the object
   * @brief Get the identifier
   */
  @Override
  public String getId()
  {
    return name;
  }
  
  /**
   * @return the type of register
   */
  public RegisterTypeEnum getType()
  {
    return type;
  }
  
  /**
   * @return true if the register is speculative
   */
  public boolean isSpeculative()
  {
    // todo: this is a small hack
    return this.name.startsWith("tg");
  }
  
  /**
   * @param argumentDataType The datatype of an argument
   *
   * @return True if value type is compatible with register, otherwise false
   * @brief Check argument and register data types if they fit within each other
   */
  public boolean canHold(final DataTypeEnum argumentDataType)
  {
    return switch (argumentDataType)
    {
      case kInt, kUInt, kLong, kULong, kBool, kByte, kShort, kChar -> type == RegisterTypeEnum.kInt;
      case kFloat, kDouble -> type == RegisterTypeEnum.kFloat;
    };
  }// end of checkDatatype
  
  /**
   * @brief Decrease reference count
   */
  public void reduceReference()
  {
    referenceCount--;
  }
  
  /**
   * @brief Increase reference count
   */
  public void increaseReference()
  {
    referenceCount++;
  }
  
  /**
   * Add a rename to an architectural register. Invalid to call on speculative registers.
   * Link the rename back, both ways.
   */
  public void addRename(RegisterModel rename)
  {
    renames.add(rename);
    rename.architecturalRegister = this;
  }
  
  /**
   * @return The architectural register that this speculative register is mapped to, or null if it is not mapped.
   */
  public RegisterModel getArchitecturalMapping()
  {
    return architecturalRegister;
  }
  
  /**
   * @param speculativeRegister Speculative register to be removed from the list of renames
   *
   * @brief Remove a rename from the list of renames. Removes both ways of relation.
   */
  public void removeRename(RegisterModel speculativeRegister)
  {
    // called on the architectural register
    renames.removeIf(rename -> rename == speculativeRegister);
    speculativeRegister.architecturalRegister = null;
  }
  
  /**
   * @return The newest mapping of the register. If the register is constant or has no renames, returns itself.
   */
  public RegisterModel getNewestMapping()
  {
    if (isConstant() || renames.isEmpty())
    {
      return this;
    }
    
    return renames.get(renames.size() - 1);
  }
  
  /**
   * Transfer the value of this register to the architectural register it is mapped to.
   */
  public void copyToArchitectural()
  {
    getArchitecturalMapping().copyFrom(this);
  }
}
