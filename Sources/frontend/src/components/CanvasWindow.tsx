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
import { WheelEventHandler, useEffect, useRef, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

type CanvasWindowProps = {
  children: ReactChildren;
};

/**
 * Canvas window component.
 * Scrollable, draggable with middle click.
 * Zoomable with scale prop.
 */
export default function CanvasWindow({ children }: CanvasWindowProps) {
  const elRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const [middleHeld, setMiddleHeld] = useState(false);
  const [scale, setScale] = useState(1);

  const scaleUp = () => {
    setScale(scale + 0.1);
  };

  const scaleDown = () => {
    setScale(scale - 0.1);
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

    // get the current offset from attributes. The right and bottom should be defined as inline styles
    const { right, bottom } = getOffsets(contentRef.current.style);

    const offsetRight = right - dx;
    const offsetBottom = bottom - dy;

    // add offset to position of the element (right, bottom)
    contentRef.current.style.right = `${offsetRight}px`;
    contentRef.current.style.bottom = `${offsetBottom}px`;
  }

  const onWheel: WheelEventHandler<HTMLDivElement> = (event) => {
    if (!elRef.current || !contentRef.current) {
      return;
    }
    // offset in the scroll direction
    const { right, bottom } = getOffsets(contentRef.current.style);

    // with shift, scroll horizontally
    const horizontal = event.shiftKey ? event.deltaY : event.deltaX;
    const vertical = event.shiftKey ? event.deltaX : event.deltaY;

    contentRef.current.style.right = `${right + horizontal}px`;
    contentRef.current.style.bottom = `${bottom + vertical}px`;
  };

  // Register on component mount
  useEffect(() => {
    elRef.current?.addEventListener('pointerdown', onPointerUpDown);
    elRef.current?.addEventListener('pointerup', onPointerUpDown);
    return () => {
      elRef.current?.removeEventListener('pointerdown', onPointerUpDown);
      elRef.current?.removeEventListener('pointerup', onPointerUpDown);
    };
  }, []);

  const cls = clsx(
    'overflow-hidden sim-bg min-h-full min-w-full',
    middleHeld ? 'cursor-grabbing' : 'cursor-grab',
  );

  // The w-6 h-6 trick to make the overflow not affect initial size of the component
  return (
    <div className={cls} ref={elRef} onWheel={onWheel}>
      <div className='relative w-6 h-6'>
        <div
          style={{ transform: `scale(${scale})`, right: 0, bottom: 0 }}
          className='absolute h-6 w-6'
          ref={contentRef}
        >
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
    ['+', '+', 'Add'],
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
 * Get offsets from the style object
 */
function getOffsets(style: CSSStyleDeclaration) {
  return {
    right: Number.parseInt(style.right || '0'),
    bottom: Number.parseInt(style.bottom || '0'),
  };
}
