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

import AnimatedButton from '@/components/AnimatedButton';
import { ReactChildren } from '@/lib/types/reactTypes';
import clsx from 'clsx';
import { ZoomIn, ZoomOut } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
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
  const [middleHeld, setMiddleHeld] = useState(false);
  const [scale, setScale] = useState(1);

  const scaleUp = () => {
    setScale(scale + 0.2);
  };

  const scaleDown = () => {
    setScale(scale - 0.2);
  };

  // Middle click hold and drag

  function onPointerUpDown(e: PointerEvent) {
    const isDown = e.type === 'pointerdown';
    const isMiddle = e.button === 1;
    if (isMiddle) {
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
  }

  function onPointerMove(ee: PointerEvent) {
    // get the change in x and y
    const dx = ee.movementX;
    const dy = ee.movementY;
    // set the new offset
    if (elRef.current) {
      elRef.current.scrollLeft = elRef.current.scrollLeft - dx;
      elRef.current.scrollTop = elRef.current.scrollTop - dy;
    }
  }

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
    'overflow-auto dotted-bg min-h-full min-w-full',
    middleHeld && 'cursor-grabbing',
    !middleHeld && 'cursor-grab',
  );

  // The w-6 h-6 trick to make the overflow not affect initial size of the component
  return (
    <div className={cls} ref={elRef}>
      <div style={{ transform: `scale(${scale})` }} className='h-6 w-6'>
        {children}
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
    'ctrl-+',
    () => {
      scaleUp();
    },
    { combinationKey: '-', preventDefault: true },
    [scaleUp],
  );

  useHotkeys(
    'ctrl+-',
    () => {
      scaleDown();
    },
    { preventDefault: true },
    [scaleDown],
  );

  return (
    <div className='absolute bottom-0 right-0 flex flex-col gap-4 p-6'>
      <AnimatedButton
        shortCut='ctrl-+'
        shortCutOptions={{ combinationKey: '-', preventDefault: true }}
        clickCallback={scaleUp}
        className='bg-gray-100 rounded-full drop-shadow'
        description='Zoom in'
      >
        <ZoomIn strokeWidth={1.5} />
      </AnimatedButton>
      <AnimatedButton
        shortCut='ctrl+-'
        shortCutOptions={{ preventDefault: true }}
        clickCallback={scaleDown}
        className='bg-gray-100 rounded-full drop-shadow'
        description='Zoom out'
      >
        <ZoomOut strokeWidth={1.5} />
      </AnimatedButton>
    </div>
  );
};
