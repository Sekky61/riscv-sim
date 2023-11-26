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
  kByte, kShort,
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
  kBool;
  
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
    return switch (this)
    {
      case kByte -> Byte.class;
      case kShort -> Short.class;
      case kInt, kUInt -> Integer.class;
      case kLong, kULong -> Long.class;
      case kFloat -> Float.class;
      case kDouble -> Double.class;
      case kBool -> Boolean.class;
    };
  }
  
  /**
   * Relevant for loads that fill upper bits with sign bit.
   *
   * @return True if the data type is signed and upper bits should be filled with sign bit.
   */
  public boolean isSigned()
  {
    return this == kInt || this == kLong;
  }
  
  /**
   * @return The bytes of the data type in little endian from the string representation
   */
  public byte[] getBytes(String value)
  {
    int    radix    = 10;
    String cutValue = value;
    if (value.startsWith("0x") || value.startsWith("0X"))
    {
      radix    = 16;
      cutValue = value.substring(2);
    }
    else if (value.startsWith("0b") || value.startsWith("0B"))
    {
      radix    = 2;
      cutValue = value.substring(2);
    }
    else if (value.startsWith("0"))
    {
      radix = 8;
      //      cutValue = value.substring(1);
    }
    byte[] bytes = new byte[getSize()];
    switch (this)
    {
      case kBool, kByte ->
      {
        // Java does not have unsigned byte, so we need to parse it manually
        try
        {
          bytes[0] = Byte.parseByte(cutValue, radix);
        }
        catch (NumberFormatException e)
        {
          bytes[0] = (byte) (Integer.parseInt(cutValue, radix) & 0xFF);
        }
      }
      case kShort ->
      {
        short shortValue = Short.decode(value);
        bytes[0] = (byte) (shortValue & 0xFF);
        bytes[1] = (byte) ((shortValue >> 8) & 0xFF);
      }
      case kInt, kUInt ->
      {
        int intValue = 0;
        try
        {
          intValue = Integer.decode(value);
        }
        catch (NumberFormatException e)
        {
          intValue = Integer.parseUnsignedInt(cutValue, radix);
        }
        bytes[0] = (byte) (intValue & 0xFF);
        bytes[1] = (byte) ((intValue >> 8) & 0xFF);
        bytes[2] = (byte) ((intValue >> 16) & 0xFF);
        bytes[3] = (byte) ((intValue >> 24) & 0xFF);
      }
      case kLong, kULong ->
      {
        long longValue = 0;
        try
        {
          longValue = Long.decode(value);
        }
        catch (NumberFormatException e)
        {
          longValue = Long.parseUnsignedLong(cutValue, radix);
        }
        bytes[0] = (byte) (longValue & 0xFF);
        bytes[1] = (byte) ((longValue >> 8) & 0xFF);
        bytes[2] = (byte) ((longValue >> 16) & 0xFF);
        bytes[3] = (byte) ((longValue >> 24) & 0xFF);
        bytes[4] = (byte) ((longValue >> 32) & 0xFF);
        bytes[5] = (byte) ((longValue >> 40) & 0xFF);
        bytes[6] = (byte) ((longValue >> 48) & 0xFF);
        bytes[7] = (byte) ((longValue >> 56) & 0xFF);
      }
      case kFloat ->
      {
        int intValue = Float.floatToIntBits(Float.parseFloat(value));
        bytes[0] = (byte) (intValue & 0xFF);
        bytes[1] = (byte) ((intValue >> 8) & 0xFF);
        bytes[2] = (byte) ((intValue >> 16) & 0xFF);
        bytes[3] = (byte) ((intValue >> 24) & 0xFF);
      }
      case kDouble ->
      {
        long longValue = Double.doubleToLongBits(Double.parseDouble(value));
        bytes[0] = (byte) (longValue & 0xFF);
        bytes[1] = (byte) ((longValue >> 8) & 0xFF);
        bytes[2] = (byte) ((longValue >> 16) & 0xFF);
        bytes[3] = (byte) ((longValue >> 24) & 0xFF);
        bytes[4] = (byte) ((longValue >> 32) & 0xFF);
        bytes[5] = (byte) ((longValue >> 40) & 0xFF);
        bytes[6] = (byte) ((longValue >> 48) & 0xFF);
        bytes[7] = (byte) ((longValue >> 56) & 0xFF);
      }
    }
    return bytes;
  }
  
  /**
   * @return The size of the data type in bytes
   */
  public int getSize()
  {
    return switch (this)
    {
      case kBool, kByte -> 1;
      case kShort -> 2;
      case kInt, kUInt, kFloat -> 4;
      case kLong, kULong, kDouble -> 8;
    };
  }
}
