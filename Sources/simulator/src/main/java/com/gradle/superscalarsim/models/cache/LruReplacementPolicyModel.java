/**
 * @file LruReplacementPolicyModel.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains LRU replacement policy for cache
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

import com.gradle.superscalarsim.models.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @class LruReplacementPolicyModel
 * @brief Least recently used replacement policy
 */
public class LruReplacementPolicyModel extends ReplacementPolicyModel
{
  
  private final int associativity;
  private final List<Integer>[] lru;
  /// History triplet in format: parsed index, old position in lru
  private final Stack<Pair<Integer, Integer>> history;
  /// id History
  private final Stack<Integer> idHistory;
  
  public LruReplacementPolicyModel(final int numberOfLines, final int associativity)
  {
    this.associativity = associativity;
    lru                = new List[numberOfLines / associativity];
    for (int i = 0; i < numberOfLines / associativity; i++)
    {
      lru[i] = new ArrayList<>();
      for (int j = 0; j < associativity; j++)
      {
        lru[i].add(j);
      }
    }
    history   = new Stack<>();
    idHistory = new Stack<>();
  }
  
  public int getLineToReplace(int id, int index)
  {
    return lru[index].get(0);
  }
  
  /**
   * @brief Update policy with latest access
   */
  public void updatePolicy(int id, int index, int line)
  {
    history.add(new Pair<>(index, lru[index].indexOf(line)));
    idHistory.add(id);
    if (!lru[index].contains(line))
    {
      if (lru[index].size() == associativity)
      {
        lru[index].remove(0);
      }
    }
    else
    {
      lru[index].remove((Integer) line);
    }
    lru[index].add(line);
  }
  
  /**
   * @brief Reverts history if index changed in current id
   */
  public void revertHistory(int id)
  {
    if (!idHistory.isEmpty() && idHistory.peek() == id)
    {
      idHistory.pop();
      Pair<Integer, Integer> lastUpdate = history.pop();
      lru[lastUpdate.getFirst()].add(lastUpdate.getSecond(), lru[lastUpdate.getFirst()].get(associativity - 1));
      lru[lastUpdate.getFirst()].remove(associativity);
    }
  }
  
}