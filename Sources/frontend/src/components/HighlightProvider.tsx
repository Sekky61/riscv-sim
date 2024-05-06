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

import type React from 'react';
import { createContext, useContext, useMemo, useState } from 'react';

/**
 * The value itself is not provided, only the setter.
 * This is crucial for optimization of re-renders - the setter is memoized, so hovering over the instructions
 * does not cause React re-renders.
 */
export type HighlightContextType = {
  setHighlightedRegister: (id: string | null) => void;
  setHighlightedInstruction: (
    id: { simcode: number; inputcode: number } | null,
  ) => void;
};

export const HighlightContext = createContext<HighlightContextType>({
  setHighlightedRegister: () => {},
  setHighlightedInstruction: () => {},
});

export const useHighlight = () => useContext(HighlightContext);

type Props = {
  children?: React.ReactNode;
};

/**
 * Styles currently highlighted instruction/register.
 */
export const HighlightProvider: React.FC<Props> = ({ children }) => {
  const [highlightedCodeId, setHighlightedCodeId] = useState<{
    simcode: number;
    inputcode: number;
  } | null>(null);

  const [highlightedRegister, setHighlightedRegister] = useState<string | null>(
    null,
  );

  // useMemo to memoize the context object
  const contextValue = useMemo(
    () => ({
      setHighlightedRegister,
      setHighlightedInstruction: setHighlightedCodeId,
    }),
    [],
  );

  // Create a style for all children with class .instruction and data-instruction-id equal to highlightedCodeId
  // It may be interesting to try to separate into 3 styles.
  // Opacity, so that the register can be highlighted over a highlighted instruction
  const instructionStyle = `
    .instruction[data-instruction-id="${highlightedCodeId?.simcode}"] {
      background-color: var(--hover-highlight-color);
    }

    .inputcodemodel[data-inputcode-id="${highlightedCodeId?.inputcode}"] {
      background-color: var(--hover-highlight-color);
    }

    .register[data-register-id="${highlightedRegister}"] {
      background-color: var(--hover-highlight-color);
    }

    .dark .instruction[data-instruction-id="${highlightedCodeId?.simcode}"] {
      background-color: var(--hover-highlight-color-dark);
    }

    .dark .inputcodemodel[data-inputcode-id="${highlightedCodeId?.inputcode}"] {
      background-color: var(--hover-highlight-color-dark);
    }

    .dark .register[data-register-id="${highlightedRegister}"] {
      background-color: var(--hover-highlight-color-dark);
    }
  `;

  return (
    <HighlightContext.Provider value={contextValue}>
      <style>{instructionStyle}</style>
      {children}
    </HighlightContext.Provider>
  );
};
