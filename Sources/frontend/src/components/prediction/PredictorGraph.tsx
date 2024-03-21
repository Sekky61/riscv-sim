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

'use client';

import {
  selectPredictorWidth,
  selectSimCodeModel,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import clsx from 'clsx';

import {
  Tooltip,
  TooltipContent,
  TooltipPortal,
  TooltipTrigger,
} from '@/components/base/ui/tooltip';
import { BranchTable } from '@/components/prediction/BranchTable';
import { PredictorIcon } from '@/components/prediction/PredictorIcon';
import type { BranchInfo } from '@/lib/types/cpuApi';

type PredictorGraphProps = {
  branchInfo: BranchInfo;
};

/**
 * A small widget displayed next to branch instructions in the fetch block.
 * It shows the state of the predictor before the prediction.
 * Hovering over the widget shows details relevant to prediction.
 */
export function PredictorGraph({ branchInfo }: PredictorGraphProps) {
  const predictorStateWidth = useAppSelector(selectPredictorWidth);
  if (predictorStateWidth === undefined) {
    // Empty field
    return null;
  }

  const state = branchInfo.predictorStateBeforePrediction;
  const verdict = branchInfo.predictorVerdict;

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className={clsx(
            'w-7 h-7 rounded-lg m-1 ring-2 ring-offset-2',
            verdict && 'ring-teal-500',
            !verdict && 'ring-rose-500',
          )}
        >
          <PredictorIcon state={state} width={predictorStateWidth} />
        </div>
      </TooltipTrigger>
      <TooltipPortal>
        <TooltipContent>
          <BranchTable branchInfo={branchInfo} />
        </TooltipContent>
      </TooltipPortal>
    </Tooltip>
  );
}

/**
 * A variant of PredictorGraph for when only a simcodeid is available.
 */
export function PredictorGraphFromCodeId({
  simCodeId,
}: { simCodeId: number | null }) {
  const q = useAppSelector((state) => selectSimCodeModel(state, simCodeId));
  const simCodeModel = q?.simCodeModel;
  const branchInfo = simCodeModel?.branchInfo;

  if (branchInfo === undefined || branchInfo === null) {
    return null;
  }

  return <PredictorGraph branchInfo={branchInfo} />;
}
