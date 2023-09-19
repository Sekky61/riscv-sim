import _supportedInstructions from 'src/constant/supportedInstructions.json';

import { InstructionDescription } from '@/lib/instructionsDatabase';

export type SupportedInstructionsKeys = keyof typeof _supportedInstructions;

export const supportedInstructions = _supportedInstructions as Record<
  SupportedInstructionsKeys,
  InstructionDescription
>;

export function isSupportedInstructionKey(
  key: string,
): key is SupportedInstructionsKeys {
  return key in supportedInstructions;
}
