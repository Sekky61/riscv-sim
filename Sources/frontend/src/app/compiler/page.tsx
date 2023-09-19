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
        <title>Disassembly demo</title>
      </Head>
      <div className='flex h-full flex-col'>
        <h1 className='m-2 mb-6 text-2xl'>Code editor</h1>
        <div className=' grid min-h-0 flex-grow grid-cols-[200px_2fr_minmax(350px,1fr)] gap-4'>
          <CompileOptions />
          <CCodeInput />
          <AsmDisplay />
        </div>
      </div>
      <CompilerShortcuts />
    </main>
  );
}
