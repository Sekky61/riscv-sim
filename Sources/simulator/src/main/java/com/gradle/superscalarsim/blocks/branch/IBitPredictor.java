/**
 * @file    IBitPredictor.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @brief File contains interface for bit predictors
 *
 * @date  1  March   2021 16:00 (created) \n
 *        28 April   2021 12:00 (revised)
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

/**
 * @class IBitPredictor
 * @brief Interface for all bit predictors
 */
public interface IBitPredictor
{
  /**
   * @brief Get current prediction saved in predictor
   * @return True if branch should be taken, false otherwise
   */
  boolean getCurrentPrediction();

  /**
   * @brief Ups the probability that branch instruction should be taken
   */
  void upTheProbability();

  /**
   * @brief Downs the probability that branch instruction should be taken
   */
  void downTheProbability();

  /**
   * @brief Predicts backwards based on saved history
   */
  void predictBackwards();

  /**
   * @brief Returns the prediction bit vector value as a human readable string
   * @return Human readable prediction bit vector value
   */
  String bitVectorToString();
}
