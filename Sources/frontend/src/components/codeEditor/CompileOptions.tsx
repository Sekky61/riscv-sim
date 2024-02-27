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

import { ExamplesButton } from '@/components/codeEditor/ExamplesButton';
import { OptimizeRadio } from '@/components/codeEditor/OptimizeRadio';
import { CompileButton } from '@/components/codeEditor/CompileButton';
import { loadCodeExamples } from '@/app/api/codeExamples/route';
import Link from 'next/link';

/**
 * The compile options component. On the left side to the editor.
 * Contains the compile button, examples and optimization flags.
 * Examples are loaded on the server side.
 */
export default async function CompileOptions() {
  const examples = await loadCodeExamples();
  return (
    <div className='flex flex-col items-stretch gap-4'>
      <div>
        <h3 className='mb-2'>Optimization</h3>
        <OptimizeRadio />
      </div>
      <CompileButton />
      <ExamplesButton examples={examples} />
      <Link className='link' href='/help#code'>
        Tips for writing code
      </Link>
    </div>
  );
}
