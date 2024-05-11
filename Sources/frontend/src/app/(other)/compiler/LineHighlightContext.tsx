/**
 * @file    LineHighlightContext.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Context for highlighting hovered lines in the code editor
 *
 * @date    27 February 2024, 09:00 (created)
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
import { createContext, useContext, useMemo, useState } from 'react';

/**
 * The value itself is not provided, only the setter.
 * This is crucial for optimization of re-renders - the setter is memoized, so hovering over the instructions
 * does not cause React re-renders.
 */
export type LineHighlightContextProps = {
  setHoveredCLine: (line: number | null) => void;
};

export const LineHighlightContext = createContext<LineHighlightContextProps>({
  setHoveredCLine: () => {},
});

export const useLineHighlight = () => useContext(LineHighlightContext);

type Props = {
  children?: React.ReactNode;
};

/**
 * Context for highlighting hovered lines in the code editor.
 * When a line is hovered, not only the line is highlighted, but also the corresponding line in the other editor.
 * The information about corresponding lines is provided by compiler.
 */
export const LineHighlightProvider: React.FC<Props> = ({ children }) => {
  const [hoveredCLine, setHoveredCLine] = useState<number | null>(null);

  // useMemo to memoize the context object
  const contextValue = useMemo(
    () => ({
      setHoveredCLine,
    }),
    [],
  );

  // Create a style for the hovered line
  const instructionStyle = `
    .cm-line[data-c-line="${hoveredCLine}"] {
      background-color: rgba(66, 135, 245, 0.20);
    }
  `;

  return (
    <LineHighlightContext.Provider value={contextValue}>
      <style>{instructionStyle}</style>
      {children}
    </LineHighlightContext.Provider>
  );
};
