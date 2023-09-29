/**
 * @file ZeroBitPredictor.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for Zero-bit predictor
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
 * @class ZeroBitPredictor
 * @brief Class containing implementation of the Zero-bit predictor
 */
public class ZeroBitPredictor implements IBitPredictor
{
  /// Bit array of current prediction
  private final boolean[] state;
  
  /**
   * @param [in] isTaken - Initial value of the bit predictor
   *
   * @brief Constructor
   */
  public ZeroBitPredictor(boolean isTaken)
  {
    this.state = new boolean[]{isTaken};
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @return True if branch should be taken, false otherwise
   * @brief Get current prediction saved in predictor
   */
  @Override
  public boolean getCurrentPrediction()
  {
    return state[0];
  }// end of getCurrentPrediction
  //----------------------------------------------------------------------
  
  /**
   * @brief Ups the probability that branch instruction should be taken
   */
  @Override
  public void upTheProbability()
  {
    //do nothing
  }// end of upTheProbability
  //----------------------------------------------------------------------
  
  /**
   * @brief Downs the probability that branch instruction should be taken
   */
  @Override
  public void downTheProbability()
  {
    //do nothing
  }// end of downTheProbability
  //----------------------------------------------------------------------
  
  /**
   * @brief Predicts backwards based on saved history
   */
  @Override
  public void predictBackwards()
  {
    //do nothing
  }// end of predictBackwards
  //----------------------------------------------------------------------
  
  /**
   * @return Human readable prediction bit vector value
   * @brief Returns the prediction bit vector value as a human readable string
   */
  @Override
  public String bitVectorToString()
  {
    return state[0] ? "Taken" : "Not Taken";
  }// end of bitVectorToString
  //----------------------------------------------------------------------
}
