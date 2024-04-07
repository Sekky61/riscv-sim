/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology 

 *          Brno University of Technology 

 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The compiler page root
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

import Head from 'next/head';

import { AsmDisplay } from '@/app/(other)/compiler/AsmDisplay';
import { CCodeInput } from '@/app/(other)/compiler/CCodeInput';
import { CompileOptions } from '@/app/(other)/compiler/CompileOptions';
import { LineHighlightProvider } from '@/app/(other)/compiler/LineHighlightContext';

export default function Page() {
  // Note: min-h-0 fixes overflow of the flex container
  return (
    <>
      <Head>
        <title>Code Editor</title>
      </Head>
      <h1>Code Editor</h1>
      <div className='flex-grow flex h-full flex-col'>
        <div
          id='editor'
          className='editor-container grid min-h-0 h-full flex-grow grid-cols-[160px_minmax(230px,5fr)_minmax(230px,3fr)] gap-4'
        >
          <CompileOptions />
          <LineHighlightProvider>
            <CCodeInput />
            <AsmDisplay />
          </LineHighlightProvider>
        </div>
      </div>
    </>
  );
}
