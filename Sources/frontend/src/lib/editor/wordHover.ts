// Taken and adapted from docs: https://codemirror.net/examples/tooltip/

import { hoverTooltip } from '@codemirror/view';

import {
  isSupportedInstructionKey,
  supportedInstructions,
} from '@/constant/supportedInstructions';

import { InstructionDescription } from '../instructionsDatabase';

/**
 * Create a tooltip (HTML element) for the given instruction
 * @param instruction The instruction to display
 */
function instructionTooltip(instruction: InstructionDescription) {
  // TODO: instructionDescription
  const dom = document.createElement('div');
  dom.className = 'instruction-tooltip';

  const instructionName = document.createElement('div');
  instructionName.textContent = instruction.name;
  instructionName.className = 'tooltip-name';

  const instructionSyntax = document.createElement('div');
  instructionSyntax.textContent = `Syntax: ${instruction.instructionSyntax}`;

  const instructionInterpretable = document.createElement('div');
  instructionInterpretable.textContent = `Interpretable as: ${instruction.interpretableAs}`;

  dom.appendChild(instructionName);
  dom.appendChild(instructionSyntax);
  dom.appendChild(instructionInterpretable);

  return dom;
}

/**
 * Setup the word hover tooltip
 */
export const wordHover = hoverTooltip((view, pos, side) => {
  // Extract hovered word
  const { from, to, text } = view.state.doc.lineAt(pos);
  let start = pos,
    end = pos;
  while (start > from && /\w/.test(text[start - from - 1])) start--;
  while (end < to && /\w/.test(text[end - from])) end++;
  if ((start == pos && side < 0) || (end == pos && side > 0)) {
    return null;
  }

  // Check if the word is an instruction
  const word = text.slice(start - from, end - from);
  if (!isSupportedInstructionKey(word)) {
    return null;
  }

  // Get info and create tooltip
  const instructionInfo = supportedInstructions[word];
  return {
    pos: start,
    end,
    above: true,
    create(_view) {
      // Gets wrapped in a .cm-tooltip
      const dom = instructionTooltip(instructionInfo);
      return { dom };
    },
  };
});
