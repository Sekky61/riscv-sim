/**
 * @file    staticInstructionLoad.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Functionality to load asset from sim API on the server side
 *
 * @date    09 February 2024, 15:00 (created)
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

import { callInstructionDescriptionImpl } from '@/lib/serverCalls';

/**
 * This function is called from getStaticProps of each page.
 * The data loaded is the instruction set definition.
 */
export async function staticInstructionLoad() {
  const response = await callInstructionDescriptionImpl();
  return {
    models: response.models,
  };
}
