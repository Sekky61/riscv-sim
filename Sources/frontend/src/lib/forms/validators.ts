/**
 * @file    validators.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief    Validators for various input json data
 *
 * @date    20 March 2024, 22:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Michal Majer
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
 */

import {
  type CpuConfig,
  type MemoryLocations,
  isaFormSchema,
  memoryLocationsSchema,
} from './Isa';

/**
 * Check if the data is a valid ISA configuration
 */
export function isIsaConfig(data: unknown): data is CpuConfig {
  return isaFormSchema.safeParse(data).success;
}

/**
 * Check if the data is a valid Memory Location collection
 */
export function isMemoryLocations(data: unknown): data is MemoryLocations {
  return memoryLocationsSchema.safeParse(data).success;
}
