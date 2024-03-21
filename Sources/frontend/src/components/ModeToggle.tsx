/**
 * @file    ModeToggle.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for toggling the light/dark mode
 *
 * @date    03 March 2024, 21:00 (created)
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

import { useTheme } from 'next-themes';
import * as React from 'react';

import { RadioInput } from './form/RadioInput';

export function ModeToggle() {
  const { setTheme, theme, themes } = useTheme();

  return (
    <RadioInput
      choices={themes}
      texts={['Light', 'Dark', 'System'] as const}
      value={theme ?? 'system'}
      onNewValue={(val) => {
        setTheme(val);
      }}
    />
  );
}
