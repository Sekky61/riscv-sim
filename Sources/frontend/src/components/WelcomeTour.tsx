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

import { ChevronLeft, ChevronRight } from 'lucide-react';
import { type StepType, TourProvider, useTour } from '@reactour/tour';
import { useEffect } from 'react';

type WelcomeTourProps = {
  children: React.ReactNode;
};

const simForwardSelector = '*[aria-label="Step forward"]';
const fetchSelector = '.fetch-position';
const compilerLinkSelector = '[href="/compiler"]';
const loadExampleSelector = '.load-example-button';
const axpyExampleSelector = '[data-example-name="AXPY"]';

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
      selector: loadExampleSelector,
      content: "Pick example 'AXPY'",
    },
    {
      selector: axpyExampleSelector,
      content: "Pick example 'AXPY'",
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
        console.log(elem);
        elem?.addEventListener(
          'click',
          () => {
            console.log('clicked');
            setCurrentStep(index + 1);
          },
          { once: true },
        );
      },
    };
  });

  // biome-ignore lint/correctness/useExhaustiveDependencies: one time effect
  useEffect(() => {
    setSteps(newSteps);
  }, []);

  console.log('steps from inj', newSteps);
  return null;
}
