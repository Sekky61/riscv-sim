/**
 * @file    CodingHelp.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The coding section of the help page
 *
 * @date    29 February 2024, 15:00 (created)
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

export function CodingHelp() {
  return (
    <>
      <p>I want to…</p>
      <Accordion type='single' collapsible>
        <AccordionItem value='item-0'>
          <AccordionTrigger>…define data in assembler.</AccordionTrigger>
          <AccordionContent>
            <p>
              The compiler implements these{' '}
              <a href='https://ftp.gnu.org/old-gnu/Manuals/gas-2.9.1/html_chapter/as_7.html'>
                GCC Assembler directives
              </a>
              : .byte, .hword, .word, .align, .ascii, .asciiz, .string, .skip
              and .zero. Note that the implementation may have slight
              differences. Now to examples:
            </p>
            <CodeSnippet language='asm'>
              N:{'                '}# N has the address pointing to the 4 byte
              constant '42'
              <br />
              {'  '}.word 42
            </CodeSnippet>
            <CodeSnippet language='asm'>
              Array:{'            '}# Array has the address pointing to the 8
              integers
              <br />
              {'  '}.word 0, 1, 2, 3, 4, 5, 6, 7
              <br />
              {'  '}.zero 8{'         '}# You can even put multiple directives
              after each other.
            </CodeSnippet>
            <CodeSnippet language='asm'>
              {'  '}.align 4
              <br />
              Glob:{'             '}# Glob points to the byte. It is aligned to
              2^4=16B boundary.
              <br />
              {'  '}.byte 1
            </CodeSnippet>
          </AccordionContent>
        </AccordionItem>
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
            </p>
            <CodeSnippet code={'extern int array[];'} language='c' />
            <p>
              In assembly, use the name as a label or a constant. In the
              following example, the register a5 will contain the address of{' '}
              <code>array</code>. The register a4 will contain the address of{' '}
              <code>ptr</code>.
            </p>
            <CodeSnippet
              code={`lla a5,array
la a4,ptr`}
              language='asm'
            />
          </AccordionContent>
        </AccordionItem>
        <AccordionItem value='item-2'>
          <AccordionTrigger>…add debug prints to my code.</AccordionTrigger>
          <AccordionContent>
            <p>
              Debug prints can be added to individual instructions using a
              special comment:
            </p>
            <CodeSnippet
              code={`addi a0,a0,1 #DEBUG"a0 is now \${a0}"`}
              language='asm'
            />
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
            </p>
            <CodeSnippet code={'volatile int a;'} language='c' />
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </>
  );
}
