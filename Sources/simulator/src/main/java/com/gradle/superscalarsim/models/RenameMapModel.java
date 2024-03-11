/**
 * @file RenameMapModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container for holding mapped register with its order
 * @date 25 April     2021 15:00 (created) \n
 * 28 April     2021 19:30 (revised)
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
package com.gradle.superscalarsim.models;

import com.gradle.superscalarsim.models.register.RegisterModel;
import org.jetbrains.annotations.NotNull;

/**
 * @class RenameMapModel
 * @brief Container for holding mapped (architectural) register with its mapping order
 */
public class RenameMapModel implements Comparable<RenameMapModel>
{
  /**
   * Name of the architectural register
   */
  private final RegisterModel architecturalRegister;
  
  /**
   * Order id, specifying order between multiple same mapping
   * Grows with time, so the newest mapping has the highest order id.
   */
  private final int order;
  
  /**
   * @param registerName Name of the architectural register
   * @param order        Order id, specifying order between multiple same mapping
   *
   * @brief Constructor
   */
  public RenameMapModel(RegisterModel register, int order)
  {
    this.architecturalRegister = register;
    this.order                 = order;
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param codeModel Model to be compared to
   *
   * @return -1 if >, 0 if ==, < if 1
   * @brief Comparator function for comparing mappings with same architectural register
   */
  @Override
  public int compareTo(@NotNull RenameMapModel codeModel)
  {
    return -Integer.compare(codeModel.order, this.order);
  }// end of compareTo
  //------------------------------------------------------
  
  /**
   * @return Name of the mapped architectural register
   * @brief Get name of the mapped architectural register
   */
  public RegisterModel getArchitecturalRegister()
  {
    return architecturalRegister;
  }// end of getArchitecturalRegister
  //------------------------------------------------------
  
  public int getOrder()
  {
    return order;
  }
}
