/**
 * @file    HighlightProvider.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   React context provider for highlighting the instructions
 *
 * @date    25 February 2024, 23:00 (created)
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

import React, { createContext, useCallback, useContext, useState } from 'react';

/**
 * The value itself is not provided, only the setter.
 * This is crucial for optimization of re-renders - the setter is memoized, so hovering over the instructions
 * does not cause React re-renders.
 */
export type HighlightContextType = (
  id: { simcode: number; inputcode: number } | null,
) => void;

export const HighlightContext = createContext<HighlightContextType>(() => {});

export const useHighlight = () => useContext(HighlightContext);

type Props = {
  children?: React.ReactNode;
};
export const HighlightProvider: React.FC<Props> = ({ children }) => {
  const [highlightedCodeId, setHighlightedCodeId] = useState<{
    simcode: number;
    inputcode: number;
  } | null>(null);

  // Memoize the setter
  const setHighlightedCodeIdMemo = useCallback(
    (id: { simcode: number; inputcode: number } | null) => {
      setHighlightedCodeId(id);
    },
    [],
  );

  // Create a style for all children with class .instruction and data-instruction-id equal to highlightedCodeId
  const instructionStyle = `
    .instruction[data-instruction-id="${highlightedCodeId?.simcode}"] {
      background-color: #f0f0f0;
    }

    .inputcodemodel[data-inputcode-id="${highlightedCodeId?.inputcode}"] {
      background-color: #f0f0f0;
    }
  `;

  return (
    <HighlightContext.Provider value={setHighlightedCodeIdMemo}>
      <style>{instructionStyle}</style>
      {children}
    </HighlightContext.Provider>
  );
};
