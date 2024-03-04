/**
 * @file    staticLoaders.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Static data (public folder) loaders
 *
 * @date    28 February 2024, 09:00 (created)
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

import { BlockDescription, CodeExample } from '@/lib/types/codeExamples';
import { promises as fs } from 'fs';
import path from 'path';

/**
 * Load a JSON file from the "public/json" directory. Can only be done server-side.
 */
async function loadPublicJsonFile<T>(fileName: string): Promise<T> {
  // Find the absolute path of the "json" directory
  const jsonDirectory = path.join(process.cwd(), 'public/json');
  // Read the "data.json" file
  const fileContents = await fs.readFile(
    `${jsonDirectory}/${fileName}`,
    'utf8',
  );
  return JSON.parse(fileContents);
}

/**
 * Load the code examples from the "public/json/codeExamples.json" file, parse it and return the data.
 */
export async function loadCodeExamples(): Promise<CodeExample[]> {
  return loadPublicJsonFile('codeExamples.json');
}

/**
 * Load the block descriptions from the "public/json/blockDescriptions.json" file, parse it and return the data.
 */
export async function loadBlockDescriptions(): Promise<
  Record<string, BlockDescription>
> {
  return loadPublicJsonFile('blockDescriptions.json');
}
