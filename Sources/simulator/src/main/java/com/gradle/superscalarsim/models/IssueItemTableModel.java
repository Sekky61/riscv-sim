/**
 * @file    IssueItemTableModel.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @brief File contains container class for displaying issue items in issue window table
 *
 * @date  18 April  2021 16:00 (created) \n
 *        28 April  2021 18:10 (revised)
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
package com.gradle.superscalarsim.models;

import java.util.List;

/**
 * @class IssueItemTableModel
 * @brief Container class for displaying issue items in issue window table
 */
public class IssueItemTableModel
{
  /// Code line of the instruction
  private final String codeLine;
  /// Destination tag (speculative register) of the instruction
  private final String destinationTag;
  /// First source tag of the instruction
  private final String source1Tag;
  /// First source value of the first source tag (register)
  private final double source1Value;
  /// Validity bit for the first source tag
  private final String source1Validity;
  /// Second source tag of the instruction
  private final String source2Tag;
  /// Second source value of the second source tag (register)
  private final double source2Value;
  /// Validity bit for the second source tag
  private final String source2Validity;

  /**
   * @brief Constructor
   * @param [in] simCodeModel       - SimCodeModel to transform into IssueItemTableModel
   * @param [in] issueItemModelList - Issue items for the simCodeModel
   */
  public IssueItemTableModel(SimCodeModel simCodeModel, List<IssueItemModel> issueItemModelList)
  {
    InputCodeArgument destinationArgument = simCodeModel.getArgumentByName("rd");
    IssueItemModel item1 = issueItemModelList.isEmpty() ? null : issueItemModelList.get(0);
    IssueItemModel item2 = issueItemModelList.size() < 2 ? null : issueItemModelList.get(1);

    this.codeLine        = simCodeModel.getRenamedCodeLine();
    this.destinationTag  = destinationArgument != null ? destinationArgument.getValue() : "";

    if(item1 != null)
    {
      this.source1Tag = item1.getTag();
      this.source1Value = item1.getValue();
      this.source1Validity = item1.isValidityBit() ? "YES" : "NO";
    }
    else
    {
      this.source1Tag = "nop";
      this.source1Value = Double.NaN;
      this.source1Validity = "";
    }

    if(item2 != null)
    {
      this.source2Tag = item2.getTag();
      this.source2Value = item2.getValue();
      this.source2Validity = item2.isValidityBit() ? "YES" : "NO";
    }
    else
    {
      this.source2Tag = "nop";
      this.source2Value = Double.NaN;
      this.source2Validity = "";
    }
  }// end of Constructor
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Dummy Constructor
   */
  public IssueItemTableModel()
  {
    this.codeLine        = "";
    this.destinationTag  = "";

    this.source1Tag      = "";
    this.source1Value    = Double.NaN;
    this.source1Validity = "";

    this.source2Tag      = "";
    this.source2Value    = Double.NaN;
    this.source2Validity = "";
  }// end of Dummy Constructor
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get code line
   * @return String value of the code line
   */
  public String getCodeLine()
  {
    return codeLine;
  }// end of getCodeLine
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get destination tag
   * @return String value of the destination tag
   */
  public String getDestinationTag()
  {
    return destinationTag;
  }// end of getDestinationTag
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get first source tag
   * @return First source tag
   */
  public String getSource1Tag()
  {
    return source1Tag;
  }// end of getSource1Tag
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get value of the first source tag
   * @return Double value of the first source tag
   */
  public double getSource1Value()
  {
    return source1Value;
  }// end of getSource1Value
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get validity bit of the first source tag
   * @return Validity bit of the first source tag
   */
  public String getSource1Validity()
  {
    return source1Validity;
  }// end of getSource1Validity
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Check if first source tag is valid
   * @return True if it is valid, false otherwise
   */
  public boolean isSource1Validity()
  {
    return source1Validity.equals("YES") || source1Validity.isEmpty() && source1Tag.equals("nop");
  }// end of isSource1Validity
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get second source tag
   * @return First source tag
   */
  public String getSource2Tag()
  {
    return source2Tag;
  }// end of getSource2Tag
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get value of the second source tag
   * @return Double value of the second source tag
   */
  public double getSource2Value()
  {
    return source2Value;
  }// end of getSource2Value
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Get validity bit of the second source tag
   * @return Validity bit of the second source tag
   */
  public String getSource2Validity()
  {
    return source2Validity;
  }// end of getSource2Validity
  //-----------------------------------------------------------------------------------------

  /**
   * @brief Check if second source tag is valid
   * @return True if it is valid, false otherwise
   */
  public boolean isSource2Validity()
  {
    return source2Validity.equals("YES") || source2Validity.isEmpty() && source2Tag.equals("nop");
  }// end of isSource2Validity
  //-----------------------------------------------------------------------------------------
}
