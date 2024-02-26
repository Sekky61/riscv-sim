/**
 * @file    route.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Static data for the app (JSON files)
 *
 * @date    31 January 2024, 10:00 (created)
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

import { CodeExample } from '@/lib/types/codeExamples';
import { promises as fs } from 'fs';
import path from 'path';

export async function GET() {
  const json = await loadCodeExamples();
  return Response.json(json);
}

export async function loadCodeExamples(): Promise<CodeExample[]> {
  // Find the absolute path of the "json" directory
  const jsonDirectory = path.join(process.cwd(), 'public/json');
  // Read the "data.json" file
  const fileContents = await fs.readFile(
    `${jsonDirectory}/codeExamples.json`,
    'utf8',
  );

  const json = JSON.parse(fileContents);
  return json;
}

export const dynamic = 'force-static';
