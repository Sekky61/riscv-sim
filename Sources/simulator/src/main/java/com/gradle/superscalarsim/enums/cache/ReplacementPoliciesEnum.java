/**
 * @file    ReplacementPoliciesEnum.java
 *
 * @author  Jakub Horky \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xhorky28@stud.fit.vutbr.cz
 *
 * @brief File contains enumeration of cache line replacement policies
 *
 * @date  04 April 2023 13:50 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Jakub Horky
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
package com.gradle.superscalarsim.enums.cache;

/**
 * @brief Types of replacement policies implemented in the cache
 */
public enum ReplacementPoliciesEnum
{
    FIFO, ///< FIFO replacement policy
    LRU, ///< LeastRecentlyUsed replacement policy
    RANDOM ///< Random replacement policy
}
