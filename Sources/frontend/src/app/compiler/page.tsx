/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology 

 *          Brno University of Technology 

 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The compiler page
 *
 * @date    19 September 2023, 22:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023  Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use client';

import Head from 'next/head';

import AsmDisplay from '@/components/codeEditor/AsmDisplay';
import CCodeInput from '@/components/codeEditor/CCodeInput';
import CompileOptions from '@/components/codeEditor/CompileOptions';
import CompilerShortcuts from '@/components/shortcuts/CompilerShortcuts';

export default function HomePage() {
  // Note: min-h-0 fixes overflow of the flex container
  return (
    <main className='h-full'>
      <Head>
        <title>Code Editor</title>
      </Head>
      <div className='flex h-full flex-col'>
        <h1 className='m-2 mb-6 text-2xl'>Code Editor</h1>
        <div className=' grid min-h-0 flex-grow grid-cols-[160px_2fr_minmax(350px,1fr)] gap-4'>
          <CompileOptions />
          <CCodeInput />
          <AsmDisplay />
        </div>
      </div>
      <CompilerShortcuts />
    </main>
  );
}
