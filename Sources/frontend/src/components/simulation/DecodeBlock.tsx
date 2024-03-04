/**
 * @file    DecodeBlock.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   A component for displaying the Decode block
 *
 * @date    26 October 2023, 16:00 (created)
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

import { selectDecode } from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';
import { InstructionListDisplay } from '@/components/simulation/InstructionListDisplay';
import { Badge } from '@/components/base/ui/badge';
import { useBlockDescriptions } from '@/components/BlockDescriptionContext';
import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/base/ui/dialog';

export default function DecodeBlock() {
  const decode = useAppSelector(selectDecode);
  const descriptions = useBlockDescriptions();

  if (!decode) return null;

  return (
    <Block
      title='Decode Block'
      stats={
        <>
          {decode.stallFlag ? (
            <Badge variant='destructive'>Stalled</Badge>
          ) : null}
        </>
      }
      detailDialog={
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Decode Block</DialogTitle>
            <DialogDescription>
              {descriptions.decode?.shortDescription}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      }
      className='decode w-block'
    >
      <InstructionListDisplay
        instructions={decode.codeBuffer}
        totalSize={decode.decodeBufferSize}
        instructionRenderer={(instruction, i) => (
          <InstructionField instructionId={instruction} key={`instr_${i}`} />
        )}
      />
    </Block>
  );
}
