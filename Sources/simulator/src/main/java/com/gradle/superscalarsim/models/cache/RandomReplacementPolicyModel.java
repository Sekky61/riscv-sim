/**
 * @file RandomReplacementPolicyModel.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains random replacement policy for cache
 * @date 04 April 2023 14:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Jakub Horky
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
package com.gradle.superscalarsim.models.cache;

import java.util.Random;

/**
 * @class RandomReplacementPolicyModel
 * @brief RANDOM replacement policy. Deterministic if the sequence of calls is the same.
 */
public class RandomReplacementPolicyModel extends ReplacementPolicyModel
{
  private final Random randomSource;
  
  private final int associativity;
  
  public RandomReplacementPolicyModel(final int numberOfLines, final int associativity)
  {
    this.associativity = associativity;
    this.randomSource  = new Random(1337);
  }
  
  public int getLineToReplace(int index)
  {
    // Generate number <0; associativity - 1>
    return randomSource.nextInt(associativity);
  }
  
  public void updatePolicy(int index, int line)
  {
    // no-op
  }
}