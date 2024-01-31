/**
 * @file    supportedInstructions.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Supported instructions - typescript types
 *
 * @date    19 September 2023, 22:00 (created)
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

import { type InstructionDescription } from '@/lib/types/instructionsDatabase';

let supportedInstructions = {} as Record<string, InstructionDescription>;

async function loadSupportedInstructions() {
  fetch('supportedInstructions.json')
    .then((response) => response.json())
    .then((data) => {
      supportedInstructions = data;
    });
}

loadSupportedInstructions();

export function getInstructionDescription(
  key: string,
): InstructionDescription | undefined {
  return supportedInstructions[key];
}
