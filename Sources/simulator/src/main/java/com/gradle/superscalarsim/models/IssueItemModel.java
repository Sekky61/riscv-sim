/**
 * @file IssueItemModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains container for item in Issue window
 * @date 3 February   2020 16:00 (created) \n
 * 16 February  2020 16:00 (revised)
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

/**
 * @class IssueItemModel
 * @brief Container for item, which should be build into a list for updating
 *        readiness of values in Issue window for one instruction
 */
public class IssueItemModel {
    /// Register name
    private String tag;
    /// Value inside of the register
    private double value;
    /// If register has correct value
    private boolean validityBit;

    /**
     * @brief Constructor
     * @param [in] tag         - Register name
     * @param [in] validityBit - Is register valid?
     */
    public IssueItemModel(String tag, boolean validityBit) {
        this.tag = tag;
        this.value = 0;
        this.validityBit = validityBit;
    }// end of Constructor
    //------------------------------------------------------

    /**
     * @brief Constructor
     * @param [in] tag         - Register name
     * @param [in] value       - Register value (if any)
     * @param [in] validityBit - Is register valid?
     */
    public IssueItemModel(String tag, double value, boolean validityBit) {
        this.tag = tag;
        this.value = value;
        this.validityBit = validityBit;
    }// end of Constructor
    //------------------------------------------------------

    /**
     * @brief Get item tag (register name)
     * @return String containing a tag
     */
    public String getTag() {
        return tag;
    }// end of getTag
    //------------------------------------------------------

    /**
     * @brief Sets item tag
     * @param [in] tag - New String value of a tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }// end of setTag
    //------------------------------------------------------

    /**
     * @brief Get item value
     * @return Double value
     */
    public double getValue() {
        return value;
    }// end of getValue
    //------------------------------------------------------

    /**
     * @brief Sets item value
     * @param [in] value - New Double value of the item
     */
    public void setValue(double value) {
        this.value = value;
    }// end of setValue
    //------------------------------------------------------

    /**
     * @brief Gets validity bit
     * @return Boolean value of the bit
     */
    public boolean isValidityBit() {
        return validityBit;
    }// end of isValidityBit
    //------------------------------------------------------

    /**
     * @brief Sets validity bit
     * @param [in] validityBit - new value of the validity bit
     */
    public void setValidityBit(boolean validityBit) {
        this.validityBit = validityBit;
    }// end of setValidityBit
    //------------------------------------------------------

    /**
     * @brief String representation for debugging purposes
     */
    @Override
    public String toString() {
        return tag + " " + value + " Valid: " + validityBit;
    }// end of toString
}
