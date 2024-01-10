/**
 * @file CacheStatisticsCounter.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@fit.vutbr.cz
 * @brief File contains class that gather statistics from the simulation
 * @date 17 April  2023 16:00 (created) \n
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023  Jakub Horky
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
package com.gradle.superscalarsim.cpu;

import com.gradle.superscalarsim.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @class CacheStatisticsCounter
 * @brief Class that contains data from blocks for displaying statistics from cache about the run
 */
public class CacheStatisticsCounter
{
  /// Counter for committed instructions
  private int accesses;
  /// Counter for how many times was simulate() called
  private int hits;
  /// Counter for how many instructions have failed
  private int misses;
  /// Counter for correctly predicted branching instructions
  private int totalDelay;
  
  ///List of data for line chart with delays, clockXdelay
  private transient List<Pair<Integer, Float>> delayList;
  ///List of data for line chart with delays, clockXhitMissAverage
  private transient List<Pair<Integer, Float>> hitMissList;
  
  /// Value of last 4 accesses: positive means there were more hits than misses
  private int last4accesses;
  
  /**
   * @brief Constructor
   */
  public CacheStatisticsCounter()
  {
    this.resetCounters();
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the counters to the initial values
   */
  public void resetCounters()
  {
    this.accesses      = 0;
    this.hits          = 0;
    this.misses        = 0;
    this.totalDelay    = 0;
    this.last4accesses = 0;
    delayList          = new ArrayList<>();
    hitMissList        = new ArrayList<>();
  }// end of resetCounters
  //----------------------------------------------------------------------
  
  public void incrementAccesses()
  {
    accesses++;
  }
  
  public void decrementAccesses()
  {
    accesses--;
  }
  
  public void incrementHits(int cycle)
  {
    last4accesses = Math.min(last4accesses + 1, 4);
    hits++;
    hitMissList.add(new Pair<>(cycle, (float) last4accesses));
  }
  
  public void decrementHits()
  {
    last4accesses = Math.max(last4accesses - 1, -4);
    hits--;
    if (!hitMissList.isEmpty())
    {
      hitMissList.remove(hitMissList.size() - 1);
    }
  }
  
  public void incrementMisses(int cycle)
  {
    last4accesses = Math.max(last4accesses - 1, -4);
    misses++;
    hitMissList.add(new Pair<>(cycle, (float) last4accesses));
  }
  
  public void decrementMisses()
  {
    last4accesses = Math.min(last4accesses + 1, 4);
    misses--;
    if (!hitMissList.isEmpty())
    {
      hitMissList.remove(hitMissList.size() - 1);
    }
  }
  
  public void incrementTotalDelay(int cycle, int delay)
  {
    delayList.add(new Pair<>(cycle, (float) delay));
    totalDelay += delay;
  }
  
  public long getAccesses()
  {
    return accesses;
  }
  
  public long getHits()
  {
    return hits;
  }
  
  public long getMisses()
  {
    return misses;
  }
  
  public long getTotalDelay()
  {
    return totalDelay;
  }
  
  public float computeAverageDelay()
  {
    if (totalDelay == 0)
    {
      return 0;
    }
    else
    {
      return ((float) totalDelay) / ((float) accesses);
    }
  }
}
