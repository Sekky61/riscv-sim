/**
 * @file    BlockDescriptionContext.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A context for the block descriptions
 *
 * @date    04 March 2024, 18:00 (created)
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

'use client';

import type React from 'react';
import { createContext, useContext } from 'react';

import type { BlockDescriptions } from '@/lib/types/codeExamples';

/**
 * A context for the block descriptions
 */
export const BlockDescriptionContext = createContext<BlockDescriptions>({});

/**
 * A provider for the block descriptions
 */
export function BlockDescriptionProvider({
  children,
  descriptions,
}: { children: React.ReactNode; descriptions: BlockDescriptions }) {
  return (
    <BlockDescriptionContext.Provider value={descriptions}>
      {children}
    </BlockDescriptionContext.Provider>
  );
}

/**
 * A hook for the block descriptions
 */
export function useBlockDescriptions() {
  return useContext(BlockDescriptionContext);
}
