/**
 * @file    PatternHistoryTable.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class of Pattern history table
 *
 * @date  1  March   2020 16:00 (created) \n
 *        28 April   2020 12:15 (revised)
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.blocks.branch;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @class PatternHistoryTable
 * @brief Class contains Pattern history table, which holds separate bit predictors
 */
public class PatternHistoryTable
{
  /// List of predictors
  private final Map<Integer, IBitPredictor> predictorMap;
  /// Size of the PHT
  private int size;
  /// Default state of the predictors
  private boolean[] defaultTaken;
  /// Reallocated flag, which is set on init/reset. It is used to draw GUI.
  private boolean reallocated;

  // enum for predictor types with values
    public enum PredictorType {
        ZERO_BIT_PREDICTOR(0),
        ONE_BIT_PREDICTOR(1),
        TWO_BIT_PREDICTOR(2);

        private final int value;

        private PredictorType(int value) {
          this.value = value;
        }
    }

  public void setDefaultTaken(boolean[] defaultTaken) {
    this.defaultTaken = defaultTaken;
  }

  public void setDefaultPredictorClass(PredictorType defaultPredictorClass) {
    this.defaultPredictorClass = defaultPredictorClass;
  }

  /// Default type of the predictor
  PredictorType defaultPredictorClass;

  /**
   * @brief Constructor
   * @param [in] size - Size of the PHT
   */
  public PatternHistoryTable(int size)
  {
    // Use tree map to keep the order of the predictors for displaying in GUI
    this.predictorMap = new TreeMap<>();
    this.reallocated = true;
    this.size = size;
    this.defaultTaken = new boolean[] {true, false};
    this.defaultPredictorClass = PredictorType.TWO_BIT_PREDICTOR;
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Resets the list of  bit vectors and sets the reallocated flag
   */
  public void reset()
  {
    this.reallocated = true;
    this.predictorMap.clear();
  }// end of reset
  //----------------------------------------------------------------------

  /**
   * @brief Get the reallocated flag
   * @return Boolean value of the reallocated flag
   */
  public boolean isReallocated()
  {
    return reallocated;
  }// end of isReallocated
  //----------------------------------------------------------------------

  /**
   * @brief Get the whole list of the bit predictors allocated in the PHT
   * @return List of the bit predictors
   */
  public Map<Integer, IBitPredictor> getPredictorMap()
  {
    return predictorMap;
  }// end of getHistoryList
  //----------------------------------------------------------------------

  /**
   * @brief Initiate PHT with Zero-bit predictors
   * @param [in] size    - Size of the PHT
   * @param [in] isTaken - Initial state of the Bit predictor
   */
  public void initiateZeroBitPredictors(int size, boolean isTaken)
  {
    this.reallocated  = true;
    this.size         = size;
    this.defaultTaken = new boolean[] {isTaken};
    this.defaultPredictorClass = PredictorType.ZERO_BIT_PREDICTOR;
    this.predictorMap.clear();
  }// end of initiateZeroBitPredictors
  //----------------------------------------------------------------------

  /**
   * @brief Initiate PHT with One-bit predictors
   * @param [in] size    - Size of the PHT
   * @param [in] isTaken - Initial state of the Bit predictor
   */
  public void initiateOneBitPredictor(int size, boolean isTaken)
  {
    this.reallocated  = true;
    this.size         = size;
    this.defaultTaken = new boolean[] {isTaken};
    this.defaultPredictorClass = PredictorType.ONE_BIT_PREDICTOR;
    this.predictorMap.clear();
  }// end of initiateOneBitPredictor
  //----------------------------------------------------------------------

  /**
   * @brief Initiate PHT with Two-bit predictors
   * @param [in] size    - Size of the PHT
   * @param [in] isTaken - Initial state of the Bit predictor
   */
  public void initiateTwoBitPredictor(int size, boolean[] initialState)
  {
    this.reallocated  = true;
    this.size         = size;
    this.defaultTaken = initialState;
    this.defaultPredictorClass = PredictorType.TWO_BIT_PREDICTOR;
    this.predictorMap.clear();
  }// end of initiateTwoBitPredictor
  //----------------------------------------------------------------------

  /**
   * @brief Get predictor on specified index
   * @param [in] index - Index of the predictor
   * @return Bit predictor
   */
  public IBitPredictor getPredictor(int index)
  {
    this.reallocated = false;
    boolean hasPredictor = this.predictorMap.containsKey(index % size);
    if(!hasPredictor) {
      // Insert default predictor
        IBitPredictor bitPredictor;
        if(this.defaultPredictorClass == PredictorType.ZERO_BIT_PREDICTOR)
        {
            bitPredictor = new ZeroBitPredictor(defaultTaken[0]);
        }
        else if (this.defaultPredictorClass == PredictorType.ONE_BIT_PREDICTOR)
        {
            bitPredictor = new OneBitPredictor(defaultTaken[0]);
        }
        else
        {
            bitPredictor = new TwoBitPredictor(defaultTaken);
        }
        this.predictorMap.put(index % size, bitPredictor);
    }

    return this.predictorMap.get(index % size);
  }// end of getPredictor
  //----------------------------------------------------------------------

  /**
   * @brief Get the value of default bit vector value
   * @return Default value of the bit vector
   */
  public boolean[] getDefaultTaken()
  {
    return defaultTaken;
  }// end of getDefaultTaken
  //----------------------------------------------------------------------

  /**
   * @brief Sets predictor value
   * @param [in] index - Index of the predictor
   */
  public void setDefault(int index)
  {
    this.reallocated = true;
    this.predictorMap.remove(index % size);
  }// end of setDefault
  //----------------------------------------------------------------------
}

