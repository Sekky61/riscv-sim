/**
 * @file    IconButton.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Generic animated button, reacts to click and optionally shortcut
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
import { useHotkeys } from 'react-hotkeys-hook';
import { OptionsOrDependencyArray } from 'react-hotkeys-hook/dist/types';

import { ReactChildren } from '@/lib/types/reactTypes';
import { useRef } from 'react';

export type IconButtonProps = {
  /**
   * True if the button should be in a highlighted state
   */
  active?: boolean;
  shortCut?: string;
  shortCutOptions?: OptionsOrDependencyArray;
  clickCallback?: () => void;
  children: ReactChildren;
  className?: string;
  description: string;
};

/**
 * A round button with an icon (children), optional shortcut
 *
 * The click callback is called when the button is clicked or when the shortcut is pressed.
 * By default, the shortcut is prevented from bubbling up to the browser.
 */
export const IconButton = ({
  active = false,
  shortCut = '',
  shortCutOptions,
  clickCallback,
  children,
  className,
  description,
}: IconButtonProps) => {
  const buttonRef = useRef<HTMLButtonElement>(null);

  useHotkeys(
    shortCut,
    () => {
      onClick();
      buttonRef.current?.toggleAttribute('data-clicked', true);
      setTimeout(() => {
        buttonRef.current?.toggleAttribute('data-clicked', false);
      }, 200);
    },
    { preventDefault: true, ...shortCutOptions },
  );

  const onClick = () => {
    clickCallback?.();
  };

  return (
    <button
      ref={buttonRef}
      type='button'
      className={clsx('iconHighlight h-8 w-8 rounded-full p-1', className)}
      onClick={onClick}
      aria-label={description}
      data-active={active ? 'true' : undefined}
    >
      {children}
    </button>
  );
};
