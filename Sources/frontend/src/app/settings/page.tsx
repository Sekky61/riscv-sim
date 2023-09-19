'use client';

import dynamic from 'next/dynamic';

// Loads only on client side
const IsaDynamic = dynamic(
  () => import('../../components/settings/IsaLocalStorageItems'),
  { ssr: false },
);

export default function Page() {
  return (
    <main>
      <section className='w-96'>
        <h2 className='text-xl'>Local storage</h2>
        <IsaDynamic />
      </section>
    </main>
  );
}
