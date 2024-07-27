/**
 * @file    WelcomeTour.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Welcome tour displayed on first visit
 *
 * @date    17 April 2024, 17:00 (created)
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

import { type StepType, TourProvider, useTour } from '@reactour/tour';
import { useEffect } from 'react';
import { toast } from 'sonner';

type WelcomeTourProps = {
  children: React.ReactNode;
};

const simForwardSelector = '*[aria-label="Step forward"]';
const fetchSelector = '.fetch-position';
const compilerLinkSelector = '[href="/compiler"]';
const loadExampleSelector = '.load-example-wrapper';
const axpyExampleSelector = '[data-example-name="AXPY"]';
const asmErrorSelector = '.asm-display';
const memoryLinkSelector = '[href="/memory"]';

const nameInputSelector = 'input[name="name"]';
const dataTypeSelector = '#dataType';
const secondTabSelector = 'button[data-radix-collection-item]:nth-child(2)';
const itemSizeSelector = 'input[name="data.size"]';
const itemConstantSelector = 'input[name="data.constant"]';
const submitSelector = 'button[type="submit"]';
const simulationLinkSelector = '[href="/"]';

const reloadSimSelector = 'button#reload-sim';
const skipToEndSelector = '[aria-label="Skip to the end of simulation"]';

/**
 * Provide tour for first time users. Mount this near the root of the app.
 */
export function WelcomeTour({ children }: WelcomeTourProps) {
  const steps: StepType[] = [
    // aria-label="Step forward"
    {
      selector: 'body',
      content:
        'Welcome to the simulator. This is the main simulation window. You can move around by holding the mouse wheel button.',
    },
    {
      selector: simForwardSelector,
      content:
        'Click the arrow to move simulation one step forward. Alternatively, use arrows on your keyboard.',
    },
    {
      selector: fetchSelector,
      content: 'As you can see, the simulation stepped one clock forward.',
    },
    {
      selector: compilerLinkSelector,
      content: "Let's simulate one of the examples next.",
      resizeObservables: [compilerLinkSelector],
    },
    {
      selector: 'body',
      content:
        'This is the compiler. You can compile your own code or use one of the examples. Let me show you how.',
    },
    {
      selector: loadExampleSelector,
      content: "Pick example 'AXPY'",
    },
    {
      selector: axpyExampleSelector,
      content: "Pick example 'AXPY'",
      resizeObservables: [axpyExampleSelector],
    },
    {
      selector: asmErrorSelector,
      content:
        'The code compiled fine, but there is a warning. The memory address a and b are not defined.',
    },
    {
      selector: memoryLinkSelector,
      content: 'Go to the memory section.',
      resizeObservables: [memoryLinkSelector],
    },
    {
      selector: 'body',
      content:
        'This is the memory section. Here, you can define data to simulate on.',
    },
    {
      selector: nameInputSelector,
      content: 'Name the data. We need to define array "a".',
    },
    {
      selector: dataTypeSelector,
      content: 'Select the data type float.',
    },
    {
      selector: 'body',
      content: 'Select the data type float.',
      position: 'left',
    },
    {
      selector: secondTabSelector,
      content: 'Select the second tab.',
    },
    {
      selector: itemSizeSelector,
      content: 'We need an array of 100 elements.',
    },
    {
      selector: itemConstantSelector,
      content: 'Pick any value here (if you are indecisive, pick 5).',
    },
    {
      selector: submitSelector,
      content: 'Save the array.',
    },
    {
      selector: 'main',
      content: 'Repeat the process for array "b".',
    },
    {
      selector: 'body',
      content: 'Now, we are ready to simulate the code.',
    },
    {
      selector: 'body',
      content:
        "You're all set! Feel free to explore the simulator. If you need help, click on the help icon in the top right corner.",
    },
  ];

  return (
    <TourProvider
      steps={steps}
      styles={{
        popover: (style) => {
          return {
            ...style,
            backgroundColor: undefined,
          };
        },
        badge: (style) => {
          return {
            ...style,
            backgroundColor: 'var(--reactour-accent)',
            color: 'black',
          };
        },
      }}
      showDots={false}
      beforeClose={() => {
        // pop a notification with the ability to start the tour again
        toast.info(
          'Tour is over. You can start it again from the settings menu.',
        );
      }}
      className='secondary-container rounded'
    >
      <StepActionInjector />
      {children}
    </TourProvider>
  );
}

function StepActionInjector() {
  const { setSteps, steps, setCurrentStep } = useTour();

  const newSteps: StepType[] = steps.map((step, index) => {
    return {
      ...step,
      action: (elem) => {
        // hack: do not add event listener for body
        if (step.selector === 'body') {
          return;
        }
        // Do not add event listeners for input fields
        if (elem?.tagName === 'INPUT') {
          return;
        }
        elem?.addEventListener(
          'click',
          () => {
            setCurrentStep(index + 1);
          },
          { once: true },
        );
      },
    };
  });

  // biome-ignore lint/correctness/useExhaustiveDependencies: one time effect
  useEffect(() => {
    if (!setSteps) {
      return;
    }
    setSteps(newSteps);
  }, []);

  return null;
}
