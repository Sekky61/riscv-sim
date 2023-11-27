/**
 * @file    layout.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The main layout of the app
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

'use client';

import { Inter as FontSans } from 'next/font/google';
import { type ReactNode, useRef } from 'react';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';

import '@/styles/globals.css';

import { persistor, store } from '@/lib/redux/store';
import { cn } from '@/lib/utils';

import Notifications from '@/components/Notifications';
import SideBar from '@/components/SideBar';
import ModalRoot from '@/components/modals/ModalRoot';

const fontSans = FontSans({
  subsets: ['latin'],
  variable: '--font-sans',
});

export default function RootLayout({ children }: { children: ReactNode }) {
  const appRef = useRef<HTMLDivElement>(null);

  return (
    <html lang='en'>
      <head>
        <meta
          name='viewport'
          content='width=device-width, initial-scale=1.0, maximum-scale=1.0,user-scalable=0'
        />
        <title>RISC-V Simulator</title>
      </head>
      <body
        className={cn(
          'min-h-screen bg-background font-sans antialiased',
          fontSans.variable,
        )}
      >
        <Provider store={store}>
          <PersistGate loading={null} persistor={persistor}>
            <ModalRoot appRef={appRef} />
            <div className='flex h-screen max-h-screen w-full'>
              <SideBar />
              <div className='relative flex-grow overflow-y-auto' ref={appRef}>
                {children}
              </div>
            </div>
            <Notifications />
          </PersistGate>
        </Provider>
      </body>
    </html>
  );
}
