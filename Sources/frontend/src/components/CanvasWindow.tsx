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
    body.style.userSelect = 'none';
    body.classList.add('cursor-grabbing');

    function onDrag(this: Window, e: MouseEvent) {
      // get the change in x and y
      const dx = e.movementX;
      const dy = e.movementY;
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

      const body = document.getElementsByTagName('body')[0];
      body.style.userSelect = 'auto';
      body.classList.remove('cursor-grabbing');
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
