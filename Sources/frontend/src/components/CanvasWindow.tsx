/**
 * @file    CanvasWindow.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Enables dragging of the contents of the canvas
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

'use client';

import { ReactChildren } from '@/lib/types/reactTypes';
import clsx from 'clsx';
import { useRef, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

type CanvasWindowProps = {
  children: ReactChildren;
  scale: number;
};

/**
 * Canvas window component.
 * Scrollable, draggable with control and mouse.
 * Zoomable with scale prop.
 *
 * TODO: when zoomed, the corners are not visible
 */
export default function CanvasWindow({ children, scale }: CanvasWindowProps) {
  const elRef = useRef<HTMLDivElement>(null);
  const [ctrlHeld, setCtrlHeld] = useState(false);

  // Add cursor change on ctrl
  useHotkeys(
    'ctrl',
    (e) => {
      setCtrlHeld(e.type === 'keydown');
    },
    { keyup: true, keydown: true },
  );

  // Dragging event listeners
  const onDragStart = () => {
    if (!ctrlHeld) {
      return;
    }

    const body = document.getElementsByTagName('body')[0];
    if (!body) {
      throw new Error('Body not found');
    }
    body.style.userSelect = 'none';
    // todo cursor grab not working
    body.classList.add('cursor-grabbing');

    function onDrag(this: Window, ee: MouseEvent) {
      // get the change in x and y
      const dx = ee.movementX;
      const dy = ee.movementY;
      // set the new offset
      if (elRef.current) {
        elRef.current.scrollLeft = elRef.current.scrollLeft - dx;
        elRef.current.scrollTop = elRef.current.scrollTop - dy;
      }
    }

    const onDragEnd = () => {
      window.removeEventListener('mousemove', onDrag);
      window.removeEventListener('mouseup', onDragEnd);
      body.style.userSelect = 'auto';
      body.classList.remove('cursor-grabbing');
    };

    window.addEventListener('mousemove', onDrag);
    window.addEventListener('mouseup', onDragEnd);
  };

  const cls = clsx(
    'relative overflow-auto w-full h-full dotted-bg',
    ctrlHeld && 'hover:cursor-grab',
  );

  return (
    <div onMouseDown={onDragStart} className={cls} ref={elRef}>
      <div style={{ transform: `scale(${scale})` }}>{children}</div>
    </div>
  );
}
