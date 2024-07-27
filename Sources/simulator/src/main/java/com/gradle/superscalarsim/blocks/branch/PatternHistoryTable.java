/**
 * @file PatternHistoryTable.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of Pattern history table
 * @date 1  March   2020 16:00 (created) \n
 * 28 April   2020 12:15 (revised)
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
package com.gradle.superscalarsim.blocks.branch;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.Map;
import java.util.TreeMap;

/**
 * @class PatternHistoryTable
 * @brief Class contains Pattern history table, which holds separate bit predictors
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class PatternHistoryTable
{
  /**
   * Collection of predictors
   * This collection is sparse, new predictors are added only when needed
   */
  private final Map<Integer, BitPredictor> predictorMap;
  
  /**
   * Size of the PHT
   */
  private final int size;
  
  /**
   * Type and initial state of all predictors in the PHT
   */
  BitPredictor defaultPredictor;
  
  /**
   * @param size                  Size of the PHT
   * @param defaultState          Default state of the predictors, used for creating new predictors
   * @param defaultPredictorClass Default type of the predictor, used for creating new predictors
   *
   * @brief Constructor
   */
  public PatternHistoryTable(int size, BitPredictor defaultPredictor)
  {
    // Use tree map to keep the order of the predictors for displaying in GUI
    // todo: type?
    this.predictorMap     = new TreeMap<>();
    this.size             = size;
    this.defaultPredictor = defaultPredictor;
  }// end of Constructor
  
  /**
   * @param index Index of the predictor
   *
   * @return Bit predictor
   * @brief Get predictor on specified index
   */
  @JsonIgnore
  public BitPredictor getPredictor(int index)
  {
    BitPredictor predictor    = this.predictorMap.get(index % size);
    boolean      hasPredictor = predictor != null;
    if (!hasPredictor)
    {
      // Insert default predictor
      predictor = new BitPredictor(this.defaultPredictor);
      this.predictorMap.put(index % size, predictor);
    }
    return predictor;
  }// end of getPredictor
  //----------------------------------------------------------------------
}

