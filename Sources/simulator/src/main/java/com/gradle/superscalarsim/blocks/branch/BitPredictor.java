/**
 * @file BitPredictor.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Bit predictor
 * @date 1  March   2021 16:00 (created) \n
 * 20 February   2024 13:00 (revised)
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

/**
 * @class BitPredictor
 * @brief Bit predictor with a state of n bits.
 * @details Basically a saturating counter.
 */
public class BitPredictor
{
  public static final int TAKEN = 1;
  public static final int NOT_TAKEN = 0;
  public static final int STRONGLY_TAKEN = 3;
  public static final int WEAKLY_TAKEN = 2;
  public static final int WEAKLY_NOT_TAKEN = 1;
  public static final int STRONGLY_NOT_TAKEN = 0;
  /**
   * State of the predictor.
   * The number is viewed as a bit vector.
   * The decision is the highest order bit.
   * The state 0 always means not taken.
   */
  private int state;
  /**
   * Size of the state in bits (for example 1 for 1-bit predictor).
   */
  private int bitWidth;
  
  /**
   * Copy constructor
   */
  public BitPredictor(BitPredictor bitPredictor)
  {
    this.bitWidth = bitPredictor.bitWidth;
    this.state    = bitPredictor.state;
  }
  
  /**
   * @param config The configuration
   *
   * @brief Constructor via predictor type
   */
  public BitPredictor(PredictorType predictorType, int initialState)
  {
    this.bitWidth = predictorType.getWidth();
    this.state    = initialState;
    assert predictorType.isValidState(initialState);
  }
  
  /**
   * @return True if branch should be taken, false otherwise
   * @brief Get prediction based on the current state
   */
  public boolean getCurrentPrediction()
  {
    // The decision is the highest order bit
    int decisionBit = bitWidth == 0 ? 0 : bitWidth - 1;
    int mask        = 1 << decisionBit;
    return (state & mask) != 0;
  }
  
  /**
   * @brief Adjusts the state of the predictor based on the actual outcome
   */
  public void sendFeedback(boolean outcome)
  {
    if (outcome)
    {
      upTheProbability();
    }
    else
    {
      downTheProbability();
    }
  }
  
  /**
   * @brief Ups the probability that branch instruction should be taken
   */
  void upTheProbability()
  {
    int maxState = (1 << bitWidth) - 1;
    if (state < maxState)
    {
      state++;
    }
  }
  
  /**
   * @brief Downs the probability that branch instruction should be taken
   */
  void downTheProbability()
  {
    if (bitWidth == 0)
    {
      return;
    }
    if (state > 0)
    {
      state--;
    }
  }
  
  /**
   * Enum for predictor types with values
   * Gets serialized as
   * <p>
   * {@code
   * {
   * "value": 1,
   * "name": "ONE_BIT_PREDICTOR"
   * }
   * }
   */
  public enum PredictorType
  {
    ZERO_BIT_PREDICTOR, ONE_BIT_PREDICTOR, TWO_BIT_PREDICTOR;
    
    int getWidth()
    {
      return switch (this)
      {
        case ZERO_BIT_PREDICTOR -> 0;
        case ONE_BIT_PREDICTOR -> 1;
        case TWO_BIT_PREDICTOR -> 2;
      };
    }
    
    public boolean isValidState(int state)
    {
      return switch (this)
      {
        case ZERO_BIT_PREDICTOR, ONE_BIT_PREDICTOR -> state == 0 || state == 1;
        case TWO_BIT_PREDICTOR -> state == 0 || state == 1 || state == 2 || state == 3;
      };
    }
  }
}
