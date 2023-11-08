/**
 * @file IInstanceManager.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains interface for object managers
 * @date 07 November  2023 15:00 (created)
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

package com.gradle.superscalarsim.managers;

import com.gradle.superscalarsim.models.Identifiable;

import java.util.WeakHashMap;

/**
 * The references are weak, so they will be garbage collected when no other references exist.
 * The purpose of this is to serialize all instances together, in normalized form.
 * The managers should be per Cpu, so that the instances are not shared between Cpus.
 *
 * @param <T> Type of the instances
 *
 * @details The WeakHashMap has weak _keys_, not values. In this case, the values are null (map is used as a set).
 * @brief Interface for managers that hold instances of objects
 */
public interface IInstanceManager<T extends Identifiable>
{
  /**
   * @param instance Instance to start tracking
   *
   * @brief Add instance to the manager
   */
  default void addInstance(T instance)
  {
    getInstances().put(instance, null);
  }
  
  /**
   * @return WeakHashMap of instances
   * @brief Get the instances
   */
  WeakHashMap<T, Object> getInstances();
}
