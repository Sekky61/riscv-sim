/**
 * @file    InstructionListDisplay.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Codemirror extension for line coloring based on mapping
 *
 * @date    28 October 2023, 13:00 (created)
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

import { useRefDimensions } from '@/lib/hooks/useRefDimensions';
import { useRef, type ReactElement } from 'react';
import { FixedSizeList } from 'react-window';

export type InstructionListDisplayProps<T> = {
  instructions: Array<T>;
  // Size of the buffer (ex. 256)
  instructionRenderer: (
    item: T | null,
    id: number,
  ) => ReactElement<{ key: string }>;
  totalSize?: number;
  legend?: ReactElement;
  columns?: number;
};

/**
 * A component for displaying a list.
 * It renders `totalSize` items, first from instructions array, then pads with nulls.
 * Calls renderer for each item.
 *
 * Can render into multiple columns.
 */
export function InstructionListDisplay<T>({
  instructions,
  totalSize,
  instructionRenderer,
  legend,
  columns = 1,
}: InstructionListDisplayProps<T>) {
  const ref = useRef(null);
  const dimensions = useRefDimensions(ref);
  const displayCount = totalSize ?? (instructions.length || 1);

  // A react-window cell renderer for InstructionListDisplay.
  const Row = ({ index, style }) => (
    <div style={style}>
      {instructionRenderer(instructions[index] ?? null, index)}
    </div>
  );

  return (
    <div ref={ref} className='h-full w-full'>
      <FixedSizeList
        width={dimensions.width}
        height={dimensions.height}
        itemCount={displayCount}
        itemSize={32}
        className='instruction-list-container surface-container-low rounded-[8px]'
      >
        {Row}
      </FixedSizeList>
    </div>
  );
}
