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
