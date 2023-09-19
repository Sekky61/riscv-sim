import clsx from 'clsx';
import { useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { OptionsOrDependencyArray } from 'react-hotkeys-hook/dist/types';

import { ReactChildren } from '@/lib/reactTypes';

export type AnimatedButtonProps = {
  active?: boolean;
  shortCut?: string;
  shortCutOptions?: OptionsOrDependencyArray;
  clickCallback?: () => void;
  children: ReactChildren;
  animationLength?: number;
  className?: string;
};

/**
 * The click callback is called when the button is clicked or when the shortcut is pressed.
 */
const AnimatedButton = ({
  active = false,
  shortCut = '',
  shortCutOptions,
  clickCallback,
  children,
  animationLength,
  className,
}: AnimatedButtonProps) => {
  const [isAnimating, setIsAnimating] = useState(false);

  useHotkeys(
    shortCut,
    () => {
      onClick();
    },
    shortCutOptions,
  );

  const onClick = () => {
    clickCallback?.();
    setIsAnimating(true);
    setTimeout(() => setIsAnimating(false), animationLength || 300);
  };

  return (
    <button
      className={clsx(
        'timelineHighlight h-8 w-8 rounded-full p-1',
        className,
        active && 'bg-gray-200',
        isAnimating && 'tlbutton-animation-outer',
      )}
      onClick={onClick}
    >
      <div
        className={clsx(
          'duration-100',
          isAnimating && 'tlbutton-animation-inner',
        )}
      >
        {children}
      </div>
    </button>
  );
};

export default AnimatedButton;
