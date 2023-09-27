/**
 * @file    OneBitPredictor.java
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
 * @brief File contains class for One-bit predictor
 *
 * @date  1  March   2020 16:00 (created) \n
 *        28 April   2020 12:00 (revised)
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

import java.util.Stack;

/**
 * @class OneBitPredictor
 * @brief Class containing implementation of the One-bit predictor
 */
public class OneBitPredictor implements IBitPredictor
{
  /// Bit array of current prediction
  private final boolean[] state;
  /// Stack of previous history values
  private final Stack<Boolean> history;
  /// Initial state of the predictor
  private final boolean initialState;

  /**
   * @brief Constructor
   * @param [in] isTaken - Initial value of the bit predictor
   */
  public OneBitPredictor(boolean isTaken)
  {
    this.state        = new boolean[] {isTaken};
    this.history      = new Stack<>();
    this.initialState = isTaken;
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Get current prediction saved in predictor
   * @return True if branch should be taken, false otherwise
   */
  @Override
  public boolean getCurrentPrediction()
  {
    return this.state[0];
  }// end of getCurrentPrediction
  //----------------------------------------------------------------------

  /**
   * @brief Ups the probability that branch instruction should be taken
   */
  @Override
  public void upTheProbability()
  {
    savePreviousState();
    this.state[0] = true;
  }// end of upTheProbability
  //----------------------------------------------------------------------

  /**
   * @brief Downs the probability that branch instruction should be taken
   */
  @Override
  public void downTheProbability()
  {
    savePreviousState();
    this.state[0] = false;
  }// end of downTheProbability
  //----------------------------------------------------------------------

  /**
   * @brief Predicts backwards based on saved history
   */
  @Override
  public void predictBackwards()
  {
    this.state[0] = !this.history.isEmpty() ? this.history.pop() : initialState;
  }// end of predictBackwards
  //----------------------------------------------------------------------

  /**
   * @brief Returns the prediction bit vector value as a human readable string
   * @return Human readable prediction bit vector value
   */
  @Override
  public String bitVectorToString()
  {
    return state[0] ? "Taken" : "Not Taken";
  }// end of bitVectorToString
  //----------------------------------------------------------------------

  /**
   * @brief Save history for backward prediction
   */
  private void savePreviousState()
  {
    this.history.push(this.state[0]);
  }// end of savePreviousState
  //----------------------------------------------------------------------
}
