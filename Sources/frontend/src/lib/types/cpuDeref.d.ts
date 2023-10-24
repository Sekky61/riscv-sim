/**
 * @file    cpuDeref.d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Types for cpu state after dereferencing
 *
 * @date    24 October 2023, 12:00 (created)
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
  InputCodeArgument,
  InputCodeModel,
  InstructionFetchBlockRef,
  SimCodeModelRef,
} from '@/lib/types/cpuApi';

export interface InstructionFetchBlock extends InstructionFetchBlockRef {
  fetchedCode: Array<SimCodeModel>;
}

export interface SimCodeModel extends SimCodeModelRef {
  inputCodeModel: InputCodeModel;
  renamedArguments: Array<InputCodeArgument>;
}
