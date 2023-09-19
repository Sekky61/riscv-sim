/**
 * @file    AnimatedButton.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   [TODO]
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
