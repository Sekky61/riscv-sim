/**
 * @file Fclass.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains floating point classification code
 * @date 18 Oct      2023 20:00 (created)
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
package com.gradle.superscalarsim.code;

/**
 * @class Fclass
 * @brief Class containing logic for classifying floating point numbers
 */
public class Fclass
{
  /**
   * Table (bit positions are numbered from 0, starting at the least significant bit):
   * <pre>
   * 0 - rs1 is -infinity
   * 1 - rs1 is a negative normal number
   * 2 - rs1 is a negative subnormal number - zeroes are excluded
   * 3 - rs1 is −0.
   * 4 - rs1 is +0.
   * 5 - rs1 is a positive subnormal number.
   * 6 - rs1 is a positive normal number.
   * 7 - rs1 is +infinity
   * 8 - rs1 is a signaling NaN.
   * 9 - rs1 is a quiet NaN.
   * </pre>
   * <p>
   * Implementing this in the stack machine is not that easy, so it is implemented as a Java function.
   *
   * @param value Value to be classified
   *
   * @return Integer value based on the Classify table
   */
  public static int classify(float value)
  {
    int     bits        = Float.floatToIntBits(value);
    boolean isInfinity  = Float.isInfinite(value);
    boolean isNeg       = (bits & 0x80000000) != 0;
    boolean isNan       = Float.isNaN(value);
    boolean isZero      = (bits & 0x7FFFFFFF) == 0;
    boolean isSubnormal = (bits & 0x7F800000) == 0 && value != 0.0f;
    
    boolean isNegInfinity = isInfinity && isNeg;
    
    int flags = 0;
    flags |= isNegInfinity ? 0x1 : 0;
    flags |= isNeg && !isInfinity && !isSubnormal ? 0x2 : 0;
    flags |= isNeg && !isInfinity && !isZero && isSubnormal ? 0x4 : 0;
    flags |= isNeg && isZero ? 0x8 : 0;
    flags |= !isNeg && isZero ? 0x10 : 0;
    flags |= !isNeg && !isZero && isSubnormal ? 0x20 : 0;
    flags |= !isNeg && !isZero && !isSubnormal ? 0x40 : 0;
    flags |= !isNeg && isInfinity ? 0x80 : 0;
    flags |= isNan && (bits & 0x00400000) != 0 ? 0x100 : 0; // TODO: signaling and quiet NaNs
    flags |= isNan && (bits & 0x00400000) == 0 ? 0x200 : 0;
    return flags;
  }
}
