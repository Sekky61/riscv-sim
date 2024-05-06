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

import { IconButton } from '@/components/IconButton';
import type { ReactChildren } from '@/lib/types/reactTypes';
import clsx from 'clsx';
import { ZoomIn, ZoomOut } from 'lucide-react';
import {
  type WheelEventHandler,
  useEffect,
  useRef,
  useState,
  type TouchEventHandler,
} from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

type CanvasWindowProps = {
  children: ReactChildren;
};

/**
 * Canvas window component.
 * Scrollable, draggable with middle click, scrollable with touch.
 * Zoomable with scale prop.
 */
export function CanvasWindow({ children }: CanvasWindowProps) {
  const elRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const [middleHeld, setMiddleHeld] = useState(false);
  const pos = useRef({ x: 0, y: 0, scale: 1 });
  const lastDrag = useRef({ x: 0, y: 0 });

  const scaleUp = () => {
    pos.current.scale += 0.1;
    if (contentRef.current) {
      contentRef.current.style.transform = getTransformString(pos.current);
    }
  };

  const scaleDown = () => {
    pos.current.scale -= 0.1;
    if (contentRef.current) {
      contentRef.current.style.transform = getTransformString(pos.current);
    }
  };

  // Middle click hold and drag

  function onPointerUpDown(e: PointerEvent) {
    const isMiddle = e.button === 1;
    if (!isMiddle) {
      return;
    }
    const isDown = e.type === 'pointerdown';
    setMiddleHeld(isDown);
    if (isDown) {
      // Pointer capture solves the problem of pointer leaving the element
      elRef.current?.setPointerCapture(e.pointerId);
      elRef.current?.addEventListener('pointermove', onPointerMove);
    } else {
      elRef.current?.releasePointerCapture(e.pointerId);
      elRef.current?.removeEventListener('pointermove', onPointerMove);
    }
  }

  function onPointerMove(ee: PointerEvent) {
    // get the change in x and y
    // set the new offset
    if (!elRef.current || !contentRef.current) {
      return;
    }

    const dx = ee.movementX;
    const dy = ee.movementY;

    pos.current.x += dx;
    pos.current.y += dy;

    contentRef.current.style.transform = getTransformString(pos.current);
  }

  const onWheel: WheelEventHandler<HTMLDivElement> = (event) => {
    if (!elRef.current || !contentRef.current) {
      return;
    }

    // do not scroll the view if scrolling a scrollable (instruction list)

    // offset in the scroll direction
    // with shift, scroll horizontally
    const horizontal = event.shiftKey ? event.deltaY : event.deltaX;
    const vertical = event.shiftKey ? event.deltaX : event.deltaY;

    pos.current.x -= horizontal;
    pos.current.y -= vertical;

    contentRef.current.style.transform = getTransformString(pos.current);
  };

  // Register on component mount
  // biome-ignore lint/correctness/useExhaustiveDependencies: hooks on mount, unhooks on unmount
  useEffect(() => {
    elRef.current?.addEventListener('pointerdown', onPointerUpDown);
    elRef.current?.addEventListener('pointerup', onPointerUpDown);
    return () => {
      elRef.current?.removeEventListener('pointerdown', onPointerUpDown);
      elRef.current?.removeEventListener('pointerup', onPointerUpDown);
    };
  }, []);

  const onTouchMove: TouchEventHandler = (event) => {
    if (!elRef.current || !contentRef.current) {
      return;
    }

    if (event.touches.length === 1) {
      // just drag
      const touch = event.touches[0];
      if (!touch) {
        return;
      }
      const dx = touch.clientX - lastDrag.current.x;
      const dy = touch.clientY - lastDrag.current.y;
      lastDrag.current.x = touch.clientX;
      lastDrag.current.y = touch.clientY;

      pos.current.x += dx;
      pos.current.y += dy;

      contentRef.current.style.transform = getTransformString(pos.current);
    }
  };

  const cls = clsx(
    'overflow-hidden sim-bg min-h-full min-w-full',
    middleHeld ? 'cursor-grabbing' : 'cursor-grab',
  );

  return (
    <div
      className={cls}
      ref={elRef}
      onWheel={onWheel}
      onTouchMove={onTouchMove}
      onTouchStart={(e) => {
        const touch = e.touches[0];
        if (!touch) {
          return;
        }
        lastDrag.current.x = touch.clientX;
        lastDrag.current.y = touch.clientY;
      }}
    >
      <div className='relative w-6 h-6'>
        <div className='absolute h-6 w-6' ref={contentRef}>
          {children}
        </div>
      </div>
      <ScaleButtons scaleUp={scaleUp} scaleDown={scaleDown} />
    </div>
  );
}

export type ScaleButtonsProps = {
  scaleUp: () => void;
  scaleDown: () => void;
};

/**
 * Scale buttons (in the corner).
 * also provides shortcuts.
 */
const ScaleButtons = ({ scaleUp, scaleDown }: ScaleButtonsProps) => {
  useHotkeys(
    // Add is the numeric plus key
    ['+', 'Add'],
    () => {
      scaleUp();
    },
    { combinationKey: '-' },
    [scaleUp],
  );

  useHotkeys(
    ['-', 'Subtract'],
    () => {
      scaleDown();
    },
    { preventDefault: true },
    [scaleDown],
  );

  return (
    <div className='absolute bottom-0 right-[100px] flex flex-col gap-4 p-6'>
      <div className='secondary-container rounded-[9px] drop-shadow w-8 h-8'>
        <IconButton clickCallback={scaleUp} description='Zoom in' animate>
          <ZoomIn strokeWidth={1.5} />
        </IconButton>
      </div>
      <div className='secondary-container rounded-[9px] drop-shadow w-8 h-8'>
        <IconButton clickCallback={scaleDown} description='Zoom out' animate>
          <ZoomOut strokeWidth={1.5} />
        </IconButton>
      </div>
    </div>
  );
};

/**
 * Create the transform() css property string for the element
 */
function getTransformString({
  x,
  y,
  scale,
}: { x: number; y: number; scale: number }) {
  return `translate(${x}px, ${y}px) scale(${scale})`;
}
