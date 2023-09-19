'use client';

import { ZoomIn, ZoomOut } from 'lucide-react';
import { useCallback, useRef, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

import { renderLine } from '@/lib/svgRender';

import AnimatedButton from '@/components/AnimatedButton';
import CanvasWindow from '@/components/CanvasWindow';
import Placement from '@/components/Placement';
import Line from '@/components/simulation/Line';
import Program from '@/components/simulation/Program';
import ReorderBuffer from '@/components/simulation/ReorderBuffer';
import Timeline from '@/components/simulation/TImeline';
import { useSvgRender } from '@/components/SvgRender';

export default function HomePage() {
  const [scale, setScale] = useState(1);

  const scaleUp = () => {
    setScale(scale + 0.2);
  };

  const scaleDown = () => {
    setScale(scale - 0.2);
  };

  const renderFunction = useCallback(() => {
    return renderLine('#ff0000');
  }, []);

  const svgRenderTarget = useRef<HTMLDivElement>(null);
  useSvgRender(svgRenderTarget, renderFunction);

  return (
    <>
      <CanvasWindow scale={scale}>
        <Placement x={50} y={100}>
          <Program />
        </Placement>
        <Placement x={250} y={150}>
          <Line length={250} />
        </Placement>
        <Placement x={500} y={100}>
          <ReorderBuffer />
        </Placement>
        <div ref={svgRenderTarget} />
      </CanvasWindow>
      <div className='pointer-events-none absolute top-0 flex w-full justify-center pt-2'>
        <Timeline className='pointer-events-auto' />
      </div>
      <div className='absolute bottom-0 right-0 flex flex-col gap-4 p-4'>
        <ScaleButtons scaleUp={scaleUp} scaleDown={scaleDown} />
      </div>
    </>
  );
}

export type ScaleButtonsProps = {
  scaleUp: () => void;
  scaleDown: () => void;
};

/**
 * also provides shortcuts
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
    <>
      <AnimatedButton
        shortCut='ctrl-+'
        shortCutOptions={{ combinationKey: '-', preventDefault: true }}
        clickCallback={scaleUp}
        className='bg-gray-100 rounded-full drop-shadow'
      >
        <ZoomIn strokeWidth={1.5} />
      </AnimatedButton>
      <AnimatedButton
        shortCut='ctrl+-'
        shortCutOptions={{ preventDefault: true }}
        clickCallback={scaleDown}
        className='bg-gray-100 rounded-full drop-shadow'
      >
        <ZoomOut strokeWidth={1.5} />
      </AnimatedButton>
    </>
  );
};
