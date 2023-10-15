/**
 * @file DataTypeEnum.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @brief File contains enumeration definition for data types
 * @date 27 October  2020 15:00 (created) \n
 * 21 November 2020 19:00 (revised)
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
package com.gradle.superscalarsim.enums;

/**
 * @brief Enumeration definition for different data types used in simulation
 */
public enum DataTypeEnum
{
  /**
   * 32bit signed integer
   */
  kInt,
  
  /**
   * 32bit unsigned integer
   */
  kUInt,
  
  /**
   * 64bit signed integer
   */
  kLong,
  
  /**
   * 64bit unsigned integer
   */
  kULong,
  
  /**
   * 32bit floating point
   */
  kFloat,
  
  /**
   * 64bit floating point
   */
  kDouble,
  
  /**
   * True/False. Used for expressions.
   */
  kBool,
  
  kSpeculative;
  
  public static DataTypeEnum fromJavaClass(Class<?> aClass)
  {
    if (aClass == Integer.class)
    {
      return kInt;
    }
    else if (aClass == Long.class)
    {
      return kLong;
    }
    else if (aClass == Float.class)
    {
      return kFloat;
    }
    else if (aClass == Double.class)
    {
      return kDouble;
    }
    else if (aClass == Boolean.class)
    {
      return kBool;
    }
    else
    {
      return null;
    }
  }
  
  public Class<?> getJavaClass()
  {
    switch (this)
    {
      case kInt:
        return Integer.class;
      case kUInt:
        return Integer.class;
      case kLong:
        return Long.class;
      case kULong:
        return Long.class;
      case kFloat:
        return Float.class;
      case kDouble:
        return Double.class;
      case kBool:
        return Boolean.class;
      case kSpeculative:
        return Long.class;
      default:
        return null;
    }
  }
}
