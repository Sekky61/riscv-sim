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

export default function Page() {
  return (
    <div>
      <section>
        <h1 className='text-2xl'>Help</h1>
        <p>This is the help page.</p>
      </section>
      <section>
        <h2 className='text-xl'>Shortcuts</h2>
        <div className='flex flex-col gap-6'>
          <div>
            <div className='mb-2'>Show simulation tab</div>
            <kbd>1</kbd>
          </div>
          <div>
            <div>Show compiler tab</div>
            <kbd>2</kbd>
          </div>
          <div>
            <div className='mb-2'>Show ISA settings tab</div>
            <kbd>3</kbd>
          </div>
          <div>
            <div>Show statistics tab</div>
            <kbd>4</kbd>
          </div>
        </div>
      </section>
    </div>
  );
}
