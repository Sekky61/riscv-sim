/**
 * @file ReplacementPolicyModel.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains replacement policy model builder for cache
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

import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;

/**
 * @class ReplacementPolicyModel
 * @brief Builder class for replacement policy
 */
public abstract class ReplacementPolicyModel
{
  
  /**
   * @brief Builds required replacement policy
   */
  public static ReplacementPolicyModel getReplacementPolicyModel(ReplacementPoliciesEnum replacementPolicy,
                                                                 int numberOfLines,
                                                                 int associativity)
  {
    return switch (replacementPolicy)
    {
      case RANDOM -> new RandomReplacementPolicyModel(numberOfLines, associativity);
      case LRU -> new LruReplacementPolicyModel(numberOfLines, associativity);
      case FIFO -> new FifoReplacementPolicyModel(numberOfLines, associativity);
    };
  }
  
  public abstract int getLineToReplace(int index);
  
  public abstract void updatePolicy(int index, int line);
}

