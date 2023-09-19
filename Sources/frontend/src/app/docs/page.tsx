import ExternalLink from '@/components/ExternalLink';

export default function Page() {
  // Source for Calling Convention: https://www.cl.cam.ac.uk/teaching/1617/ECAD+Arch/files/docs/RISCVGreenCardv8-20151013.pdf
  // also: https://riscv.org/wp-content/uploads/2015/01/riscv-calling.pdf

  return (
    <div className='pb-20'>
      <h1 className='text-2xl'>RISC-V quick reference</h1>
      <section>
        <h2 className='mt-4 text-xl'>The basics</h2>
        <p>RISC-V has a little-endian memory system.</p>
      </section>
      <section>
        <h2 className='mt-4 text-xl'>C Datatypes</h2>
        <ul className='list-disc'>
          <li>
            <code>int</code> is 32 bits
          </li>
          <li>
            <code>long</code> is as wide as a register (32 bits on RV32)
          </li>
          <li>
            <code>float</code> and <code>double</code> are 32 and 64 bits
            respectively, IEEE 754-2008 standard
          </li>
        </ul>
      </section>
      <section id='riscv-calling-convention'>
        <h2 className='mt-4 text-xl'>RISC-V Calling Convention</h2>
        <p>Registers have two names: (1) register name and (2) ABI name.</p>
        <p>
          The <i>saver</i> attribute of a register describes who is responsible
          for keeping the value in the register unchanged. If the <i>caller</i>{' '}
          is responsible, the register can be overwritten by the callee. If the{' '}
          <i>callee</i> is responsible, the register must not be overwritten by
          the callee.
        </p>
        <p>
          The RISC-V calling convention (aka. RVG convention) uses registers to
          pass arguments to functions. The first 8 arguments are passed in
          registers a0-a7.
        </p>
        <table className='mx-auto'>
          <caption>RISC-V Calling Convention</caption>
          <thead>
            <tr>
              <th scope='col'>Register</th>
              <th scope='col'>ABI name</th>
              <th scope='col'>Saver</th>
              <th scope='col'>Description</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope='row'>x0</th>
              <td>zero</td>
              <td>-</td>
              <td>Hard-wired zero</td>
            </tr>
            <tr>
              <th scope='row'>x1</th>
              <td>ra</td>
              <td>Caller</td>
              <td>Return address</td>
            </tr>
            <tr>
              <th scope='row'>x2</th>
              <td>sp</td>
              <td>Callee</td>
              <td>Stack pointer</td>
            </tr>
            <tr>
              <th scope='row'>x3</th>
              <td>gp</td>
              <td>-</td>
              <td>Global pointer</td>
            </tr>
            <tr>
              <th scope='row'>x4</th>
              <td>tp</td>
              <td>-</td>
              <td>Thread pointer</td>
            </tr>
            <tr>
              <th scope='row'>x5-7</th>
              <td>t0-2</td>
              <td>Caller</td>
              <td>Temporaries</td>
            </tr>
            <tr>
              <th scope='row'>x8</th>
              <td>s0/fp</td>
              <td>
                <span>Callee</span>
              </td>
              <td>Saved register/frame pointer</td>
            </tr>
            <tr>
              <th scope='row'>x9</th>
              <td>s1</td>
              <td>Callee</td>
              <td>Saved register</td>
            </tr>
            <tr>
              <th scope='row'>x10-x11</th>
              <td>a0-a1</td>
              <td>Caller</td>
              <td>Function arguments/return values</td>
            </tr>
            <tr>
              <th scope='row'>x12-17</th>
              <td>a2-7</td>
              <td>Caller</td>
              <td>Function arguments</td>
            </tr>
            <tr>
              <th scope='row'>x18-27</th>
              <td>s2-11</td>
              <td>
                <span>Callee</span>
              </td>
              <td>Saved registers</td>
            </tr>
            <tr>
              <th scope='row'>x28-31</th>
              <td>t3-t6</td>
              <td>Caller</td>
              <td>Temporaries</td>
            </tr>
          </tbody>
        </table>

        {/* todo table for floats */}
      </section>
      <section>
        <h2 className='mt-4 text-xl'>Resources</h2>
        <ul className='list-decimal'>
          <li>
            RISC-V specification on the official website:{' '}
            <ExternalLink
              openInNewTab
              href='https://riscv.org/technical/specifications/'
            >
              link
            </ExternalLink>
          </li>
          <li>
            Instruction set cheat sheet:{' '}
            <ExternalLink
              openInNewTab
              href='https://www.cl.cam.ac.uk/teaching/1617/ECAD+Arch/files/docs/RISCVGreenCardv8-20151013.pdf'
            >
              link
            </ExternalLink>
          </li>
        </ul>
      </section>
    </div>
  );
}
