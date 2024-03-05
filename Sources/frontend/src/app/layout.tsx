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

import { Inter as FontSans } from 'next/font/google';
import { type ReactNode } from 'react';

import '@/styles/globals.css';

import { cn } from '@/lib/utils';

import { BlockDescriptionProvider } from '@/components/BlockDescriptionContext';
import { ThemeProvider } from '@/components/ThemeProvider';
import { Toaster } from '@/components/base/ui/sonner';
import { TooltipProvider } from '@/components/base/ui/tooltip';
import PersistedStoreProvider from '@/lib/redux/PersistedStoreProvider';
import { loadBlockDescriptions } from '@/lib/staticLoaders';

/**
 * Font loading by next.js.
 * The display: swap is important for loading, but it is badly documented. It worked for a long time without it.
 */
const fontSans = FontSans({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
});

/**
 * This is the root layout of the app. It provides the state (redux), toast notifications and
 * the HTML head with viewport meta tag and title.
 *
 * Other layout are nested inside this layout.
 */
export default async function RootLayout({
  children,
}: { children: ReactNode }) {
  // Body is overflow-hidden to prevent FU configuration from expanding the page
  const descriptions = await loadBlockDescriptions();

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
          'min-h-screen font-sans antialiased overflow-hidden',
          fontSans.variable,
        )}
      >
        <ThemeProvider
          attribute='class'
          defaultTheme='system'
          enableSystem
          disableTransitionOnChange
        >
          <BlockDescriptionProvider descriptions={descriptions}>
            <PersistedStoreProvider>
              <TooltipProvider delayDuration={700} skipDelayDuration={0}>
                <div className='flex h-screen max-h-screen'>{children}</div>
                <Toaster position='top-right' />
              </TooltipProvider>
            </PersistedStoreProvider>
          </BlockDescriptionProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
