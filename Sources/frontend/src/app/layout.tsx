'use client';

import type { ReactNode } from 'react';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';

import '@/styles/globals.css';

import { persistor, store } from '@/lib/redux/store';

import ModalRoot from '@/components/modals/ModalRoot';
import Notifications from '@/components/Notifications';
import SideBar from '@/components/SideBar';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html>
      <head>
        <meta
          name='viewport'
          content='width=device-width, initial-scale=1.0, maximum-scale=1.0,user-scalable=0'
        />
        <title>RISC-V Simulator</title>
      </head>
      <body>
        <Provider store={store}>
          <PersistGate loading={null} persistor={persistor}>
            <ModalRoot />
            <div className='flex h-screen max-h-screen w-full'>
              <SideBar />
              <div className='relative flex-grow overflow-y-auto'>
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
