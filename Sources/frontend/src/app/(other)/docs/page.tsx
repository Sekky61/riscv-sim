/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The docs page
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

import { CodeSnippet } from '@/components/CodeSnippet';
import ExternalLink from '@/components/ExternalLink';

export default function Page() {
  // Source for Calling Convention: https://www.cl.cam.ac.uk/teaching/1617/ECAD+Arch/files/docs/RISCVGreenCardv8-20151013.pdf
  // also: https://riscv.org/wp-content/uploads/2015/01/riscv-calling.pdf

  return (
    <div className='pb-20 grid gap-6'>
      <h1>RISC-V Quick Reference</h1>
      <section>
        <h2>Introduction</h2>
        <p>
          RISC-V is a small, simple, open source Instruction Set Architecture.
          The popularity of RISC-V is growing, especially in the academic world.
          There is the base instruction set and a number of extensions. This
          simulator supports the base (RV32I) and the integer multiplication and
          division (M) extensions.
        </p>
        <p>
          The memory system is <b>little-endian</b>. There are <b>32 integer</b>{' '}
          registers (<code>x0-x31</code>) and <b>32 floating point</b> registers
          (<code>f0-f31</code>). Note that the register <code>x0</code>{' '}
          hard-wired to zero and cannot be changed.
        </p>
        <p>
        Note that only a subset of the RISC-V ISA is implemented in this simulator.
        </p>
      </section>
      <section>
        <h2>Syntax</h2>
        <p>
          RISC-V assembly syntax comprises of a mnemonic followed by operands,
          like in
        </p>
        <CodeSnippet language='c'>add x1, x2, x3</CodeSnippet>
        <p>
          where "add" adds the values of x2 and x3, storing the result in x1.
        </p>
        <p>
          Labels are denoted by a colon ":" after the label name. It marks a
          specific locations in the code. You can use it to jump to the
          location. For example:
        </p>
        <CodeSnippet language='c'>
          {`loop: add x1, x2, x3
      j loop # jump back to the loop`}
        </CodeSnippet>
      </section>
      <section>
        <h2>C Datatypes</h2>
        <p>
          RISC-V supports common C data types. The sizes of the data types are
          as follows:
        </p>
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
        <h2>RISC-V Calling Convention</h2>
        <p>
          The RISC-V calling convention (aka. RVG convention) uses registers to
          pass arguments to functions. The first 8 arguments are passed in
          registers a0-a7. The rest of the arguments are passed on the stack.
        </p>
        <p>
          Registers actually have two names: (1) <b>register name</b> and (2){' '}
          <b>ABI name</b>.
        </p>
        <p>
          The convention distinguishes between caller-saved and callee-saved
          registers, dictating whether the caller or the callee is responsible
          for preserving register values. This is described by the <i>saver</i>{' '}
          attribute in the table below.
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
        <h2>Resources</h2>
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
