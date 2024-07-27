/**
 * @file    codeExamples.d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Typescript types for code examples
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

/**
 * Example code. Describes the JSON structure in /public/json/codeExamples.json which is also served through the API.
 */
export type CodeExample = {
  name: string;
  type: 'c' | 'asm';
  code: string;
  entryPoint?: number | string;
};

/**
 * Description of a block such as fetch, decode, PHT
 */
export type BlockDescription = {
  name: string;
  shortDescription: string;
  longDescription: string;
};

export type BlockDescriptions = Record<string, BlockDescription>;
