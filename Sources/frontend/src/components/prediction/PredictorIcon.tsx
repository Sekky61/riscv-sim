/**
 * @file    PredictorIcon.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Icon symbolizing a predictor with its state
 *
 * @date    03 March 2024, 13:00 (created)
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

/**
 * SVG-based icon of an FSM predictor.
 * The current state (given by the `state` prop) is highlighted.
 * @param state - The current state of the predictor. Either 0, 1, 2 or 3.
 * @param width - The width of the state predictor. Either 0, 1 or 2.
 */
export function PredictorIcon({
  state,
  width,
}: { state: number; width: number }) {
  let icon = null;
  if (width === 2) {
    icon = <TwoBitIcon state={state} />;
  } else if (width === 1) {
    icon = <OneBitIcon state={state} />;
  } else {
    // 0 width predictor TODO
    icon = null;
  }

  return icon;
}

type IconProps = {
  state: number;
};

function TwoBitIcon({ state }: IconProps) {
  return (
    <svg
      viewBox='0 0 100 100'
      xmlns='http://www.w3.org/2000/svg'
      className='pointer-events-none predictor-graph'
    >
      <title>Predictor state {state}</title>
      <line x1='30' y1='18' x2='70' y2='18' strokeWidth='5' />
      <circle
        cx='18'
        cy='18'
        r='15'
        data-active={active(0, state)}

        strokeWidth='5'
      />
      <line x1='72' y1='28' x2='28' y2='72' strokeWidth='5' />
      <circle
        cx='82'
        cy='18'
        r='15'
        data-active={active(1, state)}
        strokeWidth='5'
      />

      <line x1='30' y1='82' x2='70' y2='82' strokeWidth='5' />
      <circle
        cx='18'
        cy='82'
        r='15'
        data-active={active(2, state)}
        strokeWidth='5'
      />
      <circle
        cx='82'
        cy='82'
        r='15'
        data-active={active(3, state)}
        strokeWidth='5'
      />
    </svg>
  );
}

function OneBitIcon({ state }: IconProps) {
  return (
    <svg
      viewBox='0 0 100 100'
      xmlns='http://www.w3.org/2000/svg'
      className='pointer-events-none predictor-graph'
    >
      <title>Predictor state {state}</title>
      <line x1='30' y1='50' x2='70' y2='50' strokeWidth='5' />
      <circle
        cx='18'
        cy='50'
        r='15'
        data-active={active(0, state)}
        strokeWidth='5'
      />
      <circle
        cx='82'
        cy='50'
        r='15'
        data-active={active(1, state)}
        strokeWidth='5'
      />
    </svg>
  );
}

function active(i: number, active: number): boolean {
  return i === active;
}
