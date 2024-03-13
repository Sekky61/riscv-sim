/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Educational page about the blocks of the processor
 *
 * @date    04 March 2024, 16:00 (created)
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

import { loadBlockDescriptions } from '@/lib/staticLoaders';
import { BlockDescription } from '@/lib/types/codeExamples';

/**
 * Educational page about the blocks of the processor.
 * First a general introduction and then a section for each block.
 */
export default async function Page() {
  const descriptions = await loadBlockDescriptions();
  return (
    <div className='flex flex-col gap-10 pb-16'>
      <section>
        <h1>Superscalar Architecture</h1>
        <p>Learn about the architecture and the individual units.</p>
      </section>
      <section>
        <h2>Superscalar Execution</h2>
        <p>
          Multiple instructions can be executed simultaneously in a CPU thanks
          to the superscalar architecture. It is a common feature of designs for
          high-performance processors.
        </p>
        <p>
          Front-end refers to the beginning of the pipeline. It retrieves and
          decodes a set of instructions in the program order.
        </p>

        <p>
          The rest of the pipeline is referred to as the <b>back-end</b>. It
          executes instruction <b>out of order</b>, writing the outcomes back to
          the register file after completing instructions. The instructions
          leave the pipeline in order again.
        </p>
        <p>
          Superscalar CPUs execute instructions in <b>parallel</b> by using
          several execution units in the backend. The CPU dynamically analyzes
          the dependencies between instructions and determines the optimal order
          in which to execute them to maximize parallelism while preserving
          program correctness.
        </p>
      </section>
      {Object.values(descriptions).map((blockDescription) => (
        <BlockSection
          key={blockDescription.name}
          blockDescription={blockDescription}
        />
      ))}
    </div>
  );
}

function BlockSection({
  blockDescription,
}: { blockDescription: BlockDescription }) {
  return (
    <section>
      <h2 id={blockDescription.name}>{blockDescription.name}</h2>
      <p>{blockDescription.shortDescription}</p>
    </section>
  );
}
