/**
 * @file    Shortcuts.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The shortcuts part of the help page
 *
 * @date    29 February 2024, 15:00 (created)
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

import { Fragment } from 'react';

export function ShortcutsHelp() {
  return (
    <div className='flex flex-col gap-6'>
      {shortcutCategories.map((category) => (
        <div key={category.title}>
          <h3 className='text-xl'>{category.title}</h3>
          <div
            className='grid gap-2 gap-x-4 justify-center'
            style={{
              gridTemplateColumns: 'max-content 1fr',
            }}
          >
            {category.shortcuts.map((shortcut) => (
              <ShortcutEntry key={shortcut} shortcut={shortcuts[shortcut]} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * A single row in the list of shortcuts
 */
export function ShortcutEntry({ shortcut }: { shortcut: Shortcut }) {
  // Interleave the keys with the plus sign
  const keys = shortcut.keys.map((key, i) => (
    <Fragment key={`${key}-${i + 1}`}>
      {i > 0 && ' + '}
      <kbd>{key}</kbd>
    </Fragment>
  ));

  return (
    <div className='grid grid-cols-subgrid col-span-2 m-1'>
      <div>{shortcut.description}</div>
      <div>{keys}</div>
    </div>
  );
}

type Shortcut = {
  description: string;
  keys: string[];
};

const shortcuts = {
  simulationTab: {
    description: 'Show Simulation tab',
    keys: ['1'],
  },
  compilerTab: {
    description: 'Show Compiler tab',
    keys: ['2'],
  },
  memoryTab: {
    description: 'Show Memory tab',
    keys: ['3'],
  },
  isaSettingsTab: {
    description: 'Show ISA settings tab',
    keys: ['4'],
  },
  statisticsTab: {
    description: 'Show statistics tab',
    keys: ['5'],
  },
  riscVDocTab: {
    description: 'Show RISC-V documentation tab',
    keys: ['6'],
  },
  settingsTab: {
    description: 'Show Settings tab',
    keys: ['7'],
  },
  helpTab: {
    description: 'Show Help tab',
    keys: ['8'],
  },
  stepForward: {
    description: 'Step simulation forward',
    keys: ['→'],
  },
  stepBackward: {
    description: 'Step simulation backward',
    keys: ['←'],
  },
  skipToEnd: {
    description: 'Skip to the end of simulation',
    keys: ['Ctrl', 'Enter'],
  },
  reload: {
    description: 'Reload simulation',
    keys: ['R'],
  },
  zoomIn: {
    description: 'Zoom in',
    keys: ['Ctrl', '+'],
  },
  zoomOut: {
    description: 'Zoom out',
    keys: ['Ctrl', '-'],
  },
} satisfies Record<string, Shortcut>;

type ShortcutCategory = {
  title: string;
  shortcuts: readonly (keyof typeof shortcuts)[];
};

const shortcutCategories = [
  {
    title: 'Navigation Shortcuts',
    shortcuts: [
      'simulationTab',
      'compilerTab',
      'memoryTab',
      'isaSettingsTab',
      'statisticsTab',
      'riscVDocTab',
      'settingsTab',
      'helpTab',
    ] as const,
  },
  {
    title: 'Simulation Shortcuts',
    shortcuts: [
      'stepForward',
      'stepBackward',
      'skipToEnd',
      'reload',
      'zoomIn',
      'zoomOut',
    ] as const,
  },
] as const satisfies Array<ShortcutCategory>;
