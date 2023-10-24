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

import { ComponentProps, useEffect, useRef, useState } from 'react';

type CanvasWindowProps = ComponentProps<'div'> & {
  scale: number;
};

// Drag with ctrl + click
export default function CanvasWindow({
  children,
  className,
  scale,
}: CanvasWindowProps) {
  const [_dragging, setDragging] = useState(false);
  const canvasRef = useRef<HTMLDivElement>(null);
  const elRef = useRef<HTMLDivElement>(null);

  // Register listening to ctrl key
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Control') {
        canvasRef.current?.classList.add('hover:cursor-grab');
      }
    };
    const onKeyUp = (e: KeyboardEvent) => {
      if (e.key === 'Control') {
        canvasRef.current?.classList.remove('hover:cursor-grab');
      }
    };

    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('keyup', onKeyUp);
    return () => {
      window.removeEventListener('keydown', onKeyDown);
      window.removeEventListener('keyup', onKeyUp);
    };
  }, []);

  const onDragStart = (e: React.DragEvent<HTMLDivElement>) => {
    if (!e.ctrlKey) {
      return;
    }

    setDragging(true);

    // Get body
    const body = document.getElementsByTagName('body')[0];
    if (!body) {
      throw new Error('Body not found');
    }
    body.style.userSelect = 'none';
    body.classList.add('cursor-grabbing');

    function onDrag(this: Window, ee: MouseEvent) {
      // get the change in x and y
      const dx = ee.movementX;
      const dy = ee.movementY;
      // set the position of the div to the change in x and y
      if (elRef.current) {
        elRef.current.style.left = `${elRef.current.offsetLeft + dx}px`;
        elRef.current.style.top = `${elRef.current.offsetTop + dy}px`;
      }
    }

    function onDragEnd(this: Window, _e: MouseEvent) {
      setDragging(false);

      window.removeEventListener('mousemove', onDrag);
      window.removeEventListener('mouseup', onDragEnd);

      const body2 = document.getElementsByTagName('body')[0];
      if (!body2) {
        throw new Error('Body not found');
      }
      body2.style.userSelect = 'auto';
      body2.classList.remove('cursor-grabbing');
    }

    window.addEventListener('mousemove', onDrag);
    window.addEventListener('mouseup', onDragEnd);
  };

  return (
    <div
      ref={canvasRef}
      onMouseDown={onDragStart}
      className={`${className} relative h-full w-full overflow-auto`}
    >
      <div
        ref={elRef}
        className='absolute'
        style={{ transform: `scale(${scale})` }}
      >
        {children}
      </div>
    </div>
  );
}
