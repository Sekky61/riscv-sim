/**
 * @file RegisterDataContainer.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for register data container
 * @date 15 October      2023 15:00 (revised)
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

/**
 * Holds the bit representation of the register value. This value may be
 * interpreted as a float, int, or long depending on the register's data type.
 * The details of interplay between 32bit and 64bit values are not important, as
 * the CPU core is operating only in one of these modes at a time.
 *
 * @class RegisterDataContainer
 * @brief Class for register data container
 */
public class RegisterDataContainer
{
  /**
   * A bit representation of the register value
   */
  private long bits;
  
  /**
   * @brief Constructor
   */
  public RegisterDataContainer()
  {
    this.bits = 0;
  }
  
  public static <T> RegisterDataContainer fromValue(T value)
  {
    RegisterDataContainer container = new RegisterDataContainer();
    DataTypeEnum          type      = DataTypeEnum.fromJavaClass(value.getClass());
    if (type == null)
    {
      throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }
    container.setValue(value, type);
    return container;
  }
  
  public <T> void setValue(T value, DataTypeEnum type)
  {
    switch (type)
    {
      case kInt, kUInt -> setValue((int) value);
      case kLong, kULong -> setValue((long) value);
      case kFloat -> setValue((float) value);
      case kDouble -> setValue((double) value);
      case kBool -> setValue((boolean) value);
      default -> throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }
  
  public void setValue(int bits)
  {
    this.bits = Integer.toUnsignedLong(bits);
  }
  
  public void setValue(long value)
  {
    this.bits = value;
  }
  
  public void setValue(float value)
  {
    int bits = Float.floatToIntBits(value);
    this.bits = Integer.toUnsignedLong(bits);
  }
  
  public void setValue(double value)
  {
    this.bits = Double.doubleToLongBits(value);
  }
  
  public void setValue(boolean value)
  {
    this.bits = value ? 1 : 0;
  }
  
  /**
   * @return The bit representation of the register value
   */
  public long getBits()
  {
    return bits;
  }
  
  /**
   * @param type Type to cast to (example: Integer.class)
   * @param <T>  Type to cast to
   *
   * @return Value of register, cast to given type.
   * @brief Get value of register. Interprets the saved bit sequence as a value of given type.
   */
  public Object getValue(DataTypeEnum type)
  {
    Class<?> cls = type.getJavaClass();
    if (cls == null)
    {
      throw new IllegalArgumentException("Unsupported type: " + type);
    }
    return switch (type)
    {
      case kInt, kUInt -> cls.cast((int) bits);
      case kLong, kULong -> cls.cast(bits);
      case kFloat -> cls.cast(Float.intBitsToFloat((int) bits));
      case kDouble -> cls.cast(Double.longBitsToDouble(bits));
      case kBool -> cls.cast(bits != 0);
      default -> throw new IllegalArgumentException("Unsupported type: " + type);
    };
  }
}
