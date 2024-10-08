/**
 * @file useRefDimensions.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A hook for getting the dimensions of a ref (DOM element)
 *
 * @date    21 March 2024, 15:00 (created)
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

import useResizeObserver from '@react-hook/resize-observer';
import { type RefObject, useLayoutEffect, useState } from 'react';

/**
 * A hook for getting the dimensions of a ref (DOM element)
 * Refreshes the dimensions when the ref resizes
 *
 * Based on source: https://codesandbox.io/p/sandbox/userefdimensions-65se6 and https://www.npmjs.com/package/@react-hook/resize-observer
 * @param ref - a reference to the DOM element
 * @returns the dimensions of the ref
 */
export const useRefDimensions = (target: RefObject<HTMLDivElement>) => {
  const [size, setSize] = useState<DOMRect>({
    x: 0,
    y: 0,
    width: 0,
    height: 0,
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    toJSON: () => '',
  });

  useLayoutEffect(() => {
    if (!target.current) return;
    setSize(target.current.getBoundingClientRect());
  }, [target]);

  // Where the magic happens
  useResizeObserver(target, (entry) => setSize(entry.contentRect));
  return size;
};
