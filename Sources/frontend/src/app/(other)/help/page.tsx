/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The help page of the application
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

import { CodingHelp } from '@/app/(other)/help/CodingHelp';
import { ShortcutsHelp } from '@/app/(other)/help/ShortcutsHelp';
import Link from 'next/link';

/**
 * The help page of the application
 * Shows coding tips and shortcuts
 */
export default function Page() {
  return (
    <div className='flex flex-col gap-10 pb-16'>
      <section>
        <h1 className='text-4xl'>Help</h1>
        <p>How to operate this simulator?!</p>
        <p>Here is the overview of the most important features.</p>
        <ul>
          <li>
            Step through the simulation in the <Link href='/'>simulation</Link>{' '}
            tab. The initial configuration is set for you.
          </li>
          <li>
            Change the cpu parameters in the{' '}
            <Link href='/isa'>configuration</Link> tab.
          </li>
          <li>
            Write your own programs or try other <b>examples</b> the{' '}
            <Link href='/compiler'>Code Editor</Link>.
          </li>
          <li>
            Need to simulate on large data? Look at the{' '}
            <Link href='/memory'>Memory</Link> tab.
          </li>
        </ul>
      </section>
      <section>
        <h2 id='code' className='text-2xl'>
          Tips for writing code
        </h2>
        <CodingHelp />
      </section>
      <section>
        <h2 className='text-2xl'>Shortcuts</h2>
        <ShortcutsHelp />
      </section>
    </div>
  );
}
