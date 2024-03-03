/**
 * @file    PredictorGraph.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the state of a predictor
 *
 * @date    02 March 2024, 20:00 (created)
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

import {
  selectPatternHistoryTable,
  selectPredictorWidth,
  selectSimCodeModel,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';

import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { BranchTable } from '@/components/prediction/BranchTable';

type PredictorGraphProps = {
  simCodeId: number | null;
};

/**
 * A small widget displayed next to branch instructions in the fetch block.
 * It shows the state of the predictor before the prediction.
 * Hovering over the widget shows details relevant to prediction.
 */
export function PredictorGraph({ simCodeId }: PredictorGraphProps) {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const predictorStateWidth = useAppSelector(selectPredictorWidth);
  if (!q || simCodeId === null || predictorStateWidth === undefined) {
    // Empty field
    return null;
  }

  const { simCodeModel } = q;

  const branchInfo = simCodeModel.branchInfo;

  if (!branchInfo) {
    return null;
  }

  const state = branchInfo.predictorStateBeforePrediction;
  const verdict = branchInfo.predictorVerdict;

  let icon = null;
  if (predictorStateWidth === 2) {
    icon = <TwoBitIcon state={state} />;
  } else if (predictorStateWidth === 1) {
    icon = <OneBitIcon state={state} />;
  } else {
    // 0 width predictor
    icon = null;
  }

  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div
            className={clsx(
              'instruction-bubble w-8 p-1 ring-2 ring-inset',
              verdict && 'ring-green-300',
              !verdict && 'ring-red-300',
            )}
          >
            {icon}
          </div>
        </TooltipTrigger>
        <TooltipContent>
          <BranchTable
            branchInfo={branchInfo}
            predictorWidth={predictorStateWidth}
          />
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}

function fill(i: number, active: number) {
  if (i === active) {
    return '#ff7171';
  } else {
    return 'transparent';
  }
}

type IconProps = {
  state: number;
};

function TwoBitIcon({ state }: IconProps) {
  return (
    <svg
      viewBox='0 0 100 100'
      xmlns='http://www.w3.org/2000/svg'
      className='pointer-events-none'
    >
      <title>Predictor state {state}</title>
      <line x1='30' y1='18' x2='70' y2='18' stroke='black' strokeWidth='5' />
      <circle
        cx='18'
        cy='18'
        r='15'
        fill={fill(0, state)}
        stroke='black'
        strokeWidth='5'
      />
      <line x1='72' y1='28' x2='28' y2='72' stroke='black' strokeWidth='5' />
      <circle
        cx='82'
        cy='18'
        r='15'
        fill={fill(1, state)}
        stroke='black'
        strokeWidth='5'
      />

      <line x1='30' y1='82' x2='70' y2='82' stroke='black' strokeWidth='5' />
      <circle
        cx='18'
        cy='82'
        r='15'
        fill={fill(2, state)}
        stroke='black'
        strokeWidth='5'
      />
      <circle
        cx='82'
        cy='82'
        r='15'
        fill={fill(3, state)}
        stroke='black'
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
      className='pointer-events-none'
    >
      <title>Predictor state {state}</title>
      <line x1='30' y1='50' x2='70' y2='50' stroke='black' strokeWidth='5' />
      <circle
        cx='18'
        cy='50'
        r='15'
        fill={fill(0, state)}
        stroke='black'
        strokeWidth='5'
      />
      <circle
        cx='82'
        cy='50'
        r='15'
        fill={fill(1, state)}
        stroke='black'
        strokeWidth='5'
      />
    </svg>
  );
}
