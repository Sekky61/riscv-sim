/**
 * @file    CompileOptions.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Compile options component - compile button, examples
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

import { CompileButton } from '@/app/(other)/compiler/CompileButton';
import { ExamplesButton } from '@/app/(other)/compiler/ExamplesButton';
import { MemorySummary } from '@/app/(other)/compiler/MemorySummary';
import { OptimizeRadio } from '@/app/(other)/compiler/OptimizeRadio';
import { loadCodeExamples } from '@/lib/staticLoaders';
import Link from 'next/link';

/**
 * The compile options component. On the left side to the editor.
 * Contains the compile button, examples and optimization flags.
 * Examples are loaded on the server side.
 */
export async function CompileOptions() {
  const examples = await loadCodeExamples();
  return (
    <div className='flex flex-col items-stretch gap-4'>
      <div>
        <h3 className='mb-2'>Optimization</h3>
        <OptimizeRadio />
      </div>
      <CompileButton />
      <div className='load-example-wrapper'>
        <ExamplesButton examples={examples} />
      </div>
      <MemorySummary />
      <Link className='link' href='/help#code'>
        Tips for writing code
      </Link>
    </div>
  );
}
