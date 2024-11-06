/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The settings page
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

import Image from 'next/image';
import { DevTools } from './DevTools';
import { SettingsForm } from './SettingsForm';
import FitLogo from '../../../../public/FIT_color_EN.png';

export const metadata = {
  title: 'Settings',
  description: 'The settings page of the application',
};

export default function Page() {
  return (
    <main>
      <h1>Settings</h1>
      <SettingsForm />
      <DevTools />
      <Acknowledgments />
    </main>
  );
}

function Acknowledgments() {
  return (
    <section className='mt-10'>
      <h2>Acknowledgments</h2>
      <p>
        This application is the result of the effort of
        <ul className='m-1'>
          <li>Jakub Horky</li>
          <li>Jiri Jaros</li>
          <li>Michal Majer</li>
          <li>Jan Vavra</li>
        </ul>
        A huge thank you to everyone who contributed along the way.
      </p>
      <Image src={FitLogo} alt='BUT FIT' className='min-w-64 w-80 mt-4' />
    </section>
  );
}
