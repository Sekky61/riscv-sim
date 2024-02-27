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

import { CodeSnippet } from '@/components/CodeSnippet';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/base/ui/accordion';

export default function Page() {
  return (
    <div className='grid grid-cols-2 gap-4'>
      <section className='col-span-2'>
        <h1 className='text-2xl'>Help</h1>
        <p>This is the help page.</p>
      </section>
      <section>
        <h2 id='code' className='text-xl'>
          Tips for writing code
        </h2>
        <CodingHelp />
      </section>
      <section>
        <h2 className='text-xl'>Navigation Shortcuts</h2>
        <div className='flex flex-col gap-6'>
          <div>
            <div className='mb-2'>
              Show <b>Simulation</b> tab
            </div>
            <kbd>1</kbd>
          </div>
          <div>
            <div>
              Show <b>Compiler</b> tab
            </div>
            <kbd>2</kbd>
          </div>
          <div>
            <div className='mb-2'>
              Show <b>Memory</b> tab
            </div>
            <kbd>3</kbd>
          </div>
          <div>
            <div className='mb-2'>
              Show <b>ISA settings</b> tab
            </div>
            <kbd>4</kbd>
          </div>
          <div>
            <div>
              Show <b>statistics</b> tab
            </div>
            <kbd>5</kbd>
          </div>
          <div>
            <div>
              Show <b>RISC-V documentation</b> tab
            </div>
            <kbd>6</kbd>
          </div>
          <div>
            <div>
              Show <b>Settings</b> tab
            </div>
            <kbd>7</kbd>
          </div>
          <div>
            <div>
              Show <b>Help</b> tab
            </div>
            <kbd>8</kbd>
          </div>
        </div>
      </section>
      <section>
        <h2 className='text-xl'>Simulation Shortcuts</h2>
        <div className='flex flex-col gap-6'>
          <div>
            <div className='mb-2'>Step simulation forward</div>
            <kbd>→</kbd>
          </div>
          <div>
            <div className='mb-2'>Step simulation backward</div>
            <kbd>←</kbd>
          </div>
          <div>
            <div className='mb-2'>Skip to the end of simulation</div>
            <kbd>Ctrl</kbd> + <kbd>Enter</kbd>
          </div>
          <div>
            <div className='mb-2'>Reload simulation</div>
            <kbd>R</kbd>
          </div>
          <div>
            <div className='mb-2'>Zoom in</div>
            <kbd>Ctrl</kbd> + <kbd>+</kbd>
          </div>
          <div>
            <div className='mb-2'>Zoom out</div>
            <kbd>Ctrl</kbd> + <kbd>-</kbd>
          </div>
          <div>
            <div className='mb-2'>Drag around</div>
            Hold middle mouse button (wheel) and drag
          </div>
        </div>
      </section>
    </div>
  );
}

function CodingHelp() {
  return (
    <>
      <p>I want to…</p>
      <Accordion type='single' collapsible>
        <AccordionItem value='item-1'>
          <AccordionTrigger>
            …make reference to an array specified in the Memory tab.
          </AccordionTrigger>
          <AccordionContent>
            <p>
              When utilizing the external definition of arrays, use the array's
              name.
            </p>
            <p>
              Create an array-type global variable in C. Don't use a pointer.
              For a practical usage, see the <b>AXPY</b> example.
              <CodeSnippet code={'extern int array[];'} language='c' />
            </p>
            <p>
              In assembly, use the name as a label or a constant. In the
              following example, the register a5 will contain the address of{' '}
              <code>array</code>. The register a4 will contain the address of{' '}
              <code>ptr</code>.
              <CodeSnippet
                code={`lla a5,array
la a4,ptr`}
                language='asm'
              />
            </p>
          </AccordionContent>
        </AccordionItem>
        <AccordionItem value='item-2'>
          <AccordionTrigger>…add debug prints to my code.</AccordionTrigger>
          <AccordionContent>
            <p>
              Debug prints can be added to individual instructions using a
              special comment:
              <CodeSnippet
                code={`addi a0,a0,1 #DEBUG"a0 is now \${a0}"`}
                language='asm'
              />
            </p>
            <p>
              The string inside the comment will be printed to the console in
              the simulation tab. The message will be printed on instruction
              commit. The <code>${'{reg}'}</code> syntax is used to reference
              the register's value.
            </p>
            <p>
              It is not possible to include print statements in C code or
              utilize the printf function.
            </p>
          </AccordionContent>
        </AccordionItem>
        <AccordionItem value='item-3'>
          <AccordionTrigger>
            …prevent the compiler from optimizing a variable away.
          </AccordionTrigger>
          <AccordionContent>
            <p>
              To prevent the compiler from optimizing a variable away, try the{' '}
              <code>volatile</code> keyword.
              <CodeSnippet code={'volatile int a;'} language='c' />
            </p>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </>
  );
}
