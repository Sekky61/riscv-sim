/**
 * @file TwoBitPredictor.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Two-bit predictor
 * @date 1  March   2021 16:00 (created) \n
 * 28 April   2021 12:00 (revised)
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

/**
 * @class OneBitPredictor
 * @brief Class containing implementation of the Two-bit predictor
 */
public class TwoBitPredictor implements IBitPredictor
{
  /// Bit array of current prediction
  private final boolean[] state;
  /// Initial state of the predictor
  private final boolean[] initialState;
  
  /**
   * @param [in] initialState - Initial bit array of the bit predictor
   *
   * @brief Constructor
   */
  public TwoBitPredictor(boolean[] initialState)
  {
    this.state        = new boolean[]{initialState[0], initialState[1]};
    this.initialState = new boolean[]{initialState[0], initialState[1]};
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return True if branch should be taken, false otherwise
   * @brief Get current prediction saved in predictor
   */
  @Override
  public boolean getCurrentPrediction()
  {
    return this.state[1];
  }// end of getCurrentPrediction
  //----------------------------------------------------------------------
  
  /**
   * @brief Ups the probability that branch instruction should be taken
   */
  @Override
  public void upTheProbability()
  {
    if (!this.state[0])
    {
      this.state[0] = true;
    }
    else
    {
      this.state[1] = true;
    }
  }// end of upTheProbability
  //----------------------------------------------------------------------
  
  /**
   * @brief Downs the probability that branch instruction should be taken
   */
  @Override
  public void downTheProbability()
  {
    if (this.state[0])
    {
      this.state[0] = false;
    }
    else
    {
      this.state[1] = false;
    }
  }// end of downTheProbability
  //----------------------------------------------------------------------
  
  /**
   * @return Human readable prediction bit vector value
   * @brief Returns the prediction bit vector value as a human readable string
   */
  @Override
  public String bitVectorToString()
  {
    String prefix = this.state[0] ? "Strongly" : "Weakly";
    if (!this.state[1])
    {
      prefix = this.state[0] ? "Weakly" : "Strongly";
    }
    String suffix = this.state[1] ? "Taken" : "Not Taken";
    return prefix + " " + suffix;
  }// end of bitVectorToString
  //----------------------------------------------------------------------
}
