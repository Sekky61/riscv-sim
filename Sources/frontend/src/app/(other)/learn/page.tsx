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
        <h1 className='text-4xl'>Superscalar Architecture</h1>
        <p>Learn about the architecture and the individual units.</p>
      </section>
      <section>
        <h2 className='text-2xl'>Superscalar Execution</h2>
        <p>
          Superscalar architecture is a type of CPU design that allows more than
          one instruction to be executed at the same time. It is a common
          feature of high-performance microprocessor designs.
        </p>
        <p>
          The first part of the pipeline is called front-end. It operates in
          order, fetching and decoding instructions. The second part is called
          back-end. It operates out of order, executing instructions and writing
          results back to the register file. The instructions leave the pipeline
          in order again.
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
      <h2 className='text-2xl' id={blockDescription.name}>
        {blockDescription.name}
      </h2>
      <p>{blockDescription.shortDescription}</p>
    </section>
  );
}
