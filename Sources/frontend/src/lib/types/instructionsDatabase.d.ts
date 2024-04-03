/**
 * @file    instructionsDatabase.d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Typescript types for Simulator API
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

import { InstructionFunctionModel } from '@/lib/types/cpuApi';

// Mirroring the Java simulation types

// Enumeration definition of different types of instruction
export type InstructionType =
  | 'kArithmetic'
  | 'kLoadStore'
  | 'kJumpBranch'
  | 'kLabel';

// "kSpeculative" omitted
export type DataType = 'kInt' | 'kLong' | 'kFloat' | 'kDouble';

export type InstructionDescription = InstructionFunctionModel;

/**
 * Dictionary of all supported instructions
 */
export type SupportedInstructions = {
  [key: string]: InstructionDescription;
};
